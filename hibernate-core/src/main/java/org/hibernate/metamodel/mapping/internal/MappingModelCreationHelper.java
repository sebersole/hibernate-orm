/*
 * Hibernate, Relational Persistence for Idiomatic Java
 *
 * License: GNU Lesser General Public License (LGPL), version 2.1 or later
 * See the lgpl.txt file in the root directory or http://www.gnu.org/licenses/lgpl-2.1.html
 */
package org.hibernate.metamodel.mapping.internal;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.SortedMap;
import java.util.SortedSet;
import java.util.function.BiConsumer;

import org.hibernate.FetchMode;
import org.hibernate.HibernateException;
import org.hibernate.MappingException;
import org.hibernate.NotYetImplementedFor6Exception;
import org.hibernate.collection.internal.StandardArraySemantics;
import org.hibernate.collection.internal.StandardBagSemantics;
import org.hibernate.collection.internal.StandardIdentifierBagSemantics;
import org.hibernate.collection.internal.StandardListSemantics;
import org.hibernate.collection.spi.CollectionSemantics;
import org.hibernate.dialect.Dialect;
import org.hibernate.engine.FetchStrategy;
import org.hibernate.engine.FetchStyle;
import org.hibernate.engine.FetchTiming;
import org.hibernate.engine.spi.CascadeStyle;
import org.hibernate.engine.spi.CascadeStyles;
import org.hibernate.engine.spi.SessionFactoryImplementor;
import org.hibernate.internal.util.collections.CollectionHelper;
import org.hibernate.mapping.BasicValue;
import org.hibernate.mapping.Collection;
import org.hibernate.mapping.Component;
import org.hibernate.mapping.IndexedCollection;
import org.hibernate.mapping.ManyToOne;
import org.hibernate.mapping.OneToMany;
import org.hibernate.mapping.OneToOne;
import org.hibernate.mapping.PersistentClass;
import org.hibernate.mapping.Property;
import org.hibernate.mapping.Selectable;
import org.hibernate.mapping.ToOne;
import org.hibernate.mapping.Value;
import org.hibernate.metamodel.CollectionClassification;
import org.hibernate.metamodel.MappingMetamodel;
import org.hibernate.metamodel.mapping.BasicSingularAttribute;
import org.hibernate.metamodel.mapping.CollectionIdentifierDescriptor;
import org.hibernate.metamodel.mapping.CollectionMappingType;
import org.hibernate.metamodel.mapping.CollectionPart;
import org.hibernate.metamodel.mapping.CompositeIdentifierMapping;
import org.hibernate.metamodel.mapping.EmbeddableMappingType;
import org.hibernate.metamodel.mapping.EntityIdentifierMapping;
import org.hibernate.metamodel.mapping.ManagedMappingType;
import org.hibernate.metamodel.mapping.PluralAttributeMapping;
import org.hibernate.metamodel.mapping.SingularAttributeMapping;
import org.hibernate.metamodel.mapping.StateArrayContributorMetadata;
import org.hibernate.metamodel.mapping.StateArrayContributorMetadataAccess;
import org.hibernate.metamodel.mapping.ToOneAttributeMapping;
import org.hibernate.metamodel.model.convert.spi.BasicValueConverter;
import org.hibernate.metamodel.model.domain.NavigableRole;
import org.hibernate.metamodel.spi.RuntimeModelCreationContext;
import org.hibernate.persister.collection.CollectionPersister;
import org.hibernate.persister.collection.SQLLoadableCollection;
import org.hibernate.persister.entity.EntityPersister;
import org.hibernate.persister.entity.Joinable;
import org.hibernate.persister.walking.internal.FetchStrategyHelper;
import org.hibernate.property.access.internal.PropertyAccessStrategyMapImpl;
import org.hibernate.property.access.spi.PropertyAccess;
import org.hibernate.sql.ast.spi.SqlAliasStemHelper;
import org.hibernate.type.AnyType;
import org.hibernate.type.AssociationType;
import org.hibernate.type.BasicType;
import org.hibernate.type.CollectionType;
import org.hibernate.type.CompositeType;
import org.hibernate.type.EntityType;
import org.hibernate.type.ForeignKeyDirection;
import org.hibernate.type.Type;
import org.hibernate.type.descriptor.java.ImmutableMutabilityPlan;
import org.hibernate.type.descriptor.java.JavaTypeDescriptor;
import org.hibernate.type.descriptor.java.MutabilityPlan;
import org.hibernate.type.descriptor.java.spi.JavaTypeDescriptorRegistry;

