/*
 * Hibernate, Relational Persistence for Idiomatic Java
 *
 * License: GNU Lesser General Public License (LGPL), version 2.1 or later.
 * See the lgpl.txt file in the root directory or http://www.gnu.org/licenses/lgpl-2.1.html.
 */
package org.hibernate.boot.annotations.internal;

import java.lang.annotation.Annotation;
import java.util.Map;

import org.hibernate.boot.annotations.spi.AnnotationDescriptor;
import org.hibernate.boot.annotations.spi.AnnotationDescriptorRegistry;
import org.hibernate.boot.annotations.spi.AnnotationTarget;
import org.hibernate.boot.annotations.spi.AnnotationUsage;

import static org.hibernate.boot.annotations.internal.AnnotationHelper.extractAttributeValues;

/**
 * @author Steve Ebersole
 */
public class AnnotationUsageImpl<A extends Annotation> implements AnnotationUsage<A> {
	private final AnnotationDescriptor<A> annotationDescriptor;
	private final AnnotationTarget location;

	private final Map<String, AttributeValue> valueMap;

	public AnnotationUsageImpl(
			A annotation,
			AnnotationDescriptor<A> annotationDescriptor,
			AnnotationTarget location,
			AnnotationDescriptorRegistry annotationDescriptorRegistry) {
		this.annotationDescriptor = annotationDescriptor;
		this.location = location;

		this.valueMap = extractAttributeValues( annotation, annotationDescriptor, annotationDescriptorRegistry );
	}

	@Override
	public AnnotationDescriptor<A> getAnnotationDescriptor() {
		return annotationDescriptor;
	}

	@Override
	public AnnotationTarget getAnnotationTarget() {
		return location;
	}

	@Override
	public AttributeValue getAttributeValue(String name) {
		return valueMap.get( name );
	}
}
