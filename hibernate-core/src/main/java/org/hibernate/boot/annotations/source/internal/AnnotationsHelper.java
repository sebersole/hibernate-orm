/*
 * Hibernate, Relational Persistence for Idiomatic Java
 *
 * License: GNU Lesser General Public License (LGPL), version 2.1 or later.
 * See the lgpl.txt file in the root directory or http://www.gnu.org/licenses/lgpl-2.1.html.
 */
package org.hibernate.boot.annotations.source.internal;

import java.lang.annotation.Annotation;
import java.util.Collections;
import java.util.List;
import java.util.function.Supplier;

import org.hibernate.boot.annotations.model.spi.EntityTypeMetadata;
import org.hibernate.boot.annotations.model.spi.IdentifiableTypeMetadata;
import org.hibernate.boot.annotations.source.spi.AnnotationAttributeValue;
import org.hibernate.boot.annotations.source.spi.AnnotationDescriptor;
import org.hibernate.boot.annotations.source.spi.AnnotationDescriptorRegistry;
import org.hibernate.boot.annotations.source.spi.AnnotationUsage;

/**
 * @author Steve Ebersole
 */
public class AnnotationsHelper {
	/**
	 * Get the annotation attribute value.  Return {@code null} if the value is
	 * {@linkplain AnnotationAttributeValue#isDefaultValue() the default}.
	 */
	public static <T> T getValueOrNull(AnnotationAttributeValue attributeValue) {
		if ( attributeValue == null || attributeValue.isDefaultValue() ) {
			return null;
		}
		return attributeValue.getValue();
	}

	/**
	 * Get the annotation attribute value, or {@code null} if its value is
	 * {@linkplain AnnotationAttributeValue#isDefaultValue() the default}.
	 */
	public static <T> T getValueOrNull(AnnotationAttributeValue attributeValue, T defaultValue) {
		assert defaultValue != null;
		if ( attributeValue == null || defaultValue.equals( attributeValue.getValue() ) ) {
			return null;
		}
		return attributeValue.getValue();
	}

	public static <T> T getValue(AnnotationAttributeValue attributeValue, T defaultValue) {
		if ( attributeValue == null || attributeValue.isDefaultValue() ) {
			return defaultValue;
		}
		return attributeValue.getValue();
	}

	public static <T> T getValue(AnnotationAttributeValue attributeValue, Supplier<T> defaultValueSupplier) {
		if ( attributeValue == null || attributeValue.isDefaultValue() ) {
			return defaultValueSupplier.get();
		}
		return attributeValue.getValue();
	}

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
				final AnnotationAttributeValue value = annotationUsage.getAttributeValue( "value" );
				return value.getValue();
			}
			else {
				return Collections.singletonList( annotationUsage );
			}
		}
	}

	/**
	 * Look for the given annotation on the passed type as well as any of its super-types
	 */
	public static <A extends Annotation> AnnotationUsage<A> findInheritedAnnotation(
			IdentifiableTypeMetadata base,
			AnnotationDescriptor<A> annotationDescriptor) {
		final AnnotationUsage<A> annotation = base.getManagedClass().getAnnotation( annotationDescriptor );
		if ( annotation != null ) {
			return annotation;
		}

		if ( base.getSuperType() != null ) {
			return findInheritedAnnotation( base.getSuperType(), annotationDescriptor );
		}

		return null;
	}

	/**
	 * Same as {@link #findInheritedAnnotation}, expect stopping at an entity-type
	 * boundary.  It effectively searches the given type and all of its mapped-superclasses
	 */
	public static <A extends Annotation> AnnotationUsage<A> findSemiInheritedAnnotation(
			IdentifiableTypeMetadata base,
			AnnotationDescriptor<A> annotationDescriptor) {
		final AnnotationUsage<A> annotation = base.getManagedClass().getAnnotation( annotationDescriptor );
		if ( annotation != null ) {
			return annotation;
		}

		if ( base.getSuperType() != null ) {
			if ( ! (base.getSuperType() instanceof EntityTypeMetadata) ) {
				return findSemiInheritedAnnotation( base.getSuperType(), annotationDescriptor );
			}
		}

		return null;
	}

	private AnnotationsHelper() {
		// disallow direct instantiation
	}
}
