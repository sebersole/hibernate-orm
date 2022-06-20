/*
 * Hibernate, Relational Persistence for Idiomatic Java
 *
 * License: GNU Lesser General Public License (LGPL), version 2.1 or later.
 * See the lgpl.txt file in the root directory or http://www.gnu.org/licenses/lgpl-2.1.html.
 */
package org.hibernate.sql.group.builder;

import org.hibernate.jdbc.Expectation;
import org.hibernate.metamodel.mapping.EntityMappingType;
import org.hibernate.sql.group.TableInsert;

/**
 * @author Steve Ebersole
 */
public class CustomTableInsertBuilder extends AbstractValuedTableMutationBuilder<TableInsert> implements TableInsertBuilder {
	private final String customSql;
	private final boolean isCallable;
	private final Expectation expectation;

	public CustomTableInsertBuilder(
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
	public TableInsert createMutation() {
		return new TableInsert(
				getTableName(),
				customSql,
				isCallable,
				expectation,
				isOptional(),
				getPrimaryTableIndex(),
				getTableIndexes(),
				collectValuesColumnParamIndexes(),
				getKeyColumnParamIndexMap()
		);
	}
}
