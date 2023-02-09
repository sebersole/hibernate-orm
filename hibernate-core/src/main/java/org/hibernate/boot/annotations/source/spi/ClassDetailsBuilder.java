/*
 * Hibernate, Relational Persistence for Idiomatic Java
 *
 * License: GNU Lesser General Public License (LGPL), version 2.1 or later.
 * See the lgpl.txt file in the root directory or http://www.gnu.org/licenses/lgpl-2.1.html.
 */
package org.hibernate.boot.annotations.source.spi;

import org.hibernate.boot.annotations.spi.AnnotationProcessingContext;

/**
 * Contract for creating the ClassDetails for a Java type we have not yet seen
 * as part of {@link ClassDetailsRegistry#resolveManagedClass}
 *
 * @author Steve Ebersole
 */
@FunctionalInterface
public interface ClassDetailsBuilder {
	ClassDetails buildClassDetails(String name, AnnotationProcessingContext processingContext);
}
