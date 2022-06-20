/*
 * Hibernate, Relational Persistence for Idiomatic Java
 *
 * License: GNU Lesser General Public License (LGPL), version 2.1 or later.
 * See the lgpl.txt file in the root directory or http://www.gnu.org/licenses/lgpl-2.1.html.
 */
package org.hibernate.sql.group.builder;

import java.util.HashMap;
import java.util.Map;

import org.hibernate.HibernateException;
import org.hibernate.jdbc.Expectation;
import org.hibernate.metamodel.mapping.EntityMappingType;
import org.hibernate.sql.group.TableUpdate;

/**
 * @author Steve Ebersole
 */
public class CustomTableUpdateBuilder extends AbstractValuedTableMutationBuilder<TableUpdate> implements TableUpdateBuilder {
	private final String customSql;
	private final boolean isCallable;
	private final Expectation expectation;
	private final Map<String,Integer> restrictionColumns = new HashMap<>();

	public CustomTableUpdateBuilder(
			EntityMappingType entityMapping,
			String tableName,
			boolean isOptional,
			int tableIndex,
			String customSql,
			boolean isCallable,
			Expectation expectation) {
		super( entityMapping, tableName, isOptional, tableIndex );
		this.customSql = customSql;
		this.isCallable = isCallable;
		this.expectation = expectation;
	}

	@Override
	public void addValuesColumn(String columnName, String valueExpression) {
		if ( valueExpression == null ) {
			valueExpression = "?";
		}
		super.addValuesColumn( columnName, valueExpression );
	}

	@Override
	public void addValuesLobColumn(String columnName, String valueExpression) {
		if ( valueExpression == null ) {
			valueExpression = "?";
		}
		super.addValuesLobColumn( columnName, valueExpression );
	}

	@Override
	public void addPrimaryKeyColumn(String columnName) {
		restrictionColumns.put( columnName, restrictionColumns.size() );
	}

	@Override
	public void setVersionColumn(String versionColumnName) {
		restrictionColumns.put( versionColumnName, restrictionColumns.size() );
	}

	@Override
	public void addRestrictionColumn(String columnName, String valueExpression) {
		if ( valueExpression.contains( "?" ) ) {
			restrictionColumns.put( columnName, restrictionColumns.size() );
		}
	}

	@Override
	public void setWhere(String fragment) {
		if ( fragment != null ) {
			throw new HibernateException( "Invalid attempt to apply where-restriction on top of custom sql-update mapping : " + getEntityName() );
		}
	}

	@Override
	public TableUpdate createMutation() {
		return new TableUpdate(
				getTableName(),
				customSql,
				isCallable,
				expectation,
				isOptional(),
				getPrimaryTableIndex(),
				getTableIndexes(),
				collectValuesColumnParamIndexes(),
				getKeyColumnParamIndexMap(),
				restrictionColumns
		);
	}
}
