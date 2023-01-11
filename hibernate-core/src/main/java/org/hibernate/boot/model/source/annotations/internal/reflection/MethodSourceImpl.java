/*
 * Hibernate, Relational Persistence for Idiomatic Java
 *
 * License: GNU Lesser General Public License (LGPL), version 2.1 or later.
 * See the lgpl.txt file in the root directory or http://www.gnu.org/licenses/lgpl-2.1.html.
 */
package org.hibernate.boot.model.source.annotations.internal.reflection;

import java.lang.reflect.Method;

import org.hibernate.boot.annotations.internal.LazyAnnotationTarget;
import org.hibernate.boot.annotations.spi.AnnotationDescriptorRegistry;
import org.hibernate.boot.model.source.annotations.spi.FieldSource;

/**
 * @author Steve Ebersole
 */
public class MethodSourceImpl extends LazyAnnotationTarget implements FieldSource {
	private final Method method;

	public MethodSourceImpl(Method method, AnnotationDescriptorRegistry annotationDescriptorRegistry) {
		super( method::getAnnotations, annotationDescriptorRegistry );
		this.method = method;
	}

	@Override
	public String getName() {
		return method.getName();
	}
}
