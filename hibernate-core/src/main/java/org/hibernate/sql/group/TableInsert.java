/*
 * Hibernate, Relational Persistence for Idiomatic Java
 *
 * License: GNU Lesser General Public License (LGPL), version 2.1 or later.
 * See the lgpl.txt file in the root directory or http://www.gnu.org/licenses/lgpl-2.1.html.
 */
package org.hibernate.sql.group;

import java.util.Map;
import java.util.Set;

import org.hibernate.jdbc.Expectation;

/**
 * Models an insert into a table
 *
 * @author Steve Ebersole
 */
public class TableInsert extends AbstractValuedTableMutation {
	public TableInsert(
			String tableName,
			String sqlString,
			boolean isCallable,
			Expectation expectation,
			boolean isOptional,
			int primaryTableIndex,
			Set<Integer> tableIndexes,
			Map<String, Integer> valuesColumnParamIndexMap,
			Map<String, Integer> valuesKeyColumnParamIndexMap) {
		super( tableName, sqlString, isCallable, expectation, isOptional, primaryTableIndex, tableIndexes, valuesColumnParamIndexMap, valuesKeyColumnParamIndexMap );
	}
}
