/*
 * Hibernate, Relational Persistence for Idiomatic Java
 *
 * License: GNU Lesser General Public License (LGPL), version 2.1 or later.
 * See the lgpl.txt file in the root directory or http://www.gnu.org/licenses/lgpl-2.1.html.
 */
package org.hibernate.sql.group.builder;

import org.hibernate.sql.group.ValuedTableMutation;

/**
 * TableMutationBuilder for UPDATE and DELETE mutations which can be restricted (WHERE clause)
 *
 * @author Steve Ebersole
 */
public interface RestrictedTableMutationBuilder<M extends ValuedTableMutation> extends TableMutationBuilder<M> {
	@Override
	M createMutation();
}
