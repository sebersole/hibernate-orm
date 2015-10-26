/*
 * Hibernate, Relational Persistence for Idiomatic Java
 *
 * License: GNU Lesser General Public License (LGPL), version 2.1 or later.
 * See the lgpl.txt file in the root directory or <http://www.gnu.org/licenses/lgpl-2.1.html>.
 */
package org.hibernate.boot.model.source.spi;

import java.util.List;

/**
 * Contract describing source of table constraints
 *
 * @author Hardy Ferentschik
 */
public interface ConstraintSource {
	/**
	 * @return returns the name of the constraint or {@code null} in case a generated name should be used
	 */
	String name();

	/**
	 * Obtain the logical name of the table for this constraint.
	 *
	 * @return The logical table name. Can be {@code null} in the case of the "primary table".
	 */
	String getTableName();
	
	List<String> columnNames();
	
	List<String> orderings();
}
