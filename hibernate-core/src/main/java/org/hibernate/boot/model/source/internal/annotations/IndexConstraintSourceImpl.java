/*
 * Hibernate, Relational Persistence for Idiomatic Java
 *
 * License: GNU Lesser General Public License (LGPL), version 2.1 or later.
 * See the lgpl.txt file in the root directory or <http://www.gnu.org/licenses/lgpl-2.1.html>.
 */
package org.hibernate.boot.model.source.internal.annotations;

import java.util.List;

import org.hibernate.boot.model.source.spi.IndexConstraintSource;

/**
 * @author Brett Meyer
 */
class IndexConstraintSourceImpl extends AbstractConstraintSource implements IndexConstraintSource {
	private final boolean unique;

	public IndexConstraintSourceImpl(
			String name,
			String tableName,
			List<String> columnNames,
			List<String> orderings,
			boolean unique) {
		super( name, tableName, columnNames, orderings );
		this.unique = unique;
	}

	@Override
	public String toString() {
		return "IndexConstraintSourceImpl{" +
				"name='" + name + '\'' +
				", tableName='" + tableName + '\'' +
				", columnNames=" + columnNames +
				", orderings=" + orderings +
				'}';
	}
	
	@Override
	public boolean isUnique() {
		return unique;
	}
}


