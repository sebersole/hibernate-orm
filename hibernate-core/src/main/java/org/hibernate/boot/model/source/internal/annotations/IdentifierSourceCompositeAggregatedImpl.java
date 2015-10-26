/*
 * Hibernate, Relational Persistence for Idiomatic Java
 *
 * License: GNU Lesser General Public License (LGPL), version 2.1 or later.
 * See the lgpl.txt file in the root directory or <http://www.gnu.org/licenses/lgpl-2.1.html>.
 */
package org.hibernate.boot.model.source.internal.annotations;

import java.util.List;

import org.hibernate.boot.model.source.spi.IdentifierGenerationInformation;
import org.hibernate.boot.model.source.spi.IdentifierSourceCompositeAggregated;
import org.hibernate.boot.model.source.spi.MapsIdSource;
import org.hibernate.boot.model.source.spi.SingularAttributeSourceEmbedded;
import org.hibernate.id.EntityIdentifierNature;

/**
 * @author Hardy Ferentschik
 * @author Steve Ebersole
 * @author Brett Meyer
 */
public class IdentifierSourceCompositeAggregatedImpl
		extends AbstractIdentifierSource
		implements IdentifierSourceCompositeAggregated {

	private final SingularAttributeSourceEmbedded componentAttributeSource;
	private final List<MapsIdSource> mapsIdSourceList;

	public IdentifierSourceCompositeAggregatedImpl(
			RootEntitySourceImpl rootEntitySource,
			SingularAttributeSourceEmbedded componentAttributeSource,
			List<MapsIdSource> mapsIdSourceList) {
		super( rootEntitySource );
		this.componentAttributeSource = componentAttributeSource;
		this.mapsIdSourceList = mapsIdSourceList;
	}

	@Override
	public SingularAttributeSourceEmbedded getIdentifierAttributeSource() {
		return componentAttributeSource;
	}

	@Override
	public List<MapsIdSource> getMapsIdSources() {
		return mapsIdSourceList;
	}

	@Override
	public IdentifierGenerationInformation getIndividualAttributeIdentifierGenerationInformation(String identifierAttributeName) {
		// for now, return null.  this is that stupid specj bs
		return null;
	}

	@Override
	public EntityIdentifierNature getNature() {
		return EntityIdentifierNature.AGGREGATED_COMPOSITE;
	}

	@Override
	public IdentifierGenerationInformation getIdentifierGenerationInformation() {
		return null;
	}
}
