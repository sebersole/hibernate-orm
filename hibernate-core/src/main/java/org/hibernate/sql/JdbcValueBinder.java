/*
 * Hibernate, Relational Persistence for Idiomatic Java
 *
 * License: GNU Lesser General Public License (LGPL), version 2.1 or later
 * See the lgpl.txt file in the root directory or http://www.gnu.org/licenses/lgpl-2.1.html
 */
package org.hibernate.sql;

import java.sql.CallableStatement;
import java.sql.PreparedStatement;
import java.sql.SQLException;

import org.hibernate.sql.exec.spi.ExecutionContext;

/**
 * The low-level contract for binding (writing) values to JDBC.
 *
 * @apiNote At the JDBC-level we always deal with simple/basic values; never
 * composites, entities, collections, etc
 *
 * @author Steve Ebersole
 *
 * @see JdbcValueExtractor
 */
public interface JdbcValueBinder<J> {

	// todo (6.0) : like SqlSelection, should we have a memento describing the bind positions?

	/**
	 * Bind a value to a prepared statement.
	 */
	void bind(PreparedStatement statement, J value, int index, ExecutionContext executionContext) throws SQLException;

	/**
	 * Bind a value to a CallableStatement.
	 *
	 * @apiNote Binding to a CallableStatement by position is done via {@link #bind(PreparedStatement, Object, int, ExecutionContext)} -
	 * CallableStatement extends PreparedStatement
	 */
	void bind(CallableStatement statement, J value, String name, ExecutionContext executionContext) throws SQLException;
}
