/*
 * Hibernate, Relational Persistence for Idiomatic Java
 *
 * License: GNU Lesser General Public License (LGPL), version 2.1 or later
 * See the lgpl.txt file in the root directory or http://www.gnu.org/licenses/lgpl-2.1.html
 */
package org.hibernate.sql;

import java.sql.CallableStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

import org.hibernate.sql.results.spi.JdbcValuesSourceProcessingState;
import org.hibernate.sql.results.spi.SqlSelection;

/**
 * The low-level contract for extracting (reading) values to JDBC.
 *
 * @apiNote At the JDBC-level we always deal with simple/basic values; never
 * composites, entities, collections, etc
 *
 * @author Steve Ebersole
 *
 * @see JdbcValueBinder
 */
public interface JdbcValueExtractor<J>  {
	/**
	 * Extract value from result set
	 */
	J extract(ResultSet rs, SqlSelection selectionMemento, JdbcValuesSourceProcessingState processingState) throws SQLException;

	/**
	 * Extract value from CallableStatement, by SqlSelection
	 */
	J extract(CallableStatement statement, SqlSelection selectionMemento, JdbcValuesSourceProcessingState processingState) throws SQLException;

	/**
	 * Extract value from CallableStatement, by SqlSelection
	 *
	 * todo (6.0) : name (String) versus SqlSelection?  SqlSelection is *just* a memento so in theory it should be fine
	 * 		this overload may get "folded" into the form above depending on the answer to this ^^ question
	 */
	J extract(CallableStatement statement, String name, JdbcValuesSourceProcessingState processingState) throws SQLException;
}
