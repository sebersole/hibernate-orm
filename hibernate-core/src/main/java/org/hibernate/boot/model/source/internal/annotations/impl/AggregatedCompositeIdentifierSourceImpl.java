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
package org.hibernate.boot.model.source.internal.annotations.impl;

import java.util.List;

import org.hibernate.boot.model.source.internal.annotations.AggregatedCompositeIdentifierSource;
import org.hibernate.boot.model.source.internal.annotations.MapsIdSource;
import org.hibernate.boot.model.source.internal.annotations.metadata.attribute.IdentifierGenerationInformation;
import org.hibernate.id.EntityIdentifierNature;

/**
 * @author Hardy Ferentschik
 * @author Steve Ebersole
 * @author Brett Meyer
 */
class AggregatedCompositeIdentifierSourceImpl
		extends AbstractIdentifierSource
		implements AggregatedCompositeIdentifierSource {

	private final SingularAttributeSourceEmbeddedImpl componentAttributeSource;
	private final List<MapsIdSource> mapsIdSourceList;

	public AggregatedCompositeIdentifierSourceImpl(
			RootEntitySourceImpl rootEntitySource,
			SingularAttributeSourceEmbeddedImpl componentAttributeSource,
			List<MapsIdSource> mapsIdSourceList) {
		super( rootEntitySource );
		this.componentAttributeSource = componentAttributeSource;
		this.mapsIdSourceList = mapsIdSourceList;
	}

	@Override
	public SingularAttributeSourceEmbeddedImpl getIdentifierAttributeSource() {
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
