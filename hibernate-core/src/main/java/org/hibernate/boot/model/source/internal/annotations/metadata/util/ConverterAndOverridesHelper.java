/*
 * Hibernate, Relational Persistence for Idiomatic Java
 *
 * License: GNU Lesser General Public License (LGPL), version 2.1 or later.
 * See the lgpl.txt file in the root directory or <http://www.gnu.org/licenses/lgpl-2.1.html>.
 */

/*
 * Hibernate, Relational Persistence for Idiomatic Java
 *
 * Copyright (c) 2014, Red Hat Inc. or third-party contributors as
 * indicated by the @author tags or express copyright attribution
 * statements applied by the authors.  All third-party contributions are
 * distributed under license by Red Hat Inc.
 *
 * This copyrighted material is made available to anyone wishing to use, modify,
 * copy, or redistribute it subject to the terms and conditions of the GNU
 * Lesser General Public License, as published by the Free Software Foundation.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY
 * or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU Lesser General Public License
 * for more details.
 *
 * You should have received a copy of the GNU Lesser General Public License
 * along with this distribution; if not, write to:
 * Free Software Foundation, Inc.
 * 51 Franklin Street, Fifth Floor
 * Boston, MA  02110-1301  USA
 */
package org.hibernate.boot.model.source.internal.annotations.metadata.util;

import java.util.Map;
import java.util.regex.Pattern;

import org.hibernate.boot.jandex.spi.JpaDotNames;
import org.hibernate.boot.model.source.internal.annotations.AnnotationBindingContext;
import org.hibernate.boot.model.source.internal.annotations.impl.ConvertConversionInfo;
import org.hibernate.boot.model.source.internal.annotations.metadata.attribute.AssociationOverride;
import org.hibernate.boot.model.source.internal.annotations.metadata.attribute.AttributeOverride;
import org.hibernate.boot.model.source.internal.annotations.metadata.attribute.OverrideAndConverterCollector;
import org.hibernate.boot.model.source.internal.annotations.metadata.attribute.PersistentAttribute;
import org.hibernate.boot.model.source.internal.annotations.metadata.attribute.PluralAttribute;
import org.hibernate.boot.model.source.internal.annotations.metadata.type.EntityBindingContext;
import org.hibernate.boot.model.source.internal.annotations.metadata.type.ManagedTypeMetadata;
import org.hibernate.boot.model.source.spi.AttributePath;
import org.hibernate.boot.model.source.spi.PluralAttributeElementNature;
import org.hibernate.internal.util.StringHelper;

import org.jboss.jandex.AnnotationInstance;
import org.jboss.jandex.AnnotationValue;
import org.jboss.jandex.DotName;
import org.jboss.logging.Logger;

/**
 * Helper for working with AttributeConverters, AttributeOverrides and AssociationOverrides
 *
 * @author Steve Ebersole
 */
public class ConverterAndOverridesHelper {
	private static final Logger log = Logger.getLogger( ConverterAndOverridesHelper.class );

	private ConverterAndOverridesHelper() {
	}


	/**
	 * Process Convert/Converts annotations found on the member for an attribute.
	 *
	 * @param attribute The (still in-flight) attribute definition
	 */
	public static void processConverters(PersistentAttribute attribute) {
		final AnnotationBindingContext context = attribute.getContext();
		final Map<DotName,AnnotationInstance> memberAnnotationMap = context.getMemberAnnotationInstances( attribute.getBackingMember() );

		{
			final AnnotationInstance convertAnnotation = memberAnnotationMap.get( JpaDotNames.CONVERT );
			if ( convertAnnotation != null ) {
				processConverter(
						convertAnnotation,
						attribute.getPath(),
						attribute.getContainer(),
						attribute.getContext(),
						isBasicValue( attribute ),
						isBasicElementCollection( attribute )
				);
			}
		}

		{
			final AnnotationInstance convertsAnnotation = memberAnnotationMap.get( JpaDotNames.CONVERTS );
			if ( convertsAnnotation != null ) {
				final AnnotationInstance[] convertAnnotations = context.getTypedValueExtractor( AnnotationInstance[].class ).extract(
						convertsAnnotation,
						"value"
				);
				for ( AnnotationInstance convertAnnotation : convertAnnotations ) {
					processConverter(
							convertAnnotation,
							attribute.getPath(),
							attribute.getContainer(),
							attribute.getContext(),
							isBasicValue( attribute ),
							isBasicElementCollection( attribute )
					);
				}
			}
		}
	}

