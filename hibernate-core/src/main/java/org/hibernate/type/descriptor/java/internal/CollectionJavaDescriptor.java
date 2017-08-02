/*
 * Hibernate, Relational Persistence for Idiomatic Java
 *
 * License: GNU Lesser General Public License (LGPL), version 2.1 or later.
 * See the lgpl.txt file in the root directory or <http://www.gnu.org/licenses/lgpl-2.1.html>.
 */
package org.hibernate.type.descriptor.java.internal;

import java.util.Collection;

import org.hibernate.collection.spi.PersistentCollectionTuplizer;

/**
 * @author Andrea Boriero
 */
public class CollectionJavaDescriptor extends AbstractCollectionJavaDescriptor<Collection> {
	public CollectionJavaDescriptor(PersistentCollectionTuplizer tuplizer) {
		super( Collection.class, tuplizer );
	}
}
