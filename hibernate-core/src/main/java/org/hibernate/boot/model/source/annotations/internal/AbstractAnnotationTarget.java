/*
 * Hibernate, Relational Persistence for Idiomatic Java
 *
 * License: GNU Lesser General Public License (LGPL), version 2.1 or later.
 * See the lgpl.txt file in the root directory or http://www.gnu.org/licenses/lgpl-2.1.html.
 */
package org.hibernate.boot.model.source.annotations.internal;

import java.lang.annotation.Annotation;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.function.Consumer;

import org.hibernate.boot.model.source.annotations.AnnotationAccessException;
import org.hibernate.boot.model.source.annotations.spi.AnnotationBindingContext;
import org.hibernate.boot.model.source.annotations.spi.AnnotationDescriptor;
import org.hibernate.boot.model.source.annotations.spi.AnnotationDescriptorXref;
import org.hibernate.boot.model.source.annotations.spi.AnnotationTarget;
import org.hibernate.boot.model.source.annotations.spi.AnnotationUsage;
import org.hibernate.internal.util.collections.CollectionHelper;

/**
 * @author Steve Ebersole
 */
public abstract class AbstractAnnotationTarget implements AnnotationTarget {
	private final Map<Class<? extends Annotation>,List<AnnotationUsage<?>>> usagesMap = new HashMap<>();

	public AbstractAnnotationTarget(Annotation[] annotations, AnnotationBindingContext bindingContext) {
		final AnnotationDescriptorXref descriptorXref = bindingContext.getAnnotationDescriptorXref();

		// todo (annotation-source) : handle meta-annotations

		for ( int i = 0; i < annotations.length; i++ ) {
			final Annotation annotation = annotations[i];
			final Class<? extends Annotation> annotationJavaType = annotation.annotationType();

			//noinspection rawtypes
			final AnnotationDescriptor containedDescriptor = descriptorXref.getRepeatableDescriptor( annotationJavaType );
			if ( containedDescriptor != null ) {
				final List<Annotation> repeated = AnnotationHelper.extractRepeated( annotation );
				final List<AnnotationUsage<?>> usages = CollectionHelper.arrayList( repeated.size() );
				for ( int r = 0; r < repeated.size(); r++ ) {
					//noinspection unchecked,rawtypes
					usages.add( new StandardAnnotationUsage<>( repeated.get(r), containedDescriptor, this ) );
				}
				//noinspection unchecked
				usagesMap.put( containedDescriptor.getAnnotationType(), usages );
				continue;
			}

			final AnnotationDescriptor<?> descriptor = descriptorXref.getDescriptor( annotationJavaType );
			if ( descriptor != null ) {
				//noinspection unchecked,rawtypes
				final StandardAnnotationUsage usage = new StandardAnnotationUsage( annotation, descriptor, this );
				//noinspection unchecked
				usagesMap.put( descriptor.getAnnotationType(), Collections.singletonList( usage ) );
			}
		}
	}

	@Override
	public <A extends Annotation> List<AnnotationUsage<A>> getUsages(AnnotationDescriptor<A> type) {
		//noinspection unchecked,rawtypes
		return (List) usagesMap.get( type.getAnnotationType() );
	}

	@Override
	public <A extends Annotation> void withAnnotations(AnnotationDescriptor<A> type, Consumer<AnnotationUsage<A>> consumer) {
		final List<AnnotationUsage<?>> annotationUsages = usagesMap.get( type.getAnnotationType() );
		if ( annotationUsages == null ) {
			return;
		}

		//noinspection unchecked,rawtypes
		annotationUsages.forEach( (Consumer) consumer );
	}

	@Override
	public <A extends Annotation> AnnotationUsage<A> getUsage(AnnotationDescriptor<A> type) {
		final List<AnnotationUsage<?>> annotationUsages = usagesMap.get( type.getAnnotationType() );
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
		final List<AnnotationUsage<?>> annotationUsages = usagesMap.get( type.getAnnotationType() );
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
