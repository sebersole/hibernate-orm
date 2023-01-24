/*
 * Hibernate, Relational Persistence for Idiomatic Java
 *
 * License: GNU Lesser General Public License (LGPL), version 2.1 or later.
 * See the lgpl.txt file in the root directory or http://www.gnu.org/licenses/lgpl-2.1.html.
 */
package org.hibernate.boot.annotations.source.internal;

import java.lang.annotation.Annotation;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import org.hibernate.boot.annotations.AnnotationAccessException;
import org.hibernate.boot.annotations.source.spi.AnnotationDescriptor;
import org.hibernate.boot.annotations.source.spi.AnnotationTarget;
import org.hibernate.boot.annotations.source.spi.AnnotationUsage;
import org.hibernate.boot.annotations.spi.AnnotationProcessingContext;
import org.hibernate.internal.util.IndexedConsumer;
import org.hibernate.internal.util.collections.CollectionHelper;

/**
 * Basic support for AnnotationTarget
 *
 * @author Steve Ebersole
 */
public abstract class AbstractAnnotationTarget implements AnnotationTarget {
	private final Map<Class<? extends Annotation>, List<AnnotationUsage<?>>> usageMap = new ConcurrentHashMap<>();

	public AbstractAnnotationTarget(
			Annotation[] annotations,
			AnnotationProcessingContext processingContext) {
		AnnotationHelper.processAnnotationUsages( annotations, this, usageMap::put, processingContext );
	}

	@Override
	public <A extends Annotation> List<AnnotationUsage<A>> getAnnotations(AnnotationDescriptor<A> type) {
		//noinspection unchecked,rawtypes
		return (List) usageMap.get( type.getAnnotationType() );
	}

	@Override
	public <A extends Annotation> void forEachAnnotation(AnnotationDescriptor<A> type, IndexedConsumer<AnnotationUsage<A>> consumer) {
		final List<AnnotationUsage<A>> annotationUsages = getAnnotations( type );
		if ( annotationUsages == null ) {
			return;
		}

		for ( int i = 0; i < annotationUsages.size(); i++ ) {
			consumer.accept( i, annotationUsages.get( i ) );
		}
	}

	@Override
	public <A extends Annotation> AnnotationUsage<A> getAnnotation(AnnotationDescriptor<A> type) {
		final List<AnnotationUsage<?>> annotationUsages = usageMap.get( type.getAnnotationType() );
		if ( CollectionHelper.isEmpty( annotationUsages ) ) {
			return null;
		}
		if ( annotationUsages.size() > 1 ) {

			throw new AnnotationAccessException(
					"Expected single annotation usage, but found multiple : " + type.getAnnotationType().getName()
			);
		}
		//noinspection unchecked
		return (AnnotationUsage<A>) annotationUsages.get( 0 );
	}

	@Override
	public <A extends Annotation> AnnotationUsage<A> getNamedAnnotation(
			AnnotationDescriptor<A> type,
			String name,
			String attributeName) {
		final List<AnnotationUsage<?>> annotationUsages = usageMap.get( type.getAnnotationType() );
		if ( CollectionHelper.isEmpty( annotationUsages ) ) {
			return null;
		}
		for ( int i = 0; i < annotationUsages.size(); i++ ) {
			//noinspection unchecked
			final AnnotationUsage<A> annotationUsage = (AnnotationUsage<A>) annotationUsages.get( i );
			final AnnotationUsage.AttributeValue attributeValue = annotationUsage.getAttributeValue( attributeName );
			if ( attributeValue == null ) {
				continue;
			}
			if ( name.equals( attributeValue.getValue() ) ) {
				return annotationUsage;
			}
		}
		return null;
	}
}
