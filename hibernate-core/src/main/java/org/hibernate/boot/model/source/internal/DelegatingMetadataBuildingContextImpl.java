/*
 * Hibernate, Relational Persistence for Idiomatic Java
 *
 * License: GNU Lesser General Public License (LGPL), version 2.1 or later.
 * See the lgpl.txt file in the root directory or <http://www.gnu.org/licenses/lgpl-2.1.html>.
 */
package org.hibernate.boot.model.source.internal;

import org.hibernate.boot.model.naming.ObjectNameNormalizer;
import org.hibernate.boot.spi.ClassLoaderAccess;
import org.hibernate.boot.spi.InFlightMetadataCollector;
import org.hibernate.boot.spi.MappingDefaults;
import org.hibernate.boot.spi.MetadataBuildingContext;
import org.hibernate.boot.spi.MetadataBuildingOptions;

/**
 * @author Steve Ebersole
 */
public class DelegatingMetadataBuildingContextImpl implements MetadataBuildingContext {
	private final MetadataBuildingContext delegate;

	public DelegatingMetadataBuildingContextImpl(MetadataBuildingContext delegate) {
		this.delegate = delegate;
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
	public ClassLoaderAccess getClassLoaderAccess() {
		return delegate.getClassLoaderAccess();
	}

	@Override
	public ObjectNameNormalizer getObjectNameNormalizer() {
		return delegate.getObjectNameNormalizer();
	}
}
