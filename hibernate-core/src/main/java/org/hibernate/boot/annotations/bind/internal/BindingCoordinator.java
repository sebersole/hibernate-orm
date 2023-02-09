/*
 * Hibernate, Relational Persistence for Idiomatic Java
 *
 * License: GNU Lesser General Public License (LGPL), version 2.1 or later.
 * See the lgpl.txt file in the root directory or http://www.gnu.org/licenses/lgpl-2.1.html.
 */
package org.hibernate.boot.annotations.bind.internal;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;

import org.hibernate.boot.annotations.model.internal.EntityHierarchyBuilder;
import org.hibernate.boot.annotations.model.spi.EntityHierarchy;
import org.hibernate.boot.annotations.model.spi.EntityTypeMetadata;
import org.hibernate.boot.annotations.model.spi.IdentifiableTypeMetadata;
import org.hibernate.boot.annotations.model.spi.MappedSuperclassTypeMetadata;
import org.hibernate.boot.annotations.source.internal.AnnotationProcessingContextImpl;
import org.hibernate.boot.annotations.source.internal.reflection.ClassDetailsBuilderImpl;
import org.hibernate.boot.annotations.source.internal.reflection.ClassDetailsImpl;
import org.hibernate.boot.annotations.source.spi.ClassDetails;
import org.hibernate.boot.annotations.source.spi.ClassDetailsRegistry;
import org.hibernate.boot.annotations.source.spi.JpaAnnotations;
import org.hibernate.boot.annotations.spi.AnnotationProcessingContext;
import org.hibernate.boot.model.convert.spi.ConverterDescriptor;
import org.hibernate.boot.model.convert.spi.ConverterRegistry;
import org.hibernate.boot.model.process.spi.ManagedResources;
import org.hibernate.boot.spi.MetadataBuildingContext;
import org.hibernate.internal.util.collections.CollectionHelper;
import org.hibernate.mapping.Join;
import org.hibernate.mapping.MappedSuperclass;
import org.hibernate.mapping.PersistentClass;
import org.hibernate.mapping.Property;
import org.hibernate.mapping.RootClass;

import jakarta.persistence.AttributeConverter;

import static org.hibernate.boot.annotations.AnnotationSourceLogging.ANNOTATION_SOURCE_LOGGER;
import static org.hibernate.boot.annotations.AnnotationSourceLogging.ANNOTATION_SOURCE_LOGGER_DEBUG_ENABLED;

/**
 * @author Steve Ebersole
 */
public class BindingCoordinator {
	private final AnnotationProcessingContext processingContext;

	// cache some frequently used references
	private final ClassDetailsRegistry classDetailsRegistry;
	private final ConverterRegistry converterRegistry;

	/**
	 * Entry point for processing managed-resources into boot model references
	 *
	 * @param managedResources The managed-resources (entities, XML, etc) to be processed
	 * @param buildingContext Access to needed resources
	 */
	public static void bindBootModel(
			ManagedResources managedResources,
			MetadataBuildingContext buildingContext) {
		final AnnotationProcessingContextImpl processingContext = new AnnotationProcessingContextImpl( buildingContext );
		final BindingCoordinator bindingCoordinator = new BindingCoordinator( processingContext );

		bindingCoordinator.prepare( managedResources );

		final Set<EntityHierarchy> entityHierarchies = EntityHierarchyBuilder.createEntityHierarchies( processingContext );
		final Map<EntityHierarchy,RootClass> rootClasses = bindingCoordinator.processHierarchies( entityHierarchies, managedResources );
		bindingCoordinator.processAttributes( entityHierarchies, rootClasses, managedResources );
	}

	private BindingCoordinator(AnnotationProcessingContext processingContext) {
		this.processingContext = processingContext;

		this.classDetailsRegistry = processingContext.getClassDetailsRegistry();
		this.converterRegistry = processingContext.getMetadataBuildingContext().getMetadataCollector().getConverterRegistry();
	}

	private void prepare(ManagedResources managedResources) {
		// process any packages specified as managed-resources
		if ( CollectionHelper.isNotEmpty( managedResources.getAnnotatedPackageNames() ) ) {
			for ( String packageName : managedResources.getAnnotatedPackageNames() ) {
				processManagedPackage( packageName );
			}
		}

		// walks through the managed-resources and creates "intermediate model" references
		prepareManagedResources( managedResources );
	}

