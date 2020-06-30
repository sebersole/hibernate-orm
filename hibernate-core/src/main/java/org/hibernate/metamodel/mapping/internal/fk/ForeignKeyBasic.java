/*
 * Hibernate, Relational Persistence for Idiomatic Java
 *
 * License: GNU Lesser General Public License (LGPL), version 2.1 or later
 * See the lgpl.txt file in the root directory or http://www.gnu.org/licenses/lgpl-2.1.html
 */
package org.hibernate.metamodel.mapping.internal.fk;

import org.hibernate.NotYetImplementedFor6Exception;
import org.hibernate.engine.FetchTiming;
import org.hibernate.engine.spi.SessionFactoryImplementor;
import org.hibernate.metamodel.mapping.BasicValuedModelPart;
import org.hibernate.metamodel.mapping.ColumnConsumer;
import org.hibernate.metamodel.mapping.JdbcMapping;
import org.hibernate.query.ComparisonOperator;
import org.hibernate.query.NavigablePath;
import org.hibernate.sql.ast.SqlAstJoinType;
import org.hibernate.sql.ast.spi.SqlAstCreationContext;
import org.hibernate.sql.ast.spi.SqlAstCreationState;
import org.hibernate.sql.ast.spi.SqlExpressionResolver;
import org.hibernate.sql.ast.spi.SqlSelection;
import org.hibernate.sql.ast.tree.expression.ColumnReference;
import org.hibernate.sql.ast.tree.expression.Expression;
import org.hibernate.sql.ast.tree.from.TableGroup;
import org.hibernate.sql.ast.tree.from.TableReference;
import org.hibernate.sql.ast.tree.from.TableReferenceJoin;
import org.hibernate.sql.ast.tree.predicate.ComparisonPredicate;
import org.hibernate.sql.ast.tree.predicate.Predicate;
import org.hibernate.sql.results.ResultGraphCreationException;
import org.hibernate.sql.results.graph.DomainResult;
import org.hibernate.sql.results.graph.DomainResultCreationState;
import org.hibernate.sql.results.graph.Fetch;
import org.hibernate.sql.results.graph.FetchParent;
import org.hibernate.sql.results.graph.basic.BasicFetch;
import org.hibernate.sql.results.graph.basic.BasicResult;

/**
 * @author Steve Ebersole
 */
public class ForeignKeyBasic implements ForeignKey {
	private final String referringTable;
	private final String referringColumn;
	private final String targetTable;
	private final String targetColumn;
	private final JdbcMapping jdbcMapping;

	private final SideBasic referringSide;
	private final SideBasic targetSide;

	public ForeignKeyBasic(
			String referringTable,
			String referringColumn,
			BasicValuedModelPart referringPart,
			String targetTable,
			String targetColumn,
			BasicValuedModelPart targetPart,
			JdbcMapping jdbcMapping) {
		FkDescriptorCreationLogger.LOGGER.debugf(
				"Creating ForeignKeyBasic : %s.%s -> %s.%s",
				referringTable,
				referringColumn,
				targetTable,
				targetColumn
		);

		this.referringTable = referringTable;
		this.referringColumn = referringColumn;
		this.targetTable = targetTable;
		this.targetColumn = targetColumn;
		this.jdbcMapping = jdbcMapping;

		referringSide = new SideBasic() {
			@Override
			public String getTableName() {
				return referringTable;
			}

			@Override
			public String getColumn() {
				return referringColumn;
			}

			@Override
			public JdbcMapping getJdbcMapping() {
				return jdbcMapping;
			}

			@Override
			public ForeignKeyBasic getForeignKey() {
				return ForeignKeyBasic.this;
			}

			@Override
			public BasicValuedModelPart getKeyPart() {
				return referringPart;
			}
		};

		targetSide = new SideBasic() {
			@Override
			public String getTableName() {
				return targetTable;
			}

			@Override
			public String getColumn() {
				return targetColumn;
			}

			@Override
			public JdbcMapping getJdbcMapping() {
				return jdbcMapping;
			}

			@Override
			public ForeignKeyBasic getForeignKey() {
				return ForeignKeyBasic.this;
			}

			@Override
			public BasicValuedModelPart getKeyPart() {
				return targetPart;
			}
		};
	}

	public JdbcMapping getJdbcMapping() {
		return jdbcMapping;
	}

	@Override
	public SideBasic getReferringSide() {
		return referringSide;
	}

	@Override
	public SideBasic getTargetSide() {
		return targetSide;
	}

