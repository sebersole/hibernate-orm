/*
 * Hibernate, Relational Persistence for Idiomatic Java
 *
 * License: GNU Lesser General Public License (LGPL), version 2.1 or later.
 * See the lgpl.txt file in the root directory or http://www.gnu.org/licenses/lgpl-2.1.html.
 */
package org.hibernate.boot.annotations.internal;

import java.lang.annotation.Annotation;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.function.Consumer;
import java.util.function.Supplier;

import org.hibernate.boot.annotations.AnnotationAccessException;
import org.hibernate.boot.annotations.spi.AnnotationDescriptor;
import org.hibernate.boot.annotations.spi.AnnotationDescriptorRegistry;
import org.hibernate.boot.annotations.spi.AnnotationTarget;
import org.hibernate.boot.annotations.spi.AnnotationUsage;
import org.hibernate.internal.util.collections.CollectionHelper;

/**
 * AnnotationTarget where we know the annotations up front, but
 * want to delay processing them until (unless!) they are needed
 *
 * @author Steve Ebersole
 */
public class LazyAnnotationTarget implements AnnotationTarget {
	private final Supplier<Annotation[]> annotationSupplier;
	private final AnnotationDescriptorRegistry annotationDescriptorRegistry;

	private Map<Class<? extends Annotation>, List<AnnotationUsage<?>>> usagesMap;

	public LazyAnnotationTarget(
			Supplier<Annotation[]> annotationSupplier,
			AnnotationDescriptorRegistry annotationDescriptorRegistry) {
		this.annotationSupplier = annotationSupplier;
		this.annotationDescriptorRegistry = annotationDescriptorRegistry;
	}

	@Override
	public <A extends Annotation> List<AnnotationUsage<A>> getUsages(AnnotationDescriptor<A> type) {
		//noinspection unchecked,rawtypes
		return (List) getUsagesMap().get( type.getAnnotationType() );
	}

	private Map<Class<? extends Annotation>, List<AnnotationUsage<?>>> getUsagesMap() {
		if ( usagesMap == null ) {
			usagesMap = buildUsagesMap();
		}
		return usagesMap;
	}

	private Map<Class<? extends Annotation>, List<AnnotationUsage<?>>> buildUsagesMap() {
		final Map<Class<? extends Annotation>, List<AnnotationUsage<?>>> result = new HashMap<>();
		AnnotationHelper.processAnnotationUsages(
				annotationSupplier.get(),
				this,
				result::put,
				annotationDescriptorRegistry
		);
		return result;
	}

	@Override
	public <A extends Annotation> void withAnnotations(AnnotationDescriptor<A> type, Consumer<AnnotationUsage<A>> consumer) {
		final List<AnnotationUsage<A>> annotationUsages = getUsages( type );
		if ( annotationUsages == null ) {
			return;
		}
		annotationUsages.forEach( consumer );
	}

	@Override
	public <A extends Annotation> AnnotationUsage<A> getUsage(AnnotationDescriptor<A> type) {
		final List<AnnotationUsage<A>> annotationUsages = getUsages( type );
		if ( CollectionHelper.isEmpty( annotationUsages ) ) {
			return null;
		}
		if ( annotationUsages.size() > 1 ) {
			throw new AnnotationAccessException(
					"Expected single annotation usage, but found multiple : " + type.getAnnotationType().getName()
			);
		}
		return annotationUsages.get( 0 );
	}

	@Override
	public <A extends Annotation> AnnotationUsage<A> getNamedUsage(AnnotationDescriptor<A> type, String name, String attributeName) {
		final List<AnnotationUsage<A>> annotationUsages = getUsages( type );
		if ( annotationUsages == null ) {
			return null;
		}

		for ( int i = 0; i < annotationUsages.size(); i++ ) {
			final AnnotationUsage<A> annotationUsage = annotationUsages.get( i );
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
