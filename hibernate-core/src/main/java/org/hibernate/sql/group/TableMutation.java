/*
 * Hibernate, Relational Persistence for Idiomatic Java
 *
 * License: GNU Lesser General Public License (LGPL), version 2.1 or later.
 * See the lgpl.txt file in the root directory or http://www.gnu.org/licenses/lgpl-2.1.html.
 */
package org.hibernate.sql.group;

import java.util.Set;

import org.hibernate.Incubating;
import org.hibernate.jdbc.Expectation;

/**
 * Base description of a mutation (insert, update, delete) of a database table.
 *
 * @author Steve Ebersole
 */
@Incubating
public interface TableMutation {
	/**
	 * The name of the table being mutated
	 */
	String getTableName();

	/**
	 * The mutation SQL string
	 */
	String getSqlString();

	/**
	 * Should the {@linkplain #getSqlString() SQL string} be prepared as a
	 * {@linkplain java.sql.CallableStatement callable statement}?
	 */
	boolean isCallable();

	/**
	 * The expected outcome for the mutation
	 */
	Expectation getExpectation();

	/**
	 * Is the given table optional?
	 */
	boolean isOptional();

	/**
	 * The primary index for the table being mutated
	 */
	int getPrimaryTableIndex();

	/**
	 * The indexes of all tables being mutated by this mutation.
	 *
	 * @apiNote Some physical tables will have multiple indexes relative to an entity;
	 * this is generally an edge case but something we need to account for.
	 */
	Set<Integer> getTableIndexes();

	/**
	 * The number of JDBC parameters defined on the statement for this table
	 */
	int getNumberOfParameters();
}