/**
 * @author Steve Ebersole
 */
public class MappingModelCreationHelper {
	/**
	 * A factory - disallow direct instantiation
	 */
	private MappingModelCreationHelper() {
	}

	// ~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~

	public static EntityIdentifierMapping buildEncapsulatedCompositeIdentifierMapping(
			EntityPersister entityPersister,
			Property bootProperty,
			String attributeName,
			String rootTableName,
			String[] rootTableKeyColumnNames,
			CompositeType cidType,
			MappingModelCreationProcess creationProcess) {
		final PersistentClass bootEntityDescriptor = creationProcess.getCreationContext()
				.getBootModel()
				.getEntityBinding( entityPersister.getEntityName() );

		final PropertyAccess propertyAccess = entityPersister.getRepresentationStrategy()
				.resolvePropertyAccess( bootEntityDescriptor.getIdentifierProperty() );

		final StateArrayContributorMetadataAccess attributeMetadataAccess = getStateArrayContributorMetadataAccess(
				propertyAccess
		);

		final EmbeddableMappingType embeddableMappingType = EmbeddableMappingType.from(
				(Component) bootProperty.getValue(),
				cidType,
				embeddable -> new EmbeddedIdentifierMappingImpl(
						entityPersister,
						attributeName,
						embeddable,
						attributeMetadataAccess,
						propertyAccess,
						rootTableName,
						rootTableKeyColumnNames,
						creationProcess.getCreationContext().getSessionFactory()
				),
				creationProcess
		);


		return (EmbeddedIdentifierMappingImpl) embeddableMappingType.getEmbeddedValueMapping();
	}

	public static CompositeIdentifierMapping buildNonEncapsulatedCompositeIdentifierMapping(
			EntityPersister entityPersister,
			String rootTableName,
			String[] rootTableKeyColumnNames,
			CompositeType cidType,
			PersistentClass bootEntityDescriptor,
			BiConsumer<String,SingularAttributeMapping> idSubAttributeConsumer,
			MappingModelCreationProcess creationProcess) {
		final SessionFactoryImplementor sessionFactory = creationProcess.getCreationContext().getSessionFactory();
		final Component bootCompositeDescriptor = (Component) bootEntityDescriptor.getIdentifier();

		final PropertyAccess propertyAccess = PropertyAccessStrategyMapImpl.INSTANCE.buildPropertyAccess(
				null,
				EntityIdentifierMapping.ROLE_LOCAL_NAME
		);

		final StateArrayContributorMetadataAccess attributeMetadataAccess = getStateArrayContributorMetadataAccess(
				propertyAccess
		);

		final EmbeddableMappingType embeddableMappingType = EmbeddableMappingType.from(
				bootCompositeDescriptor,
				cidType,
				attributeMappingType -> {
					final Component bootIdDescriptor = (Component) bootEntityDescriptor.getIdentifier();

					final List<SingularAttributeMapping> idAttributeMappings = new ArrayList<>( bootIdDescriptor.getPropertySpan() );

					//noinspection unchecked
					final Iterator<Property> bootIdSubPropertyItr = bootIdDescriptor.getPropertyIterator();
					int columnsConsumedSoFar = 0;

					while ( bootIdSubPropertyItr.hasNext() ) {
						final Property bootIdSubProperty = bootIdSubPropertyItr.next();
						final Type idSubPropertyType = bootIdSubProperty.getType();

						if ( idSubPropertyType instanceof AnyType ) {
							throw new HibernateException(
									"AnyType property `" + bootEntityDescriptor.getEntityName() + "#" + bootIdSubProperty.getName() +
											"` cannot be used as part of entity identifier "
							);
						}

						if ( idSubPropertyType instanceof CollectionType ) {
							throw new HibernateException(
									"Plural property `" + bootEntityDescriptor.getEntityName() + "#" + bootIdSubProperty.getName() +
											"` cannot be used as part of entity identifier "
							);
						}

						final SingularAttributeMapping idSubAttribute;

						if ( idSubPropertyType instanceof BasicType ) {
							//noinspection rawtypes
							idSubAttribute = buildBasicAttributeMapping(
									bootIdSubProperty.getName(),
									entityPersister.getNavigableRole().append( bootIdSubProperty.getName() ),
									idAttributeMappings.size(),
									bootIdSubProperty,
									attributeMappingType,
									(BasicType) idSubPropertyType,
									rootTableName,
									rootTableKeyColumnNames[columnsConsumedSoFar],
									entityPersister.getRepresentationStrategy().resolvePropertyAccess( bootIdSubProperty ),
									CascadeStyles.ALL,
									creationProcess
							);
							columnsConsumedSoFar++;
						}
						else if ( idSubPropertyType instanceof CompositeType ) {
							// nested composite
							throw new NotYetImplementedFor6Exception();
						}
						else if ( idSubPropertyType instanceof EntityType ) {
							// key-many-to-one
							final EntityType keyManyToOnePropertyType = (EntityType) idSubPropertyType;

							idSubAttribute = buildSingularAssociationAttributeMapping(
									bootIdSubProperty.getName(),
									entityPersister.getNavigableRole().append( EntityIdentifierMapping.ROLE_LOCAL_NAME ),
									idAttributeMappings.size(),
									bootIdSubProperty,
									attributeMappingType,
									keyManyToOnePropertyType,
									entityPersister.getRepresentationStrategy().resolvePropertyAccess( bootIdSubProperty ),
									CascadeStyles.ALL,
									creationProcess
							);

							columnsConsumedSoFar += keyManyToOnePropertyType.getColumnSpan( sessionFactory );
						}
						else {
							throw new UnsupportedOperationException();
						}

						idAttributeMappings.add( idSubAttribute );
						idSubAttributeConsumer.accept( idSubAttribute.getAttributeName(), idSubAttribute );
					}

					return new NonAggregatedIdentifierMappingImpl(
							attributeMappingType,
							entityPersister,
							idAttributeMappings,
							attributeMetadataAccess,
							rootTableName,
							rootTableKeyColumnNames,
							bootCompositeDescriptor,
							bootEntityDescriptor.getDeclaredIdentifierMapper(),
							creationProcess
					);
				},
				creationProcess
		);

		return (CompositeIdentifierMapping) embeddableMappingType.getEmbeddedValueMapping();
	}


