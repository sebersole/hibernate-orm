/*
 * Hibernate, Relational Persistence for Idiomatic Java
 *
 * License: GNU Lesser General Public License (LGPL), version 2.1 or later.
 * See the lgpl.txt file in the root directory or http://www.gnu.org/licenses/lgpl-2.1.html.
 */
package org.hibernate.boot.annotations.internal;

import java.util.Objects;

import org.hibernate.boot.annotations.spi.AnnotationDescriptor.AttributeDescriptor;
import org.hibernate.boot.annotations.spi.AnnotationUsage;

/**
 * @author Steve Ebersole
 */
public class AttributeValueImpl implements AnnotationUsage.AttributeValue {
	private final AttributeDescriptor<?> attributeDescriptor;
	private final Object value;

	public AttributeValueImpl(AttributeDescriptor attributeDescriptor, Object value) {
		this.attributeDescriptor = attributeDescriptor;
		this.value = value;
	}

	@Override
	public <T> AttributeDescriptor<T> getAttributeDescriptor() {
		//noinspection unchecked
		return (AttributeDescriptor<T>) attributeDescriptor;
	}

	@Override
	public <T> T getValue() {
		//noinspection unchecked
		return (T) value;
	}

	@Override
	public boolean isDefaultValue() {
		return Objects.equals( value, attributeDescriptor.getAttributeDefault() );
	}
}
