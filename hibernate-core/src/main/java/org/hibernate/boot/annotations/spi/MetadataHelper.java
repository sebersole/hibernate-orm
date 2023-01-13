/*
 * Hibernate, Relational Persistence for Idiomatic Java
 *
 * License: GNU Lesser General Public License (LGPL), version 2.1 or later.
 * See the lgpl.txt file in the root directory or http://www.gnu.org/licenses/lgpl-2.1.html.
 */
package org.hibernate.boot.annotations.spi;

import org.hibernate.boot.annotations.AnnotationSourceLogging;
import org.hibernate.boot.annotations.source.spi.AnnotationDescriptor;
import org.hibernate.boot.annotations.source.spi.AnnotationTarget;
import org.hibernate.boot.annotations.source.spi.AnnotationUsage;
import org.hibernate.boot.annotations.source.spi.ManagedClass;
import org.hibernate.boot.annotations.source.spi.MethodDetails;
import org.hibernate.boot.model.CustomSql;
import org.hibernate.engine.spi.ExecuteUpdateResultCheckStyle;

/**
 * @author Steve Ebersole
 */
public class MetadataHelper {
	public static CustomSql extractCustomSql(AnnotationUsage<?> customSqlAnnotation) {
		if ( customSqlAnnotation == null ) {
			return null;
		}

		final String sql = customSqlAnnotation.getAttributeValue( "sql" ).asString();
		final boolean isCallable = customSqlAnnotation.getValueAttributeValue().asBoolean();

		final AnnotationUsage.AttributeValue checkValue = customSqlAnnotation.getAttributeValue( "check" );
		final ExecuteUpdateResultCheckStyle checkStyle;
		if ( checkValue == null ) {
			checkStyle = isCallable
					? ExecuteUpdateResultCheckStyle.NONE
					: ExecuteUpdateResultCheckStyle.COUNT;
		}
		else {
			checkStyle = ExecuteUpdateResultCheckStyle.fromResultCheckStyle( checkValue.getValue() );
		}

		return new CustomSql( sql, isCallable, checkStyle );
	}

	/**
	 * Find the name of the method in the class (described by the descriptor) that
	 * is annotated with the given lifecycle callback annotation.
	 *
	 * @param callbackClassInfo The descriptor for the class in which to find
	 * the lifecycle callback method
	 * @param eventAnnotationType The type of lifecycle callback to look for
	 * @param listener Is the {@code callbackClassInfo} a listener, as opposed to
	 * an Entity/MappedSuperclass?  Used here to validate method signatures.
	 *
	 * @return The name of the callback method, or {@code null} indicating none was found
	 */
	public static MethodDetails findCallback(
			ManagedClass callbackClassInfo,
			AnnotationDescriptor<?> eventAnnotationType,
			boolean listener) {
		final Iterable<? extends AnnotationUsage<?>> listenerAnnotations = callbackClassInfo.getAnnotations( eventAnnotationType );
		for ( AnnotationUsage<?> listenerAnnotation : listenerAnnotations ) {
			final AnnotationTarget annotationTarget = listenerAnnotation.getAnnotationTarget();
			if ( ! (annotationTarget instanceof MethodDetails ) ) {
				AnnotationSourceLogging.ANNOTATION_SOURCE_LOGGER.debugf(
						"Skipping callback annotation [%s] for class [%s] as it was " +
								"applied to target other than a method : %s",
						eventAnnotationType.getAnnotationType(),
						callbackClassInfo.getName(),
						annotationTarget
				);
				continue;
			}

			final MethodDetails targetMethod = (MethodDetails) annotationTarget;

			// todo (annotation-source) - validate method arguments

			return targetMethod;
		}

		return null;
	}

	private MetadataHelper() {
		// disallow direct instantiation
	}
}
