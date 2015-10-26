/*
 * Hibernate, Relational Persistence for Idiomatic Java
 *
 * License: GNU Lesser General Public License (LGPL), version 2.1 or later.
 * See the lgpl.txt file in the root directory or <http://www.gnu.org/licenses/lgpl-2.1.html>.
 */
package org.hibernate.boot.model.source.internal.annotations.bind;

import java.util.Set;

import org.hibernate.AssertionFailure;
import org.hibernate.boot.model.IdentifierGeneratorDefinition;
import org.hibernate.boot.model.TruthValue;
import org.hibernate.boot.model.naming.EntityNaming;
import org.hibernate.boot.model.naming.Identifier;
import org.hibernate.boot.model.naming.ImplicitEntityNameSource;
import org.hibernate.boot.model.naming.ImplicitNamingStrategy;
import org.hibernate.boot.model.naming.PhysicalNamingStrategy;
import org.hibernate.boot.model.relational.Database;
import org.hibernate.boot.model.relational.Namespace;
import org.hibernate.boot.model.source.spi.AttributeSource;
import org.hibernate.boot.model.source.spi.DiscriminatorSource;
import org.hibernate.boot.model.source.spi.IdentifiableTypeSource;
import org.hibernate.boot.model.source.spi.IdentifierSourceSimple;
import org.hibernate.boot.model.source.spi.PluralAttributeSource;
import org.hibernate.boot.model.source.internal.annotations.RootAnnotationBindingContext;
import org.hibernate.boot.model.source.internal.annotations.ColumnSourceImpl;
import org.hibernate.boot.model.source.internal.annotations.DerivedValueSourceImpl;
import org.hibernate.boot.model.source.internal.annotations.EntityHierarchySourceImpl;
import org.hibernate.boot.model.source.internal.annotations.EntitySourceImpl;
import org.hibernate.boot.model.source.internal.annotations.IdentifiableTypeSourceAdapter;
import org.hibernate.boot.model.source.internal.annotations.InLineViewSourceImpl;
import org.hibernate.boot.model.source.internal.annotations.JoinedSubclassEntitySourceImpl;
import org.hibernate.boot.model.source.internal.annotations.MappedSuperclassSourceImpl;
import org.hibernate.boot.model.source.internal.annotations.RootEntitySourceImpl;
import org.hibernate.boot.model.source.internal.annotations.SubclassEntitySourceImpl;
import org.hibernate.boot.model.source.internal.annotations.TableSourceImpl;
import org.hibernate.boot.model.source.spi.InheritanceType;
import org.hibernate.boot.model.source.spi.NaturalIdMutability;
import org.hibernate.boot.model.source.spi.SingularAttributeSource;
import org.hibernate.boot.model.source.spi.SingularAttributeSourceBasic;
import org.hibernate.boot.registry.classloading.spi.ClassLoaderService;
import org.hibernate.boot.spi.MetadataBuildingContext;
import org.hibernate.cfg.NotYetImplementedException;
import org.hibernate.engine.jdbc.env.spi.JdbcEnvironment;
import org.hibernate.internal.util.StringHelper;
import org.hibernate.mapping.Column;
import org.hibernate.mapping.Formula;
import org.hibernate.mapping.JoinedSubclass;
import org.hibernate.mapping.MappedSuperclass;
import org.hibernate.mapping.PersistentClass;
import org.hibernate.mapping.Property;
import org.hibernate.mapping.RootClass;
import org.hibernate.mapping.SimpleValue;
import org.hibernate.mapping.Subclass;
import org.hibernate.mapping.Table;
import org.hibernate.mapping.UnionSubclass;
import org.hibernate.mapping.Value;

/**
 * Coordinates the building of the mapping model ({@link PersistentClass} and friends) from the
 * annotation source model.
 *
 * @author Steve Ebersole
 */
public class ModelProcessor {

