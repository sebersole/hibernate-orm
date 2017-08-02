/*
 * Hibernate, Relational Persistence for Idiomatic Java
 *
 * License: GNU Lesser General Public License (LGPL), version 2.1 or later.
 * See the lgpl.txt file in the root directory or <http://www.gnu.org/licenses/lgpl-2.1.html>.
 */
package org.hibernate.type.descriptor.java.internal;

import java.util.Set;

import org.hibernate.collection.spi.PersistentCollectionTuplizer;

/**
 * @author Steve Ebersole
 */
public class SetJavaDescriptor extends AbstractCollectionJavaDescriptor<Set> {
	public SetJavaDescriptor(PersistentCollectionTuplizer tuplizer) {
		super( Set.class, tuplizer );
	}

	@Override
	public Class<Set> getJavaType() {
		return Set.class;
	}

	@Override
	public String extractLoggableRepresentation(Set value) {
		return "{java.util.Set}";
	}

	@Override
	public String toString(Set value) {
		return "{Set}";
	}
}