	// ~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~
	// Non-identifier attributes

	@SuppressWarnings("rawtypes")
	public static BasicSingularAttribute buildBasicAttributeMapping(
			String attrName,
			NavigableRole navigableRole,
			int stateArrayPosition,
			Property bootProperty,
			ManagedMappingType declaringType,
			BasicType attrType,
			String tableExpression,
			String attrColumnName,
			PropertyAccess propertyAccess,
			CascadeStyle cascadeStyle,
			MappingModelCreationProcess creationProcess) {
		final BasicValue.Resolution<?> resolution = ( (BasicValue) bootProperty.getValue() ).resolve();

		final BasicValueConverter valueConverter = resolution.getValueConverter();

		final StateArrayContributorMetadataAccess attributeMetadataAccess = entityMappingType -> new StateArrayContributorMetadata() {
			private final MutabilityPlan mutabilityPlan = resolution.getMutabilityPlan();
			private final boolean nullable = bootProperty.getValue().isNullable();
			private final boolean insertable = bootProperty.isInsertable();
			private final boolean updateable = bootProperty.isUpdateable();
			private final boolean includeInOptimisticLocking = bootProperty.isOptimisticLocked();

			@Override
			public PropertyAccess getPropertyAccess() {
				return propertyAccess;
			}

			@Override
			public MutabilityPlan getMutabilityPlan() {
				return mutabilityPlan;
			}

			@Override
			public boolean isNullable() {
				return nullable;
			}

			@Override
			public boolean isInsertable() {
				return insertable;
			}

			@Override
			public boolean isUpdatable() {
				return updateable;
			}

			@Override
			public boolean isIncludedInDirtyChecking() {
				// todo (6.0) : do not believe this is correct
				return updateable;
			}

			@Override
			public boolean isIncludedInOptimisticLocking() {
				return includeInOptimisticLocking;
			}

			@Override
			public CascadeStyle getCascadeStyle() {
				return cascadeStyle;
			}
		};

		final FetchStrategy fetchStrategy = bootProperty.isLazy()
				? new FetchStrategy( FetchTiming.DELAYED, FetchStyle.SELECT )
				: FetchStrategy.IMMEDIATE_JOIN;

		if ( valueConverter != null ) {
			// we want to "decompose" the "type" into its various pieces as expected by the mapping
			assert valueConverter.getRelationalJavaDescriptor() == resolution.getRelationalJavaDescriptor();

			final BasicType<?> mappingBasicType = creationProcess.getCreationContext()
					.getDomainModel()
					.getTypeConfiguration()
					.getBasicTypeRegistry()
					.resolve(
							valueConverter.getRelationalJavaDescriptor(),
							resolution.getRelationalSqlTypeDescriptor()
					);


			return new BasicSingularAttributeImpl(
					attrName,
					navigableRole,
					stateArrayPosition,
					attributeMetadataAccess,
					fetchStrategy,
					tableExpression,
					attrColumnName,
					valueConverter,
					mappingBasicType.getJdbcMapping(),
					declaringType,
					propertyAccess
			);
		}
		else {
			return new BasicSingularAttributeImpl(
					attrName,
					navigableRole,
					stateArrayPosition,
					attributeMetadataAccess,
					fetchStrategy,
					tableExpression,
					attrColumnName,
					null,
					attrType,
					declaringType,
					propertyAccess
			);
		}
	}


