/*
 * Hibernate, Relational Persistence for Idiomatic Java
 *
 * License: GNU Lesser General Public License (LGPL), version 2.1 or later
 * See the lgpl.txt file in the root directory or http://www.gnu.org/licenses/lgpl-2.1.html
 */
package org.hibernate.metamodel.mapping.internal;

import java.util.function.Consumer;

import org.hibernate.HibernateException;
import org.hibernate.LockMode;
import org.hibernate.annotations.LazyToOneOption;
import org.hibernate.bytecode.spi.BytecodeEnhancementMetadata;
import org.hibernate.engine.FetchStrategy;
import org.hibernate.engine.FetchTiming;
import org.hibernate.engine.spi.SessionFactoryImplementor;
import org.hibernate.mapping.ToOne;
import org.hibernate.metamodel.mapping.Association;
import org.hibernate.metamodel.mapping.ColumnConsumer;
import org.hibernate.metamodel.mapping.EntityMappingType;
import org.hibernate.metamodel.mapping.JdbcMapping;
import org.hibernate.metamodel.mapping.ManagedMappingType;
import org.hibernate.metamodel.mapping.ModelPart;
import org.hibernate.metamodel.mapping.StateArrayContributorMetadataAccess;
import org.hibernate.metamodel.mapping.ToOneAttributeMapping;
import org.hibernate.metamodel.mapping.internal.fk.ForeignKey;
import org.hibernate.metamodel.mapping.internal.fk.ToOneKey;
import org.hibernate.metamodel.mapping.internal.fk.ToOneKeyBasicTarget;
import org.hibernate.metamodel.mapping.internal.fk.ToOneKeyCompositeTarget;
import org.hibernate.metamodel.model.domain.NavigableRole;
import org.hibernate.persister.entity.Loadable;
import org.hibernate.persister.entity.SingleTableEntityPersister;
import org.hibernate.property.access.spi.PropertyAccess;
import org.hibernate.query.NavigablePath;
import org.hibernate.sql.ast.Clause;
import org.hibernate.sql.ast.SqlAstJoinType;
import org.hibernate.sql.ast.spi.FromClauseAccess;
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
import org.hibernate.sql.results.ResultGraphCreationException;
import org.hibernate.sql.results.graph.DomainResultCreationState;
import org.hibernate.sql.results.graph.Fetch;
import org.hibernate.sql.results.graph.FetchParent;
import org.hibernate.sql.results.graph.Fetchable;
import org.hibernate.sql.results.graph.entity.EntityFetch;
import org.hibernate.sql.results.graph.entity.EntityResultGraphNode;
import org.hibernate.sql.results.graph.entity.internal.EntityFetchDelayedImpl;
import org.hibernate.sql.results.graph.entity.internal.EntityFetchJoinedImpl;
import org.hibernate.sql.results.graph.entity.internal.EntityFetchSelectImpl;
import org.hibernate.sql.results.internal.domain.BiDirectionalFetchImpl;
import org.hibernate.type.BasicType;
import org.hibernate.type.EntityType;
import org.hibernate.type.ForeignKeyDirection;
import org.hibernate.type.Type;
import org.hibernate.type.spi.TypeConfiguration;

/**
 * A target-side to-one attribute
 *
 * @author Steve Ebersole
 */