	public static void bindModel(
			RootAnnotationBindingContext rootBindingContext,
			Set<String> processedEntityNames) {
		new ModelProcessor( rootBindingContext ).bindHierarchies( processedEntityNames );
	}

	private final RootAnnotationBindingContext rootBindingContext;

	// local variables for perf
	private final ClassLoaderService classLoaderService;
	private final Database database;
	private final JdbcEnvironment jdbcEnvironment;
	private final ImplicitNamingStrategy implicitNamingStrategy;
	private final PhysicalNamingStrategy physicalNamingStrategy;

	private ModelProcessor(RootAnnotationBindingContext rootBindingContext) {
		this.rootBindingContext = rootBindingContext;
		this.classLoaderService = rootBindingContext.getBuildingOptions().getServiceRegistry().getService(
				ClassLoaderService.class
		);
		this.database = rootBindingContext.getMetadataCollector().getDatabase();
		this.jdbcEnvironment = database.getJdbcEnvironment();
		this.implicitNamingStrategy = rootBindingContext.getBuildingOptions().getImplicitNamingStrategy();
		this.physicalNamingStrategy = rootBindingContext.getBuildingOptions().getPhysicalNamingStrategy();
	}

	private void bindHierarchies(Set<String> processedEntityNames) {
		final Set<EntityHierarchySourceImpl> hierarchies = EntityHierarchyBuilder.createEntityHierarchies( rootBindingContext );
		for ( EntityHierarchySourceImpl hierarchy : hierarchies ) {
			if ( processedEntityNames.contains( hierarchy.getRoot().getEntityName() ) ) {
				// presumably already processed via hbm.xml
				continue;
			}
			bindPersistentClasses( hierarchy, processedEntityNames );
		}
	}

