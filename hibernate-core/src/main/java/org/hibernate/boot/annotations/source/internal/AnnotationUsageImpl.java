/*
 * Hibernate, Relational Persistence for Idiomatic Java
 *
 * License: GNU Lesser General Public License (LGPL), version 2.1 or later.
 * See the lgpl.txt file in the root directory or http://www.gnu.org/licenses/lgpl-2.1.html.
 */
package org.hibernate.boot.annotations.source.internal;

import java.lang.annotation.Annotation;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.hibernate.boot.annotations.source.spi.AnnotationAttributeValue;
import org.hibernate.boot.annotations.source.spi.AnnotationDescriptor;
import org.hibernate.boot.annotations.source.spi.AnnotationTarget;
import org.hibernate.boot.annotations.source.spi.AnnotationUsage;
import org.hibernate.boot.annotations.spi.AnnotationProcessingContext;
import org.hibernate.internal.util.collections.CollectionHelper;

/**
 * @author Steve Ebersole
 */
public class AnnotationUsageImpl<A extends Annotation> implements AnnotationUsage<A> {
	private final AnnotationDescriptor<A> annotationDescriptor;
	private final AnnotationTarget location;

	private final Map<String, AnnotationAttributeValue> valueMap;

	public AnnotationUsageImpl(
			A annotation,
			AnnotationDescriptor<A> annotationDescriptor,
			AnnotationTarget location,
			AnnotationProcessingContext processingContext) {
		this.annotationDescriptor = annotationDescriptor;
		this.location = location;

		this.valueMap = AnnotationHelper.extractAttributeValues( annotation, annotationDescriptor, processingContext );

		processingContext.registerUsage( this );
	}

	public AnnotationUsageImpl(
			AnnotationDescriptor<A> annotationDescriptor,
			AnnotationTarget location,
			Map<String, AnnotationAttributeValue> valueMap) {
		this.annotationDescriptor = annotationDescriptor;
		this.location = location;
		this.valueMap = valueMap;
	}

	public AnnotationUsageImpl(
			AnnotationDescriptor<A> annotationDescriptor,
			AnnotationTarget location,
			List<AnnotationAttributeValue> valueList) {
		this( annotationDescriptor, location, indexValues( valueList ) );
	}

	private static Map<String, AnnotationAttributeValue> indexValues(List<AnnotationAttributeValue> valueList) {
		if ( CollectionHelper.isEmpty( valueList ) ) {
			return Collections.emptyMap();
		}

		final Map<String, AnnotationAttributeValue> result = new HashMap<>();
		for ( int i = 0; i < valueList.size(); i++ ) {
			final AnnotationAttributeValue value = valueList.get( i );
			result.put( value.getAttributeDescriptor().getAttributeName(), value );
		}
		return result;
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
	public AnnotationAttributeValue getAttributeValue(String name) {
		return valueMap.get( name );
	}
}
