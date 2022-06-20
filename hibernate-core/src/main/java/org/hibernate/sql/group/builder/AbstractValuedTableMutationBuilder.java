/*
 * Hibernate, Relational Persistence for Idiomatic Java
 *
 * License: GNU Lesser General Public License (LGPL), version 2.1 or later.
 * See the lgpl.txt file in the root directory or http://www.gnu.org/licenses/lgpl-2.1.html.
 */
package org.hibernate.sql.group.builder;

import java.util.LinkedHashMap;
import java.util.Map;

import org.hibernate.metamodel.mapping.EntityMappingType;
import org.hibernate.sql.group.ValuedTableMutation;

/**
 * @author Steve Ebersole
 */
public abstract class AbstractValuedTableMutationBuilder<M extends ValuedTableMutation>
		extends AbstractTableMutationBuilder<M>
		implements ValuedTableMutationBuilder<M> {
	private final Map<String, Integer> columnParamIndexMap = new LinkedHashMap<>();
	private final Map<String, Integer> keyColumnParamIndexMap = new LinkedHashMap<>();
	private Map<String, Integer> lobColumnParamIndexMap;

	private int columnCount;

	public AbstractValuedTableMutationBuilder(
			EntityMappingType entityMapping,
			String tableName,
			boolean isOptional,
			int tableIndex) {
		super( entityMapping, tableName, isOptional, tableIndex );
	}

	protected Map<String, Integer> getColumnParamIndexMap() {
		return columnParamIndexMap;
	}

	protected Map<String, Integer> getKeyColumnParamIndexMap() {
		return keyColumnParamIndexMap;
	}

	protected Map<String, Integer> getLobColumnParamIndexMap() {
		return lobColumnParamIndexMap;
	}

	protected LinkedHashMap<String, Integer> collectValuesColumnParamIndexes() {
		final Map<String, Integer> columnParamIndexMap = getColumnParamIndexMap();
		final LinkedHashMap<String, Integer> combined = new LinkedHashMap<>( columnParamIndexMap );

		final Map<String, Integer> lobColumnParamIndexMap = getLobColumnParamIndexMap();
		if ( lobColumnParamIndexMap != null ) {
			lobColumnParamIndexMap.forEach( (columnName, position) -> {
				combined.put( columnName, columnParamIndexMap.size() + position );
			} );
		}

		return combined;
	}

	@Override
	public void addValuesColumn(String columnName, String valueExpression) {
		if ( valueExpression == null  || valueExpression.contains( "?" ) ) {
			columnParamIndexMap.put( columnName, columnCount++ );
		}
	}

	@Override
	public void addValuesLobColumn(String columnName, String valueExpression) {
		if ( valueExpression == null  || valueExpression.contains( "?" ) ) {
			if ( lobColumnParamIndexMap == null ) {
				lobColumnParamIndexMap = new LinkedHashMap<>();
			}
			lobColumnParamIndexMap.put( columnName, columnCount++ );
		}
	}

	@Override
	public void addValuesKeyColumn(String columnName, String valueExpression) {
		assert valueExpression != null;
		if ( valueExpression.contains( "?" ) ) {
			keyColumnParamIndexMap.put( columnName, columnCount++ );
		}
	}
}
