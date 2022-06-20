/*
 * Hibernate, Relational Persistence for Idiomatic Java
 *
 * License: GNU Lesser General Public License (LGPL), version 2.1 or later.
 * See the lgpl.txt file in the root directory or http://www.gnu.org/licenses/lgpl-2.1.html.
 */
package org.hibernate.engine.jdbc.mutation;

import org.hibernate.Incubating;
import org.hibernate.engine.spi.SharedSessionContractImplementor;
import org.hibernate.type.descriptor.ValueBinder;

/**
 * Used to bind JDBC parameter values into the mutation execution
 *
 * @author Steve Ebersole
 */
@Incubating
public interface ParameterBinder {
	void bindParameter(
			Object value,
			ValueBinder<Object> valueBinder,
			int position,
			String tableName,
			SharedSessionContractImplementor session);
}
