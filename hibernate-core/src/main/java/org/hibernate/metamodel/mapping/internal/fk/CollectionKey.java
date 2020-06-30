/*
 * Hibernate, Relational Persistence for Idiomatic Java
 *
 * License: GNU Lesser General Public License (LGPL), version 2.1 or later
 * See the lgpl.txt file in the root directory or http://www.gnu.org/licenses/lgpl-2.1.html
 */
package org.hibernate.metamodel.mapping.internal.fk;

import org.hibernate.metamodel.mapping.PluralAttributeMapping;
import org.hibernate.type.ForeignKeyDirection;
import org.hibernate.type.descriptor.java.JavaTypeDescriptor;

/**
 * Descriptor for the collection side of the foreign-key linking the collection
 * with its owner
 *
 * @author Steve Ebersole
 */
public interface CollectionKey extends KeyModelPart {
	String PART_NAME = "{collection-key}";

	@Override
	default String getPartName() {
		return PART_NAME;
	}

	@Override
	PluralAttributeMapping getMappedModelPart();

	@Override
	default ForeignKeyDirection getDirection() {
		// the collection-key is always defined by the target side of the foreign-key.
		// e.g.,
		//
		// `order.id` is
		return ForeignKeyDirection.TARGET;
	}

	@Override
	default JavaTypeDescriptor getJavaTypeDescriptor() {
		return getMappedModelPart().getJavaTypeDescriptor();
	}
}