	@Override
	public DomainResult createCollectionFetchDomainResult(
			NavigablePath collectionPath,
			TableGroup tableGroup,
			DomainResultCreationState creationState) {
		if ( targetTable.equals( referringTable ) ) {
			final SqlAstCreationState sqlAstCreationState = creationState.getSqlAstCreationState();
			final SqlExpressionResolver sqlExpressionResolver = sqlAstCreationState.getSqlExpressionResolver();
			final TableReference tableReference = tableGroup.resolveTableReference( referringTable );
			final String identificationVariable = tableReference.getIdentificationVariable();
			final SqlSelection sqlSelection = sqlExpressionResolver.resolveSqlSelection(
					sqlExpressionResolver.resolveSqlExpression(
							SqlExpressionResolver.createColumnReferenceKey(
									tableReference,
									targetColumn
							),
							s ->
									new ColumnReference(
											identificationVariable,
											targetColumn,
											jdbcMapping,
											creationState.getSqlAstCreationState()
													.getCreationContext()
													.getSessionFactory()
									)
					),
					jdbcMapping.getJavaTypeDescriptor(),
					sqlAstCreationState.getCreationContext().getDomainModel().getTypeConfiguration()
			);

			//noinspection unchecked
			return new BasicResult(
					sqlSelection.getValuesArrayPosition(),
					null,
					jdbcMapping.getJavaTypeDescriptor()
			);
		}
		else {
			return createDomainResult( collectionPath, tableGroup, creationState );
		}
	}

	@Override
	public DomainResult createDomainResult(
			NavigablePath navigablePath,
			TableGroup tableGroup,
			DomainResultCreationState creationState) {
		throw new NotYetImplementedFor6Exception( getClass() );
//		return createDomainResult( navigablePath, tableGroup, null, creationState );
	}

	@Override
	public Fetch createReferringKeyFetch(
			NavigablePath associationPath,
			TableGroup tableGroup,
			FetchParent fetchParent,
			DomainResultCreationState creationState) {

		final SqlAstCreationState sqlAstCreationState = creationState.getSqlAstCreationState();
		final SqlExpressionResolver sqlExpressionResolver = sqlAstCreationState.getSqlExpressionResolver();

		final TableReference tableReference = tableGroup.resolveTableReference( referringTable );
		if ( tableReference == null ) {
			throw new ResultGraphCreationException( "Could not locate referring table (`" + referringTable + "`) as part of TableGroup `" + tableGroup.getNavigablePath() + "`" );
		}

		final SqlSelection sqlSelection = sqlExpressionResolver.resolveSqlSelection(
				sqlExpressionResolver.resolveSqlExpression(
						SqlExpressionResolver.createColumnReferenceKey( tableReference, referringColumn ),
						s -> new ColumnReference(
								tableReference,
								referringColumn,
								jdbcMapping,
								creationState.getSqlAstCreationState().getCreationContext().getSessionFactory()
						)
				),
				jdbcMapping.getJavaTypeDescriptor(),
				sqlAstCreationState.getCreationContext().getDomainModel().getTypeConfiguration()
		);

		return new BasicFetch(
				sqlSelection.getValuesArrayPosition(),
				fetchParent,
				associationPath.append( PART_NAME ),
				(BasicValuedModelPart) getReferringSide().getKeyPart(),
				false,
				null,
				FetchTiming.IMMEDIATE,
				creationState
		);
	}