	private static void processConverter(
			AnnotationInstance convertAnnotation,
			AttributePath attributePath,
			OverrideAndConverterCollector container,
			EntityBindingContext context,
			boolean isBasicValue,
			boolean isBasicCollectionElement) {
		final ConvertConversionInfo conversionInfo;

		final AnnotationValue isDisabledValue = convertAnnotation.value( "disableConversion" );
		final boolean isDisabled = isDisabledValue != null && isDisabledValue.asBoolean();
		if ( isDisabled ) {
			// no need to even touch the class
			conversionInfo = new ConvertConversionInfo( false, null );
		}
		else {
			final AnnotationValue converterClassNameValue = convertAnnotation.value( "converter" );
			// if `converterValue` is null, not sure this annotation really serves any purpose...
			String converterClassName = converterClassNameValue == null
					? null
					: converterClassNameValue.asString();
			if ( "void".equals( converterClassName ) ) {
				converterClassName = null;
			}

			if ( converterClassName == null ) {
				// again, at this point not really sure what purpose this annotation serves

				return;
			}
			conversionInfo = new ConvertConversionInfo(
					true,
					context.getJandexIndex().getClassByName(
							DotName.createSimple( converterClassName )
					)
			);
		}

		final AnnotationValue specifiedNameValue = convertAnnotation.value( "attributeName" );
		final String specifiedName = specifiedNameValue == null
				? null
				: StringHelper.nullIfEmpty( specifiedNameValue.asString() );

		if ( specifiedName == null ) {
			// No attribute name was specified.  According to the spec, this is
			// ok (in fact expected) if the attribute is basic in nature
			if ( !isBasicValue && !isBasicCollectionElement ) {
				// now here we CAN throw the exception
				throw context.makeMappingException(
						"Found @Convert with no `attributeName` specified on a non-basic attribute : " +
								convertAnnotation.target().toString()
				);
			}
			container.registerConverter( attributePath, conversionInfo );
		}
		else {
			// An attribute name was specified.  Technically we should make
			// sure that the attribute is NOT basic in nature.  Collections are
			// a bit difficult here since the element/map-key could be basic while
			// the converter refers to the other which is non-basic
			if ( isBasicValue ) {
				log.debugf(
						"Found @Convert with `attributeName` specified (%s) on a non-basic attribute : %s",
						specifiedName,
						convertAnnotation.target().toString()
				);
			}
			container.registerConverter(
					buildAttributePath( attributePath, specifiedName ),
					conversionInfo
			);
		}
	}

	private static boolean isBasicValue(PersistentAttribute attribute) {
		return attribute.getAttributeNature() == PersistentAttribute.AttributeNature.BASIC;
	}

	private static boolean isBasicElementCollection(PersistentAttribute attribute) {
		if ( attribute.getAttributeNature() == PersistentAttribute.AttributeNature.BASIC ) {
			return true;
		}

		if ( attribute.getAttributeNature() == PersistentAttribute.AttributeNature.PLURAL ) {
			final PluralAttribute pluralAttribute = (PluralAttribute) attribute;
			if ( pluralAttribute.getElementDetails().getElementNature() == PluralAttributeElementNature.BASIC ) {
				return true;
			}
		}

		return false;
	}

	private static AttributePath buildAttributePath(AttributePath base, String specifiedName) {
		assert base != null;
		assert specifiedName != null;

		// these names can contain paths themselves...
		AttributePath pathSoFar = base;
		for ( String pathPart : specifiedName.split( Pattern.quote( "." ) ) ) {
			pathSoFar = pathSoFar.append( pathPart );
		}

		return pathSoFar;
	}


