/*
 * Hibernate, Relational Persistence for Idiomatic Java
 *
 * License: GNU Lesser General Public License (LGPL), version 2.1 or later.
 * See the lgpl.txt file in the root directory or http://www.gnu.org/licenses/lgpl-2.1.html.
 */
package org.hibernate.boot.annotations.model.spi;

import java.lang.annotation.Annotation;
import java.util.Collection;
import java.util.List;
import java.util.function.Consumer;

import org.hibernate.boot.annotations.source.spi.AnnotationDescriptor;
import org.hibernate.boot.annotations.source.spi.AnnotationUsage;
import org.hibernate.boot.annotations.source.spi.ClassDetails;
import org.hibernate.internal.util.IndexedConsumer;

import jakarta.persistence.AccessType;

/**
 * Intermediate representation of a {@linkplain jakarta.persistence.metamodel.ManagedType managed type}
 *
 * @author Steve Ebersole
 */
public interface ManagedTypeMetadata {
	/**
	 * The underlying managed-class
	 */
	ClassDetails getManagedClass();

	AccessType getAccessType();

	/**
	 * AnnotationProcessingContext local to this type
	 */
	LocalAnnotationProcessingContext getLocalProcessingContext();

	/**
	 * Get the number of declared attributes
	 */
	int getNumberOfAttributes();

	/**
	 * Get the declared attributes
	 */
	Collection<AttributeMetadata> getAttributes();

	/**
	 * Visit each declared attributes
	 */
	void forEachAttribute(IndexedConsumer<AttributeMetadata> consumer);

	/**
	 * Find the usages of the given annotation type.
	 * <p/>
	 * Similar to {@link ClassDetails#getAnnotation} except here we search supertypes
	 *
	 * @see #getManagedClass()
	 * @see ClassDetails#getAnnotation
	 */
	<A extends Annotation> AnnotationUsage<A> findAnnotation(AnnotationDescriptor<A> type);

	/**
	 * Find the usages of the given annotation type.
	 * <p/>
	 * Similar to {@link ClassDetails#getAnnotations} except here we search supertypes
	 */
	<A extends Annotation> List<AnnotationUsage<A>> findAnnotations(AnnotationDescriptor<A> type);

	/**
	 * Visit each usage of the given annotation type.
	 * <p/>
	 * Similar to {@link ClassDetails#forEachAnnotation} except here we search supertypes
	 */
	<A extends Annotation> void forEachAnnotation(AnnotationDescriptor<A> type, Consumer<AnnotationUsage<A>> consumer);
}
