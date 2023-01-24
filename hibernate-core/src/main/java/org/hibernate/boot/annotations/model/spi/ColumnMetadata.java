/*
 * Hibernate, Relational Persistence for Idiomatic Java
 *
 * License: GNU Lesser General Public License (LGPL), version 2.1 or later.
 * See the lgpl.txt file in the root directory or http://www.gnu.org/licenses/lgpl-2.1.html.
 */
package org.hibernate.boot.annotations.model.spi;

/**
 * Models information about a column
 *
 * @author Steve Ebersole
 */
public interface ColumnMetadata extends RelationalValueMetadata {
	default String getName() {
		return getColumnName();
	}

	String getColumnName();

	String getTable();

	Boolean getUnique();

	Boolean getNullable();

	Boolean getInsertable();

	Boolean getUpdatable();

	Integer getLength();

	Integer getPrecision();

	Integer getScale();

	String getColumnDefinition();
}
