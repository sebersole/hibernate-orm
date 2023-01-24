/*
 * Hibernate, Relational Persistence for Idiomatic Java
 *
 * License: GNU Lesser General Public License (LGPL), version 2.1 or later.
 * See the lgpl.txt file in the root directory or http://www.gnu.org/licenses/lgpl-2.1.html.
 */
package org.hibernate.boot.annotations.source.internal;

import java.lang.annotation.Annotation;
import java.lang.annotation.Documented;
import java.lang.annotation.Repeatable;
import java.lang.annotation.Retention;
import java.lang.annotation.Target;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.function.BiConsumer;

import org.hibernate.boot.annotations.AnnotationAccessException;
import org.hibernate.boot.annotations.source.internal.reflection.ClassDetailsImpl;
import org.hibernate.boot.annotations.source.spi.AnnotationDescriptor;
import org.hibernate.boot.annotations.source.spi.AnnotationDescriptor.AttributeDescriptor;
import org.hibernate.boot.annotations.source.spi.AnnotationDescriptorRegistry;
import org.hibernate.boot.annotations.source.spi.AnnotationTarget;
import org.hibernate.boot.annotations.source.spi.AnnotationUsage;
import org.hibernate.boot.annotations.source.spi.AnnotationUsage.AttributeValue;
import org.hibernate.boot.annotations.source.spi.ClassDetails;
import org.hibernate.boot.annotations.source.spi.ClassDetailsRegistry;
import org.hibernate.boot.annotations.spi.AnnotationProcessingContext;
import org.hibernate.internal.util.collections.CollectionHelper;

import static org.hibernate.internal.util.collections.CollectionHelper.arrayList;

/**
 * Collection of helper functions related to annotation handling
 *
 * @author Steve Ebersole
 */
