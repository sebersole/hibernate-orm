/*
 * Hibernate, Relational Persistence for Idiomatic Java
 *
 * License: GNU Lesser General Public License (LGPL), version 2.1 or later
 * See the lgpl.txt file in the root directory or http://www.gnu.org/licenses/lgpl-2.1.html
 */
package org.hibernate.metamodel.mapping.internal.fk;

import java.util.ArrayList;
import java.util.List;

import org.hibernate.LockMode;
import org.hibernate.engine.FetchTiming;
import org.hibernate.metamodel.mapping.ColumnConsumer;
import org.hibernate.metamodel.mapping.EmbeddableValuedModelPart;
import org.hibernate.metamodel.mapping.JdbcMapping;
import org.hibernate.metamodel.mapping.internal.MappingModelCreationProcess;
import org.hibernate.query.ComparisonOperator;
import org.hibernate.query.NavigablePath;
import org.hibernate.sql.ast.SqlAstJoinType;
import org.hibernate.sql.ast.spi.SqlAstCreationContext;
import org.hibernate.sql.ast.spi.SqlAstCreationState;
import org.hibernate.sql.ast.spi.SqlExpressionResolver;
import org.hibernate.sql.ast.spi.SqlSelection;
import org.hibernate.sql.ast.tree.expression.ColumnReference;
import org.hibernate.sql.ast.tree.from.TableGroup;
import org.hibernate.sql.ast.tree.from.TableReference;
import org.hibernate.sql.ast.tree.from.TableReferenceJoin;
import org.hibernate.sql.ast.tree.predicate.ComparisonPredicate;
import org.hibernate.sql.ast.tree.predicate.Junction;
import org.hibernate.sql.ast.tree.predicate.Predicate;
import org.hibernate.sql.results.graph.DomainResult;
import org.hibernate.sql.results.graph.DomainResultCreationState;
import org.hibernate.sql.results.graph.Fetch;
import org.hibernate.sql.results.graph.FetchParent;
import org.hibernate.sql.results.graph.embeddable.internal.EmbeddableForeignKeyResultImpl;

/**
 * @author Andrea Boriero
 */
public class ForeignKeyComposite implements ForeignKey {

	private final EmbeddableValuedModelPart referringPart;
	private final String referringTable;
	private final List<String> referringColumns;

	private final EmbeddableValuedModelPart targetPart;
	private final String targetTable;
	private final List<String> targetColumns;

	private final List<JdbcMapping> jdbcMappings;

	private final Side referringSide;
	private final Side targetSide;

