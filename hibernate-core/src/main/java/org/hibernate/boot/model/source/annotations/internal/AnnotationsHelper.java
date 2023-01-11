/*
 * Hibernate, Relational Persistence for Idiomatic Java
 *
 * License: GNU Lesser General Public License (LGPL), version 2.1 or later.
 * See the lgpl.txt file in the root directory or http://www.gnu.org/licenses/lgpl-2.1.html.
 */
package org.hibernate.boot.model.source.annotations.internal;

import java.lang.annotation.Annotation;
import java.util.Collections;
import java.util.List;

import org.hibernate.boot.annotations.spi.AnnotationDescriptor;
import org.hibernate.boot.annotations.spi.AnnotationDescriptorRegistry;
import org.hibernate.boot.annotations.spi.AnnotationUsage;

/**
 * @author Steve Ebersole
 */
public class AnnotationsHelper {
	public static List<AnnotationUsage<?>> resolveRepeatable(
			AnnotationUsage<?> annotationUsage,
			AnnotationDescriptorRegistry annotationDescriptorRegistry) {
		final AnnotationDescriptor<?> annotationDescriptor = annotationUsage.getAnnotationDescriptor();
		final Class<? extends Annotation> annotationJavaType = annotationDescriptor.getAnnotationType();

		if ( annotationDescriptor.getRepeatableContainer() != null ) {
			// The annotation is repeatable.  Since Java does not allow the repeatable and container
			// to exist on the same target, this means that this usage is the only effective
			// one for its descriptor
			return Collections.singletonList( annotationUsage );
		}
		else {
			final AnnotationDescriptor<?> repeatableDescriptor = annotationDescriptorRegistry.getRepeatableDescriptor( annotationJavaType );
			if ( repeatableDescriptor != null ) {
				// The usage type is a repeatable container.  Flatten its contained annotations
				final AnnotationUsage.AttributeValue value = annotationUsage.getAttributeValue( "value" );
				return value.getValue();
			}
			else {
				return Collections.singletonList( annotationUsage );
			}
		}
	}

	private AnnotationsHelper() {
		// disallow direct instantiation
	}
}
