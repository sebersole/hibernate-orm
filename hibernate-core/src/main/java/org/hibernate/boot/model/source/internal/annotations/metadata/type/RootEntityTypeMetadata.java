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
package org.hibernate.boot.model.source.internal.annotations.metadata.type;

import java.util.List;
import javax.persistence.AccessType;

import org.hibernate.boot.jandex.spi.HibernateDotNames;
import org.hibernate.boot.model.source.internal.annotations.RootAnnotationBindingContext;
import org.hibernate.boot.model.source.internal.annotations.metadata.attribute.PrimaryKeyJoinColumn;
import org.hibernate.boot.model.source.internal.annotations.metadata.util.ConverterAndOverridesHelper;
import org.hibernate.internal.CoreLogging;
import org.hibernate.internal.CoreMessageLogger;
import org.hibernate.internal.util.collections.CollectionHelper;

import org.jboss.jandex.AnnotationInstance;
import org.jboss.jandex.ClassInfo;

/**
 * Representation of metadata (configured via annotations or orm.xml) attached
 * to an Entity that is the root of a persistence hierarchy.
 *
 * @author Hardy Ferentschik
 * @author Steve Ebersole
 * @author Brett Meyer
 */
public class RootEntityTypeMetadata extends EntityTypeMetadata {
	private static final CoreMessageLogger LOG = CoreLogging.messageLogger( RootEntityTypeMetadata.class );

	private final String rowId;

	public RootEntityTypeMetadata(
			ClassInfo classInfo,
			AccessType defaultHierarchyAccessType,
			RootAnnotationBindingContext context) {
		super( classInfo, defaultHierarchyAccessType, context );

		// ROWID
		final AnnotationInstance rowIdAnnotation = getLocalBindingContext().getTypeAnnotationInstances( classInfo.name() ).get(
				HibernateDotNames.ROW_ID
		);
		this.rowId = rowIdAnnotation != null && rowIdAnnotation.value() != null
				? rowIdAnnotation.value().asString()
				: null;
	}

	@Override
	protected void collectConversionInfo() {
		super.collectConversionInfo();
	}

	@Override
	protected void collectAttributeOverrides() {
		collectAttributeOverrides( this );
	}

	private void collectAttributeOverrides(IdentifiableTypeMetadata type) {
		// subclasses first, since they have precedence
		ConverterAndOverridesHelper.processAttributeOverrides( this );

		if ( type.getSuperType() != null ) {
			collectAttributeOverrides( type.getSuperType() );
		}
	}

	@Override
	protected void collectAssociationOverrides() {
		super.collectAssociationOverrides();
	}

	@Override
	protected List<PrimaryKeyJoinColumn> determinePrimaryKeyJoinColumns() {
		List<PrimaryKeyJoinColumn> results = super.determinePrimaryKeyJoinColumns();
		if ( CollectionHelper.isNotEmpty( results ) ) {
			LOG.invalidPrimaryKeyJoinColumnAnnotation();
		}
		return null;
	}

	public String getRowId() {
		return rowId;
	}

	@Override
	public boolean hasMultiTenancySourceInformation() {
		return hasMultiTenancySourceInformation( this );
	}

	private static boolean hasMultiTenancySourceInformation(IdentifiableTypeMetadata typeMetadata) {
		final boolean hasLocally = typeMetadata.typeAnnotationMap().containsKey( HibernateDotNames.MULTI_TENANT )
				|| typeMetadata.typeAnnotationMap().containsKey( HibernateDotNames.TENANT_COLUMN )
				|| typeMetadata.typeAnnotationMap().containsKey( HibernateDotNames.TENANT_FORMULA );
		if ( hasLocally ) {
			return true;
		}

		if ( typeMetadata.getSuperType() != null ) {
			return hasMultiTenancySourceInformation( typeMetadata.getSuperType() );
		}

		return false;
	}
}