	public static EmbeddedAttributeMappingImpl buildEmbeddedAttributeMapping(
			String attrName,
			int stateArrayPosition,
			Property bootProperty,
			ManagedMappingType declaringType,
			CompositeType attrType,
			String tableExpression,
			String[] attrColumnNames,
			PropertyAccess propertyAccess,
			CascadeStyle cascadeStyle,
			MappingModelCreationProcess creationProcess) {
		final StateArrayContributorMetadataAccess attributeMetadataAccess = getStateArrayContributorMetadataAccess(
				bootProperty,
				attrType,
				propertyAccess,
				cascadeStyle,
				creationProcess
		);

		final EmbeddableMappingType embeddableMappingType = EmbeddableMappingType.from(
				(Component) bootProperty.getValue(),
				attrType,
				attributeMappingType -> new EmbeddedAttributeMappingImpl(
						attrName,
						declaringType.getNavigableRole().append( attrName ),
						stateArrayPosition,
						tableExpression,
						attrColumnNames,
						attributeMetadataAccess,
						FetchStrategy.IMMEDIATE_JOIN,
						attributeMappingType,
						declaringType,
						propertyAccess
				),
				creationProcess
		);

		return (EmbeddedAttributeMappingImpl) embeddableMappingType.getEmbeddedValueMapping();
	}

	@SuppressWarnings("rawtypes")
	protected static StateArrayContributorMetadataAccess getStateArrayContributorMetadataAccess(
			Property bootProperty,
			Type attrType,
			PropertyAccess propertyAccess,
			CascadeStyle cascadeStyle,
			MappingModelCreationProcess creationProcess) {
		return entityMappingType -> new StateArrayContributorMetadata() {
			private final boolean nullable = bootProperty.getValue().isNullable();
			private final boolean insertable = bootProperty.isInsertable();
			private final boolean updateable = bootProperty.isUpdateable();
			private final boolean includeInOptimisticLocking = bootProperty.isOptimisticLocked();

			private final MutabilityPlan mutabilityPlan;

			{
				if ( updateable ) {
					mutabilityPlan = new MutabilityPlan() {
						@Override
						public boolean isMutable() {
							return true;
						}

						@Override
						public Object deepCopy(Object value) {
							if ( value == null ) {
								return null;
							}

							return attrType.deepCopy( value, creationProcess.getCreationContext().getSessionFactory() );
						}

						@Override
						public Serializable disassemble(Object value) {
							throw new NotYetImplementedFor6Exception( getClass() );
						}

						@Override
						public Object assemble(Serializable cached) {
							throw new NotYetImplementedFor6Exception( getClass() );
						}
					};
				}
				else {
					mutabilityPlan = ImmutableMutabilityPlan.INSTANCE;
				}
			}

			@Override
			public PropertyAccess getPropertyAccess() {
				return propertyAccess;
			}

			@Override
			public MutabilityPlan getMutabilityPlan() {
				return mutabilityPlan;
			}

			@Override
			public boolean isNullable() {
				return nullable;
			}

			@Override
			public boolean isInsertable() {
				return insertable;
			}

			@Override
			public boolean isUpdatable() {
				return updateable;
			}

			@Override
			public boolean isIncludedInDirtyChecking() {
				// todo (6.0) : do not believe this is correct
				return updateable;
			}

			@Override
			public boolean isIncludedInOptimisticLocking() {
				return includeInOptimisticLocking;
			}

			@Override
			public CascadeStyle getCascadeStyle() {
				return cascadeStyle;
			}
		};
	}

