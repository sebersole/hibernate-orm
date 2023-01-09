/*
 * Hibernate, Relational Persistence for Idiomatic Java
 *
 * License: GNU Lesser General Public License (LGPL), version 2.1 or later.
 * See the lgpl.txt file in the root directory or http://www.gnu.org/licenses/lgpl-2.1.html.
 */
package org.hibernate.boot.model.annotations.internal;

import java.lang.annotation.Annotation;
import java.lang.annotation.Documented;
import java.lang.annotation.Repeatable;
import java.lang.annotation.Retention;
import java.lang.annotation.Target;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.function.Consumer;

import org.hibernate.boot.model.annotations.AnnotationAccessException;
import org.hibernate.boot.model.annotations.spi.AnnotationDescriptor;
import org.hibernate.boot.model.annotations.spi.AnnotationDescriptor.AttributeDescriptor;
import org.hibernate.boot.model.annotations.spi.AnnotationDescriptorRegistry;
import org.hibernate.boot.model.annotations.spi.AnnotationTarget;
import org.hibernate.boot.model.annotations.spi.AnnotationUsage;
import org.hibernate.internal.util.collections.CollectionHelper;

import static org.hibernate.internal.util.collections.CollectionHelper.arrayList;

/**
 * Basic support for AnnotationTarget
 *
 * @author Steve Ebersole
 */
public abstract class AbstractAnnotationTarget implements AnnotationTarget {
	private final Map<Class<? extends Annotation>, List<AnnotationUsage<?>>> usageMap = new ConcurrentHashMap<>();

	public AbstractAnnotationTarget(
			Annotation[] annotations,
			AnnotationDescriptorRegistry annotationDescriptorRegistry) {
		for ( int i = 0; i < annotations.length; i++ ) {
			final Annotation typeAnnotation = annotations[ i ];
			final Class<? extends Annotation> typeAnnotationType = typeAnnotation.annotationType();

			// skip a few well-know ones that are irrelevant
			if ( typeAnnotationType == Repeatable.class
					|| typeAnnotationType == Target.class
					|| typeAnnotationType == Retention.class
					|| typeAnnotationType == Documented.class ) {
				continue;
			}

			final Class<? extends Annotation> usagesKey;
			final List<AnnotationUsage<?>> usages;

			final AnnotationDescriptor<?> typeAnnotationDescriptor = annotationDescriptorRegistry.getDescriptor( typeAnnotationType );
			if ( typeAnnotationDescriptor.getRepeatableContainer() != null ) {
				// `annotations[i]` will be the only one because it is repeatable,
				// and we found this instead of the container
				usagesKey = typeAnnotationType;
				usages = Collections.singletonList( makeUsage( annotations[i], typeAnnotationDescriptor ) );
			}
			else {
				final AnnotationDescriptor<? extends Annotation> repeatableDescriptor = annotationDescriptorRegistry.getRepeatableDescriptor( typeAnnotationType );
				if ( repeatableDescriptor != null ) {
					usagesKey = repeatableDescriptor.getAnnotationType();
					// `annotations[i]` is itself the container; flatten the repeated values
					final AttributeDescriptor<Annotation[]> valueAttribute = typeAnnotationDescriptor.getValueAttribute();
					final Annotation[] repeatableValues = valueAttribute.extractRawValue( typeAnnotation );
					usages = arrayList( repeatableValues.length );
					for ( int r = 0; r < repeatableValues.length; r++ ) {
						usages.add( makeUsage( repeatableValues[r], repeatableDescriptor ) );
					}
				}
				else {
					// otherwise, it is just  the one
					usagesKey = typeAnnotationType;
					usages = Collections.singletonList( makeUsage( annotations[i], typeAnnotationDescriptor ) );
				}
			}

			usageMap.put( usagesKey, usages );
		}
	}

	private <A extends Annotation> AnnotationUsageImpl<A> makeUsage(
			Annotation typeAnnotation,
			AnnotationDescriptor<? extends Annotation> typeAnnotationDescriptor) {

		//noinspection rawtypes,unchecked
		return new AnnotationUsageImpl(
				typeAnnotation,
				typeAnnotationDescriptor,
				this
		);
	}


	@Override
	public <A extends Annotation> List<AnnotationUsage<A>> getUsages(AnnotationDescriptor<A> type) {
		//noinspection unchecked,rawtypes
		return (List) usageMap.get( type.getAnnotationType() );
	}

	@Override
	public <A extends Annotation> void withAnnotations(AnnotationDescriptor<A> type, Consumer<AnnotationUsage<A>> consumer) {
		final List<AnnotationUsage<?>> annotationUsages = usageMap.get( type.getAnnotationType() );
		if ( annotationUsages == null ) {
			return;
		}

		//noinspection unchecked,rawtypes
		annotationUsages.forEach( (Consumer) consumer );
	}

	@Override
	public <A extends Annotation> AnnotationUsage<A> getUsage(AnnotationDescriptor<A> type) {
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
	public <A extends Annotation> AnnotationUsage<A> getNamedUsage(
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
