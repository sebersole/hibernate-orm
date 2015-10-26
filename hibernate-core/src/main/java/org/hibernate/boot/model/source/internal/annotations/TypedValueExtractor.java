/*
 * Hibernate, Relational Persistence for Idiomatic Java
 *
 * License: GNU Lesser General Public License (LGPL), version 2.1 or later.
 * See the lgpl.txt file in the root directory or <http://www.gnu.org/licenses/lgpl-2.1.html>.
 */
package org.hibernate.boot.model.source.internal.annotations;

import org.jboss.jandex.AnnotationInstance;

/**
 * A contract for extracting values from Jandex representation of an
 * annotation.
 *
 * @author Steve Ebersole
 */
public interface TypedValueExtractor<T> {
	/**
	 * Extracts the value by type from the given annotation attribute value
	 * representation.  The attribute value may be {@code null} (which
	 * represents an unspecified attribute), in which case we need reference
	 * to the {@link org.hibernate.boot.registry.classloading.spi.ClassLoaderService}
	 * to be able to resolve the default value for the given attribute.
	 *
	 * @param annotationInstance The representation of the annotation usage
	 * from which to extract an attribute value.
	 * @param name The name of the attribute to extract
	 *
	 * @return The extracted value.
	 */
	public T extract(AnnotationInstance annotationInstance, String name);

	/**
	 * Just like {@link #extract(org.jboss.jandex.AnnotationInstance, String)}
	 * except that here we return the passed defaultValue if the annotation
	 * attribute value is {@code null}.
	 *
	 * @param annotationInstance The representation of the annotation usage
	 * from which to extract an attribute value.
	 * @param name The name of the attribute to extract
	 * @param defaultValue The typed value to use if the annotation
	 * attribute value is {@code null}
	 *
	 * @return The extracted value.
	 */
	public T extract(AnnotationInstance annotationInstance, String name, T defaultValue);
}
