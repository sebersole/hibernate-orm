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

import org.hibernate.boot.model.MemberDescriptor;
import org.hibernate.boot.model.source.internal.annotations.impl.ConvertConversionInfo;
import org.hibernate.boot.model.source.internal.annotations.metadata.type.EmbeddableTypeMetadata;
import org.hibernate.boot.model.source.spi.AttributePath;
import org.hibernate.boot.model.source.spi.NaturalIdMutability;
import org.hibernate.boot.model.source.spi.PluralAttributeIndexNature;

import org.jboss.jandex.ClassInfo;

/**
 * @author Steve Ebersole
 */
public class PluralAttributeIndexDetailsMapKeyEmbedded
		extends AbstractPluralAttributeIndexDetailsMapKey
		implements EmbeddedContainer {

	private final PluralAttribute pluralAttribute;
	private final EmbeddableTypeMetadata embeddableTypeMetadata;

	public PluralAttributeIndexDetailsMapKeyEmbedded(
			PluralAttribute pluralAttribute,
			MemberDescriptor backingMember,
			ClassInfo resolvedMapKeyType) {
		super( pluralAttribute, backingMember, resolvedMapKeyType );
		this.pluralAttribute = pluralAttribute;

		// we pass `this` (as EmbeddedContainer) in order to route calls back properly.
		this.embeddableTypeMetadata = new EmbeddableTypeMetadata(
				resolvedMapKeyType,
				this,
				pluralAttribute.getRole().append( "key" ),
				pluralAttribute.getPath().append( "key" ),
				pluralAttribute.getAccessType(),
				pluralAttribute.getAccessorStrategy(),
				pluralAttribute.getContext().getRootAnnotationBindingContext()
		);
	}

	public EmbeddableTypeMetadata getEmbeddableTypeMetadata() {
		return embeddableTypeMetadata;
	}

	@Override
	public PluralAttributeIndexNature getIndexNature() {
		return PluralAttributeIndexNature.AGGREGATE;
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
