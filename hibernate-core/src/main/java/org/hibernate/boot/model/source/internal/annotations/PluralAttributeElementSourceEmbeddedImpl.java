/*
 * Hibernate, Relational Persistence for Idiomatic Java
 *
 * License: GNU Lesser General Public License (LGPL), version 2.1 or later.
 * See the lgpl.txt file in the root directory or <http://www.gnu.org/licenses/lgpl-2.1.html>.
 */
package org.hibernate.boot.model.source.internal.annotations;

import org.hibernate.boot.model.source.internal.annotations.metadata.attribute.PluralAttributeElementDetailsEmbedded;
import org.hibernate.boot.model.source.internal.annotations.util.AttributeSourceBuildingHelper;
import org.hibernate.boot.model.source.spi.EmbeddableSource;
import org.hibernate.boot.model.source.spi.PluralAttributeElementNature;
import org.hibernate.boot.model.source.spi.PluralAttributeElementSourceEmbedded;

/**
 * @author Brett Meyer
 * @author Steve Ebersole
 */
public class PluralAttributeElementSourceEmbeddedImpl
		implements PluralAttributeElementSourceEmbedded {
	private final PluralAttributeElementDetailsEmbedded elementDescriptor;

	private final EmbeddableSourceImpl embeddableSource;

	public PluralAttributeElementSourceEmbeddedImpl(PluralAttributeSourceImpl pluralAttributeSource) {
		this.elementDescriptor = (PluralAttributeElementDetailsEmbedded) pluralAttributeSource.getPluralAttribute().getElementDetails();
		this.embeddableSource = new EmbeddableSourceImpl(
				elementDescriptor.getEmbeddableTypeMetadata(),
				AttributeSourceBuildingHelper.PluralAttributesDisallowedAttributeBuilder.INSTANCE
		);
	}

	@Override
	public PluralAttributeElementNature getNature() {
		return PluralAttributeElementNature.AGGREGATE;
	}


	@Override
	public EmbeddableSource getEmbeddableSource() {
		return embeddableSource;
	}
}
