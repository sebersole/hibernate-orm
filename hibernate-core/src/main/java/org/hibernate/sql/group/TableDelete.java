/*
 * Hibernate, Relational Persistence for Idiomatic Java
 *
 * License: GNU Lesser General Public License (LGPL), version 2.1 or later.
 * See the lgpl.txt file in the root directory or http://www.gnu.org/licenses/lgpl-2.1.html.
 */
package org.hibernate.sql.group;

import java.util.Map;
import java.util.Set;

import org.hibernate.HibernateException;
import org.hibernate.jdbc.Expectation;

/**
 * Models a deletion from a table
 *
 * @author Steve Ebersole
 */
public class TableDelete extends AbstractTableMutation implements RestrictedTableMutation {
	private final Map<String, Integer> restrictedColumnParamIndexMap;

	public TableDelete(
			String tableName,
			String sqlString,
			boolean isCallable,
			Expectation expectation,
			boolean isOptional,
			int primaryTableIndex,
			Set<Integer> tableIndexes,
			Map<String, Integer> restrictedColumnParamIndexMap) {
		super( tableName, sqlString, isCallable, expectation, isOptional, primaryTableIndex, tableIndexes );
		this.restrictedColumnParamIndexMap = restrictedColumnParamIndexMap;
	}

	@Override
	public Map<String, Integer> getRestrictedColumnParamIndexMap() {
		return restrictedColumnParamIndexMap;
	}

	@Override
	public Integer findRestrictedColumnParamIndex(String columnName) {
		return restrictedColumnParamIndexMap.get( columnName );
	}

	@Override
	public Integer getRestrictedColumnParamIndex(String columnName) {
		final Integer index = findRestrictedColumnParamIndex( columnName );
		if ( index == null ) {
			throw new HibernateException( "Restriction parameter-index not known for column `" + columnName + "`" );
		}
		return index;
	}

	@Override
	public int getNumberOfParameters() {
		return restrictedColumnParamIndexMap.size();
	}
}