	private Map<EntityHierarchy,RootClass> processHierarchies(Set<EntityHierarchy> entityHierarchies, ManagedResources managedResources) {
		if ( CollectionHelper.isEmpty( entityHierarchies ) ) {
			return Collections.emptyMap();
		}

		final HashMap<EntityHierarchy,RootClass> rootClasses = new HashMap<>();

		// Visit each discovered entity hierarchy
		for ( EntityHierarchy entityHierarchy : entityHierarchies ) {
			final EntityTypeMetadata rootEntityMetadata = entityHierarchy.getRoot();
			// this step creates the RootClass reference, as well as all the proper
			// supers and subs.  It also registers them with the metadata-collector
			final RootClass rootClass = (RootClass) TypeBinder.buildPersistentClass( rootEntityMetadata );
			rootClasses.put( entityHierarchy, rootClass );
			if ( ANNOTATION_SOURCE_LOGGER_DEBUG_ENABLED ) {
				ANNOTATION_SOURCE_LOGGER.debugf(
						"Entity hierarchy built : %s",
						rootClass.getEntityName()
				);
			}
		}
		return rootClasses;
	}

	private void processAttributes(Set<EntityHierarchy> entityHierarchies, Map<EntityHierarchy,RootClass> rootClasses, ManagedResources managedResources) {
		for ( EntityHierarchy entityHierarchy : entityHierarchies ) {
			final EntityTypeMetadata rootEntityMetadata = entityHierarchy.getRoot();
			final RootClass rootClass = rootClasses.get( entityHierarchy );
			processAttributes( rootEntityMetadata, rootClass );
			processAttributesUp( rootEntityMetadata, rootClass );
			processAttributesDown( rootEntityMetadata, rootClass );
		}
	}

	private void processAttributes(EntityTypeMetadata entityMetadata, PersistentClass persistentClass) {
		entityMetadata.forEachAttribute( (index, attributeMetadata) -> {
			if ( attributeMetadata.getMember().getAnnotation( JpaAnnotations.ID ) != null
					|| attributeMetadata.getMember().getAnnotation( JpaAnnotations.EMBEDDED_ID ) != null ) {
				// for now, skip...
				return;
			}
			final Property property = PropertyBinder.buildProperty(
					attributeMetadata,
					entityMetadata,
					persistentClass.getTable(),
					persistentClass::findTable
			);
			if ( property.getValue().getTable().equals( persistentClass.getTable() ) ) {
				persistentClass.addProperty( property );

			}
			else {
				final Join join = persistentClass.getSecondaryTable( property.getValue().getTable().getName() );
				join.addProperty( property );
			}
		} );
	}

	private void processAttributes(
			MappedSuperclassTypeMetadata mappedSuperclassMetadata,
			MappedSuperclass mappedSuperclass) {
		if ( mappedSuperclassMetadata.getNumberOfSubTypes() == 0 ) {
			return;
		}

		throw new UnsupportedOperationException( "Not yet implemented" );
//		mappedSuperclassMetadata.forEachAttribute( (index, attributeMetadata) -> {
//			final Property property = PropertyBinder.buildProperty(
//					attributeMetadata,
//					mappedSuperclassMetadata,
//					persistentClass.getTable(),
//					mappedSuperclass::findTable
//			);
//			if ( property.getValue().getTable().equals( persistentClass.getTable() ) ) {
//				persistentClass.addProperty( property );
//
//			}
//			else {
//				final Join join = persistentClass.getSecondaryTable( property.getValue().getTable().getName() );
//				join.addProperty( property );
//			}
//		} );
	}

	private void processAttributesUp(EntityTypeMetadata entityMetadata, PersistentClass persistentClass) {
		final IdentifiableTypeMetadata superType = entityMetadata.getSuperType();
		if ( superType == null ) {
			return;
		}

		if ( superType instanceof MappedSuperclassTypeMetadata ) {
			final MappedSuperclassTypeMetadata mappedSuperclassMetadata = (MappedSuperclassTypeMetadata) superType;
			final MappedSuperclass mappedSuperclassMapping = persistentClass.getSuperMappedSuperclass();

			assert mappedSuperclassMapping != null;
			assert mappedSuperclassMapping.getMappedClass().equals( mappedSuperclassMetadata.getManagedClass().toJavaClass() );

			processAttributes( mappedSuperclassMetadata, mappedSuperclassMapping );
			processAttributesUp( mappedSuperclassMetadata, mappedSuperclassMapping );
		}
		else {
			final EntityTypeMetadata superEntityMetadata = (EntityTypeMetadata) superType;
			final PersistentClass superPersistentClass = persistentClass.getSuperclass();

			assert superPersistentClass != null;
			assert superPersistentClass.getEntityName().equals( superEntityMetadata.getManagedClass().getName() );

			processAttributes( superEntityMetadata, superPersistentClass );
			processAttributesUp( superEntityMetadata, superPersistentClass );
		}
	}

