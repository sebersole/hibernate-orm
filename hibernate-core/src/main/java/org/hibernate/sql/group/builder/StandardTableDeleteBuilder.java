/*
 * Hibernate, Relational Persistence for Idiomatic Java
 *
 * License: GNU Lesser General Public License (LGPL), version 2.1 or later.
 * See the lgpl.txt file in the root directory or http://www.gnu.org/licenses/lgpl-2.1.html.
 */
package org.hibernate.sql.group.builder;

import java.util.HashMap;
import java.util.Map;

import org.hibernate.engine.spi.SessionFactoryImplementor;
import org.hibernate.jdbc.Expectation;
import org.hibernate.metamodel.mapping.EntityMappingType;
import org.hibernate.sql.Delete;
import org.hibernate.sql.group.TableDelete;

/**
 * @author Steve Ebersole
 */
public class StandardTableDeleteBuilder extends AbstractTableMutationBuilder<TableDelete> implements TableDeleteBuilder {
	private final Expectation expectation;
	private final Delete delete;
	private final Map<String,Integer> restrictionColumns = new HashMap<>();

	public StandardTableDeleteBuilder(
			EntityMappingType entityMapping,
			String tableName,
			boolean isOptional,
			int tableIndex,
			Expectation expectation,
			SessionFactoryImplementor factory) {
		super( entityMapping, tableName, isOptional, tableIndex );
		this.expectation = expectation;

		this.delete = new Delete();
		this.delete.setTableName( tableName );

		if ( factory.getSessionFactoryOptions().isCommentsEnabled() ) {
			this.delete.setComment( "Delete " + entityMapping.getEntityName() );
		}
	}

	@Override
	public void addPrimaryKeyColumn(String columnName) {
		delete.addPrimaryKeyColumn( columnName, "?" );
		restrictionColumns.put( columnName, restrictionColumns.size() );
	}

	@Override
	public void setVersionColumn(String versionColumnName) {
		if ( versionColumnName != null ) {
			delete.setVersionColumnName( versionColumnName );
			restrictionColumns.put( versionColumnName, restrictionColumns.size() );
		}
	}

	@Override
	public void setWhere(String fragment) {
		delete.setWhere( fragment );
	}

	@Override
	public void addWhereFragment(String fragment) {
		delete.addWhereFragment( fragment );
	}

	@Override
	public TableDelete createMutation() {
		return new TableDelete(
				getTableName(),
				delete.toStatementString(),
				false,
				expectation,
				isOptional(),
				getPrimaryTableIndex(),
				getTableIndexes(),
				restrictionColumns
		);
	}
}
