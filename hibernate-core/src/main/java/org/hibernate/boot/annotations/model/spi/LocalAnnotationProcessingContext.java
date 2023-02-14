/*
 * Hibernate, Relational Persistence for Idiomatic Java
 *
 * License: GNU Lesser General Public License (LGPL), version 2.1 or later.
 * See the lgpl.txt file in the root directory or http://www.gnu.org/licenses/lgpl-2.1.html.
 */
package org.hibernate.boot.annotations.model.spi;

import org.hibernate.boot.annotations.spi.AnnotationProcessingContext;

/**
 * AnnotationProcessingContext implementation {@linkplain #getScope() scoped} to a
 * specific managed-type
 *
 * @author Steve Ebersole
 */
public interface LocalAnnotationProcessingContext extends AnnotationProcessingContext {
	ManagedTypeMetadata getScope();
}
