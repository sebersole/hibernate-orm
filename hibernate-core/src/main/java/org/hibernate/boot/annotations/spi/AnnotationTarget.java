/*
 * Hibernate, Relational Persistence for Idiomatic Java
 *
 * License: GNU Lesser General Public License (LGPL), version 2.1 or later.
 * See the lgpl.txt file in the root directory or http://www.gnu.org/licenses/lgpl-2.1.html.
 */
package org.hibernate.boot.annotations.spi;

import java.lang.annotation.Annotation;

import org.hibernate.internal.util.IndexedConsumer;

/**
 * A model part which can be the target of annotations.
 *
 * @apiNote We treat {@linkplain java.lang.annotation.Repeatable repeatable}
 * annotations as "collapsed" meaning that usages of its container are
 * collapsed into a collection of the repeatable annotations.  Accessing
 * container annotations via this contract is not supported.
 *
 * @author Steve Ebersole
 */
public interface AnnotationTarget {
	/**
	 * Get the use of the given annotation on this target.
	 * <p/>
	 * Calling this method with the container annotation for a
	 * {@linkplain java.lang.annotation.Repeatable repeatable} annotation
	 * will always return {@code null}.  Use this method, {@link #getUsages} or
	 * {@link #forEachUsage} with the repeatable annotation instead
	 *
	 * @apiNote If the annotation is {@linkplain java.lang.annotation.Repeatable repeatable},
	 * this method will return the usage if there is just one.  If there are multiple,
	 * {@link org.hibernate.boot.annotations.AnnotationAccessException} will be thrown
	 *
	 * @return The usage or {@code null}
	 */
	<A extends Annotation> AnnotationUsage<A> getUsage(AnnotationDescriptor<A> type);

	/**
	 * Find the usages of the given annotation type.
	 * <p/>
	 * For {@linkplain java.lang.annotation.Repeatable repeatable} annotation types,
	 * returns all usages including the repetitions.
	 */
	<A extends Annotation> Iterable<AnnotationUsage<A>> getUsages(AnnotationDescriptor<A> type);

	/**
	 * Call the {@code consumer} for each {@linkplain AnnotationUsage usage} of the
	 * given {@code type}.
	 * <p/>
	 * For {@linkplain java.lang.annotation.Repeatable repeatable} annotation types,
	 * the consumer will also be called for those defined on the container.
	 * Calling this, like {@link #getUsage} with the container will "find" none - the
	 * consumer is never called.
	 */
	<A extends Annotation> void forEachUsage(AnnotationDescriptor<A> type, IndexedConsumer<AnnotationUsage<A>> consumer);

	/**
	 * Get a usage of the given annotation {@code type} with the given {@code name}.
	 *
	 * @implNote Delegates to {@link #getNamedUsage(AnnotationDescriptor, String, String)}
	 * with {@link "name"} as the {@code attributeName}.
	 */
	default <A extends Annotation> AnnotationUsage<A> getNamedUsage(AnnotationDescriptor<A> type, String name) {
		return getNamedUsage( type, name, "name" );
	}

	/**
	 * Get a usage of the given annotation {@code type} with its
	 * {@code attributeName} equal to the given {@code name}.
	 *
	 * @param attributeName The name of the annotation attribute on which
	 * to match the {@code name}.
	 */
	<A extends Annotation> AnnotationUsage<A> getNamedUsage(AnnotationDescriptor<A> type, String name, String attributeName);
}
