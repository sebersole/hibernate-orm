/*
 * Hibernate, Relational Persistence for Idiomatic Java
 *
 * License: GNU Lesser General Public License (LGPL), version 2.1 or later.
 * See the lgpl.txt file in the root directory or http://www.gnu.org/licenses/lgpl-2.1.html.
 */
package org.hibernate.boot.annotations.bind.internal;

import java.lang.annotation.Annotation;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.function.Consumer;

import org.hibernate.MappingException;
import org.hibernate.annotations.BatchSize;
import org.hibernate.annotations.DynamicInsert;
import org.hibernate.annotations.DynamicUpdate;
import org.hibernate.annotations.Loader;
import org.hibernate.annotations.SQLDelete;
import org.hibernate.annotations.SQLInsert;
import org.hibernate.annotations.SQLUpdate;
import org.hibernate.annotations.SecondaryRow;
import org.hibernate.annotations.Subselect;
import org.hibernate.annotations.Synchronize;
import org.hibernate.boot.annotations.AnnotationSourceLogging;
import org.hibernate.boot.annotations.model.spi.EntityHierarchy;
import org.hibernate.boot.annotations.model.spi.EntityTypeMetadata;
import org.hibernate.boot.annotations.model.spi.IdentifiableTypeMetadata;
import org.hibernate.boot.annotations.model.spi.LocalAnnotationProcessingContext;
import org.hibernate.boot.annotations.model.spi.ManagedTypeMetadata;
import org.hibernate.boot.annotations.model.spi.MappedSuperclassTypeMetadata;
import org.hibernate.boot.annotations.source.spi.AnnotationAttributeValue;
import org.hibernate.boot.annotations.source.spi.AnnotationDescriptor;
import org.hibernate.boot.annotations.source.spi.AnnotationUsage;
import org.hibernate.boot.annotations.source.spi.ClassDetails;
import org.hibernate.boot.annotations.source.spi.HibernateAnnotations;
import org.hibernate.boot.annotations.source.spi.JpaAnnotations;
import org.hibernate.boot.model.naming.ImplicitNamingStrategy;
import org.hibernate.boot.spi.InFlightMetadataCollector;
import org.hibernate.boot.spi.MetadataBuildingContext;
import org.hibernate.boot.spi.MetadataBuildingOptions;
import org.hibernate.internal.util.collections.CollectionHelper;
import org.hibernate.mapping.BasicValue;
import org.hibernate.mapping.Column;
import org.hibernate.mapping.Join;
import org.hibernate.mapping.JoinedSubclass;
import org.hibernate.mapping.PersistentClass;
import org.hibernate.mapping.RootClass;
import org.hibernate.mapping.SingleTableSubclass;
import org.hibernate.mapping.Subclass;
import org.hibernate.mapping.UnionSubclass;
import org.hibernate.type.SqlTypes;

import jakarta.persistence.DiscriminatorColumn;
import jakarta.persistence.DiscriminatorType;
import jakarta.persistence.InheritanceType;
import jakarta.persistence.SecondaryTable;
import jakarta.persistence.Table;

import static jakarta.persistence.InheritanceType.SINGLE_TABLE;
import static org.hibernate.boot.annotations.AnnotationSourceLogging.ANNOTATION_SOURCE_MSG_LOGGER;
import static org.hibernate.boot.annotations.source.internal.AnnotationsHelper.findInheritedAnnotation;
import static org.hibernate.boot.annotations.source.internal.AnnotationsHelper.getValue;

/**
 * @author Steve Ebersole
 */
public class TypeBinder {

