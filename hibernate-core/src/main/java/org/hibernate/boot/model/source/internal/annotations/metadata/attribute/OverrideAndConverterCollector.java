/*
 * Hibernate, Relational Persistence for Idiomatic Java
 *
 * License: GNU Lesser General Public License (LGPL), version 2.1 or later.
 * See the lgpl.txt file in the root directory or <http://www.gnu.org/licenses/lgpl-2.1.html>.
 */
package org.hibernate.boot.model.source.internal.annotations.metadata.attribute;

import org.hibernate.boot.model.source.internal.annotations.ConvertConversionInfo;
import org.hibernate.boot.model.source.spi.AttributePath;

/**
 * Contract used in normalizing AttributeConverters, AttributeOverrides and
 * AssociationOverrides.
 *
 * @author Steve Ebersole
 */
public interface OverrideAndConverterCollector {
	void registerConverter(AttributePath attributePath, ConvertConversionInfo conversionInfo);

	void registerAttributeOverride(AttributePath attributePath, AttributeOverride override);

	void registerAssociationOverride(AttributePath attributePath, AssociationOverride override);

	ConvertConversionInfo locateConversionInfo(AttributePath attributePath);

	AttributeOverride locateAttributeOverride(AttributePath attributePath);

	AssociationOverride locateAssociationOverride(AttributePath attributePath);
}
