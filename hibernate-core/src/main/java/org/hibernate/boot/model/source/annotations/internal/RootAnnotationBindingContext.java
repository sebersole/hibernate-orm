/*
 * Hibernate, Relational Persistence for Idiomatic Java
 *
 * License: GNU Lesser General Public License (LGPL), version 2.1 or later.
 * See the lgpl.txt file in the root directory or http://www.gnu.org/licenses/lgpl-2.1.html.
 */
package org.hibernate.boot.model.source.annotations.internal;

import org.hibernate.boot.internal.DelegatingMetadataBuildingContext;
import org.hibernate.boot.model.source.annotations.spi.AnnotationBindingContext;
import org.hibernate.boot.model.source.annotations.spi.AnnotationDescriptorXref;
import org.hibernate.boot.model.source.annotations.spi.HibernateAnnotations;
import org.hibernate.boot.model.source.annotations.spi.JpaAnnotations;
import org.hibernate.boot.spi.MetadataBuildingContext;

/**
 * @author Steve Ebersole
 */
public class RootAnnotationBindingContext
		extends DelegatingMetadataBuildingContext
		implements AnnotationBindingContext {
	private final AnnotationDescriptorXref annotationDescriptorXref;

	public RootAnnotationBindingContext(MetadataBuildingContext delegate) {
		super( delegate );

		annotationDescriptorXref = new AnnotationDescriptorXref();
		JpaAnnotations.forEachAnnotation( annotationDescriptorXref::register );
		HibernateAnnotations.forEachAnnotation( annotationDescriptorXref::register );
	}

	@Override
	public AnnotationDescriptorXref getAnnotationDescriptorXref() {
		return annotationDescriptorXref;
	}
}
