/*
 * Hibernate, Relational Persistence for Idiomatic Java
 *
 * License: GNU Lesser General Public License (LGPL), version 2.1 or later.
 * See the lgpl.txt file in the root directory or http://www.gnu.org/licenses/lgpl-2.1.html.
 */
package org.hibernate.boot.annotations.type.internal;

import org.hibernate.boot.annotations.source.spi.ManagedClass;
import org.hibernate.boot.annotations.spi.AnnotationBindingContext;
import org.hibernate.boot.annotations.type.spi.MappedSuperclassTypeMetadata;

import jakarta.persistence.AccessType;

/**
 * @author Steve Ebersole
 */
public class MappedSuperclassTypeMetadataImpl
		extends AbstractIdentifiableTypeMetadata
		implements MappedSuperclassTypeMetadata {

	public MappedSuperclassTypeMetadataImpl(
			ManagedClass managedClass,
			AbstractIdentifiableTypeMetadata superType,
			AccessType defaultAccessType,
			AnnotationBindingContext bindingContext) {
		super( managedClass, superType, defaultAccessType, bindingContext );
	}
}
