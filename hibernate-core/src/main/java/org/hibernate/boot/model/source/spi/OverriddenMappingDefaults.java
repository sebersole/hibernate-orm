/*
 * Hibernate, Relational Persistence for Idiomatic Java
 *
 * License: GNU Lesser General Public License (LGPL), version 2.1 or later.
 * See the lgpl.txt file in the root directory or <http://www.gnu.org/licenses/lgpl-2.1.html>.
 */
package org.hibernate.boot.model.source.spi;

import org.hibernate.boot.jaxb.mapping.spi.JaxbPersistenceUnitDefaults;
import org.hibernate.boot.spi.MappingDefaults;
import org.hibernate.cache.spi.access.AccessType;

/**
 * @author Steve Ebersole
 */
public class OverriddenMappingDefaults implements MappingDefaults {
	private final MappingDefaults baseValues;
	private final JaxbPersistenceUnitDefaults ormXmlOverrides;

	public OverriddenMappingDefaults(
			MappingDefaults baseValues,
			JaxbPersistenceUnitDefaults ormXmlOverrides) {
		this.baseValues = baseValues;
		this.ormXmlOverrides = ormXmlOverrides == null
				? new JaxbPersistenceUnitDefaults()
				: ormXmlOverrides ;
	}

	@Override
	public String getImplicitSchemaName() {
		return ormXmlOverrides.getSchema() != null
				? ormXmlOverrides.getSchema()
				: baseValues.getImplicitSchemaName();
	}

	@Override
	public String getImplicitCatalogName() {
		return ormXmlOverrides.getCatalog() != null
				? ormXmlOverrides.getCatalog()
				: baseValues.getImplicitCatalogName();
	}

	@Override
	public boolean shouldImplicitlyQuoteIdentifiers() {
		return ormXmlOverrides.getDelimitedIdentifiers() != null || baseValues.shouldImplicitlyQuoteIdentifiers();
	}

	@Override
	public String getImplicitIdColumnName() {
		return baseValues.getImplicitIdColumnName();
	}

	@Override
	public String getImplicitTenantIdColumnName() {
		return baseValues.getImplicitTenantIdColumnName();
	}

	@Override
	public String getImplicitDiscriminatorColumnName() {
		return baseValues.getImplicitDiscriminatorColumnName();
	}

	@Override
	public String getImplicitPackageName() {
		return baseValues.getImplicitPackageName();
	}

	@Override
	public boolean isAutoImportEnabled() {
		return baseValues.isAutoImportEnabled();
	}

	@Override
	public String getImplicitCascadeStyleName() {
		return ormXmlOverrides.getHbmCascade() != null
				? ormXmlOverrides.getHbmCascade()
				: baseValues.getImplicitCascadeStyleName();
	}

	@Override
	public String getImplicitPropertyAccessorName() {
		if ( ormXmlOverrides.getHbmAccess() != null ) {
			return ormXmlOverrides.getHbmAccess();
		}
		else if ( ormXmlOverrides.getAccess() != null ) {
			return ormXmlOverrides.getAccess().name();
		}

		return baseValues.getImplicitPropertyAccessorName();
	}

	@Override
	public boolean areEntitiesImplicitlyLazy() {
		return ormXmlOverrides.isHbmSingularLazy() != null
				? ormXmlOverrides.isHbmSingularLazy()
				: baseValues.areEntitiesImplicitlyLazy();
	}

	@Override
	public boolean areCollectionsImplicitlyLazy() {
		return ormXmlOverrides.isHbmPluralLazy() != null
				? ormXmlOverrides.isHbmPluralLazy()
				: baseValues.areCollectionsImplicitlyLazy();
	}

	@Override
	public AccessType getImplicitCacheAccessType() {
		return ormXmlOverrides.getHbmCacheAccess() != null
				? ormXmlOverrides.getHbmCacheAccess()
				: baseValues.getImplicitCacheAccessType();
	}
}
