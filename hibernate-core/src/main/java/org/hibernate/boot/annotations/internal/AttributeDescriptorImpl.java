/*
 * Hibernate, Relational Persistence for Idiomatic Java
 *
 * License: GNU Lesser General Public License (LGPL), version 2.1 or later.
 * See the lgpl.txt file in the root directory or http://www.gnu.org/licenses/lgpl-2.1.html.
 */
package org.hibernate.boot.annotations.internal;

import java.lang.annotation.Annotation;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;

import org.hibernate.boot.annotations.AnnotationAccessException;
import org.hibernate.boot.annotations.spi.AnnotationDescriptor;

/**
 * @author Steve Ebersole
 */
public class AttributeDescriptorImpl<T> implements AnnotationDescriptor.AttributeDescriptor<T> {
	private final Method valueMethod;

	public AttributeDescriptorImpl(Method valueMethod) {
		this.valueMethod = valueMethod;
	}

	public String getAttributeName() {
		return valueMethod.getName();
	}

	@SuppressWarnings("unchecked")
	public Class<T> getAttributeType() {
		return (Class<T>) valueMethod.getReturnType();
	}

	@SuppressWarnings("unchecked")
	public T getAttributeDefault() {
		return (T) valueMethod.getDefaultValue();
	}

	@Override
	public T extractRawValue(Annotation annotation) {
		try {
			//noinspection unchecked
			return (T) valueMethod.invoke( annotation );
		}
		catch (IllegalAccessException | InvocationTargetException e) {
			throw new AnnotationAccessException( "Unable to extract annotation attribute value", e );
		}
	}
}
