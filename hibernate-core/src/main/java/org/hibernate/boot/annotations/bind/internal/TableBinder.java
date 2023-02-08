/*
 * Hibernate, Relational Persistence for Idiomatic Java
 *
 * License: GNU Lesser General Public License (LGPL), version 2.1 or later.
 * See the lgpl.txt file in the root directory or http://www.gnu.org/licenses/lgpl-2.1.html.
 */
package org.hibernate.boot.annotations.bind.internal;

import java.util.function.Supplier;

import org.hibernate.boot.annotations.model.spi.LocalAnnotationProcessingContext;
import org.hibernate.boot.annotations.source.spi.AnnotationUsage;
import org.hibernate.boot.spi.InFlightMetadataCollector;
import org.hibernate.mapping.Table;

import static org.hibernate.internal.util.NullnessHelper.coalesceSuppliedValues;

/**
 * @author Steve Ebersole
 */
public class TableBinder {
	public static Table bindTable(
			AnnotationUsage<?> tableAnnotation,
			Supplier<String> implicitTableNameSupplier,
			boolean isAbstract,
			LocalAnnotationProcessingContext processingContext) {
		final InFlightMetadataCollector metadataCollector = processingContext
				.getMetadataBuildingContext()
				.getMetadataCollector();
		return metadataCollector.addTable(
				schemaName( tableAnnotation, processingContext ),
				catalogName( tableAnnotation, processingContext ),
				BindingHelper.extractValue( tableAnnotation, "name", implicitTableNameSupplier ),
				null,
				isAbstract,
				processingContext.getMetadataBuildingContext()
		);
	}

	public static String catalogName(
			AnnotationUsage<?> tableAnnotation,
			LocalAnnotationProcessingContext processingContext) {
		if ( tableAnnotation != null ) {
			return coalesceSuppliedValues(
					() -> tableAnnotation.getAttributeValue( "catalog" ).getValue(),
					() -> processingContext.getMetadataBuildingContext().getMappingDefaults().getImplicitCatalogName()
			);
		}

		return processingContext.getMetadataBuildingContext().getMappingDefaults().getImplicitCatalogName();
	}

	public static String schemaName(
			AnnotationUsage<?> tableAnnotation,
			LocalAnnotationProcessingContext processingContext) {
		if ( tableAnnotation != null ) {
			return coalesceSuppliedValues(
					() -> tableAnnotation.getAttributeValue( "schema" ).getValue(),
					() -> processingContext.getMetadataBuildingContext().getMappingDefaults().getImplicitSchemaName()
			);
		}
		return processingContext.getMetadataBuildingContext().getMappingDefaults().getImplicitSchemaName();
	}

	private TableBinder() {
	}
}