	public ForeignKeyComposite(
			EmbeddableValuedModelPart referringPart,
			String referringTable,
			List<String> referringColumns,
			EmbeddableValuedModelPart targetPart,
			String targetTable,
			List<String> targetColumns,
			List<JdbcMapping> jdbcMappings,
			MappingModelCreationProcess creationProcess) {
		if ( referringColumns.size() != targetColumns.size() ) {
			throw new ColumnMismatchException(
					"Number of columns for composite foreign-key did not match : `" +
							referringPart.getNavigableRole() + "` (" + referringColumns.size() + ") -> `" +
							targetPart.getNavigableRole() + "` (" + targetColumns.size() + ")"
			);
		}

		if ( FkDescriptorCreationLogger.DEBUG_ENABLED ) {
			final StringBuilder referringColumnBuffer = new StringBuilder( "(" );
			final StringBuilder targetColumnBuffer = new StringBuilder( "(" );

			for ( int i = 0; i < referringColumns.size(); i++ ) {
				if ( i > 0 ) {
					referringColumnBuffer.append( ", " );
					targetColumnBuffer.append( ", " );
				}

				referringColumnBuffer.append( referringColumns.get( i ) );
				targetColumnBuffer.append( targetColumns.get( i ) );
			}

			FkDescriptorCreationLogger.LOGGER.debugf(
					"Creating ForeignKeyBasic : %s.(%s) -> %s.(%s)",
					referringTable,
					referringColumnBuffer.toString(),
					targetTable,
					targetColumnBuffer.toString()
			);
		}

		this.referringPart = referringPart;
		this.referringTable = referringTable;
		this.referringColumns = referringColumns;
		this.targetPart = targetPart;
		this.targetTable = targetTable;
		this.targetColumns = targetColumns;

		this.jdbcMappings = jdbcMappings;

		referringSide = new SideComposite() {
			@Override
			public String getTableName() {
				return referringTable;
			}

			@Override
			public List<String> getColumnNames() {
				return referringColumns;
			}

			@Override
			public void visitColumns(ColumnConsumer columnConsumer) {
				visitReferringColumns( columnConsumer );
			}

			@Override
			public List<JdbcMapping> getJdbcMappings() {
				return jdbcMappings;
			}

			@Override
			public ForeignKeyComposite getForeignKey() {
				return ForeignKeyComposite.this;
			}

			@Override
			public EmbeddableValuedModelPart getKeyPart() {
				return referringPart;
			}
		};

		targetSide = new SideComposite() {
			@Override
			public String getTableName() {
				return targetTable;
			}

			@Override
			public List<String> getColumnNames() {
				return targetColumns;
			}

			@Override
			public void visitColumns(ColumnConsumer columnConsumer) {
				visitTargetColumns( columnConsumer );
			}

			@Override
			public List<JdbcMapping> getJdbcMappings() {
				return jdbcMappings;
			}

			@Override
			public ForeignKeyComposite getForeignKey() {
				return ForeignKeyComposite.this;
			}

			@Override
			public EmbeddableValuedModelPart getKeyPart() {
				return targetPart;
			}
		};
	}

	@Override
	public Side getReferringSide() {
		return referringSide;
	}

