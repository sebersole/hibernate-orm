/*
 * Hibernate, Relational Persistence for Idiomatic Java
 *
 * License: GNU Lesser General Public License (LGPL), version 2.1 or later.
 * See the lgpl.txt file in the root directory or http://www.gnu.org/licenses/lgpl-2.1.html.
 */
package org.hibernate.boot.model.source.annotations.internal;

import org.hibernate.boot.annotations.spi.AnnotationProcessingContext;
import org.hibernate.boot.internal.DelegatingMetadataBuildingContext;
import org.hibernate.boot.model.source.annotations.spi.AnnotationBindingContext;
import org.hibernate.boot.spi.MetadataBuildingContext;

/**
 * @author Steve Ebersole
 */
public class RootAnnotationBindingContext
		extends DelegatingMetadataBuildingContext
		implements AnnotationBindingContext {
	private final AnnotationProcessingContext processingContext;

	public RootAnnotationBindingContext(AnnotationProcessingContext processingContext, MetadataBuildingContext delegate) {
		super( delegate );
		this.processingContext = processingContext;
	}

	@Override
	public AnnotationProcessingContext getAnnotationProcessingContext() {
		return processingContext;
	}
}
