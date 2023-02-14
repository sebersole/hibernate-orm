/*
 * Hibernate, Relational Persistence for Idiomatic Java
 *
 * License: GNU Lesser General Public License (LGPL), version 2.1 or later.
 * See the lgpl.txt file in the root directory or http://www.gnu.org/licenses/lgpl-2.1.html.
 */
package org.hibernate.boot.annotations.source.internal.dynamic;

import java.lang.annotation.Annotation;
import java.util.List;

import org.hibernate.boot.annotations.source.spi.AnnotationTarget;
import org.hibernate.boot.annotations.source.spi.AnnotationUsage;

/**
 * Specialization of AnnotationTarget where the annotations are not known up
 * front.  Rather, they are {@linkplain  #apply applied} later
 *
 * @author Steve Ebersole
 */
public interface DynamicAnnotationTarget extends AnnotationTarget {
	void apply(List<AnnotationUsage<?>> annotationUsages);

	void apply(AnnotationUsage<?> annotationUsage);

	void apply(Annotation[] annotations);

	void apply(Annotation annotation);
}
