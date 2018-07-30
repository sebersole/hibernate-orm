/*
 * Hibernate, Relational Persistence for Idiomatic Java
 *
 * License: GNU Lesser General Public License (LGPL), version 2.1 or later
 * See the lgpl.txt file in the root directory or http://www.gnu.org/licenses/lgpl-2.1.html
 */
package org.hibernate.loader.internal;

import java.util.Arrays;
import java.util.List;

import org.hibernate.engine.spi.EntityKey;
import org.hibernate.engine.spi.SharedSessionContractImplementor;
import org.hibernate.loader.spi.MultiIdEntityLoader;
import org.hibernate.loader.spi.MultiIdLoaderSelectors;
import org.hibernate.loader.spi.MultiLoadOptions;
import org.hibernate.metamodel.model.domain.spi.EntityDescriptor;
import org.hibernate.query.spi.QueryOptions;
import org.hibernate.query.spi.QueryParameterBindings;
import org.hibernate.sql.ast.consume.spi.SqlAstSelectToJdbcSelectConverter;
import org.hibernate.sql.ast.consume.spi.StandardParameterBindingContext;
import org.hibernate.sql.ast.produce.metamodel.internal.SelectByEntityIdentifierBuilder;
import org.hibernate.sql.ast.produce.spi.SqlAstSelectDescriptor;
import org.hibernate.sql.ast.produce.sqm.spi.Callback;
import org.hibernate.sql.exec.internal.JdbcSelectExecutorStandardImpl;
import org.hibernate.sql.exec.internal.RowTransformerSingularReturnImpl;
import org.hibernate.sql.exec.spi.ExecutionContext;
import org.hibernate.sql.exec.spi.JdbcSelect;
import org.hibernate.sql.exec.spi.ParameterBindingContext;
import org.hibernate.sql.exec.spi.StandardEntityInstanceResolver;

/**
 * @author Steve Ebersole
 */
public class StandardMultiIdEntityLoader<J>
		implements MultiIdEntityLoader<J> {
	private final EntityDescriptor<J> entityDescriptor;
	private final MultiIdLoaderSelectors selectors;

	public StandardMultiIdEntityLoader(EntityDescriptor entityDescriptor, MultiIdLoaderSelectors selectors) {
		this.entityDescriptor = entityDescriptor;
		this.selectors = selectors;
	}

	@Override
	public EntityDescriptor<J> getEntityDescriptor() {
		return entityDescriptor;
	}

	@Override
	public List<J> load(
			Object[] ids,
			MultiLoadOptions options,
			SharedSessionContractImplementor session) {

		// todo (6.0) : account for batch size, if one

		final SelectByEntityIdentifierBuilder selectBuilder = new SelectByEntityIdentifierBuilder(
				session.getSessionFactory(),
				entityDescriptor
		);

		final SqlAstSelectDescriptor selectDescriptor = selectBuilder
				.generateSelectStatement( ids.length, session.getLoadQueryInfluencers(), options.getLockOptions() );

		final List<Object> loadIds = Arrays.asList( ids );

		final ParameterBindingContext parameterBindingContext = new StandardParameterBindingContext(
				session.getFactory(),
				QueryParameterBindings.NO_PARAM_BINDINGS,
				loadIds
		);

		final JdbcSelect jdbcSelect = SqlAstSelectToJdbcSelectConverter.interpret(
				selectDescriptor,
				parameterBindingContext
		);

		return JdbcSelectExecutorStandardImpl.INSTANCE.list(
				jdbcSelect,
				new ExecutionContext() {
					@Override
					public SharedSessionContractImplementor getSession() {
						return session;
					}

					@Override
					public QueryOptions getQueryOptions() {
						return QueryOptions.NONE;
					}

					@Override
					public ParameterBindingContext getParameterBindingContext() {
						return parameterBindingContext;
					}

					@Override
					public Callback getCallback() {
						return null;
					}
				},
				RowTransformerSingularReturnImpl.instance()
		);
	}
}
