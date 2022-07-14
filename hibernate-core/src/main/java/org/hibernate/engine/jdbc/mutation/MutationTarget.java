/*
 * Hibernate, Relational Persistence for Idiomatic Java
 *
 * License: GNU Lesser General Public License (LGPL), version 2.1 or later.
 * See the lgpl.txt file in the root directory or http://www.gnu.org/licenses/lgpl-2.1.html.
 */
package org.hibernate.engine.jdbc.mutation;

import org.hibernate.Incubating;
import org.hibernate.annotations.Table;
import org.hibernate.id.insert.InsertGeneratedIdentifierDelegate;
import org.hibernate.metamodel.mapping.ModelPart;
import org.hibernate.metamodel.model.domain.NavigableRole;

/**
 * Anything that can be the target of {@linkplain MutationExecutor mutations}
 *
 * @author Steve Ebersole
 */
@Incubating
public interface MutationTarget {
	/**
	 * The model role of this target
	 */
	NavigableRole getNavigableRole();

	/**
	 * The number of tables associated with this target
	 */
	int getNumberOfTables();

	/**
	 * The name of the table defining the identifier for this target
	 */
	String getIdentifierTableName();

	ModelPart getIdentifierDescriptor();

	/**
	 * Whether this target defines any potentially skippable tables.
	 * </p>
	 * A table is considered potentially skippable if it is defined
	 * as inverse or as optional.
	 *
	 * @see Table#inverse
	 * @see Table#optional
	 */
	boolean hasSkippableTables();

	/**
	 * The delegate for executing inserts against the root table for
	 * targets defined using post-insert id generation
	 */
	InsertGeneratedIdentifierDelegate getIdentityInsertDelegate();
}
