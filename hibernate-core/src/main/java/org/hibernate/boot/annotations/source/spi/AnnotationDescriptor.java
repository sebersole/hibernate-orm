/*
 * Hibernate, Relational Persistence for Idiomatic Java
 *
 * License: GNU Lesser General Public License (LGPL), version 2.1 or later.
 * See the lgpl.txt file in the root directory or http://www.gnu.org/licenses/lgpl-2.1.html.
 */
package org.hibernate.boot.annotations.source.spi;

import java.lang.annotation.Annotation;
import java.util.List;

/**
 * Describes an annotation type (the Class)
 *
 * @author Steve Ebersole
 */
public interface AnnotationDescriptor<A extends Annotation> extends AnnotationTarget {
	@Override
	default Kind getKind() {
		return Kind.ANNOTATION;
	}

	/**
	 * The annotation type
	 */
	Class<? extends Annotation> getAnnotationType();

	/**
	 * Descriptors for the attributes of the annotation
	 */
	List<AttributeDescriptor<?>> getAttributes();

	/**
	 * Get the attribute descriptor for the named attribute
	 */
	<X> AttributeDescriptor<X> getAttribute(String name);

	/**
	 * Shorthand for {@code getAttribute("value")}
	 */
	default <X> AttributeDescriptor<X> getValueAttribute() {
		return getAttribute( "value" );
	}

	/**
	 * If the described annotation is repeatable, returns the descriptor
	 * for the annotation which is the repeatable container
	 */
	AnnotationDescriptor<?> getRepeatableContainer();

	/**
	 * Describes an attribute of the annotation
	 */
	interface AttributeDescriptor<T> {
		/**
		 * The name of the attribute.
		 */
		String getAttributeName();

		/**
		 * The {@linkplain Class Java type} of the attribute
		 */
		Class<T> getAttributeType();

		/**
		 * The default value for this annotation
		 */
		T getAttributeDefault();

		/**
		 * Extract the value for the described attribute from
		 * an instance of the containing annotation
		 */
		T extractRawValue(Annotation annotation);
	}
}
