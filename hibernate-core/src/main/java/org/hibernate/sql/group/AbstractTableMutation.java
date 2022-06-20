/*
 * Hibernate, Relational Persistence for Idiomatic Java
 *
 * License: GNU Lesser General Public License (LGPL), version 2.1 or later.
 * See the lgpl.txt file in the root directory or http://www.gnu.org/licenses/lgpl-2.1.html.
 */
package org.hibernate.sql.group;

import java.util.Set;

import org.hibernate.jdbc.Expectation;

/**
 * @author Steve Ebersole
 */
public abstract class AbstractTableMutation implements TableMutation {
	private final String tableName;
	private final String sqlString;
	private final boolean isCallable;
	private final Expectation expectation;
	private final boolean isOptional;
	private final int primaryTableIndex;
	private final Set<Integer> tableIndexes;

	public AbstractTableMutation(
			String tableName,
			String sqlString,
			boolean isCallable,
			Expectation expectation,
			boolean isOptional,
			int primaryTableIndex,
			Set<Integer> tableIndexes) {
		this.tableName = tableName;
		this.sqlString = sqlString;
		this.isCallable = isCallable;
		this.expectation = expectation;
		this.isOptional = isOptional;
		this.primaryTableIndex = primaryTableIndex;
		this.tableIndexes = tableIndexes;
	}

	@Override
	public String getTableName() {
		return tableName;
	}

	@Override
	public String getSqlString() {
		return sqlString;
	}

	@Override
	public boolean isCallable() {
		return isCallable;
	}

	@Override
	public Expectation getExpectation() {
		return expectation;
	}

	@Override
	public boolean isOptional() {
		return isOptional;
	}

	@Override
	public int getPrimaryTableIndex() {
		return primaryTableIndex;
	}

	@Override
	public Set<Integer> getTableIndexes() {
		return tableIndexes;
	}
}
