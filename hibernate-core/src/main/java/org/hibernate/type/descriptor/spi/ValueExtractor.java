/*
 * Hibernate, Relational Persistence for Idiomatic Java
 *
 * License: GNU Lesser General Public License (LGPL), version 2.1 or later
 * See the lgpl.txt file in the root directory or http://www.gnu.org/licenses/lgpl-2.1.html
 */
package org.hibernate.type.descriptor.spi;

import java.sql.CallableStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

import org.hibernate.NotYetImplementedFor6Exception;
import org.hibernate.metamodel.model.domain.spi.AllowableParameterType;
import org.hibernate.sql.exec.spi.ExecutionContext;
import org.hibernate.sql.results.spi.InitializerCollector;

/**
 * Contract for extracting value via JDBC (from {@link ResultSet} or as output
 * param from {@link CallableStatement}).
 *
 * Operates on the
 *
 * @author Steve Ebersole
 */
public interface ValueExtractor<X> {
	/**
	 * @see AllowableParameterType#getNumberOfJdbcParametersNeeded()
	 */
	int getNumberOfJdbcParametersNeeded();


	// ~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~
	// todo (6.0) : fix this...
	// 		while this contract is intended to operate at the domain level,
	// 		its signatures below essentially make it a JDBC-level contract
	//
	//		NOTE: the `#getNumberOfJdbcParametersNeeded` method is fine
	//		as it is simply used to determine the number of JDBC parameter
	//		placeholders (`?`) to render into the SQL query string

	default void registerInitializers(InitializerCollector collector) {
		throw new NotYetImplementedFor6Exception( getClass() );
	}

	/**
	 * Extract value from result set, by position
	 *
	 * @param rs The result set from which to extract the value
	 * @param position The position of the value to extract.
	 * @param executionContext The options
	 *
	 * @return The extracted value
	 *
	 * @throws SQLException Exceptions from the underlying JDBC objects are simply re-thrown.
	 */
	X extract(ResultSet rs, int position, ExecutionContext executionContext) throws SQLException;

	X extract(CallableStatement statement, int position, ExecutionContext executionContext) throws SQLException;

	X extract(CallableStatement statement, String name, ExecutionContext executionContext) throws SQLException;
}