	@Override
	public Side getTargetSide() {
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

			List<SqlSelection> sqlSelections = new ArrayList<>();
			for ( int i = 0; i < referringColumns.size(); i++ ) {
				final JdbcMapping jdbcMapping = jdbcMappings.get( i );
				final String columnExpression = targetColumns.get( i );
				final SqlSelection sqlSelection = sqlExpressionResolver.resolveSqlSelection(
						sqlExpressionResolver.resolveSqlExpression(
								SqlExpressionResolver.createColumnReferenceKey(
										tableReference,
										columnExpression
								),
								s ->
										new ColumnReference(
												identificationVariable,
												columnExpression,
												jdbcMapping,
												creationState.getSqlAstCreationState()
														.getCreationContext()
														.getSessionFactory()
										)

						),
						jdbcMapping.getJavaTypeDescriptor(),
						sqlAstCreationState.getCreationContext().getDomainModel().getTypeConfiguration()
				);
				sqlSelections.add( sqlSelection );
			}

			return new EmbeddableForeignKeyResultImpl(
					sqlSelections,
					collectionPath,
					targetPart,
					null,
					creationState
			);
		}
		else {
			return createDomainResult( collectionPath, tableGroup, creationState );
		}
	}

	@Override
	public DomainResult createDomainResult(
			NavigablePath collectionPath,
			TableGroup tableGroup,
			DomainResultCreationState creationState) {
		//noinspection unchecked
		final SqlAstCreationState sqlAstCreationState = creationState.getSqlAstCreationState();
		final SqlExpressionResolver sqlExpressionResolver = sqlAstCreationState.getSqlExpressionResolver();
		final TableReference tableReference = tableGroup.resolveTableReference( referringTable );
		final String identificationVariable = tableReference.getIdentificationVariable();
		int size = referringColumns.size();
		List<SqlSelection> sqlSelections = new ArrayList<>(size);
		for ( int i = 0; i < size; i++ ) {
			final String columnExpression = referringColumns.get( i );
			final JdbcMapping jdbcMapping = jdbcMappings.get( i );
			final SqlSelection sqlSelection = sqlExpressionResolver.resolveSqlSelection(
					sqlExpressionResolver.resolveSqlExpression(
							SqlExpressionResolver.createColumnReferenceKey(
									tableReference,
									columnExpression
							),
							s ->
									new ColumnReference(
											identificationVariable,
											columnExpression,
											jdbcMapping,
											creationState.getSqlAstCreationState()
													.getCreationContext()
													.getSessionFactory()
									)
					),
					jdbcMapping.getJavaTypeDescriptor(),
					sqlAstCreationState.getCreationContext().getDomainModel().getTypeConfiguration()
			);
			sqlSelections.add( sqlSelection );
		}

		return new EmbeddableForeignKeyResultImpl(
				sqlSelections,
				collectionPath,
				targetPart,
				null,
				creationState
		);
	}

	@Override
	public Fetch createReferringKeyFetch(
			NavigablePath associationPath,
			TableGroup tableGroup,
			FetchParent fetchParent,
			DomainResultCreationState creationState) {
		return referringSide.getKeyPart().generateFetch(
				fetchParent,
				associationPath,
				FetchTiming.IMMEDIATE,
				false,
				LockMode.READ,
				null,
				creationState
		);
	}

	@Override
	public Fetch createTargetKeyFetch(
			NavigablePath associationPath,
			TableGroup tableGroup,
			FetchParent fetchParent,
			DomainResultCreationState creationState) {
		return targetSide.getKeyPart().generateFetch(
				fetchParent,
				associationPath,
				FetchTiming.IMMEDIATE,
				false,
				LockMode.READ,
				null,
				creationState
		);
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
		if ( targetTable.equals( referringTable ) ) {
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
			assert rhsTableExpression.equals( targetTable );
			return getPredicate( lhs, rhs, creationContext, referringColumns, targetColumns );
		}
		else {
			assert rhsTableExpression.equals( referringTable );
			return getPredicate( lhs, rhs, creationContext, targetColumns, referringColumns );
		}
	}

	private Predicate getPredicate(
			TableReference lhs,
			TableReference rhs,
			SqlAstCreationContext creationContext,
			List<String> lhsExpressions,
			List<String> rhsColumnExpressions) {
		final Junction predicate = new Junction( Junction.Nature.CONJUNCTION );
		for ( int i = 0; i < lhsExpressions.size(); i++ ) {
			JdbcMapping jdbcMapping = jdbcMappings.get( i );
			ComparisonPredicate comparisonPredicate =
					new ComparisonPredicate(
							new ColumnReference(
									lhs,
									lhsExpressions.get( i ),
									jdbcMapping,
									creationContext.getSessionFactory()
							),
							ComparisonOperator.EQUAL,
							new ColumnReference(
									rhs,
									rhsColumnExpressions.get( i ),
									jdbcMapping,
									creationContext.getSessionFactory()
							)
					);
			predicate.add( comparisonPredicate );
		}
		return predicate;
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

	@Override
	public String getReferringTableExpression() {
		return referringTable;
	}

	@Override
	public String getTargetTableExpression() {
		return targetTable;
	}

	@Override
	public void visitReferringColumns(ColumnConsumer consumer) {
		for ( int i = 0; i < referringColumns.size(); i++ ) {
			consumer.accept( referringTable, referringColumns.get( i ), jdbcMappings.get( i ) );
		}
	}

	@Override
	public void visitTargetColumns(ColumnConsumer consumer) {
		for ( int i = 0; i < referringColumns.size(); i++ ) {
			consumer.accept( targetTable, targetColumns.get( i ), jdbcMappings.get( i ) );
		}
	}

	@Override
	public boolean areTargetColumnNamesEqualsTo(String[] columnNames) {
		int length = columnNames.length;
		if ( length != targetColumns.size() ) {
			return false;
		}
		for ( int i = 0; i < length; i++ ) {
			if ( !targetColumns.contains( columnNames[i] ) ) {
				return false;
			}
		}
		return true;
	}

//	@Override
//	public MappingType getPartMappingType() {
//		throw new HibernateException( "Unexpected call to SimpleForeignKeyDescriptor#getPartMappingType" );
//	}
//
//	@Override
//	public JavaTypeDescriptor getJavaTypeDescriptor() {
//		return targetPart.getJavaTypeDescriptor();
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

}
