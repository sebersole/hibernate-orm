/*
 * Hibernate, Relational Persistence for Idiomatic Java
 *
 * License: GNU Lesser General Public License (LGPL), version 2.1 or later.
 * See the lgpl.txt file in the root directory or <http://www.gnu.org/licenses/lgpl-2.1.html>.
 */
package org.hibernate.boot.model.process.jandex.internal;

import org.hibernate.boot.internal.MetadataBuildingContextRootImpl;
import org.hibernate.boot.jaxb.Origin;
import org.hibernate.boot.jaxb.mapping.spi.JaxbEntityMappings;
import org.hibernate.boot.jaxb.spi.Binding;
import org.hibernate.boot.model.naming.ObjectNameNormalizer;
import org.hibernate.boot.model.source.spi.LocalMetadataBuildingContext;
import org.hibernate.boot.spi.ClassLoaderAccess;
import org.hibernate.boot.spi.InFlightMetadataCollector;
import org.hibernate.boot.spi.MappingDefaults;
import org.hibernate.boot.spi.MetadataBuildingOptions;
import org.hibernate.cache.spi.access.AccessType;
import org.hibernate.internal.util.StringHelper;

/**
 * @author Steve Ebersole
 */
public class BindingMetadataBuildingContext
		implements LocalMetadataBuildingContext, MappingDefaults {
	private final MetadataBuildingContextRootImpl rootContext;
	private final Binding<JaxbEntityMappings> binding;

	public BindingMetadataBuildingContext(MetadataBuildingContextRootImpl rootContext, Binding<JaxbEntityMappings> binding) {
		this.rootContext = rootContext;
		this.binding = binding;
	}


	// ~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~
	// LocalMetadataBuildingContext

	@Override
	public Origin getOrigin() {
		return binding.getOrigin();
	}


	// ~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~
	// MetadataBuildingContext

	@Override
	public MappingDefaults getMappingDefaults() {
		return this;
	}

	@Override
	public MetadataBuildingOptions getBuildingOptions() {
		return rootContext.getBuildingOptions();
	}

	@Override
	public InFlightMetadataCollector getMetadataCollector() {
		return rootContext.getMetadataCollector();
	}

	@Override
	public ClassLoaderAccess getClassLoaderAccess() {
		return rootContext.getClassLoaderAccess();
	}

	@Override
	public ObjectNameNormalizer getObjectNameNormalizer() {
		return rootContext.getObjectNameNormalizer();
	}


	// ~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~
	// MappingDefaults


	@Override
	public String getImplicitSchemaName() {
		return binding.getRoot().getSchema();
	}

	@Override
	public String getImplicitCatalogName() {
		return binding.getRoot().getCatalog();
	}

	@Override
	public String getImplicitPackageName() {
		return binding.getRoot().getPackage();
	}

	@Override
	public String getImplicitCascadeStyleName() {
		if ( StringHelper.isNotEmpty( binding.getRoot().getHbmCascade() ) ) {
			return binding.getRoot().getHbmCascade();
		}

		return rootContext.getMappingDefaults().getImplicitCascadeStyleName();
	}

	@Override
	public String getImplicitPropertyAccessorName() {
		if ( binding.getRoot().getAccess() != null ) {
			return binding.getRoot().getAccess().name();
		}

		if ( StringHelper.isNotEmpty( binding.getRoot().getHbmAccess() ) ) {
			return binding.getRoot().getHbmAccess();
		}

		return rootContext.getMappingDefaults().getImplicitPropertyAccessorName();
	}

	@Override
	public boolean areEntitiesImplicitlyLazy() {
		if ( binding.getRoot().isHbmSingularLazy() != null ) {
			return binding.getRoot().isHbmSingularLazy();
		}
		return rootContext.getMappingDefaults().areEntitiesImplicitlyLazy();
	}

	@Override
	public boolean areCollectionsImplicitlyLazy() {
		if ( binding.getRoot().isHbmPluralLazy() != null ) {
			return binding.getRoot().isHbmPluralLazy();
		}
		return rootContext.getMappingDefaults().areCollectionsImplicitlyLazy();
	}

	@Override
	public AccessType getImplicitCacheAccessType() {
		return rootContext.getMappingDefaults().getImplicitCacheAccessType();
	}

	@Override
	public boolean shouldImplicitlyQuoteIdentifiers() {
		return rootContext.getMappingDefaults().shouldImplicitlyQuoteIdentifiers();
	}

	@Override
	public String getImplicitIdColumnName() {
		return rootContext.getMappingDefaults().getImplicitIdColumnName();
	}

	@Override
	public String getImplicitTenantIdColumnName() {
		return rootContext.getMappingDefaults().getImplicitTenantIdColumnName();
	}

	@Override
	public String getImplicitDiscriminatorColumnName() {
		return rootContext.getMappingDefaults().getImplicitDiscriminatorColumnName();
	}

	@Override
	public boolean isAutoImportEnabled() {
		return rootContext.getMappingDefaults().isAutoImportEnabled();
	}
}