	public static PersistentClass buildPersistentClass(EntityTypeMetadata entityTypeMetadata) {
		final LocalAnnotationProcessingContext processingContext = entityTypeMetadata.getLocalProcessingContext();
		final EntityHierarchy hierarchy = entityTypeMetadata.getHierarchy();
		final IdentifiableTypeMetadata directSuperTypeMetadata = entityTypeMetadata.getSuperType();
		final boolean isRoot = hierarchy.getRoot() == entityTypeMetadata;

		final PersistentClass entityMapping;

		// KLUDGE ALERT: mapped-superclass is modeled terribly in the boot model,
		// and we may need to deal with that here.
		final List<MappedSuperclassTypeMetadata> intermediateMappedSuperclassTypes;

		if ( isRoot ) {
			final RootClass rootEntityMapping = new RootClass( processingContext.getMetadataBuildingContext() );
			entityMapping = rootEntityMapping;

			if ( entityTypeMetadata.getSuperType() != null ) {
				// the root has supers (which should only ever be mapped-superclasses).
				// bind them to the RootClass
			}

			rootEntityMapping.setEntityName( entityTypeMetadata.getEntityName() );
			rootEntityMapping.setJpaEntityName( entityTypeMetadata.getJpaEntityName() );

			applyPrimaryTable( entityTypeMetadata, rootEntityMapping, processingContext );
			applySecondaryTables( entityTypeMetadata, rootEntityMapping, processingContext );
			applyDiscriminator( entityTypeMetadata, rootEntityMapping, processingContext );

			if ( hierarchy.getCaching().isEnabled() ) {
				rootEntityMapping.setCached( true );
				rootEntityMapping.setCacheConcurrencyStrategy( hierarchy.getCaching().getAccessType().getExternalName() );
				rootEntityMapping.setCacheRegionName( hierarchy.getCaching().getRegion() );
				rootEntityMapping.setLazyPropertiesCacheable( hierarchy.getCaching().isCacheLazyProperties() );
			}

			if ( hierarchy.getNaturalIdCaching().isEnabled() ) {
				rootEntityMapping.setCached( true );
				rootEntityMapping.setNaturalIdCacheRegionName( hierarchy.getNaturalIdCaching().getRegion() );
			}

			if ( directSuperTypeMetadata != null ) {
				// if there is one, it should be a mapped-superclass - we need to handle
				// them specially because the boot mapping model makes us :)
				assert directSuperTypeMetadata instanceof MappedSuperclassTypeMetadata;
				MappedSuperclassTypeMetadata mappedSuperclassMetadata = (MappedSuperclassTypeMetadata) directSuperTypeMetadata;

				while ( mappedSuperclassMetadata != null ) {


					mappedSuperclassMetadata = (MappedSuperclassTypeMetadata) mappedSuperclassMetadata.getSuperType();
				}
			}
		}
		else {
			final Subclass subEntity;
			final PersistentClass superEntity;

			// resolve the super-type
			//
			// this can get kludgy because mapped-superclass does not fit nicely
			// with the PersistentClass hierarchy.
			if ( directSuperTypeMetadata instanceof EntityTypeMetadata ) {
				// our direct super is an entity, no intermediate mapped-superclass(es) to handle
				final InFlightMetadataCollector metadataCollector = processingContext.getMetadataBuildingContext().getMetadataCollector();
				superEntity = metadataCollector.getEntityBinding( ( (EntityTypeMetadata) directSuperTypeMetadata ).getEntityName() );
				intermediateMappedSuperclassTypes = Collections.emptyList();
			}
			else {
				// we do have intermediate mapped-superclass(es) to handle that
				// When we create the `Subclass` we need to know the "super entity", so we
				// need to resolve that.  After we have the `Subclass` reference we then
				// need to attach the `MappedSuperclass` references to the created `Subclass`
				intermediateMappedSuperclassTypes = new ArrayList<>();
				superEntity = resolveSuperEntity( directSuperTypeMetadata, processingContext, intermediateMappedSuperclassTypes::add );

				assert !intermediateMappedSuperclassTypes.isEmpty();
			}


			// its super-type mapping should already have been processed - find it

			switch ( hierarchy.getInheritanceType() ) {
				case JOINED: {
					subEntity = new JoinedSubclass( superEntity, processingContext.getMetadataBuildingContext() );
					applyPrimaryTable( entityTypeMetadata, (JoinedSubclass) subEntity, processingContext );
					break;
				}
				case TABLE_PER_CLASS: {
					subEntity = new UnionSubclass( superEntity, processingContext.getMetadataBuildingContext() );
					applyPrimaryTable( entityTypeMetadata, (UnionSubclass) subEntity, processingContext );
					break;
				}
				default: {
					assert hierarchy.getInheritanceType() == SINGLE_TABLE;
					subEntity = new SingleTableSubclass( superEntity, processingContext.getMetadataBuildingContext() );
					break;
				}
			}
			entityMapping = subEntity;

			subEntity.setEntityName( entityTypeMetadata.getEntityName() );
			subEntity.setJpaEntityName( entityTypeMetadata.getJpaEntityName() );

			applySecondaryTables( entityTypeMetadata, subEntity, processingContext );
		}

		bindCommonValues( entityTypeMetadata, entityMapping, processingContext );

		processingContext.getMetadataBuildingContext().getMetadataCollector().addEntityBinding( entityMapping );

		return entityMapping;
	}