	/**
	 * Make all PersistentClass and MappedSuperclass instances.
	 * <p/>
	 * At the same time we do some extra work to prepare for the next steps in binding:<ul>
	 *     <li>build a Map from all entity-names to corresponding root entity-name</li>
	 *     <li>build an index for identifier resolution</li>
	 *     <li>build an index for attribute reference resolution</li>
	 *     <li>others?</li>
	 * </ul>
	 *
	 * @param hierarchy The hierarchy
	 * @param processedEntityNames
	 */
	private void bindPersistentClasses(EntityHierarchySourceImpl hierarchy, Set<String> processedEntityNames) {
		final RootClass rootEntityBinding = new RootClass( hierarchy.getRoot().getLocalBindingContext() );
		bindTable( rootEntityBinding, hierarchy.getRoot() );

		bindBasicEntityInfo( rootEntityBinding, hierarchy.getRoot() );
		processedEntityNames.add( rootEntityBinding.getEntityName() );

		// identifier
		switch ( hierarchy.getIdentifierSource().getNature() ) {
			case SIMPLE: {
				final IdentifierSourceSimple simpleIdentifierSource = (IdentifierSourceSimple) hierarchy.getIdentifierSource();
				final SingularAttributeSourceBasic idAttributeSource = simpleIdentifierSource.getIdentifierAttributeSource();

				final SimpleValue idValue = new SimpleValue(
						rootBindingContext.getMetadataCollector(),
						rootEntityBinding.getRootTable()
				);
				if ( idAttributeSource.getTypeInformation() != null ) {
					idValue.setTypeName( idAttributeSource.getTypeInformation().getName() );
					idValue.setTypeParameters( idAttributeSource.getTypeInformation().getParameters() );
				}
				idValue.setTypeUsingReflection( hierarchy.getRoot().getClassName(), idAttributeSource.getName() );

				if ( simpleIdentifierSource.getIdentifierGenerationInformation() != null ) {
					final IdentifierGeneratorDefinition idGenDef = rootBindingContext.getIdentifierGeneratorDefinition(
							simpleIdentifierSource.getIdentifierGenerationInformation().getLocalName()
					);
					idValue.setIdentifierGeneratorStrategy( idGenDef.getStrategy() );
					idValue.setIdentifierGeneratorProperties( idGenDef.getParameters() );
				}

				final Property idProperty = new Property();
				idProperty.setValue( idValue );
				idProperty.setName( idAttributeSource.getName() );
				idProperty.setPropertyAccessorName( idAttributeSource.getPropertyAccessorName() );
				idProperty.setInsertable( idAttributeSource.isInsertable() );
				idProperty.setUpdateable( false );
				idProperty.setOptional( false );

				rootEntityBinding.setIdentifier( idValue );
				rootEntityBinding.setIdentifierProperty( idProperty );
				rootEntityBinding.setIdentifierMapper( null );

				if ( idAttributeSource.getRelationalValueSources().isEmpty() ) {
					RelationalObjectBinder.bindImplicitSimpleIdColumn(
							hierarchy.getRoot(),
							idAttributeSource,
							idValue,
							rootBindingContext
					);
				}
				else {

				}

				break;
			}
			case NON_AGGREGATED_COMPOSITE: {
				throw new NotYetImplementedException( "not yet implemented" );
			}
			case AGGREGATED_COMPOSITE: {
				throw new NotYetImplementedException( "not yet implemented" );
			}
		}

		// caching
		if ( hierarchy.getCaching() != null ) {
			rootEntityBinding.setCachingExplicitlyRequested( hierarchy.getCaching().getRequested() == TruthValue.TRUE );
			rootEntityBinding.setCacheRegionName( hierarchy.getCaching().getRegion() );
			if ( hierarchy.getCaching().getAccessType() != null ) {
				rootEntityBinding.setCacheConcurrencyStrategy( hierarchy.getCaching().getAccessType().getExternalName() );
			}
			else {
				rootEntityBinding.setCacheConcurrencyStrategy( rootBindingContext.getMappingDefaults().getImplicitCacheAccessType().getExternalName() );
			}
			rootEntityBinding.setLazyPropertiesCacheable( hierarchy.getCaching().isCacheLazyProperties() );
		}

		// natural-id caching
		if ( hierarchy.getNaturalIdCaching() != null ) {
			rootEntityBinding.setNaturalIdCacheRegionName( hierarchy.getNaturalIdCaching().getRegion() );
		}

		// bind hierarchy
		bindMappedSuperclasses( rootEntityBinding, hierarchy.getRoot() );
		bindSubclasses( rootEntityBinding, hierarchy.getRoot(), processedEntityNames );
	}

	private void bindTable(RootClass rootEntityBinding, final RootEntitySourceImpl root) {
		final Namespace namespace = database.locateNamespace(
				jdbcEnvironment.getIdentifierHelper().toIdentifier( root.getPrimaryTable().getExplicitCatalogName() ),
				jdbcEnvironment.getIdentifierHelper().toIdentifier( root.getPrimaryTable().getExplicitSchemaName() )
		);

		if ( root.getPrimaryTable() instanceof TableSourceImpl ) {
			final TableSourceImpl tableSource = (TableSourceImpl) root.getPrimaryTable();
			final Identifier logicalTableName;

			if ( StringHelper.isNotEmpty( tableSource.getExplicitTableName() ) ) {
				logicalTableName = database.toIdentifier( tableSource.getExplicitTableName() );
			}
			else {
				final ImplicitEntityNameSource implicitNamingSource = new ImplicitEntityNameSource() {
					@Override
					public EntityNaming getEntityNaming() {
						return root.getEntityNamingSource();
					}

					@Override
					public MetadataBuildingContext getBuildingContext() {
						return root.getLocalBindingContext();
					}
				};
				logicalTableName = rootBindingContext.getBuildingOptions()
						.getImplicitNamingStrategy()
						.determinePrimaryTableName( implicitNamingSource );
			}

			final Table table = namespace.createTable( logicalTableName, root.isAbstract() );
			table.setRowId( tableSource.getRowId() );

			rootEntityBinding.setTable( table );

			rootBindingContext.getMetadataCollector().addTableNameBinding( logicalTableName, table );
		}
		else {
			final InLineViewSourceImpl inLineViewSource = (InLineViewSourceImpl ) root.getPrimaryTable();
			final String selectExpression = inLineViewSource.getSelectStatement();
			final Identifier logicalTableName = jdbcEnvironment.getIdentifierHelper().toIdentifier( inLineViewSource.getLogicalName() );
			final Table table = new Table( namespace, selectExpression, root.isAbstract() );
			table.setName( logicalTableName.render() );
			rootEntityBinding.setTable( table );
		}
	}

