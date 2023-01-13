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
import java.util.Objects;

import org.hibernate.boot.annotations.AnnotationAccessException;
import org.hibernate.boot.annotations.source.spi.AnnotationDescriptor;
import org.hibernate.boot.annotations.source.spi.AnnotationTarget;
import org.hibernate.boot.annotations.source.spi.AnnotationUsage;
import org.hibernate.boot.annotations.source.spi.HibernateAnnotations;
import org.hibernate.boot.annotations.source.spi.JpaAnnotations;
import org.hibernate.internal.util.IndexedConsumer;

/**
 * Used to represent the {@linkplain JpaAnnotations JPA} and
 * {@linkplain HibernateAnnotations Hibernate} annotations.
 *
 * @implNote We never care about annotations defined on those annotation classes;
 * the {@link AnnotationTarget} contract here behaves as if no annotations where found.
 *
 * @see AnnotationUsage
 * @see AnnotationDescriptorImpl
 *
 * @author Steve Ebersole
 */
public class OrmAnnotationDescriptorImpl<T extends Annotation> implements AnnotationDescriptor<T> {
	private final Class<T> annotationType;
	private final List<AttributeDescriptor<?>> attributeDescriptors;
	private final AnnotationDescriptor<?> repeatableContainer;

	public OrmAnnotationDescriptorImpl(
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
	@Override
	public Class<? extends Annotation> getAnnotationType() {
		return annotationType;
	}

	/**
	 * Descriptors for the attributes of this annotation
	 */
	@Override
	public List<AttributeDescriptor<?>> getAttributes() {
		return attributeDescriptors;
	}

	@Override
	public <X> AttributeDescriptor<X> getAttribute(String name) {
		for ( int i = 0; i < attributeDescriptors.size(); i++ ) {
			final AttributeDescriptor<?> attributeDescriptor = attributeDescriptors.get( i );
			if ( attributeDescriptor.getAttributeName().equals( name ) ) {
				//noinspection unchecked
				return (AttributeDescriptor<X>) attributeDescriptor;
			}
		}
		throw new AnnotationAccessException( "No such attribute : " + annotationType.getName() + "." + name );
	}

	/**
	 * If the annotation is {@linkplain java.lang.annotation.Repeatable repeatable},
	 * returns the descriptor for the container annotation
	 */
	@Override
	public AnnotationDescriptor<?> getRepeatableContainer() {
		return repeatableContainer;
	}

	@Override
	public <A extends Annotation> List<AnnotationUsage<A>> getAnnotations(AnnotationDescriptor<A> type) {
		// there are none
		return Collections.emptyList();
	}

	@Override
	public <A extends Annotation> void forEachAnnotation(AnnotationDescriptor<A> type, IndexedConsumer<AnnotationUsage<A>> consumer) {
		// there are none
	}

	@Override
	public <A extends Annotation> AnnotationUsage<A> getAnnotation(AnnotationDescriptor<A> type) {
		// there are none
		return null;
	}

	@Override
	public <A extends Annotation> AnnotationUsage<A> getNamedAnnotation(AnnotationDescriptor<A> type, String name, String attributeName) {
		// there are none
		return null;
	}

	@Override
	public boolean equals(Object o) {
		if ( this == o ) {
			return true;
		}
		if ( o == null || getClass() != o.getClass() ) {
			return false;
		}
		OrmAnnotationDescriptorImpl<?> that = (OrmAnnotationDescriptorImpl<?>) o;
		return annotationType.equals( that.annotationType );
	}

	@Override
	public int hashCode() {
		return Objects.hash( annotationType );
	}
}