	private static void applyPrimaryTable(
			EntityTypeMetadata entityTypeMetadata,
			RootClass persistentClass,
			LocalAnnotationProcessingContext processingContext) {
		// todo : add to x-refs
		persistentClass.setTable( buildPrimaryTable( entityTypeMetadata, processingContext ) );
	}

	private static void applyPrimaryTable(
			EntityTypeMetadata entityTypeMetadata,
			JoinedSubclass persistentClass,
			LocalAnnotationProcessingContext processingContext) {
		// todo : add to x-refs
		persistentClass.setTable( buildPrimaryTable( entityTypeMetadata, processingContext ) );
	}

	private static void applyPrimaryTable(
			EntityTypeMetadata entityTypeMetadata,
			UnionSubclass persistentClass,
			LocalAnnotationProcessingContext processingContext) {
		// todo : add to x-refs
		persistentClass.setTable( buildPrimaryTable( entityTypeMetadata, processingContext ) );
	}

	private static org.hibernate.mapping.Table buildPrimaryTable(
			EntityTypeMetadata entityTypeMetadata,
			LocalAnnotationProcessingContext processingContext) {
		final AnnotationUsage<Table> tableAnnotation = findInheritedAnnotation( entityTypeMetadata, JpaAnnotations.TABLE );
		final AnnotationUsage<Subselect> subSelectAnnotation = findInheritedAnnotation( entityTypeMetadata, HibernateAnnotations.SUBSELECT );

		if ( AnnotationSourceLogging.ANNOTATION_SOURCE_LOGGER_DEBUG_ENABLED ) {
			if ( tableAnnotation != null && subSelectAnnotation != null ) {
				AnnotationSourceLogging.ANNOTATION_SOURCE_LOGGER.debugf(
						"Entity contained both @Table and @Subselect - %s",
						entityTypeMetadata.getEntityName()
				);
			}
		}

		final org.hibernate.mapping.Table primaryTable;
		if ( subSelectAnnotation != null ) {
			final InFlightMetadataCollector metadataCollector = processingContext
					.getMetadataBuildingContext()
					.getMetadataCollector();
			return metadataCollector.addTable(
					null,
					null,
					null,
					subSelectAnnotation.getValueAttributeValue().asString(),
					entityTypeMetadata.isAbstract(),
					processingContext.getMetadataBuildingContext()
			);
		}
		else {
			return TableBinder.bindTable(
					tableAnnotation,
					() -> tableName( entityTypeMetadata, tableAnnotation, processingContext ),
					entityTypeMetadata.isAbstract(),
					processingContext
			);
		}
	}

	static String tableName(
			EntityTypeMetadata entityTypeMetadata,
			AnnotationUsage<jakarta.persistence.Table> tableAnnotation,
			LocalAnnotationProcessingContext processingContext) {

		if ( tableAnnotation != null ) {
			final String explicitName = getValue( tableAnnotation.getAttributeValue( "name" ), null );
			if ( explicitName != null ) {
				return explicitName;
			}
		}

		final MetadataBuildingContext buildingContext = processingContext.getMetadataBuildingContext();
		final MetadataBuildingOptions buildingOptions = buildingContext.getBuildingOptions();
		final ImplicitNamingStrategy namingStrategy = buildingOptions.getImplicitNamingStrategy();

		return namingStrategy.determinePrimaryTableName( entityTypeMetadata ).render();
	}

