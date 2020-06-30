/*
 * Hibernate, Relational Persistence for Idiomatic Java
 *
 * License: GNU Lesser General Public License (LGPL), version 2.1 or later
 * See the lgpl.txt file in the root directory or http://www.gnu.org/licenses/lgpl-2.1.html
 */
package org.hibernate.metamodel.mapping.internal.fk;

import org.hibernate.type.ForeignKeyDirection;

/**
 * KeyModelPart descriptor for many-to-many join table to associated entity table
 * @author Steve Ebersole
 */
public interface JoinTableKey extends KeyModelPart {
	String PART_NAME = "{join-table-key}";

	@Override
	default String getPartName() {
		return PART_NAME;
	}

	@Override
	default ForeignKeyDirection getDirection() {
		return ForeignKeyDirection.REFERRING;
	}
}