	private void processAttributesUp(MappedSuperclassTypeMetadata mappedSuperclassMetadata, MappedSuperclass mappedSuperclass) {

	}

	private void processAttributesDown(EntityTypeMetadata entityMetadata, PersistentClass persistentClass) {
		if ( entityMetadata.getNumberOfSubTypes() == 0 ) {
			return;
		}

		throw new UnsupportedOperationException( "Not yet implemented" );
	}

	private void processAttributesDown(MappedSuperclassTypeMetadata mappedSuperclassMetadata, MappedSuperclass mappedSuperclass) {
		if ( mappedSuperclassMetadata.getNumberOfSubTypes() == 0 ) {
			return;
		}

		throw new UnsupportedOperationException( "Not yet implemented" );
	}

	private void processManagedPackage(String packageName) {
		final ClassDetails packageInfoDetails;
		try {
			packageInfoDetails = processingContext.getClassDetailsRegistry().resolveManagedClass(
					packageName + ".package-info",
					ClassDetailsBuilderImpl.INSTANCE
			);
		}
		catch (Exception e) {
			return;
		}

		assert packageInfoDetails != null;

// todo (annotation-source) : AnnotationBinder#bindPackage
//		handleIdGenerators( packageInfoDetails, context );
//		bindTypeDescriptorRegistrations( packageInfoDetails, context );
//		bindEmbeddableInstantiatorRegistrations( packageInfoDetails, context );
//		bindUserTypeRegistrations( packageInfoDetails, context );
//		bindCompositeUserTypeRegistrations( packageInfoDetails, context );
//		bindConverterRegistrations( packageInfoDetails, context );
//		bindGenericGenerators( packageInfoDetails, context );
//		bindQueries( packageInfoDetails, context );
//		bindFilterDefs( packageInfoDetails, context );
	}

	private void prepareManagedResources(ManagedResources managedResources) {
		final ClassDetails attributeConverterClassDetails = classDetailsRegistry.resolveManagedClass(
				AttributeConverter.class.getName(),
				() -> new ClassDetailsImpl( AttributeConverter.class, processingContext )
		);

		for ( Class<?> managedClassReference : managedResources.getAnnotatedClassReferences() ) {
			final ClassDetails classDetails = classDetailsRegistry.resolveManagedClass( managedClassReference.getName() );
			// if the Class is a converter, register it
			if ( isConverter( classDetails, attributeConverterClassDetails ) ) {
				//noinspection unchecked
				converterRegistry.addAttributeConverter( (Class<? extends AttributeConverter<?,?>>) managedClassReference );
			}
		}

		for ( String managedClassName : managedResources.getAnnotatedClassNames() ) {
			final ClassDetails classDetails = classDetailsRegistry.resolveManagedClass( managedClassName );
			// if the Class is a converter, register it
			if ( isConverter( classDetails, attributeConverterClassDetails ) ) {
				//noinspection unchecked
				converterRegistry.addAttributeConverter( (Class<? extends AttributeConverter<?,?>>) classDetails.toJavaClass() );
			}
		}

		if ( CollectionHelper.isNotEmpty( managedResources.getAttributeConverterDescriptors() ) ) {
			for ( ConverterDescriptor converterDescriptor : managedResources.getAttributeConverterDescriptors() ) {
				converterRegistry.addAttributeConverter( converterDescriptor );
			}
		}
	}

	private static boolean isConverter(ClassDetails classDetails, ClassDetails attributeConverterClassDetails) {
		return classDetails.getAnnotation( JpaAnnotations.CONVERTER ) != null
				|| classDetails.implementsInterface( attributeConverterClassDetails );
	}
}
