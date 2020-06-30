/*
 * Hibernate, Relational Persistence for Idiomatic Java
 *
 * License: GNU Lesser General Public License (LGPL), version 2.1 or later
 * See the lgpl.txt file in the root directory or http://www.gnu.org/licenses/lgpl-2.1.html
 */
package org.hibernate.metamodel.mapping.internal.fk;

import java.util.List;

import org.hibernate.metamodel.mapping.ColumnConsumer;
import org.hibernate.metamodel.mapping.JdbcMapping;
import org.hibernate.sql.results.graph.Fetchable;

/**
 * Represents one "side" of the foreign-key, either the referring or target,
 * allowing them to be handled consistently.
 */
public interface Side {
	/**
	 * The table for this side of the foreign-key
	 */
	String getTableName();

	/**
	 * The columns for this side of the foreign-key
	 */
	List<String> getColumnNames();

	/**
	 * Visit the key columns on this side
	 */
	void visitColumns(ColumnConsumer columnConsumer);

	/**
	 * The JdbcMappings for the columns on this side
	 */
	List<JdbcMapping> getJdbcMappings();

	/**
	 * Reference back to the FK this side is part of.
	 */
	ForeignKey getForeignKey();

	/**
	 * A model-part that can be used to read a "structured" key value.  For
	 * basic foreign-keys, that would be a simple type (Integer, UUID, ...).
	 * For composite foreign-keys, that would be an instance of corresponding
	 * embedded key type.
	 */
	Fetchable getKeyPart();
}
