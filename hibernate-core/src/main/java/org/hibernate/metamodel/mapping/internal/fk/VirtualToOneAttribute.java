/*
 * Hibernate, Relational Persistence for Idiomatic Java
 *
 * License: GNU Lesser General Public License (LGPL), version 2.1 or later
 * See the lgpl.txt file in the root directory or http://www.gnu.org/licenses/lgpl-2.1.html
 */
package org.hibernate.metamodel.mapping.internal.fk;

import java.util.List;
import java.util.function.Consumer;

import org.hibernate.LockMode;
import org.hibernate.NotYetImplementedFor6Exception;
import org.hibernate.engine.FetchStyle;
import org.hibernate.engine.FetchTiming;
import org.hibernate.metamodel.mapping.ColumnConsumer;
import org.hibernate.metamodel.mapping.EntityMappingType;
import org.hibernate.metamodel.mapping.JdbcMapping;
import org.hibernate.metamodel.mapping.ManagedMappingType;
import org.hibernate.metamodel.mapping.MappingModelCreationException;
import org.hibernate.metamodel.mapping.ToOneAttributeMapping;
import org.hibernate.metamodel.mapping.internal.AbstractVirtualAttribute;
import org.hibernate.metamodel.mapping.internal.MappingModelCreationProcess;
import org.hibernate.metamodel.model.domain.NavigableRole;
import org.hibernate.property.access.spi.PropertyAccess;
import org.hibernate.query.NavigablePath;
import org.hibernate.sql.ast.Clause;
import org.hibernate.sql.ast.SqlAstJoinType;
import org.hibernate.sql.ast.spi.SqlAliasBaseGenerator;
import org.hibernate.sql.ast.spi.SqlAstCreationContext;
import org.hibernate.sql.ast.spi.SqlExpressionResolver;
import org.hibernate.sql.ast.tree.from.TableGroup;
import org.hibernate.sql.ast.tree.from.TableGroupJoin;
import org.hibernate.sql.results.graph.DomainResultCreationState;
import org.hibernate.sql.results.graph.FetchOptions;
import org.hibernate.sql.results.graph.FetchParent;
import org.hibernate.sql.results.graph.entity.EntityFetch;
import org.hibernate.type.descriptor.java.JavaTypeDescriptor;
import org.hibernate.type.spi.TypeConfiguration;

/**
 * To-one attribute defined as part of a composite-key - a key-many-to-one
 * in legacy Hibernate terminology
 *
 * @author Steve Ebersole
 */
public class VirtualToOneAttribute extends AbstractVirtualAttribute implements ToOneAttributeMapping {
	private final EntityMappingType associatedEntityType;

	private final Cardinality cardinality;
	private final boolean nullable;

	private final ToOneKey associationKey;


	public VirtualToOneAttribute(
			NavigableRole navigableRole,
			Cardinality cardinality,
			FetchOptions fetchOptions,
			PropertyAccess propertyAccess,
			ManagedMappingType declaringType,
			int position,
			EntityMappingType associatedEntityType,
			String tableName,
			List<String> columnNames,
			List<JdbcMapping> jdbcMappings,
			boolean nullable,
			KeyModelPart owningSideKeyPart,
			@SuppressWarnings("unused") MappingModelCreationProcess creationProcess) {
		super( navigableRole, fetchOptions, propertyAccess, declaringType, position );
		this.cardinality = cardinality;
		this.associatedEntityType = associatedEntityType;
		this.nullable = nullable;

		if ( owningSideKeyPart instanceof KeyModelPartBasic ) {
			assert columnNames.size() == 1;
			assert jdbcMappings.size() == 1;

			final KeyModelPartBasic owningSideBasicKeyPart = (KeyModelPartBasic) owningSideKeyPart;
			this.associationKey = new VirtualToOneKeyBasic(
					this,
					tableName,
					columnNames.get( 0 ),
					jdbcMappings.get( 0 ),
					FetchStyle.SELECT,
					FetchTiming.DELAYED,
					nullable,
					owningSideBasicKeyPart,
					creationProcess
			);
		}
		else if ( owningSideKeyPart instanceof KeyModelPartComposite ) {
			final KeyModelPartComposite owningSideCompositeKeyPart = (KeyModelPartComposite) owningSideKeyPart;
			this.associationKey = new VirtualToOneKeyComposite(
					this,
					owningSideCompositeKeyPart.getEmbeddableTypeDescriptor().getRepresentationStrategy(),
					tableName,
					columnNames,
					jdbcMappings,
					FetchStyle.SELECT,
					FetchTiming.DELAYED,
					nullable,
					owningSideKeyPart,
					creationProcess
			);
		}
		else {
			throw new MappingModelCreationException(
					"Unrecognized type of to-one key : " + owningSideKeyPart
							+ ".  Expecting KeyModelPartBasic or KeyModelPartComposite"
			);
		}
	}

