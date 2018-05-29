/*
 * Hibernate, Relational Persistence for Idiomatic Java
 *
 * License: GNU Lesser General Public License (LGPL), version 2.1 or later
 * See the lgpl.txt file in the root directory or http://www.gnu.org/licenses/lgpl-2.1.html
 */
package org.hibernate.procedure.internal;

import org.hibernate.LockOptions;
import org.hibernate.engine.spi.SessionFactoryImplementor;
import org.hibernate.sql.ast.produce.spi.SqlExpressionResolver;
import org.hibernate.sql.results.spi.QueryResultCreationContext;

/**
 * @author Steve Ebersole
 */
public class QueryResultCreationContextImpl implements QueryResultCreationContext {
	@Override
	public LockOptions getLockOptions() {
		return null;
	}

	@Override
	public SessionFactoryImplementor getSessionFactory() {
		return null;
	}

	@Override
	public SqlExpressionResolver getSqlSelectionResolver() {
		return null;
	}

	@Override
	public boolean shouldCreateShallowEntityResult() {
		return false;
	}
}