	@SuppressWarnings("rawtypes")
	protected static StateArrayContributorMetadataAccess getStateArrayContributorMetadataAccess(
			PropertyAccess propertyAccess) {
		return entityMappingType -> new StateArrayContributorMetadata() {

			private final MutabilityPlan mutabilityPlan = ImmutableMutabilityPlan.INSTANCE;


			@Override
			public PropertyAccess getPropertyAccess() {
				return propertyAccess;
			}

			@Override
			public MutabilityPlan getMutabilityPlan() {
				return mutabilityPlan;
			}

			@Override
			public boolean isNullable() {
				return false;
			}

			@Override
			public boolean isInsertable() {
				return true;
			}

			@Override
			public boolean isUpdatable() {
				return false;
			}

			@Override
			public boolean isIncludedInDirtyChecking() {

				return false;
			}

			@Override
			public boolean isIncludedInOptimisticLocking() {
				// todo (6.0) : do not sure this is correct
				return true;
			}

			@Override
			public CascadeStyle getCascadeStyle() {
				// todo (6.0) : do not sure this is correct
				return null;
			}
		};
	}

	@SuppressWarnings("rawtypes")
	public static PluralAttributeMapping buildPluralAttributeMapping(
			String attrName,
			int stateArrayPosition,
			Property bootProperty,
			ManagedMappingType declaringType,
			PropertyAccess propertyAccess,
			CascadeStyle cascadeStyle,
			FetchMode fetchMode,
			MappingModelCreationProcess creationProcess) {

		final Collection bootValueMapping = (Collection) bootProperty.getValue();

		final RuntimeModelCreationContext creationContext = creationProcess.getCreationContext();
		final SessionFactoryImplementor sessionFactory = creationContext.getSessionFactory();
		final Dialect dialect = sessionFactory.getJdbcServices().getJdbcEnvironment().getDialect();
		final MappingMetamodel domainModel = creationContext.getDomainModel();

		final CollectionPersister collectionDescriptor = domainModel.findCollectionDescriptor( bootValueMapping.getRole() );
		assert collectionDescriptor != null;

		final String tableExpression = ( (Joinable) collectionDescriptor ).getTableName();

		final String sqlAliasStem = SqlAliasStemHelper.INSTANCE.generateStemFromAttributeName( bootProperty.getName() );

		final CollectionMappingType<?> collectionMappingType;
		final JavaTypeDescriptorRegistry jtdRegistry = creationContext.getJavaTypeDescriptorRegistry();

		final CollectionPart elementDescriptor = interpretElement(
				bootValueMapping,
				tableExpression,
				collectionDescriptor,
				sqlAliasStem,
				dialect,
				creationProcess
		);

		final CollectionPart indexDescriptor;
		CollectionIdentifierDescriptor identifierDescriptor = null;

		final CollectionSemantics collectionSemantics = collectionDescriptor.getCollectionSemantics();
		switch ( collectionSemantics.getCollectionClassification() ) {
			case ARRAY: {
				collectionMappingType = new CollectionMappingTypeImpl(
						jtdRegistry.getDescriptor( Object[].class ),
						StandardArraySemantics.INSTANCE
				);

				final BasicValue index = (BasicValue) ( (IndexedCollection) bootValueMapping ).getIndex();
				indexDescriptor = new BasicValuedCollectionPart(
						collectionDescriptor,
						CollectionPart.Nature.INDEX,
						creationContext.getTypeConfiguration().getBasicTypeForJavaType( Integer.class ),
						// no converter
						null,
						tableExpression,
						index.getColumnIterator().next().getText( dialect )
				);

				break;
			}
			case BAG: {
				collectionMappingType = new CollectionMappingTypeImpl(
						jtdRegistry.getDescriptor( java.util.Collection.class ),
						StandardBagSemantics.INSTANCE
				);

				indexDescriptor = null;

				break;
			}
			case IDBAG: {
				collectionMappingType = new CollectionMappingTypeImpl(
						jtdRegistry.getDescriptor( java.util.Collection.class ),
						StandardIdentifierBagSemantics.INSTANCE
				);

				indexDescriptor = null;

				assert collectionDescriptor instanceof SQLLoadableCollection;
				final SQLLoadableCollection loadableCollection = (SQLLoadableCollection) collectionDescriptor;
				final String identifierColumnName = loadableCollection.getIdentifierColumnName();
				assert identifierColumnName != null;

				identifierDescriptor = new CollectionIdentifierDescriptorImpl(
						collectionDescriptor,
						tableExpression,
						identifierColumnName,
						(BasicType) loadableCollection.getIdentifierType()
				);

				break;
			}
			case LIST: {
				final BasicValue index = (BasicValue) ( (IndexedCollection) bootValueMapping ).getIndex();

				indexDescriptor = new BasicValuedCollectionPart(
						collectionDescriptor,
						CollectionPart.Nature.INDEX,
						creationContext.getTypeConfiguration().getBasicTypeForJavaType( Integer.class ),
						// no converter
						null,
						tableExpression,
						index.getColumnIterator().next().getText( dialect )
				);

				collectionMappingType = new CollectionMappingTypeImpl(
						jtdRegistry.getDescriptor( java.util.List.class ),
						StandardListSemantics.INSTANCE
				);

				break;
			}
			case MAP:
			case ORDERED_MAP:
			case SORTED_MAP: {
				final Class<? extends java.util.Map> mapJavaType = collectionSemantics.getCollectionClassification() == CollectionClassification.SORTED_MAP
						? SortedMap.class
						: java.util.Map.class;
				collectionMappingType = new CollectionMappingTypeImpl(
						jtdRegistry.getDescriptor( mapJavaType ),
						collectionSemantics
				);

				indexDescriptor = interpretMapKey(
						bootValueMapping,
						collectionDescriptor,
						tableExpression,
						sqlAliasStem,
						dialect,
						creationProcess
				);

				break;
			}
			case SET:
			case ORDERED_SET:
			case SORTED_SET: {
				final Class<? extends java.util.Set> setJavaType = collectionSemantics.getCollectionClassification() == CollectionClassification.SORTED_MAP
						? SortedSet.class
						: java.util.Set.class;
				collectionMappingType = new CollectionMappingTypeImpl(
						jtdRegistry.getDescriptor( setJavaType ),
						collectionSemantics
				);

				indexDescriptor = null;

				break;
			}
			default: {
				throw new MappingException(
						"Unexpected CollectionClassification : " + collectionSemantics.getCollectionClassification()
				);
			}
		}

		final StateArrayContributorMetadata contributorMetadata = new StateArrayContributorMetadata() {
			@Override
			public PropertyAccess getPropertyAccess() {
				return propertyAccess;
			}

			@Override
			public MutabilityPlan getMutabilityPlan() {
				return ImmutableMutabilityPlan.instance();
			}

			@Override
			public boolean isNullable() {
				return bootProperty.isOptional();
			}

			@Override
			public boolean isInsertable() {
				return bootProperty.isInsertable();
			}

			@Override
			public boolean isUpdatable() {
				return bootProperty.isUpdateable();
			}

			@Override
			public boolean isIncludedInDirtyChecking() {
				return false;
			}

			@Override
			public boolean isIncludedInOptimisticLocking() {
				return bootProperty.isOptimisticLocked();
			}
		};

		final FetchStyle style = FetchStrategyHelper.determineFetchStyleByMetadata(
				fetchMode,
				collectionDescriptor.getCollectionType(),
				sessionFactory
		);

		return new PluralAttributeMappingImpl(
				attrName,
				bootProperty,
				bootValueMapping,
				propertyAccess,
				entityMappingType -> contributorMetadata,
				collectionMappingType,
				stateArrayPosition,
				elementDescriptor,
				indexDescriptor,
				identifierDescriptor,
				new FetchStrategy(
						FetchStrategyHelper.determineFetchTiming(
								style,
								collectionDescriptor.getCollectionType(),
								sessionFactory
						),
						style
				),
				cascadeStyle,
				declaringType,
				collectionDescriptor,
				creationProcess
		);
	}

