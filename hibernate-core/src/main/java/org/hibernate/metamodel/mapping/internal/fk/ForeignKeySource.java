/*
 * Hibernate, Relational Persistence for Idiomatic Java
 *
 * License: GNU Lesser General Public License (LGPL), version 2.1 or later
 * See the lgpl.txt file in the root directory or http://www.gnu.org/licenses/lgpl-2.1.html
 */
package org.hibernate.metamodel.mapping.internal.fk;

/**
 * @author Steve Ebersole
 */
public interface ForeignKeySource {
	KeyModelPart getKeyModelPart();

	default ForeignKey getForeignKeyDescriptor() {
		return getKeyModelPart().getForeignKeyDescriptor();
	}

	boolean isNullable();
}
