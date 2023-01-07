/*
 * Hibernate, Relational Persistence for Idiomatic Java
 *
 * License: GNU Lesser General Public License (LGPL), version 2.1 or later.
 * See the lgpl.txt file in the root directory or http://www.gnu.org/licenses/lgpl-2.1.html.
 */
package org.hibernate.boot.model.source.annotations.spi;

import java.lang.annotation.Annotation;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.List;
import java.util.function.Function;

import org.hibernate.Internal;

/**
 * Describes an annotation (Class)
 *
 * @author Steve Ebersole
 */
public class AnnotationDescriptor<T extends Annotation> {
	private final Class<T> annotationType;
	private final List<AttributeDescriptor<?>> attributeDescriptors;
	private final AnnotationDescriptor<?> repeatableContainer;

	public AnnotationDescriptor(
			Class<T> annotationType,
			List<AttributeDescriptor<?>> attributeDescriptors,
			AnnotationDescriptor<?> repeatableContainer) {
		this.annotationType = annotationType;
		this.attributeDescriptors = attributeDescriptors;
		this.repeatableContainer = repeatableContainer;
	}

	/**
	 * The {@linkplain Class Java type} of the annotation.
	 */
	Class<? extends Annotation> getAnnotationType() {
		return annotationType;
	}

	/**
	 * Descriptors for the attributes of this annotation
	 */
	public List<AttributeDescriptor<?>> getAttributeDescriptors() {
		return attributeDescriptors;
	}

	/**
	 * If the annotation is {@linkplain java.lang.annotation.Repeatable repeatable},
	 * returns the descriptor for the container annotation
	 */
	AnnotationDescriptor<?> getRepeatableContainer() {
		return repeatableContainer;
	}

	/**
	 * Describes an attribute of the annotation
	 */
	public static class AttributeDescriptor<T> {
		private final String name;
		private final Class<T> type;
		private final T defaultValue;

		public AttributeDescriptor(String name, Class<T> type, T defaultValue) {
			this.name = name;
			this.type = type;
			this.defaultValue = defaultValue;
		}

		/**
		 * The name of the attribute.
		 */
		public String getAttributeName() {
			return name;
		}

		/**
		 * The {@linkplain Class Java type} of the attribute
		 */
		public Class<T> getAttributeType() {
			return type;
		}

		/**
		 * The default value for this annotation
		 */
		public T getAttributeDefault() {
			return defaultValue;
		}
	}

	public static class StandardDefaultValueResolver implements Function<Method,Object> {
		/**
		 * Singleton access
		 */
		public static final StandardDefaultValueResolver INSTANCE = new StandardDefaultValueResolver();

		@Override
		public Object apply(Method method) {
			return method.getDefaultValue();
		}
	}

	@Internal
	public static <A extends Annotation> AnnotationDescriptor<A> createDetails(Class<A> javaType) {
		return createDetails( javaType, StandardDefaultValueResolver.INSTANCE );
	}

	@Internal
	public static <A extends Annotation> AnnotationDescriptor<A> createDetails(
			Class<A> javaType,
			Function<Method,Object> defaultValueResolver) {
		return createDetails( javaType, defaultValueResolver, null );
	}

	@Internal
	public static <A extends Annotation> AnnotationDescriptor<A> createDetails(
			Class<A> javaType,
			AnnotationDescriptor<?> repeatableContainer) {
		return createDetails( javaType, StandardDefaultValueResolver.INSTANCE, repeatableContainer );
	}

	@Internal
	public static <A extends Annotation> AnnotationDescriptor<A> createDetails(
			Class<A> javaType,
			Function<Method,Object> defaultValueResolver,
			AnnotationDescriptor<?> repeatableContainer) {
		assert javaType != null;
		assert defaultValueResolver != null;

		return new AnnotationDescriptor<>(
				javaType,
				extractAttributeDescriptors( javaType, defaultValueResolver ),
				repeatableContainer
		);
	}

	@Internal
	public static List<AttributeDescriptor<?>> extractAttributeDescriptors(
			Class<? extends Annotation> javaType,
			Function<Method,Object> defaultValueResolver) {
		final Method[] attributes = javaType.getDeclaredMethods();
		final List<AttributeDescriptor<?>> attributeDescriptors = new ArrayList<>();

		for ( int i = 0; i < attributes.length; i++ ) {
			//noinspection unchecked,rawtypes
			attributeDescriptors.add(
					new AttributeDescriptor(
							attributes[i].getName(),
							attributes[i].getReturnType(),
							defaultValueResolver.apply( attributes[i] )
					)
			);
		}

		return attributeDescriptors;
	}

}
