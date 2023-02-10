/*
 * Hibernate, Relational Persistence for Idiomatic Java
 *
 * License: GNU Lesser General Public License (LGPL), version 2.1 or later.
 * See the lgpl.txt file in the root directory or http://www.gnu.org/licenses/lgpl-2.1.html.
 */
package org.hibernate.boot.annotations.bind.internal;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.Properties;
import java.util.function.Supplier;

import org.hibernate.annotations.Parameter;
import org.hibernate.boot.annotations.source.internal.AnnotationsHelper;
import org.hibernate.boot.annotations.source.spi.AnnotationUsage;
import org.hibernate.boot.model.CustomSql;
import org.hibernate.engine.spi.ExecuteUpdateResultCheckStyle;
import org.hibernate.internal.util.collections.ArrayHelper;

/**
 * @author Steve Ebersole
 */
public final class BindingHelper {
	/**
	 * Build a CustomSql reference from {@link org.hibernate.annotations.SQLInsert},
	 * {@link org.hibernate.annotations.SQLUpdate}, {@link org.hibernate.annotations.SQLDelete}
	 * or {@link org.hibernate.annotations.SQLDeleteAll} annotations
	 */
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

	public static <T> T extractValue(AnnotationUsage<?> usage, String attrName) {
		if ( usage == null ) {
			return null;
		}
		return AnnotationsHelper.getValueOrNull( usage.getAttributeValue( attrName ) );
	}

	public static <T> T extractValue(AnnotationUsage<?> usage, String attrName, T defaultValue) {
		if ( usage == null ) {
			return defaultValue;
		}
		return AnnotationsHelper.getValue( usage.getAttributeValue( attrName ), defaultValue );
	}

	public static <T> T extractValue(AnnotationUsage<?> usage, String attrName, Supplier<T> defaultValueSupplier) {
		if ( usage == null ) {
			return defaultValueSupplier.get();
		}
		return AnnotationsHelper.getValue( usage.getAttributeValue( attrName ), defaultValueSupplier );
	}

	public static Properties extractProperties(AnnotationUsage<Parameter>[] parameters) {
		final Properties result = new Properties();
		if ( parameters != null ) {
			for ( int i = 0; i < parameters.length; i++ ) {
				final AnnotationUsage<Parameter> property = parameters[ i ];
				result.setProperty(
						extractValue( property, "name" ),
						extractValue( property, "value" )
				);
			}
		}
		return result;
	}

	public static Map<String,String> extractParameterMap(AnnotationUsage<Parameter>[] parameters) {
		if ( ArrayHelper.isEmpty( parameters ) ) {
			return Collections.emptyMap();
		}

		final Map<String,String> result = new HashMap<>();
		for ( int i = 0; i < parameters.length; i++ ) {
			final AnnotationUsage<Parameter> property = parameters[ i ];
			result.put(
					extractValue( property, "name" ),
					extractValue( property, "value" )
			);
		}
		return result;
	}

//	public static BasicValue extractDiscriminatorValue(
//			EntityTypeMetadata entityTypeMetadata) {
//		assert entityTypeMetadata.getHierarchy().getRoot() == entityTypeMetadata;
//
//		final InheritanceType inheritanceType = entityTypeMetadata.getHierarchy().getInheritanceType();
//		final AnnotationUsage<DiscriminatorColumn> annotation = entityTypeMetadata
//				.getManagedClass()
//				.getAnnotation( JpaAnnotations.DISCRIMINATOR_COLUMN );
//
//		final boolean createDiscriminator;
//		if ( annotation != null ) {
//			// explicitly defined
//			createDiscriminator = true;
//		}
//		else if ( inheritanceType == InheritanceType.SINGLE_TABLE && entityTypeMetadata.hasSubTypes() ) {
//			createDiscriminator = true;
//		}
//		else {
//			createDiscriminator = false;
//		}
//
//		if ( !createDiscriminator ) {
//			return null;
//		}
//
//		final BasicValue discriminatorValue = new BasicValue( entityTypeMetadata.getLocalProcessingContext() );
//		final Column column = makeDiscriminatorColumn( annotation );
//
//		if ( annotation != null ) {
//			final BasicValue discriminatorValue = new BasicValue( entityTypeMetadata.getLocalProcessingContext() );
//			final Column
//			discriminatorValue.col
//		}
//
//	}
//
//	private static Column makeDiscriminatorColumn(AnnotationUsage<DiscriminatorColumn> annotation) {
//
//	}

	private BindingHelper() {
	}
}
