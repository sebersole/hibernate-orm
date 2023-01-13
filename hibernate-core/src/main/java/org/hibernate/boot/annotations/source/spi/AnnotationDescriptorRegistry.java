/*
 * Hibernate, Relational Persistence for Idiomatic Java
 *
 * License: GNU Lesser General Public License (LGPL), version 2.1 or later.
 * See the lgpl.txt file in the root directory or http://www.gnu.org/licenses/lgpl-2.1.html.
 */
package org.hibernate.boot.annotations.source.spi;

import java.lang.annotation.Annotation;
import java.lang.annotation.Repeatable;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import org.hibernate.boot.annotations.source.internal.AnnotationDescriptorImpl;
import org.hibernate.boot.annotations.source.internal.OrmAnnotationDescriptorImpl;

import jakarta.persistence.NamedQueries;
import jakarta.persistence.NamedQuery;

/**
 * Access to AnnotationDescriptor instances based on a number of look-ups
 *
 * @author Steve Ebersole
 */
public class AnnotationDescriptorRegistry {
	private final AnnotationProcessingContext context;

	private final Map<Class<? extends Annotation>, AnnotationDescriptor<?>> descriptorMap = new ConcurrentHashMap<>();
	private final Map<Class<? extends Annotation>, AnnotationDescriptor<?>> repeatableDescriptorMap = new ConcurrentHashMap<>();

	public AnnotationDescriptorRegistry(AnnotationProcessingContext context) {
		this.context = context;

		JpaAnnotations.forEachAnnotation( this::register );
		HibernateAnnotations.forEachAnnotation( this::register );
	}

	private void register(AnnotationDescriptor<?> descriptor) {
		descriptorMap.put( descriptor.getAnnotationType(), descriptor );
		if ( descriptor.getRepeatableContainer() != null ) {
			// the descriptor is repeatable - register it under its container
			repeatableDescriptorMap.put( descriptor.getRepeatableContainer().getAnnotationType(), descriptor );
		}
	}

	/**
	 * For the given annotation type, get the corresponding
	 * {@linkplain OrmAnnotationDescriptorImpl descriptor}
	 */
	public <A extends Annotation> AnnotationDescriptor<A> getDescriptor(Class<A> javaType) {
		//noinspection unchecked
		final AnnotationDescriptor<A> existing = (AnnotationDescriptor<A>) descriptorMap.get( javaType );
		if ( existing != null ) {
			return existing;
		}

		// indicates a non-JPA and non-Hibernate annotation.  we need to track
		// these for meta-annotation handling later.
		// todo (annotation-source) : should we limit this to just JPA and Hibernate annotations?
		final AnnotationDescriptor<A> created = createAdHocAnnotationDescriptor( javaType );
		descriptorMap.put( javaType, created );
		return created;
	}

	private <A extends Annotation> AnnotationDescriptor<A> createAdHocAnnotationDescriptor(Class<A> javaType) {
		final Repeatable repeatable = javaType.getAnnotation( Repeatable.class );
		final AnnotationDescriptor<? extends Annotation> containerDescriptor;
		if ( repeatable != null ) {
			containerDescriptor = getDescriptor( repeatable.value() );
			assert containerDescriptor != null;
		}
		else {
			containerDescriptor = null;
		}

		return new AnnotationDescriptorImpl<>( javaType, containerDescriptor, context );
	}

	/**
	 * For the given annotation type, which is the container for repeatable
	 * annotations, get the descriptor for the repeatable annotation.
	 * <p/>
	 * E.g., given {@link NamedQuery} and {@link NamedQueries} passing in
	 * {@code NamedQueries.class} would return the descriptor for {@code NamedQuery}.
	 */
	public <A extends Annotation> AnnotationDescriptor<A> getRepeatableDescriptor(Class<A> javaType) {
		//noinspection unchecked
		return (AnnotationDescriptor<A>) repeatableDescriptorMap.get( javaType );
	}
}
