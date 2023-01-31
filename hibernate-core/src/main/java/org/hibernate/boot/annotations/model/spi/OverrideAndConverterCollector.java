/*
 * Hibernate, Relational Persistence for Idiomatic Java
 *
 * License: GNU Lesser General Public License (LGPL), version 2.1 or later.
 * See the lgpl.txt file in the root directory or http://www.gnu.org/licenses/lgpl-2.1.html.
 */
package org.hibernate.boot.annotations.model.spi;

import org.hibernate.boot.model.source.spi.AttributePath;
import org.hibernate.boot.model.source.spi.AttributeRole;

import jakarta.persistence.AccessType;

/**
 * Contract used in normalizing AttributeConverters, AttributeOverrides and
 * AssociationOverrides.
 *
 * @author Steve Ebersole
 */
public interface OverrideAndConverterCollector {
	String getName();
	boolean isAbstract();
//	ManagedTypeMetadata getSuperType();
//	Set<? extends ManagedTypeMetadata> getSubclasses();

	AccessType getAccessType();

	AttributeRole getAttributeRoleBase();
	AttributePath getAttributePathBase();

	void registerConverter(AttributePath attributePath, ConversionMetadata conversionInfo);
	void registerAttributeOverride(AttributePath attributePath, AttributeOverrideMetadata override);
	void registerAssociationOverride(AttributePath attributePath, AssociationOverrideMetadata override);

	ConversionMetadata locateConversionInfo(AttributePath attributePath);
	AttributeOverrideMetadata locateAttributeOverride(AttributePath attributePath);
	AssociationOverrideMetadata locateAssociationOverride(AttributePath attributePath);
}
