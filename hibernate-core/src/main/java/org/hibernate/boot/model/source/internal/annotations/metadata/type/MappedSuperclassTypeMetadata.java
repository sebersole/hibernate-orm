/*
 * Hibernate, Relational Persistence for Idiomatic Java
 *
 * License: GNU Lesser General Public License (LGPL), version 2.1 or later.
 * See the lgpl.txt file in the root directory or <http://www.gnu.org/licenses/lgpl-2.1.html>.
 */
package org.hibernate.boot.model.source.internal.annotations.metadata.type;

import javax.persistence.AccessType;

import org.hibernate.boot.model.process.jandex.spi.HibernateDotNames;
import org.hibernate.boot.model.source.internal.annotations.RootAnnotationBindingContext;

import org.jboss.jandex.ClassInfo;

/**
 * Represents the information about an entity annotated with {@code @MappedSuperclass}.
 *
 * @author Hardy Ferentschik
 * @author Steve Ebersole
 */
public class MappedSuperclassTypeMetadata extends IdentifiableTypeMetadata {
	/**
	 * This form is intended for cases where the MappedSuperclasses is part
	 * of the root super tree.
	 *
	 * @param classInfo The descriptor for the type annotated as MappedSuperclass
	 * @param defaultAccessType The default AccessType for he hierarchy
	 * @param context The binding context
	 */
	public MappedSuperclassTypeMetadata(
			ClassInfo classInfo,
			AccessType defaultAccessType,
			RootAnnotationBindingContext context) {
		super( classInfo, defaultAccessType, false, context );
	}

	/**
	 * This form is intended for cases where the MappedSuperclasses is part
	 * of the root subclass tree.
	 *
	 * @param classInfo The descriptor for the type annotated as MappedSuperclass
	 * @param superType The metadata representing the super type
	 * @param defaultAccessType The default AccessType for he hierarchy
	 * @param context The binding context
	 */
	public MappedSuperclassTypeMetadata(
			ClassInfo classInfo,
			IdentifiableTypeMetadata superType,
			AccessType defaultAccessType,
			RootAnnotationBindingContext context) {
		super( classInfo, superType, defaultAccessType, context );
	}

	@Override
	public boolean hasMultiTenancySourceInformation() {
		return hasMultiTenancySourceInformation( this );
	}

	private static boolean hasMultiTenancySourceInformation(IdentifiableTypeMetadata typeMetadata) {
		final boolean hasLocally = typeMetadata.typeAnnotationMap().containsKey( HibernateDotNames.MULTI_TENANT )
				|| typeMetadata.typeAnnotationMap().containsKey( HibernateDotNames.TENANT_COLUMN )
				|| typeMetadata.typeAnnotationMap().containsKey( HibernateDotNames.TENANT_FORMULA );
		if ( hasLocally ) {
			return true;
		}

		if ( typeMetadata.getSuperType() != null ) {
			return hasMultiTenancySourceInformation( typeMetadata.getSuperType() );
		}

		return false;
	}
}


