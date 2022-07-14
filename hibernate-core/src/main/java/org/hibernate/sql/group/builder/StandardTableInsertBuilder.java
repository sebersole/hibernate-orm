/*
 * Hibernate, Relational Persistence for Idiomatic Java
 *
 * License: GNU Lesser General Public License (LGPL), version 2.1 or later.
 * See the lgpl.txt file in the root directory or http://www.gnu.org/licenses/lgpl-2.1.html.
 */
package org.hibernate.sql.group.builder;

import org.hibernate.engine.spi.SessionFactoryImplementor;
import org.hibernate.id.insert.InsertGeneratedIdentifierDelegate;
import org.hibernate.jdbc.Expectation;
import org.hibernate.metamodel.mapping.EntityMappingType;
import org.hibernate.sql.Insert;
import org.hibernate.sql.group.TableInsert;

/**
 * Builder for table insertion descriptors without custom sql-insert
 *
 * @author Steve Ebersole
 */
public class StandardTableInsertBuilder extends AbstractValuedTableMutationBuilder<TableInsert> implements TableInsertBuilder {
	private final InsertGeneratedIdentifierDelegate identifierDelegate;
	private final Expectation expectation;
	private final Insert insert;

	public StandardTableInsertBuilder(
			EntityMappingType entityMapping,
			InsertGeneratedIdentifierDelegate identifierDelegate,
			String tableName,
			boolean isOptional,
			int tableIndex,
			Expectation expectation,
			SessionFactoryImplementor factory) {
		super( entityMapping, tableName, isOptional, tableIndex );
		this.identifierDelegate = identifierDelegate;
		this.expectation = expectation;

		if ( identifierDelegate != null ) {
			assert tableIndex == 0;
			this.insert = identifierDelegate.prepareIdentifierGeneratingInsert( factory.getSqlStringGenerationContext() );
		}
		else {
			this.insert = new Insert( factory.getJdbcServices().getDialect() );
		}

		this.insert.setTableName( tableName );

		if ( factory.getSessionFactoryOptions().isCommentsEnabled() ) {
			this.insert.setComment( "Insert " + entityMapping.getEntityName() );
		}
	}

	@Override
	public void addValuesColumn(String columnName, String valueExpression) {
		if ( valueExpression == null ) {
			valueExpression = "?";
		}
		super.addValuesColumn( columnName, valueExpression );
		insert.addColumn( columnName, valueExpression );
	}

	@Override
	public void addValuesLobColumn(String columnName, String valueExpression) {
		if ( valueExpression == null ) {
			valueExpression = "?";
		}
		super.addValuesLobColumn( columnName, valueExpression );
		insert.addLobColumn( columnName, valueExpression );
	}

	@Override
	public void addValuesKeyColumn(String columnName, String valueExpression) {
		if ( valueExpression == null ) {
			valueExpression = "?";
		}
		super.addValuesKeyColumn( columnName, valueExpression );
		insert.addColumn( columnName, valueExpression );
	}



	@Override
	public TableInsert createMutation() {
		// if no columns were register
		return new TableInsert(
				getTableName(),
				insert.toStatementString(),
				false,
				expectation,
				isOptional(),
				getPrimaryTableIndex(),
				getTableIndexes(),
				collectValuesColumnParamIndexes(),
				getKeyColumnParamIndexMap()
		);
	}
}
