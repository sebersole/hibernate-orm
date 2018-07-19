/*
 * Hibernate, Relational Persistence for Idiomatic Java
 *
 * License: GNU Lesser General Public License (LGPL), version 2.1 or later
 * See the lgpl.txt file in the root directory or http://www.gnu.org/licenses/lgpl-2.1.html
 */
package org.hibernate.sql.exec.spi;

import java.sql.PreparedStatement;
import java.sql.SQLException;

/**
 * Performs parameter value binding to a JDBC PreparedStatement.
 *
 * @apiNote It is important to understand a major assumption here - the
 * JdbcParameterBinder is already "inclusion" aware based on Clause, etc.
 * The practical implication of this is that there must be a unique
 * JdbcParameterBinder for every usage of a parameter (named/positional)
 * in a query
 *
 * @author Steve Ebersole
 * @author John O'Hara
 */
public interface JdbcParameterBinder {
	int bindParameterValue(
			PreparedStatement statement,
			int startPosition,
			ExecutionContext executionContext) throws SQLException;

	int getNumberOfJdbcParametersNeeded();
}
