/*
 * Hibernate, Relational Persistence for Idiomatic Java
 *
 * License: GNU Lesser General Public License (LGPL), version 2.1 or later
 * See the lgpl.txt file in the root directory or http://www.gnu.org/licenses/lgpl-2.1.html
 */
package org.hibernate.sql.exec.internal;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import org.hibernate.engine.spi.SharedSessionContractImplementor;
import org.hibernate.metamodel.model.domain.spi.Writeable;
import org.hibernate.metamodel.model.relational.spi.Column;
import org.hibernate.query.spi.QueryParameterBinding;
import org.hibernate.query.spi.QueryParameterBindings;
import org.hibernate.query.spi.QueryParameterImplementor;
import org.hibernate.sql.SqlExpressableType;
import org.hibernate.sql.ast.Clause;
import org.hibernate.sql.exec.spi.ExecutionContext;
import org.hibernate.sql.exec.spi.JdbcParameter;
import org.hibernate.sql.exec.spi.JdbcParameterBinding;
import org.hibernate.sql.exec.spi.JdbcParameterBindings;
import org.hibernate.sql.exec.spi.RowTransformer;
import org.hibernate.sql.results.internal.RowReaderStandardImpl;
import org.hibernate.sql.results.internal.values.JdbcValues;
import org.hibernate.sql.results.spi.Initializer;
import org.hibernate.sql.results.spi.QueryResult;
import org.hibernate.sql.results.spi.QueryResultAssembler;
import org.hibernate.sql.results.spi.RowReader;

/**
 * @author Steve Ebersole
 */
public class Helper {
	public static <R> RowReader<R> createRowReader(
			ExecutionContext executionContext,
			RowTransformer<R> rowTransformer,
			JdbcValues jdbcValues) {
		final List<QueryResultAssembler> returnAssemblers = new ArrayList<>();
		final List<Initializer> initializers = new ArrayList<>();
		for ( QueryResult queryResult : jdbcValues.getResultSetMapping().getQueryResults() ) {
			queryResult.registerInitializers( initializers::add );
			returnAssemblers.add( queryResult.getResultAssembler() );
		}

		return new RowReaderStandardImpl<>(
				returnAssemblers,
				initializers,
				rowTransformer,
				executionContext.getCallback()
		);
	}



	public static JdbcParameterBindings createJdbcParameterBindings(
			QueryParameterBindings<QueryParameterBinding<?>> domainParamBindings,
			Map<QueryParameterImplementor,List<JdbcParameter>> jdbcParamsByDomainParams,
			SharedSessionContractImplementor session) {
		final JdbcParameterBindings jdbcParameterBindings = new JdbcParameterBindingsImpl();

		domainParamBindings.visitBindings(
				(queryParameterImplementor, queryParameterBinding) -> {
					final List<JdbcParameter> jdbcParameters = jdbcParamsByDomainParams.get( queryParameterImplementor );
					final Object bindValue = domainParamBindings.getBinding( queryParameterImplementor ).getBindValue();
					queryParameterBinding.getBindType().dehydrate(
							queryParameterBinding.getBindType().unresolve( bindValue, session ),
							new Writeable.JdbcValueCollector() {
								private int position = 0;

								@Override
								public void collect(Object jdbcValue, SqlExpressableType type, Column boundColumn) {
									final JdbcParameter jdbcParameter = jdbcParameters.get( position );
									jdbcParameterBindings.addBinding(
											jdbcParameter,
											new JdbcParameterBinding() {
												@Override
												public SqlExpressableType getBindType() {
													return jdbcParameter.getType();
												}

												@Override
												public Object getBindValue() {
													return jdbcValue;
												}
											}
									);
									position++;
								}
							},
							Clause.IRRELEVANT,
							session
					);
				}
		);
//		for ( Map.Entry<QueryParameterImplementor, List<JdbcParameter>> entry : jdbcParamsByDomainParams.entrySet() ) {
//			final QueryParameterBinding<?> binding = domainParamBindings.getBinding( entry.getKey() );
//			binding.getBindType().dehydrate(
//					binding.getBindType().unresolve( binding.getBindValue(), session ),
//					new Writeable.JdbcValueCollector() {
//						private int position = 0;
//
//						@Override
//						public void collect(Object jdbcValue, SqlExpressableType type, Column boundColumn) {
//							jdbcParameterBindings.addBinding(
//									entry.getValue().get( position ),
//									new JdbcParameterBinding() {
//										@Override
//										public SqlExpressableType getBindType() {
//											return type;
//										}
//
//										@Override
//										public Object getBindValue() {
//											return jdbcValue;
//										}
//									}
//							);
//						}
//					},
//					clause,
//					session
//			);
//		}

		return jdbcParameterBindings;
	}
}