public class ToOneAttributeTarget
		extends AbstractSingularAttributeMapping
		implements ToOneAttributeMapping {

	private final NavigableRole navigableRole;

	private final String sqlAliasStem;
	private final boolean isNullable;
	private final boolean unwrapProxy;
	private final EntityMappingType associatedEntityType;
	private final ToOneKey toOneKey;

	private final String referencedPropertyName;

	private final Cardinality cardinality;

	public ToOneAttributeTarget(
			String name,
			NavigableRole navigableRole,
			Cardinality cardinality,
			int stateArrayPosition,
			ToOne bootValue,
			StateArrayContributorMetadataAccess attributeMetadataAccess,
			FetchStrategy mappedFetchStrategy,
			EntityMappingType associatedEntityType,
			ManagedMappingType declaringType,
			PropertyAccess propertyAccess,
			MappingModelCreationProcess creationProcess) {
		super(
				name,
				stateArrayPosition,
				attributeMetadataAccess,
				mappedFetchStrategy,
				declaringType,
				propertyAccess
		);

		this.navigableRole = navigableRole;

		this.sqlAliasStem = SqlAliasStemHelper.INSTANCE.generateStemFromAttributeName( name );
		this.isNullable = bootValue.isNullable();
		this.unwrapProxy = resolveUnwrapProxy( associatedEntityType, bootValue.getLazyToOneOption(), creationProcess );
		this.associatedEntityType = associatedEntityType;

		this.referencedPropertyName = bootValue.getReferencedPropertyName();
		if ( bootValue.isReferenceToPrimaryKey() ) {
			assert referencedPropertyName == null;
		}
		else {
			assert referencedPropertyName != null;
		}

		this.cardinality = cardinality;

		this.toOneKey = generateKey( bootValue, declaringType, creationProcess );
	}

	private static boolean resolveUnwrapProxy(
			EntityMappingType associatedEntityType,
			LazyToOneOption lazyToOneOption,
			MappingModelCreationProcess creationProcess) {
		// enable proxy unwrapping if:
		// 		1) the bytecode proxy feature is enabled
		// 		2) the associated entity is enhanced for lazy loading
		//		3) the user has not explicitly asked for PROXY via `@LazyToOne`

		final SessionFactoryImplementor sessionFactory = creationProcess.getCreationContext().getSessionFactory();

		if ( ! sessionFactory.getSessionFactoryOptions().isEnhancementAsProxyEnabled() ) {
			return false;
		}

		if ( lazyToOneOption == LazyToOneOption.PROXY ) {
			return false;
		}

		final BytecodeEnhancementMetadata enhancementMetadata = associatedEntityType.getEntityPersister().getBytecodeEnhancementMetadata();

		if ( ! enhancementMetadata.isEnhancedForLazyLoading() ) {
			return false;
		}

		//noinspection RedundantIfStatement
		if ( ! ( ( Loadable) associatedEntityType ).hasSubclasses() ) {
			return false;
		}

		return true;
	}

	private ToOneKey generateKey(
			ToOne bootValue,
			ManagedMappingType declaringType,
			MappingModelCreationProcess creationProcess) {
		final SessionFactoryImplementor sessionFactory = creationProcess.getCreationContext().getSessionFactory();

		final EntityType toOneType = (EntityType) bootValue.getType();
		final Type keyType = toOneType.getIdentifierOrUniqueKeyType( sessionFactory );

		if ( keyType instanceof BasicType ) {
			return new ToOneKeyBasicTarget( bootValue, this, creationProcess );
		}

		return new ToOneKeyCompositeTarget( bootValue, this, creationProcess );
	}

	@Override
	public ToOneKey getKeyModelPart() {
		return toOneKey;
	}

	public String getReferencedPropertyName() {
		return referencedPropertyName;
	}

	@Override
	public Cardinality getCardinality() {
		return cardinality;
	}

	@Override
	public EntityMappingType getMappedTypeDescriptor() {
		return getEntityMappingType();
	}

	@Override
	public EntityMappingType getEntityMappingType() {
		return associatedEntityType;
	}

	@Override
	public NavigableRole getNavigableRole() {
		return navigableRole;
	}

	@Override
	public int getJdbcTypeCount(TypeConfiguration typeConfiguration) {
		return toOneKey.getJdbcTypeCount( typeConfiguration );
	}

	@Override
	public void visitJdbcTypes(
			Consumer<JdbcMapping> action,
			Clause clause,
			TypeConfiguration typeConfiguration) {
		toOneKey.visitJdbcTypes( action, clause, typeConfiguration );
	}

	@Override
	public void visitColumns(ColumnConsumer consumer) {
		toOneKey.visitColumns( consumer );
	}

	@Override
	public Fetch resolveCircularFetch(
			NavigablePath fetchablePath,
			FetchParent fetchParent,
			DomainResultCreationState creationState) {
		// NOTE - a circular fetch reference ultimately needs 2 pieces of information:
		//		1) The NavigablePath that is circular (`fetchablePath`)
		//		2) The NavigablePath to the entity-valued-reference that is the "other side" of the circularity

		final ModelPart parentModelPart = fetchParent.getReferencedModePart();

		if ( ! Fetchable.class.isInstance( parentModelPart ) ) {
			// the `fetchParent` would have to be a Fetch as well for this to be circular...
			return null;
		}

		final FetchParent associationFetchParent = fetchParent.resolveContainingAssociationParent();
		if ( associationFetchParent == null ) {
			return null;
		}
		final ModelPart referencedModePart = associationFetchParent.getReferencedModePart();
		assert referencedModePart instanceof Association;

		final Association associationParent = (Association) referencedModePart;

		if ( toOneKey.getForeignKeyDescriptor().equals( associationParent.getForeignKeyDescriptor() ) ) {
			// we need to determine the NavigablePath referring to the entity that the bi-dir
			// fetch will "return" for its Assembler.  so we walk "up" the FetchParent graph
			// to find the "referenced entity" reference

			return createBiDirectionalFetch( fetchablePath, fetchParent );
		}

		// this is the case of a JoinTable
		// 	PARENT(id)
		// 	PARENT_CHILD(parent_id, child_id)
		// 	CHILD(id)
		// 	the FKDescriptor for the association `Parent.child` will be
		//		PARENT_CHILD.child.id -> CHILD.id
		// and the FKDescriptor for the association `Child.parent` will be
		//		PARENT_CHILD.parent.id -> PARENT.id
		// in such a case the associationParent.getIdentifyingColumnExpressions() is PARENT_CHILD.parent_id
		// while the getIdentifyingColumnExpressions for this association is PARENT_CHILD.child_id
		// so we will check if the parentAssociation ForeignKey Target match with the association entity identifier table and columns
		final ForeignKey associationParentForeignKey = associationParent.getForeignKeyDescriptor();
		if ( referencedModePart instanceof ToOneAttributeMapping
				&& ( (ToOneAttributeMapping) referencedModePart ).getDeclaringType() == getPartMappingType() ) {
			if ( this.toOneKey.getForeignKeyDescriptor().getReferringTableExpression()
					.equals( associationParentForeignKey.getReferringTableExpression() ) ) {
				final SingleTableEntityPersister entityPersister = (SingleTableEntityPersister) getDeclaringType();
				if ( associationParentForeignKey.getTargetTableExpression()
						.equals( entityPersister.getTableName() ) ) {
					final String[] identifierColumnNames = entityPersister.getIdentifierColumnNames();
					if ( associationParentForeignKey.areTargetColumnNamesEqualsTo( identifierColumnNames ) ) {
						return createBiDirectionalFetch( fetchablePath, fetchParent );
					}
					return null;
				}

			}
		}
		return null;
	}

	private Fetch createBiDirectionalFetch(NavigablePath fetchablePath, FetchParent fetchParent) {
		final EntityResultGraphNode referencedEntityReference = resolveEntityGraphNode( fetchParent );

		if ( referencedEntityReference == null ) {
			throw new HibernateException(
					"Could not locate entity-valued reference for circular path `" + fetchablePath + "`"
			);
		}

		return new BiDirectionalFetchImpl(
				FetchTiming.IMMEDIATE,
				fetchablePath,
				fetchParent,
				this,
				referencedEntityReference.getNavigablePath()
		);
	}

	protected EntityResultGraphNode resolveEntityGraphNode(FetchParent fetchParent) {
		FetchParent processingParent = fetchParent;
		while ( processingParent != null ) {
			if ( processingParent instanceof EntityResultGraphNode ) {
				return (EntityResultGraphNode) processingParent;
			}

			if ( processingParent instanceof Fetch ) {
				processingParent = ( (Fetch) processingParent ).getFetchParent();
				continue;
			}

			processingParent = null;
		}

		return null;
	}

	@Override
	public EntityFetch generateFetch(
			FetchParent fetchParent,
			NavigablePath fetchablePath,
			FetchTiming fetchTiming,
			boolean selected,
			LockMode lockMode,
			String resultVariable,
			DomainResultCreationState creationState) {
		final SqlAstCreationState sqlAstCreationState = creationState.getSqlAstCreationState();
		final FromClauseAccess fromClauseAccess = sqlAstCreationState.getFromClauseAccess();

		final TableGroup parentTableGroup = fromClauseAccess.getTableGroup(
				fetchParent.getNavigablePath()
		);

		if ( fetchTiming == FetchTiming.IMMEDIATE && selected ) {
			fromClauseAccess.resolveTableGroup(
					fetchablePath,
					np -> {
						final SqlAstJoinType sqlAstJoinType;
						if ( isNullable ) {
							sqlAstJoinType = SqlAstJoinType.LEFT;
						}
						else {
							sqlAstJoinType = SqlAstJoinType.INNER;
						}

						final TableGroupJoin tableGroupJoin = createTableGroupJoin(
								fetchablePath,
								parentTableGroup,
								null,
								sqlAstJoinType,
								lockMode,
								creationState.getSqlAstCreationState()
						);

						return tableGroupJoin.getJoinedGroup();
					}
			);

			return new EntityFetchJoinedImpl(
					fetchParent,
					this,
					lockMode,
					true,
					fetchablePath,
					creationState
			);
		}

		// the associated entity table was not joined.

		final Fetch keyFetch;
		switch ( cardinality ) {
			case MANY_TO_ONE:
			case LOGICAL_ONE_TO_ONE: {
				keyFetch = toOneKey.getForeignKeyDescriptor().getReferringSide().getKeyPart().generateFetch(
						fetchParent,
						fetchablePath,
						fetchTiming,
						selected,
						lockMode,
						null,
						creationState
				);
				break;
			}
			case ONE_TO_ONE: {
				keyFetch = toOneKey.getForeignKeyDescriptor().createTargetKeyFetch(
						fetchablePath,
						parentTableGroup,
						fetchParent,
						creationState
				);
				break;
			}
			default: {
				throw new ResultGraphCreationException( "Unhandled to-one cardinality : " + cardinality );
			}
		}

		assert !selected;
		if ( fetchTiming == FetchTiming.IMMEDIATE ) {
			return new EntityFetchSelectImpl(
					fetchParent,
					this,
					lockMode,
					isNullable,
					fetchablePath,
					keyFetch,
					creationState
			);
		}

		return new EntityFetchDelayedImpl(
				fetchParent,
				this,
				lockMode,
				isNullable,
				fetchablePath,
				keyFetch
		);
	}

	@Override
	public int getNumberOfFetchables() {
		return getEntityMappingType().getNumberOfFetchables();
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
		final String aliasRoot = explicitSourceAlias == null ? sqlAliasStem : explicitSourceAlias;
		final SqlAliasBase sqlAliasBase = aliasBaseGenerator.createSqlAliasBase( aliasRoot );

		final TableReference primaryTableReference = getEntityMappingType().createPrimaryTableReference(
				sqlAliasBase,
				sqlExpressionResolver,
				creationContext
		);

		final TableGroup tableGroup = new StandardTableGroup(
				navigablePath,
				this,
				lockMode,
				primaryTableReference,
				sqlAliasBase,
				(tableExpression) -> getEntityMappingType().containsTableReference( tableExpression ),
				(tableExpression, tg) -> getEntityMappingType().createTableReferenceJoin(
						tableExpression,
						sqlAliasBase,
						primaryTableReference,
						false,
						sqlExpressionResolver,
						creationContext
				),
				creationContext.getSessionFactory()
		);

		final TableReference lhsTableReference;
		final TableReference rhsTableReference;
		if ( toOneKey.getDirection().normalize() == ForeignKeyDirection.REFERRING ) {
			lhsTableReference = lhs.resolveTableReference(
					toOneKey.getForeignKeyDescriptor().getReferringSide().getTableName()
			);
			rhsTableReference = tableGroup.resolveTableReference(
					toOneKey.getForeignKeyDescriptor().getTargetSide().getTableName()
			);
		}
		else {
			lhsTableReference = lhs.resolveTableReference(
					toOneKey.getForeignKeyDescriptor().getTargetSide().getTableName()
			);
			rhsTableReference = tableGroup.resolveTableReference(
					toOneKey.getForeignKeyDescriptor().getReferringSide().getTableName()
			);
		}

		final TableGroupJoin tableGroupJoin = new TableGroupJoin(
				navigablePath,
				sqlAstJoinType,
				tableGroup,
				toOneKey.getForeignKeyDescriptor().generateJoinPredicate(
						lhsTableReference,
						rhsTableReference,
						sqlAstJoinType,
						sqlExpressionResolver,
						creationContext
				)
		);

		lhs.addTableGroupJoin( tableGroupJoin );

		return tableGroupJoin;
	}

	@Override
	public String getSqlAliasStem() {
		return sqlAliasStem;
	}

	public boolean isNullable() {
		return isNullable;
	}

	public boolean isUnwrapProxy() {
		return unwrapProxy;
	}

	@Override
	public EntityMappingType getAssociatedEntityMappingType() {
		return getEntityMappingType();
	}

	@Override
	public ModelPart getKeyTargetMatchPart() {
		return toOneKey.getForeignKeyDescriptor().getTargetSide().getKeyPart();
	}

	@Override
	public String toString() {
		return "SingularAssociationAttributeMapping {" + navigableRole + "}";
	}
}
