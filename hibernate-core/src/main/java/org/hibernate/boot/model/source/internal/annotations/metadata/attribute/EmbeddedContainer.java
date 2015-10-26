/*
 * Hibernate, Relational Persistence for Idiomatic Java
 *
 * License: GNU Lesser General Public License (LGPL), version 2.1 or later.
 * See the lgpl.txt file in the root directory or <http://www.gnu.org/licenses/lgpl-2.1.html>.
 */
package org.hibernate.boot.model.source.internal.annotations.metadata.attribute;

import org.hibernate.boot.model.MemberDescriptor;
import org.hibernate.boot.model.source.internal.annotations.ConvertConversionInfo;
import org.hibernate.boot.model.source.spi.AttributePath;
import org.hibernate.boot.model.source.spi.NaturalIdMutability;

/**
 * Defines the container of an embedded value.  Acts as the container for
 * a EmbeddableTypeMetadata, bridging back to the "embedded".
 *
 * @author Steve Ebersole
 */
public interface EmbeddedContainer extends OverrideAndConverterCollector {
	MemberDescriptor getBackingMember();

	ConvertConversionInfo locateConversionInfo(AttributePath attributePath);

	AttributeOverride locateAttributeOverride(AttributePath attributePath);

	AssociationOverride locateAssociationOverride(AttributePath attributePath);

	NaturalIdMutability getContainerNaturalIdMutability();

	boolean getContainerOptionality();
	boolean getContainerUpdatability();
	boolean getContainerInsertability();
}
