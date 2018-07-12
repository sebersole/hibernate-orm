/*
 * Hibernate, Relational Persistence for Idiomatic Java
 *
 * License: GNU Lesser General Public License (LGPL), version 2.1 or later
 * See the lgpl.txt file in the root directory or http://www.gnu.org/licenses/lgpl-2.1.html
 */
package org.hibernate.type.descriptor.spi;

import java.sql.PreparedStatement;
import java.sql.SQLException;

import org.hibernate.metamodel.model.domain.spi.AllowableParameterType;
import org.hibernate.sql.exec.spi.ExecutionContext;

/**
 * Contract for binding value(s) to a {@link PreparedStatement}.
 *
 * @apiNote The value encapsulated is a "domain-level" value, meaning
 * it might correspond to one-or-more JDBC values.
 *
 * @author Steve Ebersole
 */
public interface ValueBinder {
	/**
	 * @see AllowableParameterType#getNumberOfJdbcParametersNeeded()
	 */
	int getNumberOfJdbcParametersNeeded();

	/**
	 * Bind a value to a prepared statement.
	 *
	 * @param st The prepared statement to which to bind the value.
	 * @param position The position at which to bind the value within the prepared statement
	 * @param value The value to bind
	 * @param executionContext The options.
	 *
	 * @throws SQLException Indicates a JDBC error occurred.
	 */
	void bind(PreparedStatement st, int position, Object value, ExecutionContext executionContext) throws SQLException;

	/**
	 * Bind a value to a prepared statement.
	 *
	 * @apiNote This form is functionally only available when the binder
	 * is a "basic type" mapping to just a single JDBC type
	 *
	 * @param st The prepared statement to which to bind the value.
	 * @param name The name of the parameter to bind the value within the prepared statement
	 * @param value The value to bind
	 * @param executionContext The options.
	 *
	 * @throws SQLException Indicates a JDBC error occurred.
	 */
	void bind(PreparedStatement st, String name, Object value, ExecutionContext executionContext) throws SQLException;
}