	private static CollectionPart interpretMapKey(
			Collection bootValueMapping,
			CollectionPersister collectionDescriptor,
			String tableExpression,
			String sqlAliasStem,
			Dialect dialect,
			MappingModelCreationProcess creationProcess) {
		assert bootValueMapping instanceof IndexedCollection;
		final IndexedCollection indexedCollection = (IndexedCollection) bootValueMapping;
		final Value bootMapKeyDescriptor = indexedCollection.getIndex();

		if ( bootMapKeyDescriptor instanceof BasicValue ) {
			final BasicValue basicValue = (BasicValue) bootMapKeyDescriptor;
			return new BasicValuedCollectionPart(
					collectionDescriptor,
					CollectionPart.Nature.INDEX,
					basicValue.resolve().getJdbcMapping(),
					basicValue.resolve().getValueConverter(),
					tableExpression,
					basicValue.getColumnIterator().next().getText( dialect )
			);
		}

		if ( bootMapKeyDescriptor instanceof Component ) {
			final Component component = (Component) bootMapKeyDescriptor;
			final CompositeType compositeType = (CompositeType) component.getType();

			final List<String> columnExpressions = CollectionHelper.arrayList( component.getColumnSpan() );
			final Iterator<Selectable> columnIterator = component.getColumnIterator();
			while ( columnIterator.hasNext() ) {
				columnExpressions.add( columnIterator.next().getText( dialect ) );
			}

			final EmbeddableMappingType mappingType = EmbeddableMappingType.from(
					component,
					compositeType,
					inflightDescriptor -> new EmbeddedCollectionPart(
							collectionDescriptor,
							CollectionPart.Nature.INDEX,
							inflightDescriptor,
							// parent-injection
							null,
							tableExpression,
							columnExpressions,
							sqlAliasStem
					),
					creationProcess
			);

			return (CollectionPart) mappingType.getEmbeddedValueMapping();
		}

		if ( bootMapKeyDescriptor instanceof OneToMany || bootMapKeyDescriptor instanceof ToOne ) {
			final EntityType indexEntityType = (EntityType) collectionDescriptor.getIndexType();
			final EntityPersister associatedEntity = creationProcess.getEntityPersister( indexEntityType.getAssociatedEntityName() );

			return new EntityCollectionPart(
					collectionDescriptor,
					CollectionPart.Nature.INDEX,
					bootValueMapping,
					bootMapKeyDescriptor,
					associatedEntity,
					creationProcess
			);
		}

		throw new NotYetImplementedFor6Exception(
				"Support for plural attributes with index type [" + bootMapKeyDescriptor + "] not yet implemented"
		);
	}

