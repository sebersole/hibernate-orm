/*
 * Hibernate, Relational Persistence for Idiomatic Java
 *
 * License: GNU Lesser General Public License (LGPL), version 2.1 or later.
 * See the lgpl.txt file in the root directory or http://www.gnu.org/licenses/lgpl-2.1.html.
 */
package org.hibernate.boot.annotations.source.internal.reflection;

import java.lang.reflect.Method;

import org.hibernate.boot.annotations.source.internal.LazyAnnotationTarget;
import org.hibernate.boot.annotations.source.spi.AnnotationProcessingContext;
import org.hibernate.boot.annotations.source.spi.FieldDetails;

/**
 * @author Steve Ebersole
 */
public class MethodDetailsImpl extends LazyAnnotationTarget implements FieldDetails {
	private final Method method;

	public MethodDetailsImpl(Method method, AnnotationProcessingContext processingContext) {
		super( method::getAnnotations, processingContext );
		this.method = method;
	}

	@Override
	public String getName() {
		return method.getName();
	}
}
