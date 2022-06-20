/*
 * Hibernate, Relational Persistence for Idiomatic Java
 *
 * License: GNU Lesser General Public License (LGPL), version 2.1 or later.
 * See the lgpl.txt file in the root directory or http://www.gnu.org/licenses/lgpl-2.1.html.
 */
package org.hibernate.sql.group.builder;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

import org.hibernate.engine.spi.SessionFactoryImplementor;
import org.hibernate.jdbc.Expectation;
import org.hibernate.metamodel.mapping.EntityMappingType;
import org.hibernate.sql.Update;
import org.hibernate.sql.group.TableUpdate;

/**
 * @author Steve Ebersole
 */
public class StandardTableUpdateBuilder extends AbstractValuedTableMutationBuilder<TableUpdate> implements TableUpdateBuilder {
	private final Expectation expectation;
	private final Update update;
	private final Map<String,Integer> restrictionColumns = new HashMap<>();

	public StandardTableUpdateBuilder(
			EntityMappingType entityMapping,
			String tableName,
			boolean isOptional,
			int tableIndex,
			Expectation expectation,
			SessionFactoryImplementor factory) {
		super( entityMapping, tableName, isOptional, tableIndex );
		this.expectation = expectation;

		this.update = new Update( factory.getJdbcServices().getDialect() );
		this.update.setTableName( tableName );

		if ( factory.getSessionFactoryOptions().isCommentsEnabled() ) {
			this.update.setComment( "Update " + entityMapping.getEntityName() );
		}
	}

	@Override
	public void addValuesColumn(String columnName, String valueExpression) {
		if ( valueExpression == null ) {
			valueExpression = "?";
		}
		super.addValuesColumn( columnName, valueExpression );
		update.addColumn( columnName, valueExpression );
	}

	@Override
	public void addValuesLobColumn(String columnName, String valueExpression) {
		if ( valueExpression == null ) {
			valueExpression = "?";
		}
		super.addValuesLobColumn( columnName, valueExpression );
		update.addLobColumn( columnName, valueExpression );
	}

	@Override
	public void addPrimaryKeyColumn(String columnName) {
		update.addPrimaryKeyColumn( columnName, "?" );
		restrictionColumns.put( columnName, restrictionColumns.size() );
	}

	@Override
	public void setVersionColumn(String versionColumnName) {
		update.setVersionColumnName( versionColumnName );
		restrictionColumns.put( versionColumnName, restrictionColumns.size() );
	}

	@Override
	public void setWhere(String fragment) {
		update.setWhere( fragment );
	}

	@Override
	public void addRestrictionColumn(String columnName, String valueExpression) {
		update.addWhereColumn( columnName, valueExpression );
		if ( valueExpression.contains( "?" ) ) {
			restrictionColumns.put( columnName, restrictionColumns.size() );
		}
	}

	@Override
	public TableUpdate createMutation() {
		return new TableUpdate(
				getTableName(),
				update.toStatementString(),
				false,
				expectation,
				isOptional(),
				getPrimaryTableIndex(),
				getTableIndexes(),
				collectValuesColumnParamIndexes(),
				Collections.emptyMap(),
				restrictionColumns
		);
	}
}