	private void bindBasicEntityInfo(PersistentClass entityBinding, EntitySourceImpl entitySource) {
		entityBinding.setEntityName( entitySource.getEntityName() );
		entityBinding.setJpaEntityName( entitySource.getJpaEntityName() );
		entityBinding.setClassName( entitySource.getClassName() );

		if ( entitySource.getCustomPersisterClassName() != null ) {
			entityBinding.setEntityPersisterClass(
					classLoaderService.classForName( entitySource.getCustomPersisterClassName() )
			);
		}

		entityBinding.setAbstract( entitySource.isAbstract() );

		entityBinding.setLazy( entitySource.isLazy() );
		entityBinding.setProxyInterfaceName( entitySource.getProxy() );
		entityBinding.setBatchSize( entitySource.getBatchSize() );

		entityBinding.setDiscriminatorValue( entitySource.getDiscriminatorMatchValue() );

		entityBinding.setDynamicInsert( entitySource.isDynamicInsert() );
		entityBinding.setDynamicUpdate( entitySource.isDynamicUpdate() );
		entityBinding.setSelectBeforeUpdate( entitySource.isSelectBeforeUpdate() );
		entityBinding.setLoaderName( entitySource.getCustomLoaderName() );
		entityBinding.setCustomSqlInsert( entitySource.getCustomSqlInsert() );
		entityBinding.setCustomSqlUpdate( entitySource.getCustomSqlUpdate() );
		entityBinding.setCustomSqlDelete( entitySource.getCustomSqlDelete() );

		entityBinding.setOptimisticLockStyle( entitySource.getHierarchy().getOptimisticLockStyle() );

		bindAttributes( entityBinding, entitySource );

		rootBindingContext.getMetadataCollector().addEntityBinding( entityBinding );
	}

	private void bindAttributes(PersistentClass entityBinding, EntitySourceImpl entitySource) {
		for ( AttributeSource attributeSource : entitySource.attributeSources() ) {
			final Property attributeBinding = new Property();
			attributeBinding.setName( attributeSource.getName() );
			attributeBinding.setPropertyAccessorName( attributeSource.getPropertyAccessorName() );
			attributeBinding.setOptimisticLocked( attributeSource.isIncludedInOptimisticLocking() );

			if ( attributeSource.isSingular() ) {
				final SingularAttributeSource singularAttributeSource = (SingularAttributeSource) attributeSource;
				final Value value;
				switch ( singularAttributeSource.getSingularAttributeNature() ) {
					case BASIC: {
						final SingularAttributeSourceBasic basicAttributeSource = (SingularAttributeSourceBasic) singularAttributeSource;
						SimpleValue basicValue = new SimpleValue(
								rootBindingContext.getMetadataCollector(),
								// this table binding means nothing, the individual column table bindings are the important ones
								entityBinding.getTable()
						);
						value = basicValue;
						basicValue.setTypeName( basicAttributeSource.getTypeInformation().getName() );
						if ( basicAttributeSource.getNaturalIdMutability() == NaturalIdMutability.NOT_NATURAL_ID ) {
							attributeBinding.setNaturalIdentifier( false );
							attributeBinding.setUpdateable( basicAttributeSource.isUpdatable() );
						}
						else {
							attributeBinding.setNaturalIdentifier( true );
							attributeBinding.setUpdateable(
									basicAttributeSource.isUpdatable()
											|| basicAttributeSource.getNaturalIdMutability() == NaturalIdMutability.MUTABLE
							);
						}

						basicValue.setJpaAttributeConverterDescriptor( basicAttributeSource.resolveAttributeConverterDescriptor() );

						break;
					}
					default: {
						throw new NotYetImplementedException( "todo" );
					}
				}
				attributeBinding.setValue( value );

				// todo : account for value generations
				//attributeBinding.setValueGenerationStrategy( singularAttributeSource. );
			}
			else {
				final PluralAttributeSource pluralAttributeSource = (PluralAttributeSource) attributeSource;
				throw new NotYetImplementedException(
						"plural attribute binding not yet implemented : " + pluralAttributeSource.getAnnotatedAttribute().toString()
				);
			}

			entityBinding.addProperty( attributeBinding );
		}

	}

