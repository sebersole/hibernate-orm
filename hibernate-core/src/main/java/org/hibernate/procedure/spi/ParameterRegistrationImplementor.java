/*
 * Hibernate, Relational Persistence for Idiomatic Java
 *
 * License: GNU Lesser General Public License (LGPL), version 2.1 or later.
 * See the lgpl.txt file in the root directory or <http://www.gnu.org/licenses/lgpl-2.1.html>.
 */
package org.hibernate.procedure.spi;

import org.hibernate.procedure.ParameterRegistration;
import org.hibernate.query.QueryParameter;
import org.hibernate.procedure.internal.ProcedureCallMementoImpl;

/**
 * Additional internal contract for ParameterRegistration
 *
 * @author Steve Ebersole
 */
public interface ParameterRegistrationImplementor<T> extends ParameterRegistration<T> {
	ProcedureCallImplementor getProcedureCall();

	@Override
	ParameterBindImplementor<T> getBind();

	/**
	 * Access to the SQL type(s) for this parameter
	 *
	 * @return The SQL types (JDBC type codes)
	 */
	int[] getSqlTypes();

	/**
	 * Extract value from the statement after execution (used for OUT/INOUT parameters).
	 *
	 * @param statement The callable statement
	 *
	 * @return The extracted value
	 */
	T extract(CallableStatement statement);

	ProcedureCallMementoImpl.ParameterMemento toMemento();
}
