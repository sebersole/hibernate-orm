/*
 * Hibernate, Relational Persistence for Idiomatic Java
 *
 * License: GNU Lesser General Public License (LGPL), version 2.1 or later.
 * See the lgpl.txt file in the root directory or http://www.gnu.org/licenses/lgpl-2.1.html.
 */
package org.hibernate.boot.model.source.annotations.internal;

import java.lang.annotation.Annotation;
import java.util.Map;

import org.hibernate.boot.model.source.annotations.spi.AnnotationDescriptor;
import org.hibernate.boot.model.source.annotations.spi.AnnotationTarget;
import org.hibernate.boot.model.source.annotations.spi.AnnotationUsage;

/**
 * @author Steve Ebersole
 */
public class StandardAnnotationUsage<A extends Annotation> implements AnnotationUsage<A> {
	private final AnnotationDescriptor<A> annotationDescriptor;
	private final AnnotationTarget location;

	private final Map<String, AttributeValue> valueMap;

	public StandardAnnotationUsage(
			A annotation,
			AnnotationDescriptor<A> annotationDescriptor,
			AnnotationTarget location) {
		this.annotationDescriptor = annotationDescriptor;
		this.location = location;

		this.valueMap = AnnotationHelper.extractAttributeValues( annotation, annotationDescriptor );
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
