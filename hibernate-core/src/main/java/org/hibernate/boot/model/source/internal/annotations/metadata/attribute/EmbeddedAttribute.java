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

import javax.persistence.AccessType;

import org.hibernate.boot.jandex.spi.HibernateDotNames;
import org.hibernate.boot.model.MemberDescriptor;
import org.hibernate.boot.model.source.internal.annotations.impl.ConvertConversionInfo;
import org.hibernate.boot.model.source.internal.annotations.metadata.type.EmbeddableTypeMetadata;
import org.hibernate.boot.model.source.internal.annotations.metadata.type.ManagedTypeMetadata;
import org.hibernate.boot.model.source.spi.AttributePath;
import org.hibernate.boot.model.source.spi.AttributeRole;
import org.hibernate.boot.model.source.spi.NaturalIdMutability;
import org.hibernate.mapping.PropertyGeneration;

import org.jboss.jandex.AnnotationInstance;
import org.jboss.jandex.ClassInfo;

/**
 * Represents a singular persistent attribute that is Embedded
 *
 * @author Steve Ebersole
 */
public class EmbeddedAttribute extends AbstractSingularAttribute implements EmbeddedContainer {
	private final EmbeddableTypeMetadata embeddedDelegate;

	private final ColumnInclusion insertability;
	private final ColumnInclusion updateability;

	public EmbeddedAttribute(
			ManagedTypeMetadata managedTypeMetadata,
			String attributeName,
			AttributePath attributePath,
			AttributeRole attributeRole,
			MemberDescriptor backingMember,
			AccessType accessType,
			String accessorStrategy) {
		super(
				managedTypeMetadata,
				attributeName,
				attributePath,
				attributeRole,
				backingMember,
				AttributeNature.EMBEDDED,
				accessType,
				accessorStrategy
		);

		this.insertability = new ColumnInclusion( managedTypeMetadata.canAttributesBeInsertable() );
		this.updateability = new ColumnInclusion( managedTypeMetadata.canAttributesBeUpdatable() );

		// See if the user specified a @Target annotation to name a
		// more-specific type
		ClassInfo embeddableType = getContext().getJandexIndex().getClassByName( backingMember.type().name() );
		final AnnotationInstance targetAnnotation = memberAnnotationMap().get( HibernateDotNames.TARGET );
		if ( targetAnnotation != null ) {
			embeddableType = getContext().getJandexIndex().getClassByName(
					targetAnnotation.value().asClass().name()
			);
		}

		// we pass `this` (as EmbeddedContainer) in order to route calls back properly.
		this.embeddedDelegate = new EmbeddableTypeMetadata(
				embeddableType,
				this,
				attributeRole,
				attributePath,
				accessType,
				accessorStrategy,
				managedTypeMetadata.getLocalBindingContext().getRootAnnotationBindingContext()
		);

		if ( isId() ) {
			updateability.disable();
		}

		if ( getNaturalIdMutability() == NaturalIdMutability.IMMUTABLE ) {
			updateability.disable();
		}
	}

	public EmbeddableTypeMetadata getEmbeddableTypeMetadata() {
		return embeddedDelegate;
	}


	// EmbeddedContainer ~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~

	@Override
	public ConvertConversionInfo locateConversionInfo(AttributePath attributePath) {
		return getContainer().locateConversionInfo( attributePath );
	}

	@Override
	public AttributeOverride locateAttributeOverride(AttributePath attributePath) {
		return getContainer().locateAttributeOverride( attributePath );
	}

	@Override
	public AssociationOverride locateAssociationOverride(AttributePath attributePath) {
		return getContainer().locateAssociationOverride( attributePath );
	}

	@Override
	public void registerConverter(AttributePath attributePath, ConvertConversionInfo conversionInfo) {
		getContainer().registerConverter( attributePath, conversionInfo );
	}

	@Override
	public void registerAttributeOverride(AttributePath attributePath, AttributeOverride override) {
		getContainer().registerAttributeOverride( attributePath, override );
	}

	@Override
	public void registerAssociationOverride(AttributePath attributePath, AssociationOverride override) {
		getContainer().registerAssociationOverride( attributePath, override );
	}

	@Override
	public NaturalIdMutability getContainerNaturalIdMutability() {
		return super.getNaturalIdMutability();
	}

	@Override
	public boolean getContainerOptionality() {
		return isOptional();
	}

	@Override
	public boolean getContainerUpdatability() {
		return isUpdatable();
	}

	@Override
	public boolean getContainerInsertability() {
		return isInsertable();
	}


	// PersistentAttribute ~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~

	// todo : implement these

	@Override
	public boolean isLazy() {
		return false;
	}

	@Override
	public boolean isOptional() {
		return false;
	}

	@Override
	public boolean isInsertable() {
		return insertability.shouldInclude();
	}

	@Override
	public boolean isUpdatable() {
		return updateability.shouldInclude();
	}


	@Override
	public PropertyGeneration getPropertyGeneration() {
		return null;
	}
}
