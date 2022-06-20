/*
 * Hibernate, Relational Persistence for Idiomatic Java
 *
 * License: GNU Lesser General Public License (LGPL), version 2.1 or later.
 * See the lgpl.txt file in the root directory or http://www.gnu.org/licenses/lgpl-2.1.html.
 */
package org.hibernate.sql.group.builder;

import java.util.HashSet;
import java.util.Locale;
import java.util.Set;

import org.hibernate.metamodel.mapping.EntityMappingType;
import org.hibernate.sql.group.TableMutation;

/**
 * Base support for TableMutationBuilder implementations
 *
 * @author Steve Ebersole
 */
public abstract class AbstractTableMutationBuilder<M extends TableMutation> implements TableMutationBuilder<M> {
	private final String entityName;
	private final String tableName;
	private final boolean isOptional;
	private final int primaryTableIndex;
	private final Set<Integer> tableIndexes = new HashSet<>();

	public AbstractTableMutationBuilder(
			EntityMappingType entityMapping,
			String tableName,
			boolean isOptional,
			int tableIndex) {
		this.entityName = entityMapping.getEntityName();
		this.tableName = tableName;
		this.isOptional = isOptional;
		this.primaryTableIndex = tableIndex;
		this.tableIndexes.add( tableIndex );
	}

	public String getEntityName() {
		return entityName;
	}

	@Override
	public String getTableName() {
		return tableName;
	}

	public boolean isOptional() {
		return isOptional;
	}

	public int getPrimaryTableIndex() {
		return primaryTableIndex;
	}

	@Override
	public Set<Integer> getTableIndexes() {
		return tableIndexes;
	}

	@Override
	public void addTableIndex(int tableIndex) {
		tableIndexes.add( tableIndex );
	}

	@Override
	public String toString() {
		return String.format(
				Locale.ROOT,
				"AbstractTableMutationBuilder(`%s`, `%s`)",
				entityName,
				tableName
		);
	}
}
