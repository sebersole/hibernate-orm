/*
 * Hibernate, Relational Persistence for Idiomatic Java
 *
 * License: GNU Lesser General Public License (LGPL), version 2.1 or later
 * See the lgpl.txt file in the root directory or http://www.gnu.org/licenses/lgpl-2.1.html
 */
package org.hibernate.sql.exec.spi;

import java.util.List;

/**
 * @author Steve Ebersole
 */
public interface JdbcCall extends JdbcAnonBlock {
	/**
	 * If the call is a function, returns the function return descriptor
	 */
	JdbcCallFunctionReturn getFunctionReturn();

	List<JdbcCallParameterExtractor> getParameterExtractors();

	List<JdbcCallRefCursorExtractor> getCallRefCursorExtractors();
}
