/*
 * Hibernate, Relational Persistence for Idiomatic Java
 *
 * License: GNU Lesser General Public License (LGPL), version 2.1 or later.
 * See the lgpl.txt file in the root directory or <http://www.gnu.org/licenses/lgpl-2.1.html>.
 */
package org.hibernate.boot.model.source.internal.annotations;

import org.hibernate.boot.model.source.internal.annotations.metadata.type.EntityTypeMetadata;
import org.hibernate.boot.model.source.spi.SubclassEntitySource;

/**
 * @author Hardy Ferentschik
 * @author Strong Liu
 * @author Steve Ebersole
 */
public class SubclassEntitySourceImpl extends EntitySourceImpl implements SubclassEntitySource {
	public SubclassEntitySourceImpl(
			EntityTypeMetadata metadata,
			EntityHierarchySourceImpl hierarchy,
			IdentifiableTypeSourceAdapter superTypeSource) {
		super( metadata, hierarchy, superTypeSource );
	}
}


