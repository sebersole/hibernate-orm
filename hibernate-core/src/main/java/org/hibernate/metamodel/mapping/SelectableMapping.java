/*
 * Hibernate, Relational Persistence for Idiomatic Java
 *
 * License: GNU Lesser General Public License (LGPL), version 2.1 or later
 * See the lgpl.txt file in the root directory or http://www.gnu.org/licenses/lgpl-2.1.html
 */
package org.hibernate.metamodel.mapping;

import org.hibernate.Incubating;
import org.hibernate.annotations.ColumnTransformer;

/**
 * Mapping of a selectable (column/formula)
 *
 * @author Christian Beikov
 */
@Incubating
public interface SelectableMapping extends SqlTypedMapping {
	/**
	 * The name of the table to which this selectable is mapped
	 */
	String getContainingTableExpression();

	/**
	 * The selection's expression.  This is the column name or formula
	 */
	String getSelectionExpression();

	/**
	 * The selection's read expression accounting for formula treatment as well
	 * as {@link ColumnTransformer#read()}
	 */
	String getCustomReadExpression();

	/**
	 * The selection's write expression accounting {@link ColumnTransformer#write()}
	 *
	 * @apiNote Always null for formula mappings
	 */
	String getCustomWriteExpression();

	/**
	 * Is the mapping a formula instead of a physical column?
	 */
	boolean isFormula();

	/**
	 * Is the mapping considered nullable?
	 */
	boolean isNullable();

	boolean isInsertable();

	boolean isUpdateable();
}
