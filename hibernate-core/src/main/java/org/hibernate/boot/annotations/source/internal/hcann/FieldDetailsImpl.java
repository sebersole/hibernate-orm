/*
 * Hibernate, Relational Persistence for Idiomatic Java
 *
 * License: GNU Lesser General Public License (LGPL), version 2.1 or later.
 * See the lgpl.txt file in the root directory or http://www.gnu.org/licenses/lgpl-2.1.html.
 */
package org.hibernate.boot.annotations.source.internal.hcann;

import org.hibernate.annotations.common.reflection.XProperty;
import org.hibernate.boot.annotations.source.internal.LazyAnnotationTarget;
import org.hibernate.boot.annotations.source.spi.AnnotationProcessingContext;
import org.hibernate.boot.annotations.source.spi.FieldDetails;


/**
 * @author Steve Ebersole
 */
public class FieldDetailsImpl extends LazyAnnotationTarget implements FieldDetails {
	private final XProperty xProperty;

	public FieldDetailsImpl(XProperty xProperty, AnnotationProcessingContext processingContext) {
		super( xProperty::getAnnotations, processingContext );
		this.xProperty = xProperty;
	}

	@Override
	public String getName() {
		return xProperty.getName();
	}
}
