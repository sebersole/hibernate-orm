/*
 * Hibernate, Relational Persistence for Idiomatic Java
 *
 * License: GNU Lesser General Public License (LGPL), version 2.1 or later.
 * See the lgpl.txt file in the root directory or http://www.gnu.org/licenses/lgpl-2.1.html.
 */
package org.hibernate.boot.annotations.source.internal;

import java.lang.annotation.Annotation;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.function.Consumer;

import org.hibernate.boot.annotations.AnnotationAccessException;
import org.hibernate.boot.annotations.source.spi.AnnotationDescriptor;
import org.hibernate.boot.annotations.source.spi.AnnotationTarget;
import org.hibernate.boot.annotations.source.spi.AnnotationUsage;
import org.hibernate.boot.annotations.spi.AnnotationProcessingContext;
import org.hibernate.internal.util.collections.CollectionHelper;

/**
 * Implementation of  AnnotationTarget where the annotations are not known up
 * front; rather, they are {@linkplain  #apply(AnnotationUsage) applied} later
 *
 * @author Steve Ebersole
 */
public abstract class DelayedAnnotationTarget implements AnnotationTarget {
	private final AnnotationProcessingContext processingContext;
	private final Map<Class<? extends Annotation>,List<AnnotationUsage<?>>> usagesMap = new HashMap<>();

	public DelayedAnnotationTarget(AnnotationProcessingContext processingContext) {
		this.processingContext = processingContext;
	}

	public void apply(List<AnnotationUsage<?>> annotationUsages) {
		// todo (annotation-source) : handle meta-annotations
		annotationUsages.forEach( this::apply );
	}

	/**
	 * Applies the given {@code annotationUsage} to this target.
	 *
	 * @apiNote
	 * todo (annotation-source) : It is undefined currently what happens if the
	 * 		{@link AnnotationUsage#getAnnotationDescriptor() annotation type} is
	 * 		already applied on this target.
	 */
	public void apply(AnnotationUsage<?> annotationUsage) {
		final AnnotationDescriptor<?> annotationDescriptor = annotationUsage.getAnnotationDescriptor();
		final Class<? extends Annotation> annotationJavaType = annotationDescriptor.getAnnotationType();

		final List<AnnotationUsage<?>> usages = AnnotationsHelper.resolveRepeatable( annotationUsage, processingContext.getAnnotationDescriptorRegistry() );
		final List<AnnotationUsage<?>> previousList = usagesMap.put( annotationJavaType, usages );

		if ( previousList != null ) {
			// todo (annotation-source) : ignore?  log?  exception?
		}
	}

	public void apply(Annotation[] annotations) {
		// todo (annotation-source) : handle meta-annotations
		for ( int i = 0; i < annotations.length; i++ ) {
			apply( annotations[i] );
		}
	}

	protected void apply(Annotation annotation) {
		//noinspection rawtypes,unchecked
		final AnnotationUsageImpl usage = new AnnotationUsageImpl(
				annotation,
				processingContext.getAnnotationDescriptorRegistry().getDescriptor( annotation.annotationType() ),
				this,
				processingContext
		);
		apply( usage );
	}

	@Override
	public void forEachAnnotation(Consumer<AnnotationUsage<? extends Annotation>> consumer) {
		usagesMap.forEach( (c, annotationUsages) -> annotationUsages.forEach( consumer ) );
	}

	@Override
	public <A extends Annotation> List<AnnotationUsage<A>> getAnnotations(AnnotationDescriptor<A> type) {
		//noinspection unchecked,rawtypes
		return (List) usagesMap.get( type.getAnnotationType() );
	}

	@Override
	public <A extends Annotation> void forEachAnnotation(
			AnnotationDescriptor<A> type,
			Consumer<AnnotationUsage<A>> consumer) {
		final List<AnnotationUsage<A>> annotationUsages = getAnnotations( type );
		if ( annotationUsages == null ) {
			return;
		}

		for ( int i = 0; i < annotationUsages.size(); i++ ) {
			consumer.accept( annotationUsages.get( i ) );
		}
	}

	@Override
	public <A extends Annotation> AnnotationUsage<A> getAnnotation(AnnotationDescriptor<A> type) {
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
	public <A extends Annotation> AnnotationUsage<A> getNamedAnnotation(
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
