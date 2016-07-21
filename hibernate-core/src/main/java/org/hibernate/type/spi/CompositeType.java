/*
 * Hibernate, Relational Persistence for Idiomatic Java
 *
 * License: GNU Lesser General Public License (LGPL), version 2.1 or later.
 * See the lgpl.txt file in the root directory or <http://www.gnu.org/licenses/lgpl-2.1.html>.
 */
package org.hibernate.type.spi;

import org.hibernate.persister.embeddable.EmbeddablePersister;

/**
 * @author Steve Ebersole
 */
public interface CompositeType extends Type_2, org.hibernate.sqm.domain.EmbeddableType {
	EmbeddablePersister getEmbeddablePersister();
}
