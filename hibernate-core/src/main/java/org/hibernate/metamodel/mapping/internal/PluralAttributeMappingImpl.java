/*
 * Hibernate, Relational Persistence for Idiomatic Java
 *
 * License: GNU Lesser General Public License (LGPL), version 2.1 or later
 * See the lgpl.txt file in the root directory or http://www.gnu.org/licenses/lgpl-2.1.html
 */
package org.hibernate.metamodel.mapping.internal;

import java.util.function.BiFunction;
import java.util.function.Consumer;
import java.util.function.Supplier;

import org.hibernate.LockMode;
import org.hibernate.NotYetImplementedFor6Exception;
import org.hibernate.engine.FetchStrategy;
import org.hibernate.engine.FetchStyle;
import org.hibernate.engine.FetchTiming;
import org.hibernate.engine.spi.CascadeStyle;
import org.hibernate.engine.spi.LoadQueryInfluencers;
import org.hibernate.engine.spi.SessionFactoryImplementor;
import org.hibernate.jpa.spi.JpaCompliance;
import org.hibernate.mapping.BasicValue;
import org.hibernate.mapping.Collection;
import org.hibernate.mapping.Component;
import org.hibernate.mapping.DependantValue;
import org.hibernate.mapping.IndexedCollection;
import org.hibernate.mapping.KeyValue;
import org.hibernate.mapping.List;
import org.hibernate.mapping.ManyToOne;
import org.hibernate.mapping.Property;
import org.hibernate.mapping.ToOne;
import org.hibernate.metamodel.mapping.BasicEntityIdentifierMapping;
import org.hibernate.metamodel.mapping.CollectionIdentifierDescriptor;
import org.hibernate.metamodel.mapping.CollectionMappingType;
import org.hibernate.metamodel.mapping.CollectionPart;
import org.hibernate.metamodel.mapping.ColumnConsumer;
import org.hibernate.metamodel.mapping.EmbeddableMappingType;
import org.hibernate.metamodel.mapping.EntityIdentifierMapping;
import org.hibernate.metamodel.mapping.EntityMappingType;
import org.hibernate.metamodel.mapping.JdbcMapping;
import org.hibernate.metamodel.mapping.ManagedMappingType;
import org.hibernate.metamodel.mapping.ModelPart;
import org.hibernate.metamodel.mapping.NonTransientException;
import org.hibernate.metamodel.mapping.PluralAttributeMapping;
import org.hibernate.metamodel.mapping.StateArrayContributorMetadataAccess;
import org.hibernate.metamodel.mapping.internal.fk.CollectionKey;
import org.hibernate.metamodel.mapping.internal.fk.CollectionKeyBasic;
import org.hibernate.metamodel.mapping.internal.fk.CollectionKeyComposite;
import org.hibernate.metamodel.mapping.internal.fk.ForeignKey;
import org.hibernate.metamodel.mapping.internal.fk.JoinTableKey;
import org.hibernate.metamodel.mapping.internal.fk.JoinTableKeyBasic;
import org.hibernate.metamodel.mapping.internal.fk.KeyModelPart;
import org.hibernate.metamodel.mapping.ordering.OrderByFragment;
import org.hibernate.metamodel.mapping.ordering.OrderByFragmentTranslator;
import org.hibernate.metamodel.mapping.ordering.TranslationContext;
import org.hibernate.metamodel.model.domain.NavigableRole;
import org.hibernate.persister.collection.CollectionPersister;
import org.hibernate.persister.entity.Joinable;
import org.hibernate.property.access.spi.PropertyAccess;
import org.hibernate.query.NavigablePath;
import org.hibernate.sql.ast.SqlAstJoinType;
import org.hibernate.sql.ast.spi.SqlAliasBase;
import org.hibernate.sql.ast.spi.SqlAliasBaseGenerator;
import org.hibernate.sql.ast.spi.SqlAliasStemHelper;
import org.hibernate.sql.ast.spi.SqlAstCreationContext;
import org.hibernate.sql.ast.spi.SqlAstCreationState;
import org.hibernate.sql.ast.spi.SqlExpressionResolver;
import org.hibernate.sql.ast.tree.from.StandardTableGroup;
import org.hibernate.sql.ast.tree.from.TableGroup;
import org.hibernate.sql.ast.tree.from.TableGroupJoin;
import org.hibernate.sql.ast.tree.from.TableReference;
import org.hibernate.sql.ast.tree.from.TableReferenceJoin;
import org.hibernate.sql.ast.tree.predicate.Predicate;
import org.hibernate.sql.results.graph.DomainResult;
import org.hibernate.sql.results.graph.DomainResultCreationState;
import org.hibernate.sql.results.graph.Fetch;
import org.hibernate.sql.results.graph.FetchOptions;
import org.hibernate.sql.results.graph.FetchParent;
import org.hibernate.sql.results.graph.collection.internal.CollectionDomainResult;
import org.hibernate.sql.results.graph.collection.internal.DelayedCollectionFetch;
import org.hibernate.sql.results.graph.collection.internal.EagerCollectionFetch;
import org.hibernate.sql.results.graph.collection.internal.SelectEagerCollectionFetch;
import org.hibernate.type.BasicType;
import org.hibernate.type.CompositeType;
import org.hibernate.type.EntityType;
import org.hibernate.type.Type;

