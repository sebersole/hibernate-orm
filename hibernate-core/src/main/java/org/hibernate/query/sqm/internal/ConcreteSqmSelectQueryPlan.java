/*
 * Hibernate, Relational Persistence for Idiomatic Java
 *
 * License: GNU Lesser General Public License (LGPL), version 2.1 or later
 * See the lgpl.txt file in the root directory or http://www.gnu.org/licenses/lgpl-2.1.html
 */
package org.hibernate.query.sqm.internal;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import javax.persistence.Tuple;
import javax.persistence.TupleElement;

import org.hibernate.ScrollMode;
import org.hibernate.engine.spi.SessionFactoryImplementor;
import org.hibernate.internal.util.collections.streams.StingArrayCollector;
import org.hibernate.metamodel.model.relational.spi.Column;
import org.hibernate.query.IllegalQueryOperationException;
import org.hibernate.query.JpaTupleTransformer;
import org.hibernate.query.spi.ParameterBindingContext;
import org.hibernate.query.spi.QueryOptions;
import org.hibernate.query.spi.QueryParameterImplementor;
import org.hibernate.query.spi.ScrollableResultsImplementor;
import org.hibernate.query.spi.SelectQueryPlan;
import org.hibernate.query.sqm.tree.SqmSelectStatement;
import org.hibernate.query.sqm.tree.select.SqmSelection;
import org.hibernate.sql.JdbcValueCollector;
import org.hibernate.sql.JdbcValueMapper;
import org.hibernate.sql.ast.consume.spi.SqlAstSelectToJdbcSelectConverter;
import org.hibernate.sql.ast.produce.spi.SqlAstBuildingContext;
import org.hibernate.sql.ast.produce.spi.SqlAstSelectDescriptor;
import org.hibernate.sql.ast.produce.sqm.spi.Callback;
import org.hibernate.sql.ast.produce.sqm.spi.SqmSelectToSqlAstConverter;
import org.hibernate.sql.ast.tree.spi.expression.Expression;
import org.hibernate.sql.ast.tree.spi.expression.ParameterSpec;
import org.hibernate.sql.ast.tree.spi.expression.ParameterSpecGroup;
import org.hibernate.sql.exec.internal.JdbcSelectExecutorStandardImpl;
import org.hibernate.sql.exec.internal.RowTransformerJpaTupleImpl;
import org.hibernate.sql.exec.internal.RowTransformerPassThruImpl;
import org.hibernate.sql.exec.internal.RowTransformerSingularReturnImpl;
import org.hibernate.sql.exec.internal.RowTransformerTupleTransformerAdapter;
import org.hibernate.sql.exec.internal.StandardJdbcParameterBindings;
import org.hibernate.sql.exec.internal.TupleElementImpl;
import org.hibernate.sql.exec.spi.ExecutionContext;
import org.hibernate.sql.exec.spi.JdbcSelect;
import org.hibernate.sql.exec.spi.RowTransformer;

/**
 * Standard Hibernate implementation of SelectQueryPlan for SQM-backed
 * {@link org.hibernate.query.Query} implementations, which means
 * HQL/JPQL or {@link javax.persistence.criteria.CriteriaQuery}
 *
 * @author Steve Ebersole
 */
public class ConcreteSqmSelectQueryPlan<R> implements SelectQueryPlan<R> {
	private final SqmSelectStatement sqm;
	private final SqmParameterMetadataImpl sqmParameterMetadata;
	private final RowTransformer<R> rowTransformer;

	@SuppressWarnings("WeakerAccess")
	public ConcreteSqmSelectQueryPlan(
			SqmSelectStatement sqm,
			SqmParameterMetadataImpl sqmParameterMetadata,
			Class<R> resultType,
			QueryOptions queryOptions) {
		this.sqm = sqm;
		this.sqmParameterMetadata = sqmParameterMetadata;

		this.rowTransformer = determineRowTransformer( sqm, resultType, queryOptions );
	}

