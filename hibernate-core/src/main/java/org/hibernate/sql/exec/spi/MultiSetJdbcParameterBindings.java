/*
 * Hibernate, Relational Persistence for Idiomatic Java
 *
 * License: GNU Lesser General Public License (LGPL), version 2.1 or later
 * See the lgpl.txt file in the root directory or http://www.gnu.org/licenses/lgpl-2.1.html
 */
package org.hibernate.sql.exec.spi;

/**
 * JdbcValueBindings impl that allows for binding multiple sets of
 * parameters in an iterative execution.  Iterations continue until
 * the binding sets are exhausted which is indicated by {@link #next} returning
 * false (like JDBC ResultSet)
 *
 * Generally this is only useful for non-select statements
 *
 * @author Steve Ebersole
 */
public interface MultiSetJdbcParameterBindings extends JdbcParameterBindings {
	boolean next();
}