	@Override
	public Fetch createTargetKeyFetch(
			NavigablePath associationPath,
			TableGroup tableGroup,
			FetchParent fetchParent,
			DomainResultCreationState creationState) {
		final SqlExpressionResolver sqlExpressionResolver = creationState.getSqlAstCreationState().getSqlExpressionResolver();
		final SessionFactoryImplementor sessionFactory = creationState.getSqlAstCreationState()
				.getCreationContext()
				.getSessionFactory();

		// E.g...
		//
		// `Book.readers` is the association this is the FK-descriptor for.
		//
		// The db model is:
		//
		// Book( id INTEGER, ... )
		// Book_Reader( book_id, reader_id )
		//
		final TableReference targetTableReference = tableGroup.getTableReference( getTargetTableExpression() );
		final Expression keyColumnExpr = sqlExpressionResolver.resolveSqlExpression(
				SqlExpressionResolver.createColumnReferenceKey( targetTableReference, getTargetColumn() ),
				processingState -> new ColumnReference(
						targetTableReference,
						getTargetColumn(),
						getJdbcMapping(),
						sessionFactory
				)
		);

		final SqlSelection sqlSelection = sqlExpressionResolver.resolveSqlSelection(
				keyColumnExpr,
				getJdbcMapping().getJavaTypeDescriptor(),
				sessionFactory.getTypeConfiguration()
		);

		return new BasicFetch(
				sqlSelection.getValuesArrayPosition(),
				fetchParent,
				associationPath.append( PART_NAME ),
				(BasicValuedModelPart) getTargetSide().getKeyPart(),
				false,
				null,
				FetchTiming.IMMEDIATE,
				creationState
		);
	}

//	@Override
//	public <T> DomainResult<T> createDomainResult(
//			NavigablePath navigablePath,
//			TableGroup tableGroup,
//			String resultVariable,
//			DomainResultCreationState creationState) {
//		final SqlAstCreationState sqlAstCreationState = creationState.getSqlAstCreationState();
//		final SqlExpressionResolver sqlExpressionResolver = sqlAstCreationState.getSqlExpressionResolver();
//		final TableReference tableReference = tableGroup.resolveTableReference( referringTable );
//		final String identificationVariable = tableReference.getIdentificationVariable();
//		final SqlSelection sqlSelection = sqlExpressionResolver.resolveSqlSelection(
//				sqlExpressionResolver.resolveSqlExpression(
//						SqlExpressionResolver.createColumnReferenceKey(
//								tableReference,
//								referringColumn
//						),
//						s ->
//								new ColumnReference(
//										identificationVariable,
//										referringColumn,
//										jdbcMapping,
//										creationState.getSqlAstCreationState().getCreationContext().getSessionFactory()
//								)
//				),
//				jdbcMapping.getJavaTypeDescriptor(),
//				sqlAstCreationState.getCreationContext().getDomainModel().getTypeConfiguration()
//		);
//
//		//noinspection unchecked
//		return new BasicResult(
//				sqlSelection.getValuesArrayPosition(),
//				null,
//				jdbcMapping.getJavaTypeDescriptor()
//		);
//	}

	@Override
	public Predicate generateJoinPredicate(
			TableReference lhs,
			TableReference rhs,
			SqlAstJoinType sqlAstJoinType,
			SqlExpressionResolver sqlExpressionResolver,
			SqlAstCreationContext creationContext) {
		final String rhsTableExpression = rhs.getTableExpression();
		final String lhsTableExpression = lhs.getTableExpression();
		if ( lhsTableExpression.equals( referringTable ) ) {
			assert rhsTableExpression.equals( targetTable )
					: "Target table mismatch: expecting `" + targetTable + "` but found `" + rhsTableExpression + "`";
			return new ComparisonPredicate(
					new ColumnReference(
							lhs,
							referringColumn,
							jdbcMapping,
							creationContext.getSessionFactory()
					),
					ComparisonOperator.EQUAL,
					new ColumnReference(
							rhs,
							targetColumn,
							jdbcMapping,
							creationContext.getSessionFactory()
					)
			);
		}
		else {
			assert rhsTableExpression.equals( referringTable );
			return new ComparisonPredicate(
					new ColumnReference(
							lhs,
							targetColumn,
							jdbcMapping,
							creationContext.getSessionFactory()
					),
					ComparisonOperator.EQUAL,
					new ColumnReference(
							rhs,
							referringColumn,
							jdbcMapping,
							creationContext.getSessionFactory()
					)
			);
		}
	}

	@Override
	public Predicate generateJoinPredicate(
			TableGroup lhs,
			TableGroup tableGroup,
			SqlAstJoinType sqlAstJoinType,
			SqlExpressionResolver sqlExpressionResolver,
			SqlAstCreationContext creationContext) {
		TableReference lhsTableReference;
		TableReference rhsTableKeyReference;
		if ( targetTable.equals( referringTable )  ) {
			lhsTableReference = getTableReferenceWhenTargetEqualsKey( lhs, tableGroup, referringTable );

			rhsTableKeyReference = getTableReference(
					lhs,
					tableGroup,
					targetTable
			);
		}
		else {
			lhsTableReference = getTableReference( lhs, tableGroup, referringTable );

			rhsTableKeyReference = getTableReference(
					lhs,
					tableGroup,
					targetTable
			);
		}

		return generateJoinPredicate(
				lhsTableReference,
				rhsTableKeyReference,
				sqlAstJoinType,
				sqlExpressionResolver,
				creationContext
		);
	}

