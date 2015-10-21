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
package org.hibernate.boot.model.source.internal.annotations.metadata.attribute;

import org.hibernate.boot.jandex.spi.HibernateDotNames;
import org.hibernate.boot.jandex.spi.JpaDotNames;
import org.hibernate.boot.model.MemberDescriptor;
import org.hibernate.boot.model.source.internal.annotations.impl.ConvertConversionInfo;
import org.hibernate.boot.model.source.internal.annotations.metadata.type.EmbeddableTypeMetadata;
import org.hibernate.boot.model.source.internal.annotations.metadata.type.EntityBindingContext;
import org.hibernate.boot.model.source.spi.AttributePath;
import org.hibernate.boot.model.source.spi.AttributeRole;
import org.hibernate.boot.model.source.spi.CollectionNature;
import org.hibernate.boot.model.source.spi.NaturalIdMutability;
import org.hibernate.boot.model.source.spi.PluralAttributeElementNature;

import org.jboss.jandex.AnnotationInstance;
import org.jboss.jandex.AnnotationValue;
import org.jboss.jandex.ClassInfo;

/**
 * @author Steve Ebersole
 */
public class PluralAttributeElementDetailsEmbedded implements PluralAttributeElementDetails, EmbeddedContainer {
	private final PluralAttribute pluralAttribute;

	private final ClassInfo javaType;
	private final EmbeddableTypeMetadata embeddableTypeMetadata;

	public PluralAttributeElementDetailsEmbedded(
			PluralAttribute pluralAttribute,
			ClassInfo inferredElementType) {
		this.pluralAttribute = pluralAttribute;
		this.javaType = determineJavaType( pluralAttribute, inferredElementType );

		if ( this.javaType == null ) {
			throw pluralAttribute.getContext().makeMappingException(
					"Could not determine element type information for plural attribute : "
							+ pluralAttribute.getBackingMember().toString()
			);
		}

		this.embeddableTypeMetadata = buildEmbeddedMetadata( pluralAttribute, javaType );
	}

	private static ClassInfo determineJavaType(
			PluralAttribute pluralAttribute,
			ClassInfo inferredElementType) {
		final EntityBindingContext context = pluralAttribute.getContext();
		final AnnotationInstance targetAnnotation = pluralAttribute.memberAnnotationMap().get( HibernateDotNames.TARGET );
		if ( targetAnnotation != null ) {
			final AnnotationValue targetValue = targetAnnotation.value();
			if ( targetValue != null ) {
				return context.getJandexIndex().getClassByName( targetValue.asClass().name() );
			}
		}

		final AnnotationInstance elementCollectionAnnotation = pluralAttribute.memberAnnotationMap().get( JpaDotNames.ELEMENT_COLLECTION );
		if ( elementCollectionAnnotation != null ) {
			final AnnotationValue targetClassValue = elementCollectionAnnotation.value( "targetClass" );
			if ( targetClassValue != null ) {
				return context.getJandexIndex().getClassByName( targetClassValue.asClass().name() );
			}
		}

		return inferredElementType;
	}

	private EmbeddableTypeMetadata buildEmbeddedMetadata(PluralAttribute pluralAttribute, ClassInfo javaType) {
		final boolean isMap = pluralAttribute.getCollectionNature() == CollectionNature.MAP;
		final AttributeRole role = isMap
				? pluralAttribute.getRole().append( "value" )
				: pluralAttribute.getRole().append( "element" );
		final AttributePath path = isMap
				? pluralAttribute.getPath().append( "value" )
				: pluralAttribute.getPath();

		// we pass `this` (as EmbeddedContainer) in order to route calls back properly.
		return new EmbeddableTypeMetadata(
				javaType,
				this,
				role,
				path,
				pluralAttribute.getAccessType(),
				pluralAttribute.getAccessorStrategy(),
				pluralAttribute.getContext().getRootAnnotationBindingContext()
		);
	}

	@Override
	public ClassInfo getJavaType() {
		return javaType;
	}

	@Override
	public PluralAttributeElementNature getElementNature() {
		return PluralAttributeElementNature.AGGREGATE;
	}

	public EmbeddableTypeMetadata getEmbeddableTypeMetadata() {
		return embeddableTypeMetadata;
	}

	// ~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~
	// EmbeddedContainer impl

	@Override
	public MemberDescriptor getBackingMember() {
		return pluralAttribute.getBackingMember();
	}

	@Override
	public ConvertConversionInfo locateConversionInfo(AttributePath attributePath) {
		return pluralAttribute.getContainer().locateConversionInfo( attributePath );
	}

	@Override
	public AttributeOverride locateAttributeOverride(AttributePath attributePath) {
		return pluralAttribute.getContainer().locateAttributeOverride( attributePath );
	}

	@Override
	public AssociationOverride locateAssociationOverride(AttributePath attributePath) {
		return pluralAttribute.getContainer().locateAssociationOverride( attributePath );
	}

	@Override
	public NaturalIdMutability getContainerNaturalIdMutability() {
		return null;
	}

	@Override
	public boolean getContainerOptionality() {
		return false;
	}

	@Override
	public boolean getContainerUpdatability() {
		return true;
	}

	@Override
	public boolean getContainerInsertability() {
		return true;
	}

	@Override
	public void registerConverter(AttributePath attributePath, ConvertConversionInfo conversionInfo) {
		pluralAttribute.getContainer().registerConverter( attributePath, conversionInfo );
	}

	@Override
	public void registerAttributeOverride(AttributePath attributePath, AttributeOverride override) {
		pluralAttribute.getContainer().registerAttributeOverride( attributePath, override );
	}

	@Override
	public void registerAssociationOverride(AttributePath attributePath, AssociationOverride override) {
		pluralAttribute.getContainer().registerAssociationOverride( attributePath, override );
	}
}
