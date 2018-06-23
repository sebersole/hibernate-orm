/*
 * Hibernate, Relational Persistence for Idiomatic Java
 *
 * License: GNU Lesser General Public License (LGPL), version 2.1 or later
 * See the lgpl.txt file in the root directory or http://www.gnu.org/licenses/lgpl-2.1.html
 */
package org.hibernate.loader.internal;

import java.util.Arrays;
import java.util.List;

import org.hibernate.engine.spi.SharedSessionContractImplementor;
import org.hibernate.loader.spi.MultiIdEntityLoader;
import org.hibernate.loader.spi.MultiIdLoaderSelectors;
import org.hibernate.loader.spi.MultiLoadOptions;
import org.hibernate.metamodel.model.domain.spi.EntityDescriptor;
import org.hibernate.query.spi.QueryOptions;
import org.hibernate.sql.ast.consume.spi.SqlAstSelectToJdbcSelectConverter;
import org.hibernate.sql.ast.produce.metamodel.internal.SelectByEntityIdentifierBuilder;
import org.hibernate.sql.ast.produce.spi.SqlAstSelectDescriptor;
import org.hibernate.sql.ast.produce.sqm.spi.Callback;
import org.hibernate.sql.ast.tree.spi.expression.StandardJdbcParameter;
import org.hibernate.sql.exec.internal.JdbcSelectExecutorStandardImpl;
import org.hibernate.sql.exec.internal.RowTransformerSingularReturnImpl;
import org.hibernate.sql.exec.internal.StandardJdbcParameterBindings;
import org.hibernate.sql.exec.spi.ExecutionContext;
import org.hibernate.sql.exec.spi.JdbcSelect;

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

		final SqlAstSelectDescriptor selectDescriptor = selectBuilder.generateSelectStatement(
				ids.length,
				session.getLoadQueryInfluencers(),
				options.getLockOptions()
		);


		final JdbcSelect jdbcSelect = SqlAstSelectToJdbcSelectConverter.interpret(
				selectDescriptor,
				entityDescriptor.getFactory()
		);

		final List<Object> loadIds = Arrays.asList( ids );

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
					public Callback getCallback() {
						return null;
					}
				},
				null,
				RowTransformerSingularReturnImpl.instance()
		);
	}

	private StandardJdbcParameterBindings idsToJdbcParamBindings(Object[] ids, SharedSessionContractImplementor session) {
		final StandardJdbcParameterBindings.Builder jdbcParamBindingsBuilder = new StandardJdbcParameterBindings.Builder();
		entityDescriptor.getHierarchy().getIdentifierDescriptor().dehydrate(
				id,
				(jdbcValue, boundColumn, jdbcValueMapper) -> jdbcParamBindingsBuilder.add(
						new StandardJdbcParameter( jdbcValueMapper ),
						jdbcValue
				),
				session
		);
		return jdbcParamBindingsBuilder.build();
	}
}