	private static void applySecondaryTables(
			EntityTypeMetadata entityTypeMetadata,
			PersistentClass persistentClass,
			LocalAnnotationProcessingContext processingContext) {
		final InFlightMetadataCollector metadataCollector = processingContext
				.getMetadataBuildingContext()
				.getMetadataCollector();
		final List<AnnotationUsage<SecondaryTable>> annotations = entityTypeMetadata.findAnnotations( JpaAnnotations.SECONDARY_TABLE );
		if ( CollectionHelper.isEmpty( annotations ) ) {
			return;
		}

		for ( int i = 0; i < annotations.size(); i++ ) {
			final AnnotationUsage<SecondaryTable> secondaryTableAnnotation = annotations.get( i );
			final org.hibernate.mapping.Table secondaryTable = TableBinder.bindTable(
					secondaryTableAnnotation,
					() -> {
						throw new MappingException( "SecondaryTable#name is required" );
					},
					false,
					processingContext
			);

			final Join join = new Join();
			join.setPersistentClass( persistentClass );
			persistentClass.addJoin( join );
			join.setTable( secondaryTable );

			final AnnotationUsage<SecondaryRow> secondaryRowAnnotation = entityTypeMetadata.getManagedClass().getNamedAnnotation(
					HibernateAnnotations.SECONDARY_ROW,
					secondaryTable.getName()
			);
			if ( secondaryRowAnnotation != null ) {
				join.setOptional( BindingHelper.extractValue( secondaryRowAnnotation, "optional", true ) );
				join.setInverse( !BindingHelper.extractValue( secondaryRowAnnotation, "owned", true ) );
			}
		}
	}

	private static void applyDiscriminator(
			EntityTypeMetadata entityTypeMetadata,
			RootClass rootEntityMapping,
			LocalAnnotationProcessingContext processingContext) {
		assert entityTypeMetadata.getHierarchy().getRoot() == entityTypeMetadata;

		final InheritanceType inheritanceType = entityTypeMetadata.getHierarchy().getInheritanceType();
		// since we start from the root-entity findInheritedAnnotation v. findSemiInheritedAnnotation
		// is irrelevant.  so avoid the type-checking in findSemiInheritedAnnotation
		final AnnotationUsage<DiscriminatorColumn> annotation = findInheritedAnnotation(
				entityTypeMetadata,
				JpaAnnotations.DISCRIMINATOR_COLUMN
		);

		// check if we need to create the discriminator
		final boolean createDiscriminator;
		if ( annotation != null ) {
			// explicitly defined
			createDiscriminator = true;
		}
		else if ( inheritanceType == InheritanceType.SINGLE_TABLE && entityTypeMetadata.hasSubTypes() ) {
			// implicitly needed
			createDiscriminator = true;
		}
		else {
			createDiscriminator = false;
		}

		if ( !createDiscriminator ) {
			return;
		}


		final Column column = new Column();
		if ( annotation == null ) {
			// defaults defined on the annotation
			column.setName( "DTYPE" );
			column.setSqlTypeCode( SqlTypes.VARCHAR );
			column.setLength( 31 );
		}
		else {
			final AnnotationAttributeValue nameValue = annotation.getAttributeValue( "name" );
			column.setName( getValue( nameValue, "DTYPE" ) );

			final AnnotationAttributeValue discriminatorType = annotation.getAttributeValue( "discriminatorType" );
			final DiscriminatorType type = getValue( discriminatorType, DiscriminatorType.STRING );
			switch ( type ) {
				case CHAR: {
					column.setSqlTypeCode( SqlTypes.CHAR );
					break;
				}
				case INTEGER: {
					column.setSqlTypeCode( SqlTypes.INTEGER );
					break;
				}
				default: {
					column.setSqlTypeCode( SqlTypes.VARCHAR );
					final AnnotationAttributeValue length = annotation.getAttributeValue( "length" );
					column.setLength( getValue( length, 31 ) );
				}
			}
		}

		final BasicValue discriminatorMapping = new BasicValue(
				processingContext.getMetadataBuildingContext(),
				rootEntityMapping.getTable()
		);
		discriminatorMapping.addColumn( column );

		rootEntityMapping.setDiscriminator( discriminatorMapping );
	}

	private static void bindCommonValues(
			EntityTypeMetadata entityTypeMetadata,
			PersistentClass entityMapping,
			LocalAnnotationProcessingContext processingContext) {
		entityMapping.setClassName( entityTypeMetadata.getManagedClass().getClassName() );
		applyBatchSize( entityTypeMetadata, entityMapping, processingContext );
		applySqlCustomizations( entityTypeMetadata, entityMapping, processingContext );
		applySynchronizedTableNames( entityTypeMetadata, entityMapping, processingContext );
	}

