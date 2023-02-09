/*
 * Hibernate, Relational Persistence for Idiomatic Java
 *
 * License: GNU Lesser General Public License (LGPL), version 2.1 or later.
 * See the lgpl.txt file in the root directory or http://www.gnu.org/licenses/lgpl-2.1.html.
 */
package org.hibernate.boot.annotations.bind.internal;

import java.util.function.Supplier;

import org.hibernate.boot.annotations.model.spi.LocalAnnotationProcessingContext;
import org.hibernate.boot.annotations.source.internal.AnnotationsHelper;
import org.hibernate.boot.annotations.source.spi.AnnotationUsage;
import org.hibernate.mapping.Column;

/**
 * @author Steve Ebersole
 */
public class ColumnBinder {
	public static Column bindColumn(
			AnnotationUsage<?> annotationUsage,
			Supplier<String> defaultNameSupplier,
			LocalAnnotationProcessingContext processingContext) {
		return bindColumn(
				annotationUsage,
				defaultNameSupplier,
				false,
				true,
				255,
				0,
				0,
				processingContext
		);
	}

	public static Column bindColumn(
			AnnotationUsage<?> annotationUsage,
			Supplier<String> defaultNameSupplier,
			boolean uniqueByDefault,
			boolean nullableByDefault,
			long lengthByDefault,
			int precisionByDefault,
			int scaleByDefault,
			LocalAnnotationProcessingContext processingContext) {
		final Column result = new Column();
		result.setName( columnName( annotationUsage, defaultNameSupplier, processingContext ) );
		result.setUnique( extractValue( annotationUsage, "unique", uniqueByDefault ) );
		result.setNullable( extractValue( annotationUsage, "nullable", nullableByDefault ) );
		result.setSqlType( extractValue( annotationUsage, "columnDefinition", null ) );
		result.setLength( extractValue( annotationUsage, "length", lengthByDefault ) );
		result.setPrecision( extractValue( annotationUsage, "precision", precisionByDefault ) );
		result.setScale( extractValue( annotationUsage, "scale", scaleByDefault ) );
		return result;
	}

	private static <T> T extractValue(AnnotationUsage<?> usage, String attrName, T defaultValue) {
		return AnnotationsHelper.getValue( usage.getAttributeValue( attrName ), defaultValue );
	}

	private static String columnName(
			AnnotationUsage<?> columnAnnotation,
			Supplier<String> defaultNameSupplier,
			LocalAnnotationProcessingContext processingContext) {
		return AnnotationsHelper.getValue( columnAnnotation.getAttributeValue( "name" ), defaultNameSupplier );
	}

	private ColumnBinder() {
	}
}