	@SuppressWarnings("unchecked")
	private RowTransformer<R> determineRowTransformer(
			SqmSelectStatement sqm,
			Class<R> resultType,
			QueryOptions queryOptions) {
		if ( resultType == null || resultType.isArray() ) {
			if ( queryOptions.getTupleTransformer() != null ) {
				return makeRowTransformerTupleTransformerAdapter( sqm, queryOptions );
			}
			else {
				return RowTransformerPassThruImpl.instance();
			}
		}

		// NOTE : if we get here, a result-type of some kind (other than Object[].class) was specified

		if ( Tuple.class.isAssignableFrom( resultType ) ) {
			// resultType is Tuple..
			if ( queryOptions.getTupleTransformer() == null ) {
				final List<TupleElement<?>> tupleElementList = new ArrayList<>();
				for ( SqmSelection selection : sqm.getQuerySpec().getSelectClause().getSelections() ) {
					tupleElementList.add(
							new TupleElementImpl(
									selection.getSelectableNode().getJavaTypeDescriptor().getJavaType(),
									selection.getAlias()
							)
					);
				}
				return (RowTransformer<R>) new RowTransformerJpaTupleImpl( tupleElementList );
//				return (RowTransformer<R>) new RowTransformerTupleImpl(
//						sqm.getQuerySpec().getSelectClause().getSelections()
//								.stream()
//								.map( selection -> (TupleElement<?>) new TupleElementImpl(
//										( (SqmTypeImplementor) selection.asExpression().getExpressableType() ).getDomainType().getReturnedClass(),
//										selection.getAlias()
//								) )
//								.collect( Collectors.toList() )
//				);
			}

			// there can be a TupleTransformer IF it is a JpaTupleBuilder,
			// otherwise this is considered an error
			if ( queryOptions.getTupleTransformer() instanceof JpaTupleTransformer ) {
				return makeRowTransformerTupleTransformerAdapter( sqm, queryOptions );
			}

			throw new IllegalArgumentException(
					"Illegal combination of Tuple resultType and (non-JpaTupleBuilder) TupleTransformer : " +
							queryOptions.getTupleTransformer()
			);
		}

		// NOTE : if we get here we have a resultType of some kind

		if ( queryOptions.getTupleTransformer() != null ) {
			// aside from checking the type parameters for the given TupleTransformer
			// there is not a decent way to verify that the TupleTransformer returns
			// the same type.  We rely on the API here and assume the best
			return makeRowTransformerTupleTransformerAdapter( sqm, queryOptions );
		}
		else if ( sqm.getQuerySpec().getSelectClause().getSelections().size() > 1 ) {
			throw new IllegalQueryOperationException( "Query defined multiple selections, return cannot be typed (other that Object[] or Tuple)" );
		}
		else {
			return RowTransformerSingularReturnImpl.instance();
		}
	}

	@SuppressWarnings("unchecked")
	private RowTransformer makeRowTransformerTupleTransformerAdapter(
			SqmSelectStatement sqm,
			QueryOptions queryOptions) {
		return new RowTransformerTupleTransformerAdapter<>(
				sqm.getQuerySpec().getSelectClause().getSelections()
						.stream()
						.map( SqmSelection::getAlias )
						.collect( StingArrayCollector.INSTANCE ),
				queryOptions.getTupleTransformer()
		);
	}

	@Override
	@SuppressWarnings("unchecked")
	public List<R> performList(
			ExecutionContext executionContext,
			ParameterBindingContext domainParamBindingContext) {

		// ~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~
		// Convert SQM -> SQL-AST
		final SqmSelectToSqlAstConverter sqmConverter = createSqmSelectToSqlAstConverter(
				executionContext,
				domainParamBindingContext
		);

		final SqlAstSelectDescriptor interpretation = sqmConverter.interpret( sqm );


		// ~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~
		// Convert SQL-AST -> JdbcOperation

		final JdbcSelect jdbcSelect = SqlAstSelectToJdbcSelectConverter.interpret(
				interpretation,
				executionContext.getSessionFactory()
		);


		// ~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~
		// Build JDBC param bindings

		final StandardJdbcParameterBindings jdbcParameterBindings = generateJdbcParameterBindings(
				executionContext,
				domainParamBindingContext,
				sqmConverter
		);

		// ~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~
		// Execute

		// todo (6.0) : make these executors resolvable to allow plugging in custom ones.
		//		Dialect?

		return JdbcSelectExecutorStandardImpl.INSTANCE.list(
				jdbcSelect,
				executionContext,
				jdbcParameterBindings,
				rowTransformer
		);
	}

