/*
 * Hibernate, Relational Persistence for Idiomatic Java
 *
 * License: GNU Lesser General Public License (LGPL), version 2.1 or later.
 * See the lgpl.txt file in the root directory or http://www.gnu.org/licenses/lgpl-2.1.html.
 */
package org.hibernate.boot.annotations.source.internal;

import java.util.Objects;

import org.hibernate.boot.annotations.source.spi.AnnotationDescriptor;
import org.hibernate.boot.annotations.source.spi.AnnotationUsage;

/**
 * @author Steve Ebersole
 */
public class AttributeValueImpl implements AnnotationUsage.AttributeValue {
	private final AnnotationDescriptor.AttributeDescriptor<?> attributeDescriptor;
	private final Object value;

	public AttributeValueImpl(AnnotationDescriptor.AttributeDescriptor attributeDescriptor, Object value) {
		this.attributeDescriptor = attributeDescriptor;
		this.value = value;
	}

	@Override
	public <T> AnnotationDescriptor.AttributeDescriptor<T> getAttributeDescriptor() {
		//noinspection unchecked
		return (AnnotationDescriptor.AttributeDescriptor<T>) attributeDescriptor;
	}

	@Override
	public <T> T getValue() {
		//noinspection unchecked
		return (T) value;
	}

	@Override
	public <T> T getValue(Class<T> type) {
		// todo (annotation-source) : possibly add some simple conversions.

		// todo (annotation-source) : or possibly typed AttributeDescriptor impls (`IntAttributeDescriptor`, ...)
		//		which can be a factory for AttributeValue refs based on the descriptor type.

		return getValue();
	}

	@Override
	public boolean isDefaultValue() {
		return Objects.equals( value, attributeDescriptor.getAttributeDefault() );
	}
}