public class AnnotationHelper {
	/**
	 * Processes `annotations` creating {@link AnnotationUsage} instances
	 *
	 * @param annotations The annotations to process
	 * @param target The target of the usages
	 * @param consumer Consumer of the generated usages
	 */
	public static void processAnnotationUsages(
			Annotation[] annotations,
			AnnotationTarget target,
			BiConsumer<Class<? extends Annotation>, List<AnnotationUsage<?>>> consumer,
			AnnotationProcessingContext processingContext) {
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

			final AnnotationDescriptorRegistry annotationDescriptorRegistry = processingContext.getAnnotationDescriptorRegistry();
			final AnnotationDescriptor<?> typeAnnotationDescriptor = annotationDescriptorRegistry.getDescriptor( typeAnnotationType );
			if ( typeAnnotationDescriptor.getRepeatableContainer() != null ) {
				// `annotations[i]` will be the only one because it is repeatable,
				// and we found this instead of the container
				usagesKey = typeAnnotationType;
				usages = Collections.singletonList( makeUsage(
						annotations[i],
						typeAnnotationDescriptor,
						target,
						processingContext
				) );
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
						usages.add( makeUsage(
								repeatableValues[r],
								repeatableDescriptor,
								target,
								processingContext
						) );
					}
				}
				else {
					// otherwise, it is just  the one
					usagesKey = typeAnnotationType;
					usages = Collections.singletonList( makeUsage(
							annotations[i],
							typeAnnotationDescriptor,
							target,
							processingContext
					) );
				}
			}

			consumer.accept( usagesKey, usages );
		}
	}

	private static AnnotationUsage<?> makeUsage(
			Annotation annotation,
			AnnotationDescriptor<?> typeAnnotationDescriptor,
			AnnotationTarget target,
			AnnotationProcessingContext processingContext) {
		//noinspection unchecked,rawtypes
		return new AnnotationUsageImpl( annotation, typeAnnotationDescriptor, target, processingContext );
	}

	/**
	 * Given an {@code annotation} (and its {@linkplain OrmAnnotationDescriptorImpl descriptor}),
	 * extract the {@link AttributeValue attribute values}.
	 */
	public static <A extends Annotation> Map<String, AttributeValue> extractAttributeValues(
			A annotation,
			AnnotationDescriptor<A> annotationDescriptor,
			AnnotationProcessingContext processingContext) {
		if ( CollectionHelper.isEmpty( annotationDescriptor.getAttributes() ) ) {
			return Collections.emptyMap();
		}

		if ( annotationDescriptor.getAttributes().size() == 1 ) {
			final AttributeDescriptor<?> attributeDescriptor = annotationDescriptor.getAttributes().get( 0 );
			return Collections.singletonMap(
					attributeDescriptor.getAttributeName(),
					new AttributeValueImpl( attributeDescriptor, extractAttributeValue( annotation, attributeDescriptor, processingContext ) )
			);
		}

		final Map<String, AttributeValue> valueMap = new HashMap<>();
		for ( int i = 0; i < annotationDescriptor.getAttributes().size(); i++ ) {
			final AttributeDescriptor<?> attributeDescriptor = annotationDescriptor.getAttributes().get( i );
			valueMap.put(
					attributeDescriptor.getAttributeName(),
					new AttributeValueImpl( attributeDescriptor, extractAttributeValue( annotation, attributeDescriptor, processingContext ) )
			);
		}
		return valueMap;
	}

	public static <A extends Annotation, T> T extractAttributeValue(
			A annotation,
			AttributeDescriptor<T> attributeDescriptor,
			AnnotationProcessingContext processingContext) {
		final AnnotationDescriptorRegistry annotationDescriptorRegistry = processingContext.getAnnotationDescriptorRegistry();
		final Object rawValue = extractRawAttributeValue( annotation, attributeDescriptor.getAttributeName() );

		if ( attributeDescriptor.getAttributeType().isAnnotation() ) {
			// the attribute type is an annotation. we want to wrap that in a usage.  target?
			//noinspection unchecked,rawtypes
			final AnnotationDescriptor<?> descriptor = annotationDescriptorRegistry.getDescriptor( (Class) attributeDescriptor.getAttributeType() );
			//noinspection unchecked
			return (T) makeUsage( (Annotation) rawValue, descriptor, null, processingContext );
		}

		if ( attributeDescriptor.getAttributeType().equals( Class.class ) ) {
			assert rawValue instanceof Class;
			final Class<?> rawClassValue = (Class<?>) rawValue;
			//noinspection unchecked
			return (T) resolveClassReference( processingContext, rawClassValue );
		}

		if ( attributeDescriptor.getAttributeType().isArray()
				&& attributeDescriptor.getAttributeType().getComponentType().isAnnotation() ) {
			// the attribute type is an array of annotations. we want to wrap those in a usage.  target?
			//noinspection unchecked
			final Class<? extends Annotation> annotationJavaType = (Class<? extends Annotation>) attributeDescriptor.getAttributeType().getComponentType();
			final AnnotationDescriptor<? extends Annotation> valuesAnnotationDescriptor = annotationDescriptorRegistry.getDescriptor( annotationJavaType );
			final Annotation[] rawValues = (Annotation[]) rawValue;
			final AnnotationUsage<?>[] usages = new AnnotationUsage[ rawValues.length ];
			for ( int i = 0; i < rawValues.length; i++ ) {
				final Annotation valueAnnotation = rawValues[i];
				usages[i] = makeUsage( valueAnnotation, valuesAnnotationDescriptor, null, processingContext );
			}
			//noinspection unchecked
			return (T) usages;
		}

		if ( attributeDescriptor.getAttributeType().isArray()
				&& attributeDescriptor.getAttributeType().getComponentType().equals( Class.class ) ) {
			//noinspection unchecked
			final Class<? extends Annotation> annotationJavaType = (Class<? extends Annotation>) attributeDescriptor.getAttributeType().getComponentType();
			final AnnotationDescriptor<? extends Annotation> valuesAnnotationDescriptor = annotationDescriptorRegistry.getDescriptor( annotationJavaType );
			final Class<?>[] rawValues = (Class<?>[]) rawValue;
			final ClassDetails[] classDetails = new ClassDetails[ rawValues.length ];
			for ( int i = 0; i < rawValues.length; i++ ) {
				final Class<?> rawClassValue = rawValues[ i ];
				classDetails[i] = resolveClassReference( processingContext, rawClassValue );
			}
			//noinspection unchecked
			return (T) classDetails;
		}

		//noinspection unchecked
		return (T) rawValue;
	}

	private static ClassDetails resolveClassReference(AnnotationProcessingContext processingContext, Class<?> rawClassValue) {
		final ClassDetailsRegistry classDetailsRegistry = processingContext.getClassDetailsRegistry();
		final ClassDetails existing = classDetailsRegistry.findManagedClass( rawClassValue.getName() );
		if ( existing != null ) {
			return existing;
		}
		else {
			final ClassDetails classDetails = new ClassDetailsImpl( rawClassValue, processingContext );
			classDetailsRegistry.addManagedClass( classDetails );
			return classDetails;
		}
	}

	public static <A extends Annotation, T> T extractRawAttributeValue(
			A annotation,
			String attributeName) {
		try {
			final Method method = annotation.getClass().getDeclaredMethod( attributeName );
			//noinspection unchecked
			return (T) method.invoke( annotation );
		}
		catch (NoSuchMethodException | InvocationTargetException | IllegalAccessException e) {
			throw new AnnotationAccessException(
					String.format(
							Locale.ROOT,
							"Unable to extract attribute value : %s.%s",
							annotation.annotationType().getName(),
							attributeName
					),
					e
			);
		}
	}

	private AnnotationHelper() {
		// disallow direct instantiation
	}
}
