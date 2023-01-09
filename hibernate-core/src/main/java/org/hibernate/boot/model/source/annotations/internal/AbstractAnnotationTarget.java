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

import org.hibernate.boot.model.annotations.AnnotationAccessException;
import org.hibernate.boot.model.annotations.internal.AnnotationUsageImpl;
import org.hibernate.boot.model.annotations.spi.AnnotationDescriptor;
import org.hibernate.boot.model.annotations.spi.AnnotationDescriptorRegistry;
import org.hibernate.boot.model.annotations.spi.AnnotationProcessingContext;
import org.hibernate.boot.model.annotations.spi.AnnotationTarget;
import org.hibernate.boot.model.annotations.spi.AnnotationUsage;
import org.hibernate.internal.util.collections.CollectionHelper;

/**
 * Base support for things which can be {@linkplain java.lang.annotation.Target targeted}
 * by annotations, providing access to the details about those associated annotations
 *
 * @author Steve Ebersole
 */
public abstract class AbstractAnnotationTarget implements AnnotationTarget {
	private final AnnotationProcessingContext processingContext;
	private final Map<Class<? extends Annotation>,List<AnnotationUsage<?>>> usagesMap = new HashMap<>();

	public AbstractAnnotationTarget(AnnotationProcessingContext processingContext) {
		this.processingContext = processingContext;
	}

	public AbstractAnnotationTarget(List<AnnotationUsage<?>> annotationUsages, AnnotationProcessingContext processingContext) {
		this( processingContext );
		apply( annotationUsages );
	}

	public void apply(List<AnnotationUsage<?>> annotationUsages) {
		// todo (annotation-source) : handle meta-annotations
		annotationUsages.forEach( this::apply );
	}

	public void apply(AnnotationUsage<?> annotationUsage) {
		final AnnotationDescriptor<?> annotationDescriptor = annotationUsage.getAnnotationDescriptor();
		final Class<? extends Annotation> annotationJavaType = annotationDescriptor.getAnnotationType();

		if ( annotationDescriptor.getRepeatableContainer() != null ) {
			// The annotation is repeatable.  Since Java does not allow the repeatable and container
			// to exist on the same target, this means that this usage is the only effective
			// one for its descriptor
			usagesMap.put( annotationJavaType, Collections.singletonList( annotationUsage ) );
		}
		else {
			final AnnotationDescriptorRegistry descriptorXref = processingContext.getAnnotationDescriptorRegistry();
			final AnnotationDescriptor<?> repeatableDescriptor = descriptorXref.getRepeatableDescriptor( annotationJavaType );
			if ( repeatableDescriptor != null ) {
				// The usage type is a repeatable container.  Flatten its contained annotations
				final AnnotationUsage.AttributeValue value = annotationUsage.getAttributeValue( "value" );
				final List<AnnotationUsage<?>> repeatableUsages = value.getValue();
				usagesMap.put( repeatableDescriptor.getAnnotationType(), repeatableUsages );
			}
			else {
				usagesMap.put( annotationJavaType, Collections.singletonList( annotationUsage ) );
			}
		}
	}

	public void apply(Annotation[] annotations) {
		// todo (annotation-source) : handle meta-annotations
		for ( int i = 0; i < annotations.length; i++ ) {
			apply( annotations[i] );
		}
	}

	protected void apply(Annotation annotation) {
		final AnnotationDescriptorRegistry descriptorXref = processingContext.getAnnotationDescriptorRegistry();
		//noinspection rawtypes,unchecked
		final AnnotationUsageImpl usage = new AnnotationUsageImpl(
				annotation,
				descriptorXref.getDescriptor( annotation.annotationType() ),
				this
		);
		apply( usage );
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
