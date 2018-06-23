/*
 * Hibernate, Relational Persistence for Idiomatic Java
 *
 * License: GNU Lesser General Public License (LGPL), version 2.1 or later
 * See the lgpl.txt file in the root directory or http://www.gnu.org/licenses/lgpl-2.1.html
 */
package org.hibernate.sql.exec.spi;

import org.hibernate.engine.spi.SessionFactoryImplementor;
import org.hibernate.engine.spi.SharedSessionContractImplementor;
import org.hibernate.query.spi.QueryOptions;
import org.hibernate.sql.ast.produce.sqm.spi.Callback;
import org.hibernate.sql.results.spi.MappingResolutionContext;

/**
 * Contextual information needed while executing some JDBC operation.
 *
 * @author Steve Ebersole
 */
public interface ExecutionContext extends MappingResolutionContext {
	SharedSessionContractImplementor getSession();

	QueryOptions getQueryOptions();

	Callback getCallback();

	@Override
	default SessionFactoryImplementor getSessionFactory() {
		return getSession().getFactory();
	}
}