	private void bindMappedSuperclasses(RootClass rootEntityBinding, RootEntitySourceImpl rootEntitySource) {
		rootEntityBinding.setSuperMappedSuperclass( bindMappedSuperclass( rootEntitySource.getSuperType() ) );
	}

	private MappedSuperclass bindMappedSuperclass(IdentifiableTypeSourceAdapter superType) {
		if ( superType == null ) {
			return null;
		}

		if ( !MappedSuperclassSourceImpl.class.isInstance( superType ) ) {
			return null;
		}

		final MappedSuperclass mappedSuperclassSuper = bindMappedSuperclass( superType.getSuperType() );
		final MappedSuperclass binding = new MappedSuperclass( mappedSuperclassSuper, null );
		final Class clazz = classLoaderService.classForName( superType.getTypeName() );
		binding.setMappedClass( clazz );
		rootBindingContext.getMetadataCollector().addMappedSuperclass( clazz, binding );

		return binding;
	}

	private void bindSubclasses(
			PersistentClass entityBinding,
			EntitySourceImpl entitySource,
			Set<String> processedEntityNames) {
		for ( IdentifiableTypeSource subType : entitySource.getSubTypes() ) {
			if ( subType instanceof JoinedSubclassEntitySourceImpl ) {
				final JoinedSubclassEntitySourceImpl joinedSubTypeSource = (JoinedSubclassEntitySourceImpl) subType;
				bindJoinedSubclass(
						new JoinedSubclass( entityBinding, joinedSubTypeSource.getLocalBindingContext() ),
						joinedSubTypeSource,
						processedEntityNames
				);
			}
			else if ( subType instanceof SubclassEntitySourceImpl ) {
				final SubclassEntitySourceImpl subTypeSource = (SubclassEntitySourceImpl) subType;
				if ( entitySource.getHierarchy().getHierarchyInheritanceType() ==  InheritanceType.DISCRIMINATED ) {
					bindDiscriminatedSubclass(
							new Subclass( entityBinding, subTypeSource.getLocalBindingContext() ),
							subTypeSource,
							processedEntityNames
					);
				}
				else {
					bindUnionSubclass(
							new UnionSubclass( entityBinding, subTypeSource.getLocalBindingContext() ),
							subTypeSource,
							processedEntityNames
					);
				}
			}
			else if ( subType instanceof MappedSuperclassSourceImpl ) {
				final MappedSuperclass mappedSuperclassSub = new MappedSuperclass( null, entityBinding );
				bindMappedSuperclassSubclass(
						entityBinding,
						mappedSuperclassSub,
						(MappedSuperclassSourceImpl) subType,
						processedEntityNames
				);
			}
			else {
				throw new AssertionFailure( "Unexpected subclass type : " + subType );
			}
		}

	}