	/**
	 * Process AttributeConverter (plus plural) annotations found on the member for an attribute.
	 *
	 * @param attribute The (still in-flight) attribute definition
	 */
	public static void processAttributeOverrides(PersistentAttribute attribute) {
		final EntityBindingContext context = attribute.getContext();
		final Map<DotName,AnnotationInstance> memberAnnotationMap = context.getMemberAnnotationInstances( attribute.getBackingMember() );

		{
			final AnnotationInstance override = memberAnnotationMap.get( JpaDotNames.ATTRIBUTE_OVERRIDE );
			if ( override != null ) {
				processAttributeOverride(
						override,
						attribute.getPath(),
						attribute.getContainer(),
						attribute.getContext()
				);
			}
		}

		{
			final AnnotationInstance overrides = memberAnnotationMap.get( JpaDotNames.ATTRIBUTE_OVERRIDES );
			if ( overrides != null ) {
				final AnnotationInstance[] overrideAnnotations = context.getTypedValueExtractor( AnnotationInstance[].class ).extract(
						overrides,
						"value"
				);
				for ( AnnotationInstance overrideAnnotation : overrideAnnotations ) {
					processAttributeOverride(
							overrideAnnotation,
							attribute.getPath(),
							attribute.getContainer(),
							attribute.getContext()
					);
				}
			}
		}
	}


	/**
	 * Process AttributeConverter (plus plural) annotations found at the type (Class) level.
	 *
	 * @param managedType The (still in-flight) type definition
	 */
	public static void processAttributeOverrides(ManagedTypeMetadata managedType) {
		final EntityBindingContext context = managedType.getLocalBindingContext();
		final Map<DotName,AnnotationInstance> memberAnnotationMap = context.getTypeAnnotationInstances(
				managedType.getClassInfo().name()
		);

		{
			final AnnotationInstance override = memberAnnotationMap.get( JpaDotNames.ATTRIBUTE_OVERRIDE );
			if ( override != null ) {
				processAttributeOverride(
						override,
						managedType.getAttributePathBase(),
						managedType,
						managedType.getLocalBindingContext()
				);
			}
		}

		{
			final AnnotationInstance overrides = memberAnnotationMap.get( JpaDotNames.ATTRIBUTE_OVERRIDES );
			if ( overrides != null ) {
				final AnnotationInstance[] overrideAnnotations = context.getTypedValueExtractor( AnnotationInstance[].class ).extract(
						overrides,
						"value"
				);
				for ( AnnotationInstance overrideAnnotation : overrideAnnotations ) {
					processAttributeOverride(
							overrideAnnotation,
							managedType.getAttributePathBase(),
							managedType,
							managedType.getLocalBindingContext()
					);
				}
			}
		}
	}

	private static void processAttributeOverride(
			AnnotationInstance overrideAnnotation,
			AttributePath attributePath,
			OverrideAndConverterCollector container,
			EntityBindingContext context) {
		// the name is required...
		final String specifiedPath = overrideAnnotation.value( "name" ).asString();
		container.registerAttributeOverride(
				buildAttributePath( attributePath, specifiedPath ),
				new AttributeOverride( attributePath.getFullPath(), overrideAnnotation, context )
		);
	}

	public static void processAssociationOverrides(PersistentAttribute attribute) {
		final EntityBindingContext context = attribute.getContext();
		final Map<DotName,AnnotationInstance> memberAnnotationMap = context.getMemberAnnotationInstances( attribute.getBackingMember() );

		{
			final AnnotationInstance overrideAnnotation = memberAnnotationMap.get( JpaDotNames.ASSOCIATION_OVERRIDE );
			if ( overrideAnnotation != null ) {
				processAssociationOverride( overrideAnnotation, attribute );
			}
		}

		{
			final AnnotationInstance overridesAnnotation = memberAnnotationMap.get( JpaDotNames.ASSOCIATION_OVERRIDES );
			if ( overridesAnnotation != null ) {
				final AnnotationInstance[] overrideAnnotations = context.getTypedValueExtractor( AnnotationInstance[].class ).extract(
						overridesAnnotation,
						"value"
				);
				for ( AnnotationInstance overrideAnnotation : overrideAnnotations ) {
					processAssociationOverride( overrideAnnotation, attribute );
				}
			}
		}
	}

	private static void processAssociationOverride(
			AnnotationInstance overrideAnnotation,
			PersistentAttribute attribute) {
		// the name is required...
		final String specifiedPath = overrideAnnotation.value( "name" ).asString();
		attribute.getContainer().registerAssociationOverride(
				buildAttributePath( attribute.getPath(), specifiedPath ),
				new AssociationOverride( attribute.getPath().getFullPath(), overrideAnnotation, attribute.getContext() )
		);
	}

}
