/*
 * Hibernate, Relational Persistence for Idiomatic Java
 *
 * License: GNU Lesser General Public License (LGPL), version 2.1 or later.
 * See the lgpl.txt file in the root directory or http://www.gnu.org/licenses/lgpl-2.1.html.
 */
package org.hibernate.boot.annotations.model.internal;

import java.util.List;

import org.hibernate.boot.annotations.model.spi.AttributeMetadata;
import org.hibernate.boot.annotations.model.spi.EntityHierarchy;
import org.hibernate.boot.annotations.model.spi.MappedSuperclassTypeMetadata;
import org.hibernate.boot.annotations.source.spi.ClassDetails;
import org.hibernate.boot.annotations.spi.AnnotationProcessingContext;

import jakarta.persistence.AccessType;

/**
 * @author Steve Ebersole
 */
public class MappedSuperclassTypeMetadataImpl
		extends AbstractIdentifiableTypeMetadata
		implements MappedSuperclassTypeMetadata {

	private final List<AttributeMetadata> attributeList;

	public MappedSuperclassTypeMetadataImpl(
			ClassDetails classDetails,
			EntityHierarchy hierarchy,
			AccessType defaultAccessType,
			AnnotationProcessingContext processingContext) {
		super( classDetails, hierarchy, false, defaultAccessType, processingContext );

		this.attributeList = resolveAttributes();
	}

	public MappedSuperclassTypeMetadataImpl(
			ClassDetails classDetails,
			EntityHierarchy hierarchy,
			AbstractIdentifiableTypeMetadata superType,
			AnnotationProcessingContext processingContext) {
		super( classDetails, hierarchy, superType, processingContext );

		this.attributeList = resolveAttributes();
	}

	@Override
	protected List<AttributeMetadata> attributeList() {
		return attributeList;
	}
}
