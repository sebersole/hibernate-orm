/*
 * Hibernate, Relational Persistence for Idiomatic Java
 *
 * License: GNU Lesser General Public License (LGPL), version 2.1 or later.
 * See the lgpl.txt file in the root directory or http://www.gnu.org/licenses/lgpl-2.1.html.
 */
package org.hibernate.engine.jdbc.mutation.spi;

import org.hibernate.engine.jdbc.mutation.ParameterBinder;
import org.hibernate.engine.spi.SharedSessionContractImplementor;

/**
 * Internal extension contract for ParameterBinder
 *
 * @author Steve Ebersole
 */
public interface ParameterBinderImplementor extends ParameterBinder {
	/**
	 * Called before the execution of the PreparedStatement for the specified table
	 */
	boolean beforeStatement(String tableName, SharedSessionContractImplementor session);
}