	private static CollectionPart interpretElement(
			Collection bootDescriptor,
			String tableExpression,
			CollectionPersister collectionDescriptor,
			String sqlAliasStem,
			Dialect dialect,
			MappingModelCreationProcess creationProcess) {
		final Value element = bootDescriptor.getElement();

		if ( element instanceof BasicValue ) {
			final BasicValue basicElement = (BasicValue) element;
			return new BasicValuedCollectionPart(
					collectionDescriptor,
					CollectionPart.Nature.ELEMENT,
					basicElement.resolve().getJdbcMapping(),
					basicElement.resolve().getValueConverter(),
					tableExpression,
					basicElement.getColumnIterator().next().getText( dialect )
			);
		}

		if ( element instanceof Component ) {
			final Component component = (Component) element;
			final CompositeType compositeType = (CompositeType) collectionDescriptor.getElementType();

			final List<String> columnExpressions = CollectionHelper.arrayList( component.getColumnSpan() );
			final Iterator<Selectable> columnIterator = component.getColumnIterator();
			while ( columnIterator.hasNext() ) {
				columnExpressions.add( columnIterator.next().getText( dialect ) );
			}

			final EmbeddableMappingType mappingType = EmbeddableMappingType.from(
					component,
					compositeType,
					embeddableMappingType -> new EmbeddedCollectionPart(
							collectionDescriptor,
							CollectionPart.Nature.ELEMENT,
							embeddableMappingType,
							// parent-injection
							null,
							tableExpression,
							columnExpressions,
							sqlAliasStem
					),
					creationProcess
			);

			return (CollectionPart) mappingType.getEmbeddedValueMapping();
		}

		if ( element instanceof OneToMany || element instanceof ToOne ) {
			final EntityType elementEntityType = (EntityType) collectionDescriptor.getElementType();
			final EntityPersister associatedEntity = creationProcess.getEntityPersister( elementEntityType.getAssociatedEntityName() );

			final EntityCollectionPart elementDescriptor = new EntityCollectionPart(
					collectionDescriptor,
					CollectionPart.Nature.ELEMENT,
					bootDescriptor,
					element,
					associatedEntity,
					creationProcess
			);

			return elementDescriptor;
		}

		throw new NotYetImplementedFor6Exception(
				"Support for plural attributes with element type [" + element + "] not yet implemented"
		);
	}

