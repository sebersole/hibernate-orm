/*
 * Hibernate, Relational Persistence for Idiomatic Java
 *
 * License: GNU Lesser General Public License (LGPL), version 2.1 or later.
 * See the lgpl.txt file in the root directory or http://www.gnu.org/licenses/lgpl-2.1.html.
 */
package org.hibernate.boot.internal;

import org.hibernate.boot.model.TypeDefinitionRegistry;
import org.hibernate.boot.model.naming.ObjectNameNormalizer;
import org.hibernate.boot.spi.BootstrapContext;
import org.hibernate.boot.spi.InFlightMetadataCollector;
import org.hibernate.boot.spi.MappingDefaults;
import org.hibernate.boot.spi.MetadataBuildingContext;
import org.hibernate.boot.spi.MetadataBuildingOptions;

/**
 * @author Steve Ebersole
 */
public class DelegatingMetadataBuildingContext implements MetadataBuildingContext {
	private final MetadataBuildingContext delegate;

	public DelegatingMetadataBuildingContext(MetadataBuildingContext delegate) {
		this.delegate = delegate;
	}

	@Override
	public BootstrapContext getBootstrapContext() {
		return delegate.getBootstrapContext();
	}

	@Override
	public MetadataBuildingOptions getBuildingOptions() {
		return delegate.getBuildingOptions();
	}

	@Override
	public MappingDefaults getMappingDefaults() {
		return delegate.getMappingDefaults();
	}

	@Override
	public InFlightMetadataCollector getMetadataCollector() {
		return delegate.getMetadataCollector();
	}

	@Override
	public ObjectNameNormalizer getObjectNameNormalizer() {
		return delegate.getObjectNameNormalizer();
	}

	@Override
	public int getPreferredSqlTypeCodeForBoolean() {
		return delegate.getPreferredSqlTypeCodeForBoolean();
	}

	@Override
	public int getPreferredSqlTypeCodeForDuration() {
		return delegate.getPreferredSqlTypeCodeForDuration();
	}

	@Override
	public int getPreferredSqlTypeCodeForUuid() {
		return delegate.getPreferredSqlTypeCodeForUuid();
	}

	@Override
	public int getPreferredSqlTypeCodeForInstant() {
		return delegate.getPreferredSqlTypeCodeForInstant();
	}

	@Override
	public int getPreferredSqlTypeCodeForArray() {
		return delegate.getPreferredSqlTypeCodeForArray();
	}

	@Override
	public TypeDefinitionRegistry getTypeDefinitionRegistry() {
		return delegate.getTypeDefinitionRegistry();
	}

	@Override
	public String getCurrentContributorName() {
		return delegate.getCurrentContributorName();
	}
}