	private void bindJoinedSubclass(
			JoinedSubclass joinedSubclass,
			JoinedSubclassEntitySourceImpl joinedSubTypeSource,
			Set<String> processedEntityNames) {
		bindBasicEntityInfo( joinedSubclass, joinedSubTypeSource );
		processedEntityNames.add( joinedSubclass.getEntityName() );

		// todo : bind JoinedSubclass specific info


		rootBindingContext.getMetadataCollector().addEntityBinding( joinedSubclass );
		bindSubclasses( joinedSubclass, joinedSubTypeSource, processedEntityNames );
	}

	private void bindDiscriminatedSubclass(
			Subclass subclass,
			SubclassEntitySourceImpl subTypeSource,
			Set<String> processedEntityNames) {
		bindBasicEntityInfo( subclass, subTypeSource );
		processedEntityNames.add( subclass.getEntityName() );

		// todo : bind Subclass specific info


		rootBindingContext.getMetadataCollector().addEntityBinding( subclass );
		bindSubclasses( subclass, subTypeSource, processedEntityNames );
	}

	private void bindUnionSubclass(
			UnionSubclass unionSubclass,
			SubclassEntitySourceImpl subTypeSource,
			Set<String> processedEntityNames) {
		bindBasicEntityInfo( unionSubclass, subTypeSource );
		processedEntityNames.add( unionSubclass.getEntityName() );

		// todo : bind UnionSubclass specific info


		rootBindingContext.getMetadataCollector().addEntityBinding( unionSubclass );
		bindSubclasses( unionSubclass, subTypeSource, processedEntityNames );
	}

	private void bindMappedSuperclassSubclass(
			PersistentClass entityBinding,
			MappedSuperclass mappedSuperclass,
			MappedSuperclassSourceImpl mappedSuperclassSubSource,
			Set<String> processedEntityNames) {
		final Class clazz = classLoaderService.classForName( mappedSuperclassSubSource.getTypeName() );
		mappedSuperclass.setMappedClass( clazz );
		rootBindingContext.getMetadataCollector().addMappedSuperclass( clazz, mappedSuperclass );

		for ( IdentifiableTypeSource subType : mappedSuperclassSubSource.getSubTypes() ) {
			if ( subType instanceof JoinedSubclassEntitySourceImpl ) {
				final JoinedSubclassEntitySourceImpl joinedSubTypeSource = (JoinedSubclassEntitySourceImpl) subType;
				bindJoinedSubclass(
						new JoinedSubclass( entityBinding, joinedSubTypeSource.getLocalBindingContext() ),
						joinedSubTypeSource,
						processedEntityNames
				);
			}
			else if ( subType instanceof SubclassEntitySourceImpl ) {
				final SubclassEntitySourceImpl subTypeSource = (SubclassEntitySourceImpl) subType;
				if ( mappedSuperclassSubSource.getHierarchy().getHierarchyInheritanceType() ==  InheritanceType.DISCRIMINATED ) {
					bindDiscriminatedSubclass(
							new Subclass( entityBinding, subTypeSource.getLocalBindingContext() ),
							subTypeSource,
							processedEntityNames
					);
				}
				else {
					bindUnionSubclass(
							new UnionSubclass( entityBinding, subTypeSource.getLocalBindingContext() ),
							subTypeSource,
							processedEntityNames
					);
				}
			}
			else if ( subType instanceof MappedSuperclassSourceImpl ) {
				final MappedSuperclass mappedSuperclassSub = new MappedSuperclass( mappedSuperclass, entityBinding );
				bindMappedSuperclassSubclass(
						entityBinding,
						mappedSuperclassSub,
						(MappedSuperclassSourceImpl) subType,
						processedEntityNames
				);
			}
			else {
				throw new AssertionFailure( "Unexpected subclass type : " + subType );
			}
		}

	}





