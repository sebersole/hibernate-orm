/*
 * Hibernate, Relational Persistence for Idiomatic Java
 *
 * License: GNU Lesser General Public License (LGPL), version 2.1 or later.
 * See the lgpl.txt file in the root directory or http://www.gnu.org/licenses/lgpl-2.1.html.
 */
package org.hibernate.engine.jdbc.group;

import java.sql.PreparedStatement;

import org.hibernate.Incubating;
import org.hibernate.engine.spi.SharedSessionContractImplementor;
import org.hibernate.type.descriptor.JdbcBindingLogging;
import org.hibernate.type.descriptor.ValueBinder;

import static org.hibernate.engine.jdbc.group.StandardParameterStrategy.STANDARD_PARAMETER_BINDING_STRATEGY;

/**
 * @author Steve Ebersole
 */
@Incubating
public interface ParameterStrategy {
	Binder resolveBinder(String tableName, int numberOfParameters);
	void finishUpRow(SharedSessionContractImplementor session);

	interface Binder {
		void bindParameter(
				Object value,
				ValueBinder<Object> valueBinder,
				int position,
				PreparedStatement statement,
				SharedSessionContractImplementor session);
	}

	static ParameterStrategy determineStrategy() {
		return JdbcBindingLogging.TRACE_ENABLED
				? new LoggingOrderParameterStrategy()
				: STANDARD_PARAMETER_BINDING_STRATEGY;
	}
}
