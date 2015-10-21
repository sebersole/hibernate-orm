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

import org.hibernate.boot.jandex.spi.HibernateDotNames;
import org.hibernate.boot.model.naming.EntityNaming;
import org.hibernate.boot.model.source.internal.annotations.DiscriminatorSource;
import org.hibernate.boot.model.source.internal.annotations.metadata.type.EntityTypeMetadata;
import org.hibernate.boot.model.source.internal.annotations.util.AnnotationBindingHelper;
import org.hibernate.boot.spi.MetadataBuildingContext;

import org.jboss.jandex.AnnotationInstance;
import org.jboss.jandex.AnnotationValue;

/**
 * @author Steve Ebersole
 */
public abstract class DiscriminatorSourceSupport implements DiscriminatorSource {
	private final EntityTypeMetadata entityTypeMetadata;
	private final boolean forced;
	private final boolean includedInInsert;

	public DiscriminatorSourceSupport(EntityTypeMetadata entityTypeMetadata) {
		this.entityTypeMetadata = entityTypeMetadata;

		final AnnotationInstance discriminatorOptions = AnnotationBindingHelper.findTypeAnnotation(
				HibernateDotNames.DISCRIMINATOR_OPTIONS,
				entityTypeMetadata
		);

		this.forced = determineWhetherForced( discriminatorOptions );
		this.includedInInsert = determineWhetherToIncludeInInsert( discriminatorOptions );
	}

	@SuppressWarnings("SimplifiableIfStatement")
	private static boolean determineWhetherForced(AnnotationInstance discriminatorOptions) {
		if ( discriminatorOptions == null ) {
			return false;
		}

		final AnnotationValue forcedValue = discriminatorOptions.value( "force" );
		if ( forcedValue == null ) {
			return false;
		}

		return forcedValue.asBoolean();
	}

	@SuppressWarnings("SimplifiableIfStatement")
	private static boolean determineWhetherToIncludeInInsert(AnnotationInstance discriminatorOptions) {
		if ( discriminatorOptions == null ) {
			return true;
		}

		final AnnotationValue insertValue = discriminatorOptions.value( "insert" );
		if ( insertValue == null ) {
			return true;
		}

		return insertValue.asBoolean();
	}

	@Override
	public boolean isForced() {
		return forced;
	}

	@Override
	public boolean isInserted() {
		return includedInInsert;
	}

	@Override
	public EntityNaming getEntityNaming() {
		return entityTypeMetadata;
	}

	@Override
	public MetadataBuildingContext getBuildingContext() {
		return entityTypeMetadata.getLocalBindingContext();
	}
}