	private StandardJdbcParameterBindings generateJdbcParameterBindings(
			ExecutionContext executionContext,
			ParameterBindingContext domainParamBindingContext, SqmSelectToSqlAstConverter sqmConverter) {
		final StandardJdbcParameterBindings.Builder builder = new StandardJdbcParameterBindings.Builder();
		for ( Map.Entry<Expression, QueryParameterImplementor> entry : sqmConverter.getSqlParamExprToQueryParam().entrySet() ) {
			final Expression sqlExpr = entry.getKey();

			// todo (6.0) : make this easier somehow?

			if ( sqlExpr instanceof ParameterSpecGroup ) {
				final Iterator<ParameterSpec<?>> paramItr = ( (ParameterSpecGroup) sqlExpr ).getGroupedParameters().iterator();
				entry.getValue().getHibernateType().toJdbcValues(
						domainParamBindingContext.getQueryParameterBindings().getBinding( entry.getValue() ),
						(jdbcValue, boundColumn, jdbcValueMapper) -> {
							assert paramItr.hasNext();
							builder.add( paramItr.next(), jdbcValue );
						},
						executionContext.getSession()
				);
				assert ! paramItr.hasNext();
			}
			else {
				entry.getValue().getHibernateType().toJdbcValues(
						domainParamBindingContext.getQueryParameterBindings().getBinding( entry.getValue() ),
						new JdbcValueCollector() {
							private boolean called = false;

							@Override
							public void collect(
									Object jdbcValue,
									Column boundColumn,
									JdbcValueMapper jdbcValueMapper) {
								if ( called ) {
									throw new IllegalStateException(
											"Not expecting multiple jdbc bind values for query parameter : " + entry.getValue()
									);
								}
								called = true;
								builder.add( (ParameterSpec) sqlExpr, jdbcValue );
							}
						},
						executionContext.getSession()
				);
			}
		}

		return builder.build();
	}

	private JdbcSelect buildJdbcSelect(
			ExecutionContext executionContext,
			ParameterBindingContext domainParamBindingContext) {
		final SqmSelectToSqlAstConverter sqmConverter = createSqmSelectToSqlAstConverter(
				executionContext,
				domainParamBindingContext
		);

		final SqlAstSelectDescriptor interpretation = sqmConverter.interpret( sqm );

		return SqlAstSelectToJdbcSelectConverter.interpret(
				interpretation,
				executionContext.getSessionFactory()
		);
	}

	private SqmSelectToSqlAstConverter createSqmSelectToSqlAstConverter(
			ExecutionContext executionContext,
			ParameterBindingContext domainParamBindingContext) {
		return new SqmSelectToSqlAstConverter(
					new SqlAstBuildingContext() {
						@Override
						public SessionFactoryImplementor getSessionFactory() {
							return executionContext.getSession().getFactory();
						}

						@Override
						public Callback getCallback() {
							return executionContext.getCallback();
						}

						@Override
						public QueryOptions getQueryOptions() {
							return executionContext.getQueryOptions();
						}
					},
					sqmParameterMetadata.getSqmParamToQueryParamXref()
			);
	}

	@Override
	@SuppressWarnings("unchecked")
	public ScrollableResultsImplementor performScroll(
			ScrollMode scrollMode,
			ExecutionContext executionContext,
			ParameterBindingContext domainParamBindingContext) {

		// ~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~
		// Convert SQM -> SQL-AST
		final SqmSelectToSqlAstConverter sqmConverter = createSqmSelectToSqlAstConverter(
				executionContext,
				domainParamBindingContext
		);

		final SqlAstSelectDescriptor interpretation = sqmConverter.interpret( sqm );


		// ~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~
		// Convert SQL-AST -> JdbcOperation

		final JdbcSelect jdbcSelect = SqlAstSelectToJdbcSelectConverter.interpret(
				interpretation,
				executionContext.getSessionFactory()
		);


		// ~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~
		// Build JDBC param bindings

		final StandardJdbcParameterBindings jdbcParameterBindings = generateJdbcParameterBindings(
				executionContext,
				domainParamBindingContext,
				sqmConverter
		);

		return JdbcSelectExecutorStandardImpl.INSTANCE.scroll(
				jdbcSelect,
				scrollMode,
				executionContext,
				jdbcParameterBindings,
				rowTransformer
		);
	}
}
