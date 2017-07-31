/*
 * Hibernate, Relational Persistence for Idiomatic Java
 *
 * License: GNU Lesser General Public License (LGPL), version 2.1 or later.
 * See the lgpl.txt file in the root directory or <http://www.gnu.org/licenses/lgpl-2.1.html>.
 */
package org.hibernate.type.descriptor.java.internal;

import java.util.Map;
import java.util.Set;

import org.hibernate.collection.spi.PersistentCollectionTuplizer;

/**
 * @author Steve Ebersole
 */
public class MapJavaDescriptor extends AbstractCollectionJavaDescriptor<Map> {
	public MapJavaDescriptor(PersistentCollectionTuplizer tuplizer) {
		super( Map.class, tuplizer );
	}

	@Override
	public Class<Map> getJavaType() {
		return Map.class;
	}

	@Override
	public String extractLoggableRepresentation(Map value) {
		return "{java.util.Map}";
	}

	@Override
	public String toString(Map value) {
		return "{Map}";
	}

}
