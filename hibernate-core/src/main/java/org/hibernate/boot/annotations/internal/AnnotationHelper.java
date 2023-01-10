/*
 * Hibernate, Relational Persistence for Idiomatic Java
 *
 * License: GNU Lesser General Public License (LGPL), version 2.1 or later.
 * See the lgpl.txt file in the root directory or http://www.gnu.org/licenses/lgpl-2.1.html.
 */
package org.hibernate.boot.annotations.internal;

import java.lang.annotation.Annotation;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.function.Consumer;

import org.hibernate.boot.annotations.AnnotationAccessException;
import org.hibernate.boot.annotations.spi.AnnotationDescriptor;
import org.hibernate.boot.annotations.spi.AnnotationDescriptor.AttributeDescriptor;
import org.hibernate.boot.annotations.spi.AnnotationUsage.AttributeValue;
import org.hibernate.internal.util.collections.CollectionHelper;

/**
 * Collection of helper functions related to annotation handling
 *
 * @author Steve Ebersole
 */
public class AnnotationHelper {
	private AnnotationHelper() {
		// disallow direct instantiation
	}

	/**
	 * Call the passed {@code consumer} if the {@code attributeValue} is specified
	 */
	public static <T> void ifSpecified(AttributeValue attributeValue, Consumer<T> consumer) {
		if ( attributeValue == null ) {
			return;
		}
		consumer.accept( attributeValue.getValue() );
	}

	/**
	 * Call the passed {@code consumer} if the {@code attributeValue} is specified and
	 * is not a default value
	 */
	public static <T> void ifNotDefault(AttributeValue attributeValue, Consumer<T> consumer) {
		if ( attributeValue == null ) {
			return;
		}
		if ( attributeValue.isDefaultValue() ) {
			return;
		}
		consumer.accept( attributeValue.getValue() );
	}

	/**
	 * Return {@code null} if the {@code attributeValue} is not specified,
	 * or is the default value
	 */
	public static <T> T nullIfUnspecified(AttributeValue attributeValue) {
		if ( attributeValue == null ) {
			return null;
		}
		if ( attributeValue.isDefaultValue() ) {
			return null;
		}
		return attributeValue.getValue();
	}

	public static <T> void ifSpecified(T value, Consumer<T> consumer) {
		if ( value == null ) {
			return;
		}
		consumer.accept( value );
	}

	/**
	 * Given an {@code annotation} (and its {@linkplain OrmAnnotationDescriptorImpl descriptor}),
	 * extract the {@link AttributeValue attribute values}.
	 */
	public static <A extends Annotation> Map<String, AttributeValue> extractAttributeValues(
			A annotation,
			AnnotationDescriptor<A> annotationDescriptor) {
		if ( CollectionHelper.isEmpty( annotationDescriptor.getAttributes() ) ) {
			return Collections.emptyMap();
		}

		if ( annotationDescriptor.getAttributes().size() == 1 ) {
			final AttributeDescriptor<?> attributeDescriptor = annotationDescriptor.getAttributes().get( 0 );
			return Collections.singletonMap(
					attributeDescriptor.getAttributeName(),
					new AttributeValueImpl( attributeDescriptor, extractAttributeValue( annotation, attributeDescriptor ) )
			);
		}

		final Map<String, AttributeValue> valueMap = new HashMap<>();
		for ( int i = 0; i < annotationDescriptor.getAttributes().size(); i++ ) {
			final AttributeDescriptor<?> attributeDescriptor = annotationDescriptor.getAttributes().get( i );
			valueMap.put(
					attributeDescriptor.getAttributeName(),
					new AttributeValueImpl( attributeDescriptor, extractAttributeValue( annotation, attributeDescriptor ) )
			);
		}
		return valueMap;
	}

	/**
	 *
	 * @param container
	 * @return
	 * @param <A>
	 * @param <C>
	 */
	public static <A extends Annotation, C extends Annotation> List<A> extractRepeated(C container) {
		return extractAttributeValue( container, "value" );
	}

	public static <A extends Annotation, T> T extractAttributeValue(A annotation, AttributeDescriptor<T> attributeDescriptor) {
		return extractAttributeValue( annotation, attributeDescriptor.getAttributeName() );
	}

	public static <A extends Annotation, T> T extractAttributeValue(A annotation, String attributeName) {
		try {
			final Method method = annotation.getClass().getDeclaredMethod( attributeName );
			//noinspection unchecked
			return (T) method.invoke( annotation );
		}
		catch (NoSuchMethodException | InvocationTargetException | IllegalAccessException e) {
			throw new AnnotationAccessException(
					String.format(
							Locale.ROOT,
							"Unable to extract attribute value : %s.%s",
							annotation.annotationType().getName(),
							attributeName
					),
					e
			);
		}
	}

}