	private void bindDiscriminator(RootClass rootEntityBinding, DiscriminatorSource discriminatorSource) {
		if ( discriminatorSource == null ) {
			return;
		}

		rootEntityBinding.setDiscriminatorInsertable( discriminatorSource.isInserted() );
		rootEntityBinding.setForceDiscriminator( discriminatorSource.isForced() );

		final SimpleValue discriminatorValue = new SimpleValue( rootBindingContext.getMetadataCollector(), rootEntityBinding.getTable() );
		discriminatorValue.setTypeName( discriminatorSource.getExplicitHibernateTypeName() );

		if ( discriminatorSource.getDiscriminatorRelationalValueSource() instanceof ColumnSourceImpl ) {
			final ColumnSourceImpl source = (ColumnSourceImpl) discriminatorSource.getDiscriminatorRelationalValueSource();
			final Column discriminatorColumn = new Column();

			// column name
			Identifier logicalName = StringHelper.isNotEmpty( source.getName() )
					? jdbcEnvironment.getIdentifierHelper().toIdentifier( source.getName() )
					: implicitNamingStrategy.determineDiscriminatorColumnName( discriminatorSource );
			if ( logicalName == null ) {
				logicalName = jdbcEnvironment.getIdentifierHelper().toIdentifier(
						rootBindingContext.getMappingDefaults().getImplicitDiscriminatorColumnName()
				);
			}

			final Identifier physicalName = physicalNamingStrategy.toPhysicalColumnName( logicalName, jdbcEnvironment );
			discriminatorColumn.setName( physicalName.render( jdbcEnvironment.getDialect() ) );

			// column type
			if ( source.getDatatype() != null ) {
				discriminatorColumn.setSqlTypeCode( source.getDatatype().getTypeCode() );
			}

			// nullability
			discriminatorColumn.setNullable( source.isNullable() == TruthValue.TRUE );

			// unique (kind of silly for a discriminator to be unique, but...)
			discriminatorColumn.setUnique( source.isUnique() );

			// size
			if ( source.getSizeSource() != null ) {
				if ( source.getSizeSource().getLength() != null ) {
					discriminatorColumn.setLength( source.getSizeSource().getLength() );
				}
				if ( source.getSizeSource().getScale() != null ) {
					discriminatorColumn.setScale( source.getSizeSource().getScale() );
				}
				if ( source.getSizeSource().getPrecision() != null ) {
					discriminatorColumn.setPrecision( source.getSizeSource().getPrecision() );
				}
			}

			// read/write fragments
			if ( StringHelper.isNotEmpty( source.getReadFragment() ) ) {
				discriminatorColumn.setCustomRead( source.getReadFragment() );
			}
			if ( StringHelper.isNotEmpty( source.getWriteFragment() ) ) {
				discriminatorColumn.setCustomWrite( source.getWriteFragment() );
			}

			// default value
			if ( StringHelper.isNotEmpty( source.getDefaultValue() ) ) {
				discriminatorColumn.setDefaultValue( source.getDefaultValue() );
			}

			// comment
			if ( StringHelper.isNotEmpty( source.getComment() ) ) {
				discriminatorColumn.setComment( source.getComment() );
			}
			else {
				discriminatorColumn.setComment( "Discriminator column : "  + rootEntityBinding.getEntityName() );
			}

			// check
			if ( StringHelper.isNotEmpty( source.getCheckCondition() ) ) {
				discriminatorColumn.setCheckConstraint( source.getCheckCondition() );
			}


			discriminatorValue.addColumn( discriminatorColumn );
			rootEntityBinding.getTable().addColumn( discriminatorColumn );
		}
		else {
			final DerivedValueSourceImpl source = (DerivedValueSourceImpl) discriminatorSource.getDiscriminatorRelationalValueSource();
			final Formula discriminatorFormula = new Formula( source.getExpression() );

			discriminatorValue.addFormula( discriminatorFormula );
		}

		rootEntityBinding.setDiscriminator( discriminatorValue );
	}
}
