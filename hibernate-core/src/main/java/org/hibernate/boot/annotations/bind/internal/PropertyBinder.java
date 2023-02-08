/*
 * Hibernate, Relational Persistence for Idiomatic Java
 *
 * License: GNU Lesser General Public License (LGPL), version 2.1 or later.
 * See the lgpl.txt file in the root directory or http://www.gnu.org/licenses/lgpl-2.1.html.
 */
package org.hibernate.boot.annotations.bind.internal;

import java.util.Locale;
import java.util.function.Function;

import org.hibernate.MappingException;
import org.hibernate.boot.annotations.model.spi.AttributeMetadata;
import org.hibernate.boot.annotations.model.spi.LocalAnnotationProcessingContext;
import org.hibernate.boot.annotations.model.spi.ManagedTypeMetadata;
import org.hibernate.boot.annotations.source.spi.AnnotationUsage;
import org.hibernate.boot.annotations.source.spi.JpaAnnotations;
import org.hibernate.mapping.BasicValue;
import org.hibernate.mapping.Property;
import org.hibernate.mapping.Table;

import jakarta.persistence.Column;

/**
 * Responsible for building {@link Property} references
 *
 * @author Steve Ebersole
 */
public class PropertyBinder {
	public static Property buildProperty(
			AttributeMetadata attributeMetadata,
			ManagedTypeMetadata declaringTypeMetadata,
			Table implicitTable,
			Function<String,Table> tableLocator) {
		switch ( attributeMetadata.getNature() ) {
			case BASIC: {
				return buildBasicProperty( attributeMetadata, declaringTypeMetadata, implicitTable, tableLocator );
			}
			case EMBEDDED: {
				return buildEmbeddedProperty( attributeMetadata, declaringTypeMetadata );
			}
			case ANY: {
				return buildAnyProperty( attributeMetadata, declaringTypeMetadata );
			}
			case TO_ONE: {
				return buildToOneProperty( attributeMetadata, declaringTypeMetadata );
			}
			case PLURAL: {
				return buildPluralProperty( attributeMetadata, declaringTypeMetadata );
			}
		}

		throw new MappingException(
				String.format(
						Locale.ROOT,
						"Unknown attribute nature for `%s.%s` : %s",
						declaringTypeMetadata.getManagedClass().getName(),
						attributeMetadata.getName(),
						attributeMetadata.getNature()
				)
		);
	}

	private static Property buildBasicProperty(
			AttributeMetadata attributeMetadata,
			ManagedTypeMetadata declaringTypeMetadata,
			Table implicitTable,
			Function<String, Table> tableLocator) {
		final LocalAnnotationProcessingContext processingContext = declaringTypeMetadata.getLocalProcessingContext();
		final Property property = new Property();

		final AnnotationUsage<Column> columnAnnotation = attributeMetadata.getMember().getAnnotation( JpaAnnotations.COLUMN );
		final Table table = determineColumnTable( columnAnnotation, implicitTable, tableLocator, processingContext );

		final BasicValue valueMapping = new BasicValue( processingContext.getMetadataBuildingContext() );
		valueMapping.setTable( table );

		property.setValue( valueMapping );
		property.setInsertable( BindingHelper.extractValue( columnAnnotation, "insertable", true ) );
		property.setUpdateable( BindingHelper.extractValue( columnAnnotation, "updatable", true ) );

		return property;
	}

	private static Table determineColumnTable(
			AnnotationUsage<Column> columnAnnotation,
			Table implicitTable,
			Function<String,Table> tableLocator,
			LocalAnnotationProcessingContext processingContext) {
		final String tableName = BindingHelper.extractValue( columnAnnotation, "table" );
		if ( tableName == null ) {
			return implicitTable;
		}

		return tableLocator.apply( tableName );
	}

	private static Property buildEmbeddedProperty(
			AttributeMetadata attributeMetadata,
			ManagedTypeMetadata managedTypeMetadata) {
		return null;
	}

	private static Property buildAnyProperty(
			AttributeMetadata attributeMetadata,
			ManagedTypeMetadata managedTypeMetadata) {
		return null;
	}

	private static Property buildToOneProperty(
			AttributeMetadata attributeMetadata,
			ManagedTypeMetadata managedTypeMetadata) {
		return null;
	}

	private static Property buildPluralProperty(
			AttributeMetadata attributeMetadata,
			ManagedTypeMetadata managedTypeMetadata) {
		return null;
	}
}