	@Override
	public EntityMappingType getAssociatedEntityMappingType() {
		return associatedEntityType;
	}

	@Override
	public void visitColumns(ColumnConsumer consumer) {
		associationKey.visitColumns( consumer );
	}

	@Override
	public int getJdbcTypeCount(TypeConfiguration typeConfiguration) {
		return associationKey.getJdbcTypeCount( typeConfiguration );
	}

	@Override
	public void visitJdbcTypes(
			Consumer<JdbcMapping> action,
			Clause clause,
			TypeConfiguration typeConfiguration) {
		associationKey.visitJdbcTypes( action, typeConfiguration );
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
		return associationKey.generateEntityFetch(
				fetchParent,
				fetchablePath,
				fetchTiming,
				selected,
				lockMode,
				resultVariable,
				creationState
		);
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
		throw new NotYetImplementedFor6Exception( getClass() );
//		final String aliasRoot = explicitSourceAlias == null ? getSqlAliasStem() : explicitSourceAlias;
//		final SqlAliasBase sqlAliasBase = aliasBaseGenerator.createSqlAliasBase( aliasRoot );
//
//		final ForeignKeyDirection direction = associationKey.getDirection().normalize();
//		final TableReference lhsTableReference;
//		final TableReference rhsTableReference;
//
//		if ( direction == ForeignKeyDirection.REFERRING ) {
//
//		}
//		else {
//			assert direction == ForeignKeyDirection.TARGET;
//		}
//
//		final TableReference referringTableRef = new TableReference(
//				associationKey.getForeignKeyDescriptor().getReferringSide().getTableName(),
//				sqlAliasBase.generateNewAlias(),
//				isNullable(),
//				creationContext.getSessionFactory()
//		);
//
//		final TableGroup tableGroup = new StandardTableGroup(
//				navigablePath,
//				this,
//				lockMode,
//				referringTableRef,
//				sqlAliasBase,
//				(tableExpression) -> getEntityMappingType().containsTableReference( tableExpression ),
//				(tableExpression, tg) -> getEntityMappingType().createTableReferenceJoin(
//						tableExpression,
//						sqlAliasBase,
//						referringTableRef,
//						false,
//						sqlExpressionResolver,
//						creationContext
//				),
//				creationContext.getSessionFactory()
//		);
//
//		final TableReference lhsTableReference;
//		final TableReference rhsTableReference;
//		if ( foreignKeyDirection == ForeignKeyDirection.REFERRING ) {
//			lhsTableReference = lhs.resolveTableReference(
//					associationKey.getForeignKeyDescriptor().getReferringSide().getTableName()
//			);
//			rhsTableReference = tableGroup.resolveTableReference(
//					associationKey.getForeignKeyDescriptor().getTargetSide().getTableName()
//			);
//		}
//		else {
//			lhsTableReference = lhs.resolveTableReference(
//					associationKey.getForeignKeyDescriptor().getTargetSide().getTableName()
//			);
//			rhsTableReference = tableGroup.resolveTableReference(
//					associationKey.getForeignKeyDescriptor().getReferringSide().getTableName()
//			);
//		}
//
//		final TableGroupJoin tableGroupJoin = new TableGroupJoin(
//				navigablePath,
//				sqlAstJoinType,
//				tableGroup,
//				associationKey.getForeignKeyDescriptor().generateJoinPredicate(
//						lhsTableReference,
//						rhsTableReference,
//						sqlAstJoinType,
//						sqlExpressionResolver,
//						creationContext
//				)
//		);
//
//		lhs.addTableGroupJoin( tableGroupJoin );
//
//		return tableGroupJoin;
	}

	@Override
	public String getSqlAliasStem() {
		return getAttributeName();
	}

	@Override
	public int getNumberOfFetchables() {
		return 0;
	}

	@Override
	public JavaTypeDescriptor<?> getJavaTypeDescriptor() {
		return getAssociatedEntityMappingType().getJavaTypeDescriptor();
	}

	@Override
	public KeyModelPart getKeyModelPart() {
		return associationKey;
	}

	@Override
	public boolean isNullable() {
		return nullable;
	}

	@Override
	public Cardinality getCardinality() {
		return cardinality;
	}

	@Override
	public EntityMappingType getMappedTypeDescriptor() {
		return getAssociatedEntityMappingType();
	}

	@Override
	public EntityMappingType getEntityMappingType() {
		return getAssociatedEntityMappingType();
	}

	@Override
	public EntityMappingType getPartMappingType() {
		return getAssociatedEntityMappingType();
	}
}
