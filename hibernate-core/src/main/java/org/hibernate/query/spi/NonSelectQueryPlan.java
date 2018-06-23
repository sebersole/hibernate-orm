/*
 * Hibernate, Relational Persistence for Idiomatic Java
 *
 * License: GNU Lesser General Public License (LGPL), version 2.1 or later
 * See the lgpl.txt file in the root directory or http://www.gnu.org/licenses/lgpl-2.1.html
 */
package org.hibernate.query.spi;

import org.hibernate.engine.spi.SharedSessionContractImplementor;
import org.hibernate.sql.exec.spi.JdbcParameterBindings;

/**
 * @author Steve Ebersole
 */
public interface NonSelectQueryPlan {

	/**
	 * todo (6.0) : is ParameterBindingContext really needed here?
	 */
	int executeUpdate(
			SharedSessionContractImplementor persistenceContext,
			QueryOptions queryOptions,
			JdbcParameterBindings jdbcParameterBindings,
			ParameterBindingContext parameterBindingContext);
}
