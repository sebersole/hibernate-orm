/*
 * Hibernate, Relational Persistence for Idiomatic Java
 *
 * License: GNU Lesser General Public License (LGPL), version 2.1 or later.
 * See the lgpl.txt file in the root directory or <http://www.gnu.org/licenses/lgpl-2.1.html>.
 */
package org.hibernate.boot.model.source.internal.annotations;

import org.hibernate.boot.model.source.internal.annotations.metadata.type.MappedSuperclassTypeMetadata;
import org.hibernate.boot.model.source.spi.MappedSuperclassSource;

/**
 * Adapter for a MappedSuperclass
 *
 * @author Steve Ebersole
 */
public class MappedSuperclassSourceImpl extends IdentifiableTypeSourceAdapter implements MappedSuperclassSource {
	/**
	 * Form for use in creating MappedSuperclassSource that are the supers of the root entity
	 *
	 * @param mappedSuperclassTypeMetadata Metadata about the MappedSuperclass
	 * @param hierarchy The hierarchy
	 */
	protected MappedSuperclassSourceImpl(
			MappedSuperclassTypeMetadata mappedSuperclassTypeMetadata,
			EntityHierarchySourceImpl hierarchy) {
		// false here indicates that this is not the root entity of a hierarchy
		super( mappedSuperclassTypeMetadata, hierarchy, false );
	}

	/**
	 * Form for use in creating MappedSuperclassSource that are part of the subclass tree of the root entity
	 *
	 * @param mappedSuperclassTypeMetadata Metadata about the MappedSuperclass
	 * @param hierarchy The hierarchy
	 * @param superTypeSource The source object for the super type.
	 */
	protected MappedSuperclassSourceImpl(
			MappedSuperclassTypeMetadata mappedSuperclassTypeMetadata,
			EntityHierarchySourceImpl hierarchy,
			IdentifiableTypeSourceAdapter superTypeSource) {
		super( mappedSuperclassTypeMetadata, hierarchy, superTypeSource );
	}
}
