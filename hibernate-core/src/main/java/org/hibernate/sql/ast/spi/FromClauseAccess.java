/*
 * Hibernate, Relational Persistence for Idiomatic Java
 *
 * License: GNU Lesser General Public License (LGPL), version 2.1 or later
 * See the lgpl.txt file in the root directory or http://www.gnu.org/licenses/lgpl-2.1.html
 */
package org.hibernate.sql.ast.spi;

import java.util.function.Function;

import org.hibernate.sql.ast.SqlTreeCreationException;
import org.hibernate.sql.ast.tree.from.TableGroup;

/**
 * Access to TableGroup indexing.  The indexing is defined in terms of navigable paths
 *
 * @author Steve Ebersole
 */
public interface FromClauseAccess {
	/**
	 * Find a TableGroup by the NavigablePath it is registered under.  Returns
	 * {@code null} if no TableGroup is registered under that NavigablePath
	 * @param navigablePath
	 */
	TableGroup findTableGroup(String navigablePath);

	/**
	 * Get a  TableGroup by the NavigablePath it is registered under.  If there is
	 * no registration, an exception is thrown.
	 * @param navigablePath
	 */
	default TableGroup getTableGroup(String navigablePath) throws SqlTreeCreationException {
		final TableGroup tableGroup = findTableGroup( navigablePath );
		if ( tableGroup == null ) {
			throw new SqlTreeCreationException( "Could not locate TableGroup - " + navigablePath );
		}
		return tableGroup;
	}

	/**
	 * Register a TableGroup under the given `navigablePath`.  Logs a message
	 * if thhis registration over-writes an existing one.
	 */
	void registerTableGroup(String navigablePath, TableGroup tableGroup);

	/**
	 * Finds the TableGroup associated with the given `navigablePath`.  If one is not found,
	 * it is created via the given `creator`, registered under `navigablePath` and returned.
	 *
	 * @apiNote If the `creator` is called, there is no need for it to register the TableGroup
	 * it creates.  It will be registered by this method after.
	 *
	 * @see #findTableGroup
	 * @see #registerTableGroup
	 */
	default TableGroup resolveTableGroup(String navigablePath, Function<String, TableGroup> creator) {
		TableGroup tableGroup = findTableGroup( navigablePath );
		if ( tableGroup == null ) {
			tableGroup = creator.apply( navigablePath );
			registerTableGroup( navigablePath, tableGroup );
		}
		return tableGroup;
	}
}