	private static void applyBatchSize(
			EntityTypeMetadata entityTypeMetadata,
			PersistentClass entityMapping,
			LocalAnnotationProcessingContext processingContext) {
		final AnnotationUsage<BatchSize> batchSizeAnnotation = entityTypeMetadata
				.getManagedClass()
				.getAnnotation( HibernateAnnotations.BATCH_SIZE );
		if ( batchSizeAnnotation == null ) {
			// todo (annotation-source) : do we need to set any magic number?
			return;
		}

		entityMapping.setBatchSize( batchSizeAnnotation.getAttributeValue( "size" ).getValue() );
	}

	private static void applySynchronizedTableNames(
			EntityTypeMetadata entityTypeMetadata,
			PersistentClass entityMapping,
			LocalAnnotationProcessingContext processingContext) {
		final AnnotationUsage<Synchronize> synchronizeAnnotation = entityTypeMetadata
				.getManagedClass()
				.getAnnotation( HibernateAnnotations.SYNCHRONIZE );
		if ( synchronizeAnnotation == null ) {
			return;
		}
		entityMapping.addSynchronizedTable( synchronizeAnnotation.getValueAttributeValue().getValue() );
	}

	private static void applySqlCustomizations(
			EntityTypeMetadata entityTypeMetadata,
			PersistentClass entityMapping,
			LocalAnnotationProcessingContext processingContext) {
		final AnnotationUsage<Loader> loader = findAnnotation( entityTypeMetadata, HibernateAnnotations.LOADER );
		final AnnotationUsage<DynamicInsert> dynamicInsert = findAnnotation( entityTypeMetadata, HibernateAnnotations.DYNAMIC_INSERT );
		final AnnotationUsage<DynamicUpdate> dynamicUpdate = findAnnotation( entityTypeMetadata, HibernateAnnotations.DYNAMIC_UPDATE );
		final AnnotationUsage<SQLInsert> customInsert = findAnnotation( entityTypeMetadata, HibernateAnnotations.SQL_INSERT );
		final AnnotationUsage<SQLUpdate> customUpdate = findAnnotation( entityTypeMetadata, HibernateAnnotations.SQL_UPDATE );
		final AnnotationUsage<SQLDelete> customDelete = findAnnotation( entityTypeMetadata, HibernateAnnotations.SQL_DELETE );

		if ( loader != null ) {
			entityMapping.setLoaderName( loader.getAttributeValue( "namedQuery" ).getValue() );
		}

		if ( dynamicInsert != null ) {
			if ( customInsert != null ) {
				ANNOTATION_SOURCE_MSG_LOGGER.dynamicAndCustomInsert( entityTypeMetadata.getEntityName() );
			}
			entityMapping.setDynamicInsert( dynamicInsert.getValueAttributeValue().getValue() );
		}

		if ( dynamicUpdate != null ) {
			if ( customUpdate != null ) {
				ANNOTATION_SOURCE_MSG_LOGGER.dynamicAndCustomUpdate( entityTypeMetadata.getEntityName() );
			}
			entityMapping.setDynamicUpdate( dynamicUpdate.getValueAttributeValue().getValue() );
		}

		entityMapping.setCustomSqlInsert( BindingHelper.extractCustomSql( customInsert ) );
		entityMapping.setCustomSqlUpdate( BindingHelper.extractCustomSql( customUpdate ) );
		entityMapping.setCustomSqlDelete( BindingHelper.extractCustomSql( customDelete ) );
	}

	private static <A extends Annotation> AnnotationUsage<A> findAnnotation(
			ManagedTypeMetadata typeMetadata,
			AnnotationDescriptor<A> descriptor) {
		final ClassDetails classDetails = typeMetadata.getManagedClass();
		return classDetails.getAnnotation( descriptor );
	}

	private static PersistentClass resolveSuperEntity(
			IdentifiableTypeMetadata superType,
			LocalAnnotationProcessingContext processingContext,
			Consumer<MappedSuperclassTypeMetadata> intermediateMappedSuperclassCollector) {
		final InFlightMetadataCollector metadataCollector = processingContext.getMetadataBuildingContext().getMetadataCollector();
		if ( superType instanceof MappedSuperclassTypeMetadata ) {
			intermediateMappedSuperclassCollector.accept( (MappedSuperclassTypeMetadata) superType );
		}



		throw new UnsupportedOperationException();
	}
}