	protected TableReference getTableReferenceWhenTargetEqualsKey(TableGroup lhs, TableGroup tableGroup, String table) {
		if ( tableGroup.getPrimaryTableReference().getTableExpression().equals( table ) ) {
			return tableGroup.getPrimaryTableReference();
		}
		if ( lhs.getPrimaryTableReference().getTableExpression().equals( table ) ) {
			return lhs.getPrimaryTableReference();
		}

		for ( TableReferenceJoin tableJoin : lhs.getTableReferenceJoins() ) {
			if ( tableJoin.getJoinedTableReference().getTableExpression().equals( table ) ) {
				return tableJoin.getJoinedTableReference();
			}
		}

		throw new IllegalStateException( "Could not resolve binding for table `" + table + "`" );
	}

	protected TableReference getTableReference(TableGroup lhs, TableGroup tableGroup, String table) {
		if ( lhs.getPrimaryTableReference().getTableExpression().equals( table ) ) {
			return lhs.getPrimaryTableReference();
		}
		else if ( tableGroup.getPrimaryTableReference().getTableExpression().equals( table ) ) {
			return tableGroup.getPrimaryTableReference();
		}

		final TableReference tableReference = lhs.resolveTableReference( table );
		if ( tableReference != null ) {
			return tableReference;
		}

		throw new IllegalStateException( "Could not resolve binding for table `" + table + "`" );
	}

//	@Override
//	public JavaTypeDescriptor getJavaTypeDescriptor() {
//		return jdbcMapping.getJavaTypeDescriptor();
//	}
//
//	@Override
//	public NavigableRole getNavigableRole() {
//		throw new UnsupportedOperationException();
//	}
//
//	@Override
//	public EntityMappingType findContainingEntityMapping() {
//		throw new UnsupportedOperationException();
//	}

	@Override
	public String getReferringTableExpression() {
		return referringTable;
	}

	@Override
	public void visitReferringColumns(ColumnConsumer consumer) {
		consumer.accept( referringTable, referringColumn, jdbcMapping );
	}

	@Override
	public String getTargetTableExpression() {
		return targetTable;
	}

	@Override
	public void visitTargetColumns(ColumnConsumer consumer) {
		consumer.accept( targetTable, targetColumn, jdbcMapping );
	}

	@Override
	public boolean areTargetColumnNamesEqualsTo(String[] columnNames) {
		if ( columnNames.length != 1 ) {
			return false;
		}
		return targetColumn.equals( columnNames[0] );
	}

//	@Override
//	public int getJdbcTypeCount(TypeConfiguration typeConfiguration) {
//		return 1;
//	}
//
//	@Override
//	public List<JdbcMapping> getJdbcMappings(TypeConfiguration typeConfiguration) {
//		return Collections.singletonList( jdbcMapping );
//	}
//
//	@Override
//	public void visitJdbcTypes(
//			Consumer<JdbcMapping> action,
//			Clause clause,
//			TypeConfiguration typeConfiguration) {
//		action.accept( jdbcMapping );
//	}
//
//	@Override
//	public void visitJdbcValues(
//			Object value,
//			Clause clause,
//			JdbcValuesConsumer valuesConsumer,
//			SharedSessionContractImplementor session) {
//		valuesConsumer.consume( value, jdbcMapping );
//	}
//
//
//	@Override
//	public String getContainingTableExpression() {
//		return referringTable;
//	}
//
//	@Override
//	public String getMappedColumnExpression() {
//		return referringColumn;
//	}
//
//	@Override
//	public String getFetchableName() {
//		return PART_NAME;
//	}
//
//	@Override
//	public FetchOptions getMappedFetchOptions() {
//		return this;
//	}
//
//	@Override
//	public FetchStyle getStyle() {
//		return FetchStyle.JOIN;
//	}
//
//	@Override
//	public FetchTiming getTiming() {
//		return FetchTiming.IMMEDIATE;
//	}
//
//	@Override
//	public Fetch generateFetch(
//			FetchParent fetchParent,
//			NavigablePath fetchablePath,
//			FetchTiming fetchTiming,
//			boolean selected,
//			LockMode lockMode,
//			String resultVariable,
//			DomainResultCreationState creationState) {
//		return null;
//	}
//
//	@Override
//	public MappingType getMappedTypeDescriptor() {
//		return null;
//	}
//
//	@Override
//	public JdbcMapping getJdbcMapping() {
//		return jdbcMapping;
//	}

	public String getTargetTable() {
		return targetTable;
	}

	public String getTargetColumn() {
		return targetColumn;
	}
}
