/*
 * Hibernate, Relational Persistence for Idiomatic Java
 *
 * License: GNU Lesser General Public License (LGPL), version 2.1 or later.
 * See the lgpl.txt file in the root directory or <http://www.gnu.org/licenses/lgpl-2.1.html>.
 */
package org.hibernate.type.descriptor.java.internal;

import java.util.Set;
import java.util.SortedMap;

import org.hibernate.collection.spi.PersistentCollectionTuplizer;

/**
 * @author Chris Cranford
 */
public class SortedMapJavaDescriptorAbstract extends AbstractCollectionJavaDescriptor<SortedMap> {
	public SortedMapJavaDescriptorAbstract(PersistentCollectionTuplizer tuplizer) {
		super( SortedMap.class, tuplizer );
	}

	@Override
	public Class<SortedMap> getJavaType() {
		return SortedMap.class;
	}

	@Override
	public String extractLoggableRepresentation(SortedMap value) {
		return "{java.util.SortedMap}";
	}

	@Override
	public String toString(SortedMap value) {
		return "{SortedMap}";
	}
}
