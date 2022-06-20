/*
 * Hibernate, Relational Persistence for Idiomatic Java
 *
 * License: GNU Lesser General Public License (LGPL), version 2.1 or later.
 * See the lgpl.txt file in the root directory or http://www.gnu.org/licenses/lgpl-2.1.html.
 */
package org.hibernate.engine.jdbc.group;

import java.util.function.BiConsumer;

import org.hibernate.Incubating;
import org.hibernate.Internal;
import org.hibernate.sql.group.MutationSqlGroup;
import org.hibernate.sql.group.TableMutation;

/**
 * Grouping of {@link java.sql.PreparedStatement} references
 *
 * @author Steve Ebersole
 */
@Incubating
public interface PreparedStatementGroup {
	/**
	 * The number of statements in this group
	 */
	int getNumberOfStatements();

	/**
	 * Visit the details for each table mutation
	 */
	void forEachStatement(BiConsumer<String, PreparedStatementDetails> action);

	/**
	 * Get the PreparedStatement in this group related to the given table-name
	 */
	PreparedStatementDetails getPreparedStatementDetails(String tableName);

	/**
	 * Release resources held by this group.
	 */
	void release();

	@Internal
	MutationSqlGroup<? extends TableMutation> getSqlGroup();
}
