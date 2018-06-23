/*
 * Hibernate, Relational Persistence for Idiomatic Java
 *
 * License: GNU Lesser General Public License (LGPL), version 2.1 or later
 * See the lgpl.txt file in the root directory or http://www.gnu.org/licenses/lgpl-2.1.html
 */
package org.hibernate.query.sql.internal;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;

import org.hibernate.ScrollMode;
import org.hibernate.query.ParameterMetadata;
import org.hibernate.query.spi.ParameterBindingContext;
import org.hibernate.query.spi.QueryParameterImplementor;
import org.hibernate.query.spi.ScrollableResultsImplementor;
import org.hibernate.query.sql.spi.NativeSelectQueryPlan;
import org.hibernate.sql.ast.tree.spi.expression.ParameterSpec;
import org.hibernate.sql.exec.internal.JdbcSelectExecutorStandardImpl;
import org.hibernate.sql.exec.internal.JdbcSelectImpl;
import org.hibernate.sql.exec.internal.StandardJdbcParameterBindings;
import org.hibernate.sql.exec.spi.ExecutionContext;
import org.hibernate.sql.exec.spi.JdbcSelect;
import org.hibernate.sql.exec.spi.JdbcSelectExecutor;
import org.hibernate.sql.exec.spi.RowTransformer;
import org.hibernate.sql.results.spi.ResultSetMappingDescriptor;

/**
 * @author Steve Ebersole
 */
public class NativeSelectQueryPlanImpl<R> implements NativeSelectQueryPlan<R> {
	private final String sql;
	private final Set<String> affectedTableNames;

	private final ParameterMetadata<? extends QueryParameterImplementor<?>> parameterMetadata;

	private final ResultSetMappingDescriptor resultSetMapping;
	private final RowTransformer<R> rowTransformer;

	public NativeSelectQueryPlanImpl(
			String sql,
			Set<String> affectedTableNames,
			ParameterMetadata parameterMetadata,
			ResultSetMappingDescriptor resultSetMapping,
			RowTransformer<R> rowTransformer) {
		this.sql = sql;
		this.affectedTableNames = affectedTableNames;
		this.parameterMetadata = parameterMetadata;
		this.resultSetMapping = resultSetMapping;
		this.rowTransformer = rowTransformer;
	}

	@Override
	public List<R> performList(
			ExecutionContext executionContext,
			ParameterBindingContext domainParamBindingContext) {
		final JdbcSelect jdbcSelect = generateJdbcSelect();

		StandardJdbcParameterBindings.Builder builder = new StandardJdbcParameterBindings.Builder();

		domainParamBindingContext.getQueryParameterBindings().visitBindings(
				(param, bindValue) -> builder.add( (ParameterSpec) param, bindValue )
		);

		// todo (6.0) : need to make this swappable (see note in executor class)
		final JdbcSelectExecutor executor = JdbcSelectExecutorStandardImpl.INSTANCE;

		return executor.list( jdbcSelect, executionContext, builder.build(), rowTransformer );
	}

	private JdbcSelect generateJdbcSelect() {
		final List<ParameterSpec> parameterSpecs = new ArrayList<>();

		parameterMetadata.visitRegistrations(
				collector -> {
					collector.getHibernateType().toJdbcParameters(
							(column, parameterSpec) -> parameterSpecs.add( parameterSpec )
					);
				}
		);

		return new JdbcSelectImpl(
				sql,
				parameterSpecs,
				resultSetMapping,
				affectedTableNames
		);
	}

	@Override
	public ScrollableResultsImplementor<R> performScroll(
			ScrollMode scrollMode,
			ExecutionContext executionContext,
			ParameterBindingContext domainParamBindingContext) {
		final JdbcSelect jdbcSelect = generateJdbcSelect();

		StandardJdbcParameterBindings.Builder builder = new StandardJdbcParameterBindings.Builder();

		domainParamBindingContext.getQueryParameterBindings().visitBindings(
				(param, bindValue) -> builder.add( (ParameterSpec) param, bindValue )
		);

		final JdbcSelectExecutor executor = JdbcSelectExecutorStandardImpl.INSTANCE;

		return executor.scroll( jdbcSelect, scrollMode, executionContext, builder.build(), rowTransformer );
	}
}
