/*
 * Hibernate, Relational Persistence for Idiomatic Java
 *
 * License: GNU Lesser General Public License (LGPL), version 2.1 or later.
 * See the lgpl.txt file in the root directory or http://www.gnu.org/licenses/lgpl-2.1.html.
 */
package org.hibernate.boot.annotations.internal;

import java.lang.annotation.Annotation;
import java.util.List;

import org.hibernate.boot.annotations.AnnotationAccessException;
import org.hibernate.boot.annotations.spi.AnnotationDescriptor;
import org.hibernate.boot.annotations.spi.AnnotationDescriptorRegistry;
import org.hibernate.boot.annotations.spi.AnnotationTarget;
import org.hibernate.boot.annotations.spi.HibernateAnnotations;
import org.hibernate.boot.annotations.spi.JpaAnnotations;

/**
 * AnnotationDescriptor used for annotations other than {@linkplain JpaAnnotations JPA}
 * and {@linkplain HibernateAnnotations Hibernate} annotations.
 * <p/>
 * Processes the annotations associated with the {@linkplain #getAnnotationType() annotation class}
 * and makes them available via its {@link AnnotationTarget} implementation.
 *
 * @author Steve Ebersole
 */
public class AnnotationDescriptorImpl<A extends Annotation>
		extends AbstractAnnotationTarget
		implements AnnotationDescriptor<A> {
	private final Class<A> annotationType;
	private final List<AttributeDescriptor<?>> attributeDescriptors;
	private final AnnotationDescriptor<?> repeatableContainer;

	public AnnotationDescriptorImpl(
			Class<A> annotationType,
			AnnotationDescriptor<?> repeatableContainer,
			AnnotationDescriptorRegistry annotationDescriptorRegistry) {
		super( annotationType.getAnnotations(), annotationDescriptorRegistry );
		this.annotationType = annotationType;
		this.repeatableContainer = repeatableContainer;

		this.attributeDescriptors = AnnotationDescriptorBuilder.extractAttributeDescriptors( annotationType );
	}

	@Override
	public Class<? extends Annotation> getAnnotationType() {
		return annotationType;
	}

	@Override
	public List<AttributeDescriptor<?>> getAttributes() {
		return attributeDescriptors;
	}

	@Override
	public AnnotationDescriptor<?> getRepeatableContainer() {
		return repeatableContainer;
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
}
