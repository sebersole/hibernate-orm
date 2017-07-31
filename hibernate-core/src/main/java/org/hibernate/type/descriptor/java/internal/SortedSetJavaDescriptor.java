/*
 * Hibernate, Relational Persistence for Idiomatic Java
 *
 * License: GNU Lesser General Public License (LGPL), version 2.1 or later.
 * See the lgpl.txt file in the root directory or <http://www.gnu.org/licenses/lgpl-2.1.html>.
 */
package org.hibernate.type.descriptor.java.internal;

import java.util.SortedSet;

import org.hibernate.collection.spi.PersistentCollectionTuplizer;

/**
 * @author Chris Cranford
 */
public class SortedSetJavaDescriptor extends AbstractCollectionJavaDescriptor<SortedSet> {
	public SortedSetJavaDescriptor(PersistentCollectionTuplizer tuplizer) {
		super( SortedSet.class, tuplizer );
	}

	@Override
	public Class<SortedSet> getJavaType() {
		return SortedSet.class;
	}

	@Override
	public String extractLoggableRepresentation(SortedSet value) {
		return "{java.util.SortedSet}";
	}

	@Override
	public String toString(SortedSet value) {
		return "{SortedSet}";
	}
}
