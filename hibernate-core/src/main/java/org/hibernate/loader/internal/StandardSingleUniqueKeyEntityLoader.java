/*
 * Hibernate, Relational Persistence for Idiomatic Java
 *
 * License: GNU Lesser General Public License (LGPL), version 2.1 or later.
 * See the lgpl.txt file in the root directory or <http://www.gnu.org/licenses/lgpl-2.1.html>.
 */
package org.hibernate.loader.internal;

import java.util.Collections;
import java.util.EnumMap;
import java.util.List;

import org.hibernate.LockMode;
import org.hibernate.LockOptions;
import org.hibernate.engine.spi.LoadQueryInfluencers;
import org.hibernate.engine.spi.SharedSessionContractImplementor;
import org.hibernate.loader.spi.SingleUniqueKeyEntityLoader;
import org.hibernate.metamodel.model.domain.internal.SingularPersistentAttributeEntity;
import org.hibernate.metamodel.model.domain.spi.EntityDescriptor;
import org.hibernate.query.spi.QueryOptions;
import org.hibernate.sql.ast.consume.spi.SqlAstSelectToJdbcSelectConverter;
import org.hibernate.sql.ast.produce.metamodel.internal.SelectByUniqueKeyBuilder;
import org.hibernate.sql.ast.produce.spi.SqlAstSelectDescriptor;
import org.hibernate.sql.ast.produce.sqm.spi.Callback;
import org.hibernate.sql.exec.internal.JdbcSelectExecutorStandardImpl;
import org.hibernate.sql.exec.internal.LoadParameterBindingContext;
import org.hibernate.sql.exec.internal.RowTransformerSingularReturnImpl;
import org.hibernate.sql.exec.spi.ExecutionContext;
import org.hibernate.sql.exec.spi.JdbcSelect;
import org.hibernate.sql.exec.spi.ParameterBindingContext;

/**
 * @author Andrea Boriero
 */
public class StandardSingleUniqueKeyEntityLoader<T> implements SingleUniqueKeyEntityLoader<T> {
	private final EntityDescriptor<T> entityDescriptor;
	private final String propertyName;

	private EnumMap<LockMode, JdbcSelect> selectByLockMode = new EnumMap<>( LockMode.class );
	private EnumMap<LoadQueryInfluencers.InternalFetchProfileType, JdbcSelect> selectByInternalCascadeProfile;


	public StandardSingleUniqueKeyEntityLoader(String propertyName, EntityDescriptor<T> entityDescriptor) {
		this.entityDescriptor = entityDescriptor;
		this.propertyName = propertyName;

		// todo (6.0) : selectByLockMode and selectByInternalCascadeProfile
	}

	@Override
	public T load(
			Object uk, SharedSessionContractImplementor session, Options options) {
		final List<Object> loadIds = Collections.singletonList( uk );

		final ParameterBindingContext parameterBindingContext = new LoadParameterBindingContext(
				session.getFactory(),
				loadIds
		);

		final JdbcSelect jdbcSelect = resolveJdbcSelect(
				options.getLockOptions(),
				parameterBindingContext,
				session
		);

		final List<T> list = JdbcSelectExecutorStandardImpl.INSTANCE.list(
				jdbcSelect,
				getExecutionContext( session, parameterBindingContext ),
				RowTransformerSingularReturnImpl.instance()
		);

		if ( list.isEmpty() ) {
			return null;
		}

		return list.get( 0 );
	}

	private JdbcSelect resolveJdbcSelect(
			LockOptions lockOptions,
			ParameterBindingContext parameterBindingContext,
			SharedSessionContractImplementor session) {
		final LoadQueryInfluencers loadQueryInfluencers = session.getLoadQueryInfluencers();
		if ( entityDescriptor.isAffectedByEnabledFilters( session ) ) {
			// special case of not-cacheable based on enabled filters effecting this load.
			//
			// This case is special because the filters need to be applied in order to
			// 		properly restrict the SQL/JDBC results.  For this reason it has higher
			// 		precedence than even "internal" fetch profiles.
			return createJdbcSelect( lockOptions, loadQueryInfluencers, parameterBindingContext );
		}

		if ( loadQueryInfluencers.getEnabledInternalFetchProfileType() != null ) {
			if ( LockMode.UPGRADE.greaterThan( lockOptions.getLockMode() ) ) {
				if ( selectByInternalCascadeProfile == null ) {
					selectByInternalCascadeProfile = new EnumMap<>( LoadQueryInfluencers.InternalFetchProfileType.class );
				}
				return selectByInternalCascadeProfile.computeIfAbsent(
						loadQueryInfluencers.getEnabledInternalFetchProfileType(),
						internalFetchProfileType -> createJdbcSelect(
								lockOptions,
								loadQueryInfluencers,
								parameterBindingContext
						)
				);
			}
		}

		// otherwise see if the loader for the requested load can be cached - which
		// 		also means we should look in the cache for an existing one

		final boolean cacheable = determineIfCacheable( lockOptions, loadQueryInfluencers );

		if ( cacheable ) {
			return selectByLockMode.computeIfAbsent(
					lockOptions.getLockMode(),
					lockMode -> createJdbcSelect( lockOptions, loadQueryInfluencers, parameterBindingContext )
			);
		}

		return createJdbcSelect(
				lockOptions,
				loadQueryInfluencers,
				parameterBindingContext
		);

	}

	private JdbcSelect createJdbcSelect(
			LockOptions lockOptions,
			LoadQueryInfluencers queryInfluencers,
			ParameterBindingContext parameterBindingContext) {
		SingularPersistentAttributeEntity attribute =
				(SingularPersistentAttributeEntity) entityDescriptor.getSingularAttribute( propertyName );

		final SelectByUniqueKeyBuilder selectBuilder = new SelectByUniqueKeyBuilder(
				entityDescriptor.getFactory(),
				entityDescriptor,
				attribute
		);

		final SqlAstSelectDescriptor selectDescriptor = selectBuilder
				.generateSelectStatement( 1, queryInfluencers, lockOptions );


		return SqlAstSelectToJdbcSelectConverter.interpret(
				selectDescriptor,
				parameterBindingContext
		);
	}

	@SuppressWarnings("RedundantIfStatement")
	private boolean determineIfCacheable(LockOptions lockOptions, LoadQueryInfluencers loadQueryInfluencers) {
		if ( entityDescriptor.isAffectedByEntityGraph( loadQueryInfluencers ) ) {
			return false;
		}

		if ( lockOptions.getTimeOut() == LockOptions.WAIT_FOREVER ) {
			return false;
		}

		return true;
	}

	private ExecutionContext getExecutionContext(
			SharedSessionContractImplementor session,
			ParameterBindingContext parameterBindingContext) {
		return new ExecutionContext() {
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
		};
	}
}