	@SuppressWarnings("rawtypes")
	private static class CollectionMappingTypeImpl implements CollectionMappingType {
		private final JavaTypeDescriptor collectionJtd;
		private final CollectionSemantics semantics;

		@SuppressWarnings("WeakerAccess")
		public CollectionMappingTypeImpl(
				JavaTypeDescriptor collectionJtd,
				CollectionSemantics semantics) {
			this.collectionJtd = collectionJtd;
			this.semantics = semantics;
		}

		@Override
		public CollectionSemantics getCollectionSemantics() {
			return semantics;
		}

		@Override
		public JavaTypeDescriptor getMappedJavaTypeDescriptor() {
			return collectionJtd;
		}
	}

	public static ToOneAttributeMapping buildSingularAssociationAttributeMapping(
			String attrName,
			NavigableRole navigableRole,
			int stateArrayPosition,
			Property bootProperty,
			ManagedMappingType declaringType,
			EntityType attrType,
			PropertyAccess propertyAccess,
			CascadeStyle cascadeStyle,
			MappingModelCreationProcess creationProcess) {
		if ( bootProperty.getValue() instanceof ToOne ) {
			final ToOne bootToOne = (ToOne) bootProperty.getValue();
			final EntityPersister entityPersister = creationProcess.getEntityPersister( bootToOne.getReferencedEntityName() );
			final StateArrayContributorMetadataAccess stateArrayContributorMetadataAccess = getStateArrayContributorMetadataAccess(
					bootProperty,
					attrType,
					propertyAccess,
					cascadeStyle,
					creationProcess
			);
			SessionFactoryImplementor sessionFactory = creationProcess.getCreationContext().getSessionFactory();

			final AssociationType type = (AssociationType) bootProperty.getType();
			final FetchStyle fetchStyle = FetchStrategyHelper.determineFetchStyleByMetadata(
					bootProperty.getValue().getFetchMode(),
					type,
					sessionFactory
			);

			final FetchTiming fetchTiming;

			if ( fetchStyle == FetchStyle.JOIN
					|| ( bootToOne instanceof OneToOne && bootToOne.isNullable() )
					|| ! bootToOne.isLazy() ) {
				fetchTiming = FetchTiming.IMMEDIATE;
			}
			else {
				fetchTiming = FetchStrategyHelper.determineFetchTiming( fetchStyle, type, sessionFactory );
			}

			final FetchStrategy fetchStrategy = new FetchStrategy( fetchTiming, fetchStyle );

			final ForeignKeyDirection direction;
			final ToOneAttributeMapping.Cardinality cardinality;

			if ( bootToOne instanceof OneToOne ) {
				final OneToOne oneToOneBootValue = (OneToOne) bootToOne;

				if ( oneToOneBootValue.isConstrained() ) {
					direction = ForeignKeyDirection.REFERRING;
				}
				else {
					direction = ForeignKeyDirection.TARGET;
				}

				cardinality = ToOneAttributeMapping.Cardinality.ONE_TO_ONE;
			}
			else {
				direction = ForeignKeyDirection.REFERRING;
				cardinality = ( (ManyToOne) bootToOne ).isLogicalOneToOne()
						? ToOneAttributeMapping.Cardinality.LOGICAL_ONE_TO_ONE
						: ToOneAttributeMapping.Cardinality.MANY_TO_ONE;
			}

			if ( direction == ForeignKeyDirection.REFERRING ) {
				return new ToOneAttributeReferring(
						attrName,
						navigableRole,
						cardinality,
						stateArrayPosition,
						(ToOne) bootProperty.getValue(),
						stateArrayContributorMetadataAccess,
						fetchStrategy,
						entityPersister,
						declaringType,
						propertyAccess,
						creationProcess
				);
			}
			else {
				return new ToOneAttributeTarget(
						attrName,
						navigableRole,
						cardinality,
						stateArrayPosition,
						bootToOne,
						stateArrayContributorMetadataAccess,
						fetchStrategy,
						entityPersister,
						declaringType,
						propertyAccess,
						creationProcess
				);
			}
		}
		else {
			throw new NotYetImplementedFor6Exception( "AnyType support has not yet been implemented" );
		}
	}
}