import org.jboss.logging.Logger;

/**
 * @author Steve Ebersole
 */
public class PluralAttributeMappingImpl
		extends AbstractAttributeMapping
		implements PluralAttributeMapping, FetchOptions {
	private static final Logger log = Logger.getLogger( PluralAttributeMappingImpl.class );

	public interface Aware {
		void injectAttributeMapping(PluralAttributeMapping attributeMapping);
	}

	@SuppressWarnings("rawtypes")
	private final CollectionMappingType collectionMappingType;
	private final int stateArrayPosition;
	private final PropertyAccess propertyAccess;
	private final StateArrayContributorMetadataAccess stateArrayContributorMetadataAccess;

	private final CollectionKey collectionKey;
	private final JoinTableKey manyToManyKey;

	private final CollectionPart elementDescriptor;
	private final CollectionPart indexDescriptor;
	private final CollectionIdentifierDescriptor identifierDescriptor;
	private final FetchTiming fetchTiming;
	private final FetchStyle fetchStyle;

	private final CascadeStyle cascadeStyle;

	private final CollectionPersister collectionDescriptor;
	private final String separateCollectionTable;

	private final String sqlAliasStem;

	private final IndexMetadata indexMetadata;


	private OrderByFragment orderByFragment;
	private OrderByFragment manyToManyOrderByFragment;

	@SuppressWarnings({"WeakerAccess", "rawtypes"})
	public PluralAttributeMappingImpl(
			String attributeName,
			Property bootProperty,
			Collection bootDescriptor,
			PropertyAccess propertyAccess,
			StateArrayContributorMetadataAccess stateArrayContributorMetadataAccess,
			CollectionMappingType collectionMappingType,
			int stateArrayPosition,
			CollectionPart elementDescriptor,
			CollectionPart indexDescriptor,
			CollectionIdentifierDescriptor identifierDescriptor,
			FetchStrategy fetchStrategy,
			CascadeStyle cascadeStyle,
			ManagedMappingType declaringType,
			CollectionPersister collectionDescriptor,
			MappingModelCreationProcess creationProcess) {
		this(
				attributeName,
				bootProperty,
				bootDescriptor,
				propertyAccess,
				stateArrayContributorMetadataAccess,
				collectionMappingType,
				stateArrayPosition,
				elementDescriptor,
				indexDescriptor,
				identifierDescriptor,
				fetchStrategy.getTiming(),
				fetchStrategy.getStyle(),
				cascadeStyle,
				declaringType,
				collectionDescriptor,
				creationProcess
		);
	}

	@SuppressWarnings({"WeakerAccess", "rawtypes"})
	public PluralAttributeMappingImpl(
			String attributeName,
			Property bootProperty,
			Collection bootDescriptor,
			PropertyAccess propertyAccess,
			StateArrayContributorMetadataAccess stateArrayContributorMetadataAccess,
			CollectionMappingType collectionMappingType,
			int stateArrayPosition,
			CollectionPart elementDescriptor,
			CollectionPart indexDescriptor,
			CollectionIdentifierDescriptor identifierDescriptor,
			FetchTiming fetchTiming,
			FetchStyle fetchStyle,
			CascadeStyle cascadeStyle,
			ManagedMappingType declaringType,
			CollectionPersister collectionDescriptor,
			MappingModelCreationProcess creationProcess) {
		super( attributeName, declaringType );
		this.propertyAccess = propertyAccess;
		this.stateArrayContributorMetadataAccess = stateArrayContributorMetadataAccess;
		this.collectionMappingType = collectionMappingType;
		this.stateArrayPosition = stateArrayPosition;
		this.elementDescriptor = elementDescriptor;
		this.indexDescriptor = indexDescriptor;
		this.identifierDescriptor = identifierDescriptor;
		this.fetchTiming = fetchTiming;
		this.fetchStyle = fetchStyle;
		this.cascadeStyle = cascadeStyle;
		this.collectionDescriptor = collectionDescriptor;

		this.sqlAliasStem = SqlAliasStemHelper.INSTANCE.generateStemFromAttributeName( attributeName );

		this.collectionKey = generateCollectionKey( bootDescriptor, this, creationProcess );

		if ( bootDescriptor.isOneToMany() ) {
			separateCollectionTable = null;
			manyToManyKey = null;
		}
		else {
			separateCollectionTable = ( (Joinable) collectionDescriptor ).getTableName();

			if ( elementDescriptor instanceof EntityCollectionPart ) {
				manyToManyKey = generateJoinTableKey(
						(ManyToOne) bootDescriptor.getElement(),
						bootDescriptor,
						(EntityCollectionPart) elementDescriptor,
						this,
						creationProcess
				);
			}
			else if ( indexDescriptor != null )  {
				if ( indexDescriptor instanceof EntityCollectionPart ) {
					final IndexedCollection indexedCollection = (IndexedCollection) bootDescriptor;
					manyToManyKey = generateJoinTableKey(
							(ManyToOne) indexedCollection.getIndex(),
							bootDescriptor,
							(EntityCollectionPart) indexDescriptor,
							this,
							creationProcess
					);
				}
				else {
					manyToManyKey = null;
				}
			}
			else {
				manyToManyKey = null;
			}
		}


		indexMetadata = new IndexMetadata() {
			final int baseIndex;

			{
				if ( bootDescriptor instanceof List ) {
					baseIndex = ( (List) bootDescriptor ).getBaseIndex();
				}
				else {
					baseIndex = -1;
				}
			}

			@Override
			public CollectionPart getIndexDescriptor() {
				return indexDescriptor;
			}

			@Override
			public int getListIndexBase() {
				return baseIndex;
			}
		};

		if ( collectionDescriptor instanceof Aware ) {
			( (Aware) collectionDescriptor ).injectAttributeMapping( this );
		}

		if ( elementDescriptor instanceof Aware ) {
			( (Aware) elementDescriptor ).injectAttributeMapping( this );
		}

		if ( indexDescriptor instanceof Aware ) {
			( (Aware) indexDescriptor ).injectAttributeMapping( this );
		}

		creationProcess.registerInitializationCallback(
				"PluralAttributeMapping initialization : " + getNavigableRole().getFullPath(),
				() -> {
					try {
						finishInitialization( bootProperty, bootDescriptor, creationProcess );
						return true;
					}
					catch (NotYetImplementedFor6Exception nye) {
						throw nye;
					}
					catch (Exception e) {
						if ( e instanceof NonTransientException ) {
							throw e;
						}

						return false;
					}
				}
		);
	}

	private static CollectionKey generateCollectionKey(
			Collection bootDescriptor,
			PluralAttributeMappingImpl pluralAttributeMapping,
			MappingModelCreationProcess creationProcess) {
		final KeyValue bootCollectionKey = bootDescriptor.getKey();

		final boolean isBasic;
		final Supplier<JdbcMapping> jdbcMappingSupplier;

		final boolean isComposite;
		final Component bootCompositeDescriptor;

		if ( bootCollectionKey instanceof DependantValue ) {
			final KeyValue wrappedValue = ( (DependantValue) bootCollectionKey ).getWrappedValue();
			isBasic = wrappedValue instanceof BasicValue;
			isComposite = wrappedValue instanceof Component;
			jdbcMappingSupplier = isBasic ? () -> ( (BasicValue) wrappedValue ).resolve().getJdbcMapping() : null;
			bootCompositeDescriptor = isComposite ? (Component) wrappedValue : null;
		}
		else {
			isBasic = bootCollectionKey instanceof BasicValue;
			isComposite = bootCollectionKey instanceof Component;
			jdbcMappingSupplier = isBasic ? () -> ( (BasicValue) bootCollectionKey ).resolve().getJdbcMapping() : null;
			bootCompositeDescriptor = isComposite ? (Component) bootCollectionKey : null;
		}

		if ( isBasic ) {
			return new CollectionKeyBasic(
					pluralAttributeMapping,
					bootDescriptor,
					jdbcMappingSupplier,
					creationProcess
			);
		}

		if ( isComposite ) {
			final EmbeddableMappingType embeddableMappingType = EmbeddableMappingType.from(
					bootCompositeDescriptor,
					(CompositeType) bootCompositeDescriptor.getType(),
					embeddableDescriptor -> new CollectionKeyComposite(
							pluralAttributeMapping,
							embeddableDescriptor,
							bootDescriptor,
							bootCompositeDescriptor,
							creationProcess
					),
					creationProcess
			);
			return (CollectionKeyComposite) embeddableMappingType.getEmbeddedValueMapping();
		}

		throw new UnsupportedOperationException( "Unexpected collection-key nature : " + bootCollectionKey );
	}

	private JoinTableKey generateJoinTableKey(
			ToOne collectionPartBootValue,
			Collection bootDescriptor,
			EntityCollectionPart collectionPart,
			PluralAttributeMapping pluralAttributeMapping,
			MappingModelCreationProcess creationProcess) {
		assert ! bootDescriptor.isOneToMany();

		final SessionFactoryImplementor sessionFactory = creationProcess.getCreationContext().getSessionFactory();
		final EntityMappingType associatedEntity = collectionPart.getEntityMappingType();

		final EntityIdentifierMapping associatedEntityIdentifier = associatedEntity.getIdentifierMapping();
		if ( associatedEntityIdentifier != null ) {
			if ( associatedEntityIdentifier instanceof BasicEntityIdentifierMapping ) {
				final BasicEntityIdentifierMapping basicIdentifier = (BasicEntityIdentifierMapping) associatedEntityIdentifier;

				return new JoinTableKeyBasic (
						basicIdentifier,
						pluralAttributeMapping,
						collectionPart,
						bootDescriptor,
						collectionPartBootValue,
						pluralAttributeMapping.getSeparateCollectionTable(),
						creationProcess
				);
			}
		}

		// wait for the identifier to complete initialization...
		final EntityType legacyAssociatedEntityType = (EntityType) collectionPartBootValue.getType();
		final Type legacyKeyType = legacyAssociatedEntityType.getIdentifierOrUniqueKeyType( sessionFactory );
		if ( legacyKeyType instanceof BasicType ) {
			return new JoinTableKeyBasic(
					associatedEntity,
					pluralAttributeMapping,
					collectionPart,
					bootDescriptor,
					collectionPartBootValue,
					pluralAttributeMapping.getSeparateCollectionTable(),
					creationProcess
			);
		}

		throw new NotYetImplementedFor6Exception(
				"Support for non-basic collection-table fks not yet implemented : " + pluralAttributeMapping.getNavigableRole().getFullPath()
		);
	}


	@SuppressWarnings("unused")
	public void finishInitialization(
			Property bootProperty,
			Collection bootDescriptor,
			MappingModelCreationProcess creationProcess) {
		final boolean hasOrder = bootDescriptor.getOrderBy() != null;
		final boolean hasManyToManyOrder = bootDescriptor.getManyToManyOrdering() != null;

		if ( hasOrder || hasManyToManyOrder ) {
			final TranslationContext context = new TranslationContext() {
				@Override
				public JpaCompliance getJpaCompliance() {
					return collectionDescriptor.getFactory().getSessionFactoryOptions().getJpaCompliance();
				}
			};

			if ( hasOrder ) {
				if ( log.isDebugEnabled() ) {
					log.debugf(
							"Translating order-by fragment [%s] for collection role : %s",
							bootDescriptor.getOrderBy(),
							collectionDescriptor.getRole()
					);
				}
				orderByFragment = OrderByFragmentTranslator.translate(
						bootDescriptor.getOrderBy(),
						this,
						context
				);
			}

			if ( hasManyToManyOrder ) {
				if ( log.isDebugEnabled() ) {
					log.debugf(
							"Translating many-to-many order-by fragment [%s] for collection role : %s",
							bootDescriptor.getOrderBy(),
							collectionDescriptor.getRole()
					);
				}
				manyToManyOrderByFragment = OrderByFragmentTranslator.translate(
						bootDescriptor.getManyToManyOrdering(),
						this,
						context
				);
			}
		}
	}


	@Override
	public NavigableRole getNavigableRole() {
		return getCollectionDescriptor().getNavigableRole();
	}

	@Override
	@SuppressWarnings("rawtypes")
	public CollectionMappingType getMappedTypeDescriptor() {
		return collectionMappingType;
	}

	@Override
	public KeyModelPart getKeyModelPart() {
		return collectionKey;
	}

	@Override
	public ForeignKey getForeignKeyDescriptor() {
		return collectionKey.getForeignKeyDescriptor();
	}

	@Override
	public boolean isNullable() {
		return false;
	}

	@Override
	public CollectionPersister getCollectionDescriptor() {
		return collectionDescriptor;
	}

	@Override
	public CollectionPart getElementDescriptor() {
		return elementDescriptor;
	}

	@Override
	public CollectionPart getIndexDescriptor() {
		return indexDescriptor;
	}

	@Override
	public IndexMetadata getIndexMetadata() {
		return indexMetadata;
	}

	@Override
	public CollectionIdentifierDescriptor getIdentifierDescriptor() {
		return identifierDescriptor;
	}

	@Override
	public OrderByFragment getOrderByFragment() {
		return orderByFragment;
	}

	@Override
	public OrderByFragment getManyToManyOrderByFragment() {
		return manyToManyOrderByFragment;
	}

	@Override
	public String getSeparateCollectionTable() {
		return separateCollectionTable;
	}

	@Override
	public int getStateArrayPosition() {
		return stateArrayPosition;
	}

	@Override
	public StateArrayContributorMetadataAccess getAttributeMetadataAccess() {
		return stateArrayContributorMetadataAccess;
	}

	@Override
	public PropertyAccess getPropertyAccess() {
		return propertyAccess;
	}

	@Override
	public String getFetchableName() {
		return getAttributeName();
	}

	@Override
	public FetchOptions getMappedFetchOptions() {
		return this;
	}

	@Override
	public FetchStyle getStyle() {
		return fetchStyle;
	}

	@Override
	public FetchTiming getTiming() {
		return fetchTiming;
	}

	@Override
	public <T> DomainResult<T> createDomainResult(
			NavigablePath navigablePath,
			TableGroup tableGroup,
			String resultVariable,
			DomainResultCreationState creationState) {
		final TableGroup collectionTableGroup = creationState.getSqlAstCreationState()
				.getFromClauseAccess()
				.getTableGroup( navigablePath );

		assert collectionTableGroup != null;

		//noinspection unchecked
		return new CollectionDomainResult( navigablePath, this, resultVariable, tableGroup, creationState );
	}

	@Override
	public Fetch generateFetch(
			FetchParent fetchParent,
			NavigablePath fetchablePath,
			FetchTiming fetchTiming,
			boolean selected,
			LockMode lockMode,
			String resultVariable,
			DomainResultCreationState creationState) {
		final SqlAstCreationState sqlAstCreationState = creationState.getSqlAstCreationState();

		if ( fetchTiming == FetchTiming.IMMEDIATE) {
			if ( selected ) {
				final TableGroup collectionTableGroup = sqlAstCreationState.getFromClauseAccess().resolveTableGroup(
						fetchablePath,
						p -> {
							final TableGroup lhsTableGroup = sqlAstCreationState.getFromClauseAccess().getTableGroup(
									fetchParent.getNavigablePath() );
							final TableGroupJoin tableGroupJoin = createTableGroupJoin(
									fetchablePath,
									lhsTableGroup,
									null,
									SqlAstJoinType.LEFT,
									lockMode,
									creationState.getSqlAstCreationState()
							);
							return tableGroupJoin.getJoinedGroup();
						}
				);

				return new EagerCollectionFetch(
						fetchablePath,
						this,
						collectionTableGroup,
						fetchParent,
						creationState
				);
			}
			else {
				return new SelectEagerCollectionFetch( fetchablePath, this, fetchParent );
			}
		}

		if ( getCollectionDescriptor().getCollectionType().hasHolder() ) {
			return new SelectEagerCollectionFetch( fetchablePath, this, fetchParent );
		}

		return new DelayedCollectionFetch(
				fetchablePath,
				this,
				fetchParent
		);
	}

	@Override
	public String getSqlAliasStem() {
		return sqlAliasStem;
	}

	@Override
	public TableGroupJoin createTableGroupJoin(
			NavigablePath navigablePath,
			TableGroup lhs,
			String explicitSourceAlias,
			SqlAstJoinType sqlAstJoinType,
			LockMode lockMode,
			SqlAliasBaseGenerator aliasBaseGenerator,
			SqlExpressionResolver sqlExpressionResolver,
			SqlAstCreationContext creationContext) {
		final CollectionPersister collectionDescriptor = getCollectionDescriptor();
		if ( collectionDescriptor.isOneToMany() ) {
			return createOneToManyTableGroupJoin(
					navigablePath,
					lhs,
					explicitSourceAlias,
					sqlAstJoinType,
					lockMode,
					aliasBaseGenerator,
					sqlExpressionResolver,
					creationContext
			);
		}
		else {
			return createCollectionTableGroupJoin(
					navigablePath,
					lhs,
					explicitSourceAlias,
					sqlAstJoinType,
					lockMode,
					aliasBaseGenerator,
					sqlExpressionResolver,
					creationContext
			);
		}
	}

	@Override
	public CollectionKey getCollectionKey() {
		return collectionKey;
	}

	@Override
	public JoinTableKey getJoinTableKey() {
		return manyToManyKey;
	}

	@SuppressWarnings("unused")
	private TableGroupJoin createOneToManyTableGroupJoin(
			NavigablePath navigablePath,
			TableGroup lhs,
			String explicitSourceAlias,
			SqlAstJoinType sqlAstJoinType,
			LockMode lockMode,
			SqlAliasBaseGenerator aliasBaseGenerator,
			SqlExpressionResolver sqlExpressionResolver,
			SqlAstCreationContext creationContext) {
		final TableGroup tableGroup = createOneToManyTableGroup(
				navigablePath,
				sqlAstJoinType == SqlAstJoinType.INNER
						&& !getAttributeMetadataAccess().resolveAttributeMetadata( null ).isNullable(),
				lockMode,
				aliasBaseGenerator,
				sqlExpressionResolver,
				creationContext
		);

		final TableGroupJoin tableGroupJoin = new TableGroupJoin(
				navigablePath,
				sqlAstJoinType,
				tableGroup,
				getForeignKeyDescriptor().generateJoinPredicate(
						lhs,
						tableGroup,
						sqlAstJoinType,
						sqlExpressionResolver,
						creationContext
				)
		);

		lhs.addTableGroupJoin( tableGroupJoin );

		return tableGroupJoin;
	}

	private TableGroup createOneToManyTableGroup(
			NavigablePath navigablePath,
			boolean canUseInnerJoins,
			LockMode lockMode,
			SqlAliasBaseGenerator aliasBaseGenerator,
			SqlExpressionResolver sqlExpressionResolver,
			SqlAstCreationContext creationContext) {
		final EntityCollectionPart entityPartDescriptor;
		if ( elementDescriptor instanceof EntityCollectionPart ) {
			entityPartDescriptor = (EntityCollectionPart) elementDescriptor;
		}
		else {
			assert indexDescriptor instanceof EntityCollectionPart;
			entityPartDescriptor = (EntityCollectionPart) indexDescriptor;
		}

		final SqlAliasBase sqlAliasBase = aliasBaseGenerator.createSqlAliasBase( getSqlAliasStem() );

		final TableReference primaryTableReference = entityPartDescriptor.getEntityMappingType()
				.createPrimaryTableReference(
						sqlAliasBase,
						sqlExpressionResolver,
						creationContext
				);

		return new StandardTableGroup(
				navigablePath,
				this,
				lockMode,
				primaryTableReference,
				sqlAliasBase,
				(tableExpression) -> entityPartDescriptor.getEntityMappingType()
						.containsTableReference( tableExpression ),
				(tableExpression, tg) -> entityPartDescriptor.getEntityMappingType().createTableReferenceJoin(
						tableExpression,
						sqlAliasBase,
						primaryTableReference,
						canUseInnerJoins,
						sqlExpressionResolver,
						creationContext
				),
				creationContext.getSessionFactory()
		);
	}

	@SuppressWarnings("unused")
	private TableGroupJoin createCollectionTableGroupJoin(
			NavigablePath navigablePath,
			TableGroup lhs,
			String explicitSourceAlias,
			SqlAstJoinType sqlAstJoinType,
			LockMode lockMode,
			SqlAliasBaseGenerator aliasBaseGenerator,
			SqlExpressionResolver sqlExpressionResolver,
			SqlAstCreationContext creationContext) {
		final TableGroup tableGroup = createCollectionTableGroup(
				navigablePath,
				sqlAstJoinType == SqlAstJoinType.INNER
						&& !getAttributeMetadataAccess().resolveAttributeMetadata( null ).isNullable(),
				lockMode,
				aliasBaseGenerator,
				sqlExpressionResolver,
				creationContext
		);

		final TableGroupJoin tableGroupJoin = new TableGroupJoin(
				navigablePath,
				sqlAstJoinType,
				tableGroup,
				getForeignKeyDescriptor().generateJoinPredicate(
						lhs,
						tableGroup,
						sqlAstJoinType,
						sqlExpressionResolver,
						creationContext
				)
		);

		lhs.addTableGroupJoin( tableGroupJoin );

		return tableGroupJoin;
	}

	private TableGroup createCollectionTableGroup(
			NavigablePath navigablePath,
			boolean canUseInnerJoin,
			LockMode lockMode,
			SqlAliasBaseGenerator aliasBaseGenerator,
			SqlExpressionResolver sqlExpressionResolver,
			SqlAstCreationContext creationContext) {
		final SqlAliasBase sqlAliasBase = aliasBaseGenerator.createSqlAliasBase( getSqlAliasStem() );

		assert !getCollectionDescriptor().isOneToMany();

		final String collectionTableName = ( (Joinable) collectionDescriptor ).getTableName();
		final TableReference collectionTableReference = new TableReference(
				collectionTableName,
				sqlAliasBase.generateNewAlias(),
				true,
				creationContext.getSessionFactory()
		);

		final Consumer<TableGroup> tableGroupFinalizer;
		final BiFunction<String, TableGroup, TableReferenceJoin> tableReferenceJoinCreator;
		final java.util.function.Predicate<String> tableReferenceJoinNameChecker;
		if ( elementDescriptor instanceof EntityCollectionPart || indexDescriptor instanceof EntityCollectionPart ) {
			final EntityCollectionPart entityPartDescriptor;
			if ( elementDescriptor instanceof EntityCollectionPart ) {
				entityPartDescriptor = (EntityCollectionPart) elementDescriptor;
			}
			else {
				entityPartDescriptor = (EntityCollectionPart) indexDescriptor;
			}

			final EntityMappingType mappingType = entityPartDescriptor.getEntityMappingType();
			final TableReference associatedPrimaryTable = mappingType.createPrimaryTableReference(
					sqlAliasBase,
					sqlExpressionResolver,
					creationContext
			);

			tableReferenceJoinNameChecker = mappingType::containsTableReference;
			tableReferenceJoinCreator = (tableExpression, tableGroup) -> mappingType.createTableReferenceJoin(
					tableExpression,
					sqlAliasBase,
					associatedPrimaryTable,
					canUseInnerJoin && !getAttributeMetadataAccess().resolveAttributeMetadata( null ).isNullable(),
					sqlExpressionResolver,
					creationContext
			);

			tableGroupFinalizer = tableGroup -> {
				final SqlAstJoinType joinType = canUseInnerJoin && !getAttributeMetadataAccess().resolveAttributeMetadata(
						null ).isNullable()
						? SqlAstJoinType.INNER
						: SqlAstJoinType.LEFT;
				final TableReferenceJoin associationJoin = new TableReferenceJoin(
						joinType,
						associatedPrimaryTable,
						collectionKey.getForeignKeyDescriptor().generateJoinPredicate(
								associatedPrimaryTable,
								collectionTableReference,
								joinType,
								sqlExpressionResolver,
								creationContext
						)
				);
				( (StandardTableGroup) tableGroup ).addTableReferenceJoin( associationJoin );
			};
		}
		else {
			tableReferenceJoinCreator = (tableExpression, tableGroup) -> {
				throw new UnsupportedOperationException(
						"element-collection cannot contain joins : " + collectionTableReference.getTableExpression() + " -> " + tableExpression
				);
			};
			tableReferenceJoinNameChecker = s -> false;
			tableGroupFinalizer = null;
		}

		final StandardTableGroup tableGroup = new StandardTableGroup(
				navigablePath,
				this,
				lockMode,
				collectionTableReference,
				sqlAliasBase,
				tableReferenceJoinNameChecker,
				tableReferenceJoinCreator,
				creationContext.getSessionFactory()
		);

		if ( tableGroupFinalizer != null ) {
			tableGroupFinalizer.accept( tableGroup );
		}

		return tableGroup;
	}

	@Override
	public TableGroup createRootTableGroup(
			NavigablePath navigablePath,
			String explicitSourceAlias,
			boolean canUseInnerJoins,
			LockMode lockMode,
			SqlAliasBaseGenerator aliasBaseGenerator,
			SqlExpressionResolver sqlExpressionResolver,
			Supplier<Consumer<Predicate>> additionalPredicateCollectorAccess,
			SqlAstCreationContext creationContext) {
		if ( getCollectionDescriptor().isOneToMany() ) {
			return createOneToManyTableGroup(
					navigablePath,
					canUseInnerJoins,
					lockMode,
					aliasBaseGenerator,
					sqlExpressionResolver,
					creationContext
			);
		}
		else {
			return createCollectionTableGroup(
					navigablePath,
					canUseInnerJoins,
					lockMode,
					aliasBaseGenerator,
					sqlExpressionResolver,
					creationContext
			);
		}
	}

	@Override
	public boolean isAffectedByEnabledFilters(LoadQueryInfluencers influencers) {
		return getCollectionDescriptor().isAffectedByEnabledFilters( influencers );
	}

	@Override
	public boolean isAffectedByEntityGraph(LoadQueryInfluencers influencers) {
		return getCollectionDescriptor().isAffectedByEntityGraph( influencers );
	}

	@Override
	public boolean isAffectedByEnabledFetchProfiles(LoadQueryInfluencers influencers) {
		return getCollectionDescriptor().isAffectedByEnabledFetchProfiles( influencers );
	}

	@Override
	public String getRootPathName() {
		return getCollectionDescriptor().getRole();
	}

	@Override
	public ModelPart findSubPart(String name, EntityMappingType treatTargetType) {
		final CollectionPart.Nature nature = CollectionPart.Nature.fromName( name );
		if ( nature == CollectionPart.Nature.ELEMENT ) {
			return elementDescriptor;
		}
		else if ( nature == CollectionPart.Nature.INDEX ) {
			return indexDescriptor;
		}
		else if ( nature == CollectionPart.Nature.ID ) {
			return identifierDescriptor;
		}

		if ( elementDescriptor instanceof EntityCollectionPart ) {
			return ( (EntityCollectionPart) elementDescriptor ).findSubPart( name );
		}
		return null;
	}

	@Override
	public void visitSubParts(Consumer<ModelPart> consumer, EntityMappingType treatTargetType) {
		consumer.accept( elementDescriptor );
		if ( indexDescriptor != null ) {
			consumer.accept( indexDescriptor );
		}
	}

	@Override
	public int getNumberOfFetchables() {
		return indexDescriptor == null ? 1 : 2;
	}

	@Override
	public String toString() {
		return "PluralAttribute(" + getCollectionDescriptor().getRole() + ")";
	}

	@Override
	public void visitColumns(ColumnConsumer consumer) {
//		collectionKey.visitColumns( consumer );
//
//		if ( identifierDescriptor != null ) {
//			identifierDescriptor.visitColumns( consumer );
//		}
//
//		if ( indexDescriptor != null ) {
//			indexDescriptor.visitColumns( consumer );
//		}
//
//		elementDescriptor.visitColumns( consumer );
	}
}
