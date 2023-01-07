/*
 * Hibernate, Relational Persistence for Idiomatic Java
 *
 * License: GNU Lesser General Public License (LGPL), version 2.1 or later.
 * See the lgpl.txt file in the root directory or http://www.gnu.org/licenses/lgpl-2.1.html.
 */
package org.hibernate.boot.model.source.annotations.spi;

import java.lang.annotation.Annotation;
import java.util.List;
import java.util.function.Consumer;

/**
 * A model part which can be the target of an annotation
 *
 * @author Steve Ebersole
 */
public interface AnnotationTarget {
	/**
	 * Find the usages of the given annotation type.  For
	 * {@linkplain java.lang.annotation.Repeatable repeatable}
	 * annotation types, returns all usages including the repetitions.
	 */
	<A extends Annotation> List<AnnotationUsage<A>> getUsages(AnnotationDescriptor<A> type);

	/**
	 * Like {@link #getUsages} but allowing functional access
	 */
	<A extends Annotation> void withAnnotations(AnnotationDescriptor<A> type, Consumer<AnnotationUsage<A>> consumer);

	/**
	 * Get a singular usage of the given annotation type.  For
	 * {@linkplain java.lang.annotation.Repeatable repeatable}
	 * annotation types, will throw an exception if there are
	 * multiple usages
	 */
	<A extends Annotation> AnnotationUsage<A> getUsage(AnnotationDescriptor<A> type);

	/**
	 * Get a usage of the given annotation type with the given name.
	 *
	 * @implNote Delegates to {@link #getNamedUsage(AnnotationDescriptor, String, String)}
	 * 		with {@link "name"} as the {@code attributeName}.
	 */
	default <A extends Annotation> AnnotationUsage<A> getNamedUsage(AnnotationDescriptor<A> type, String name) {
		return getNamedUsage( type, name, "name" );
	}

	/**
	 * Get a usage of the given annotation type with the given name.
	 *
	 * @param attributeName The name of the annotation attribute on which
	 * to match the {@code name}.
	 */
	<A extends Annotation> AnnotationUsage<A> getNamedUsage(AnnotationDescriptor<A> type, String name, String attributeName);
}
