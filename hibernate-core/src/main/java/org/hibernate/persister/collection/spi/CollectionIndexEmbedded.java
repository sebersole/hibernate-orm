/*
 * Hibernate, Relational Persistence for Idiomatic Java
 *
 * License: GNU Lesser General Public License (LGPL), version 2.1 or later
 * See the lgpl.txt file in the root directory or http://www.gnu.org/licenses/lgpl-2.1.html
 */
package org.hibernate.persister.collection.spi;

import org.hibernate.persister.embedded.spi.EmbeddedValuedNavigable;

/**
 * @author Steve Ebersole
 */
public interface CollectionIndexEmbedded<J> extends CollectionIndex<J>, EmbeddedValuedNavigable<J> {
	@Override
	default IndexClassification getClassification() {
		return IndexClassification.EMBEDDABLE;
	}
}
