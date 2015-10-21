/*
 * Hibernate, Relational Persistence for Idiomatic Java
 *
 * License: GNU Lesser General Public License (LGPL), version 2.1 or later.
 * See the lgpl.txt file in the root directory or <http://www.gnu.org/licenses/lgpl-2.1.html>.
 */

/*
 * Hibernate, Relational Persistence for Idiomatic Java
 *
 * Copyright (c) 2012, Red Hat Inc. or third-party contributors as
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

import org.hibernate.boot.model.source.internal.annotations.MapsIdSource;
import org.hibernate.boot.model.source.internal.annotations.SingularAttributeSource;
import org.hibernate.boot.model.source.internal.annotations.TableSpecificationSource;
import org.hibernate.boot.model.source.internal.annotations.metadata.type.EntityBindingContext;
import org.hibernate.boot.model.source.internal.annotations.metadata.type.RootEntityTypeMetadata;
import org.hibernate.boot.model.source.internal.annotations.util.AttributeSourceBuildingHelper;

import org.jboss.jandex.AnnotationInstance;

/**
 * Represents the source information for a "root entity" in an entity hierarchy
 * as defined by annotations and XML overrides.
 *
 * @author Hardy Ferentschik
 * @author Steve Ebersole
 * @author Brett Meyer
 */
public class RootEntitySourceImpl extends EntitySourceImpl {
	private List<SingularAttributeSource> identifierAttributes;
	private List<MapsIdSource> mapsIdSources;

	/**
	 * Constructs the root entity.  Called from the construction of the
	 * hierarchy.  Part of a very choreographed series of constructor calls
	 * to build an entity hierarchy.
	 * <p/>
	 * The root entity in the hierarchy is a pivot of sorts.  Above it, it is
	 * ok to have MappedSuperclasses.  Below it, there will be a mix of
	 * Entity and MappedSuperclass.
	 * <p/>
	 * The MappedSuperclasses that are part of the root entity's super tree are
	 * created in duplicate.  For example, given a typical scenario with a common
	 * MappedSuperclass used for all entities, each hierarchy gets its own copy
	 * of the MappedSuperclassSourceImpl for that common class.  This is
	 * important because each usage might imply different default AccessType for
	 * attributes.
	 *
	 * @param entityTypeMetadata The metadata for the root entity
	 * @param hierarchy The hierarchy we are building
	 */
	public RootEntitySourceImpl(
			RootEntityTypeMetadata entityTypeMetadata,
			EntityHierarchySourceImpl hierarchy) {
		super( entityTypeMetadata, hierarchy, true );

		this.identifierAttributes = AttributeSourceBuildingHelper.buildIdentifierAttributeSources(
				entityTypeMetadata,
				AttributeSourceBuildingHelper.IdentifierPathAttributeBuilder.INSTANCE
		);

		this.mapsIdSources = AttributeSourceBuildingHelper.buildMapsIdSources(
				entityTypeMetadata,
				AttributeSourceBuildingHelper.IdentifierPathAttributeBuilder.INSTANCE
		);
	}

	public List<SingularAttributeSource> getIdentifierAttributes() {
		return identifierAttributes;
	}

	public List<MapsIdSource> getMapsIdSources() {
		return mapsIdSources;
	}

	@Override
	public RootEntityTypeMetadata getEntityClass() {
		return (RootEntityTypeMetadata) super.getEntityClass();
	}

	@Override
	public EntityHierarchySourceImpl getHierarchy() {
		return (EntityHierarchySourceImpl) super.getHierarchy();
	}

	@Override
	protected boolean isRootEntity() {
		return true;
	}

	@Override
	protected TableSpecificationSource buildPrimaryTable(
			AnnotationInstance tableAnnotation,
			EntityBindingContext bindingContext) {
		return TableSourceImpl.build( tableAnnotation, getEntityClass().getRowId(), bindingContext );
	}
}


