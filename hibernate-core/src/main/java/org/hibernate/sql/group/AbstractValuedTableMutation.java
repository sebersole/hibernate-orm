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
 * @author Steve Ebersole
 */
public abstract class AbstractValuedTableMutation extends AbstractTableMutation implements ValuedTableMutation {
	private final Map<String, Integer> valuesColumnParamIndexMap;
	private final Map<String, Integer> valuesKeyColumnParamIndexMap;

	public AbstractValuedTableMutation(
			String tableName,
			String sqlString,
			boolean isCallable,
			Expectation expectation,
			boolean isOptional,
			int primaryTableIndex,
			Set<Integer> tableIndexes,
			Map<String, Integer> valuesColumnParamIndexMap,
			Map<String, Integer> valuesKeyColumnParamIndexMap) {
		super( tableName, sqlString, isCallable, expectation, isOptional, primaryTableIndex, tableIndexes );
		this.valuesColumnParamIndexMap = valuesColumnParamIndexMap;
		this.valuesKeyColumnParamIndexMap = valuesKeyColumnParamIndexMap;
	}

	@Override
	public Map<String, Integer> getValuesColumnParamIndexMap() {
		return valuesColumnParamIndexMap;
	}

	@Override
	public Integer findValuesColumnParamIndex(String columnName) {
		return valuesColumnParamIndexMap.get( columnName );
	}

	@Override
	public Integer getValuesColumnParamIndex(String columnName) {
		final Integer index = findValuesColumnParamIndex( columnName );
		if ( index == null ) {
			throw new HibernateException( "Values parameter-index not known for column `" + columnName + "`" );
		}
		return index;
	}

	@Override
	public Map<String, Integer> getValuesKeyColumnParamIndexMap() {
		return valuesKeyColumnParamIndexMap;
	}

	@Override
	public Integer findValuesKeyColumnParamIndex(String columnName) {
		return valuesKeyColumnParamIndexMap.get( columnName );
	}

	@Override
	public Integer getValuesKeyColumnParamIndex(String columnName) {
		final Integer index = findValuesKeyColumnParamIndex( columnName );
		if ( index == null ) {
			throw new HibernateException( "Values parameter-index not known for key column `" + columnName + "`" );
		}
		return index;
	}

	@Override
	public int getNumberOfParameters() {
		return ( valuesColumnParamIndexMap == null ? 0 : valuesColumnParamIndexMap.size() )
				+ ( valuesKeyColumnParamIndexMap == null ? 0 : valuesKeyColumnParamIndexMap.size() );
	}
}
