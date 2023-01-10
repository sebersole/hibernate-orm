/*
 * Hibernate, Relational Persistence for Idiomatic Java
 *
 * License: GNU Lesser General Public License (LGPL), version 2.1 or later.
 * See the lgpl.txt file in the root directory or http://www.gnu.org/licenses/lgpl-2.1.html.
 */
package org.hibernate.boot.annotations.internal;

import java.lang.annotation.Annotation;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.List;
import java.util.function.Function;

import org.hibernate.Internal;
import org.hibernate.boot.annotations.spi.AnnotationDescriptor;
import org.hibernate.boot.annotations.spi.HibernateAnnotations;
import org.hibernate.boot.annotations.spi.JpaAnnotations;

/**
 * Builders for AnnotationDescriptor instances.
 *
 * @see HibernateAnnotations
 * @see JpaAnnotations
 *
 * @author Steve Ebersole
 */
public class AnnotationDescriptorBuilder {
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

		return new OrmAnnotationDescriptorImpl<>(
				javaType,
				extractAttributeDescriptors( javaType ),
				repeatableContainer
		);
	}

	@Internal
	public static List<OrmAnnotationDescriptorImpl.AttributeDescriptor<?>> extractAttributeDescriptors(Class<? extends Annotation> javaType) {
		final Method[] attributes = javaType.getDeclaredMethods();
		final List<OrmAnnotationDescriptorImpl.AttributeDescriptor<?>> attributeDescriptors = new ArrayList<>();

		for ( int i = 0; i < attributes.length; i++ ) {
			attributeDescriptors.add( new AttributeDescriptorImpl<>( attributes[i] ) );
		}

		return attributeDescriptors;
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
}
