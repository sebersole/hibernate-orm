/*
 * Hibernate, Relational Persistence for Idiomatic Java
 *
 * License: GNU Lesser General Public License (LGPL), version 2.1 or later.
 * See the lgpl.txt file in the root directory or <http://www.gnu.org/licenses/lgpl-2.1.html>.
 */
package org.hibernate.type.descriptor.java.internal;

import java.util.List;

import org.hibernate.collection.spi.PersistentCollectionTuplizer;

/**
 * @author Steve Ebersole
 */
public class ListJavaDescriptor extends AbstractCollectionJavaDescriptor<List> {
	protected ListJavaDescriptor(PersistentCollectionTuplizer tuplizer) {
		super( List.class, tuplizer );
	}

	@Override
	public Class<List> 	getJavaType() {
		return List.class;
	}

	@Override
	public String extractLoggableRepresentation(List value) {
		return "{java.util.list}";
	}

	@Override
	public String toString(List value) {
		return "{List}";
	}
}
