/*
 * Hibernate, Relational Persistence for Idiomatic Java
 *
 * License: GNU Lesser General Public License (LGPL), version 2.1 or later.
 * See the lgpl.txt file in the root directory or http://www.gnu.org/licenses/lgpl-2.1.html.
 */
package org.hibernate.boot.model.source.annotations.spi;

import java.lang.annotation.Annotation;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import org.hibernate.Internal;

import jakarta.persistence.NamedQueries;
import jakarta.persistence.NamedQuery;

/**
 * @author Steve Ebersole
 */
public class AnnotationDescriptorXref {
	private final Map<Class<? extends Annotation>, AnnotationDescriptor<?>> descriptorMap = new ConcurrentHashMap<>();
	private final Map<Class<? extends Annotation>, AnnotationDescriptor<?>> repeatableDescriptorMap = new ConcurrentHashMap<>();

	@Internal
	public void register(AnnotationDescriptor<?> descriptor) {
		descriptorMap.put( descriptor.getAnnotationType(), descriptor );
		if ( descriptor.getRepeatableContainer() != null ) {
			repeatableDescriptorMap.put( descriptor.getRepeatableContainer().getAnnotationType(), descriptor );
		}
	}

	public <A extends Annotation> AnnotationDescriptor<A> getDescriptor(Class<A> javaType) {
		//noinspection unchecked
		return (AnnotationDescriptor<A>) descriptorMap.get( javaType );
	}

	/**
	 * For the given annotation type which is the container for repeatable
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
