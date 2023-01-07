/*
 * Hibernate, Relational Persistence for Idiomatic Java
 *
 * License: GNU Lesser General Public License (LGPL), version 2.1 or later.
 * See the lgpl.txt file in the root directory or http://www.gnu.org/licenses/lgpl-2.1.html.
 */
package org.hibernate.boot.model.source.annotations.spi;

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
	List<AnnotationUsage> getUsages(AnnotationDescriptor type);

	/**
	 * Like {@link #getUsages} but allowing functional access
	 */
	void withAnnotations(AnnotationDescriptor type, Consumer<AnnotationUsage> consumer);

	/**
	 * Get a singular usage of the given annotation type.  For
	 * {@linkplain java.lang.annotation.Repeatable repeatable}
	 * annotation types, will throw an exception if there are
	 * multiple usages
	 */
	AnnotationUsage getUsage(AnnotationDescriptor type);

	/**
	 * Get a usage of the given annotation type with the given name.
	 *
	 * @implNote Delegates to {@link #getNamedUsage(AnnotationDescriptor, String, String)}
	 * with {@link "name"} as the {@code attributeName}.
	 */
	AnnotationUsage getNamedUsage(AnnotationDescriptor type, String name);

	/**
	 * Get a usage of the given annotation type with the given name.
	 *
	 * @param attributeName The name of the annotation attribute on which
	 * to match the {@code name}.
	 */
	AnnotationUsage getNamedUsage(AnnotationDescriptor type, String name, String attributeName);
}
