/*
 * Hibernate, Relational Persistence for Idiomatic Java
 *
 * License: GNU Lesser General Public License (LGPL), version 2.1 or later.
 * See the lgpl.txt file in the root directory or http://www.gnu.org/licenses/lgpl-2.1.html.
 */
package org.hibernate.boot.model.source.spi;

import org.hibernate.annotations.Cache;
import org.hibernate.annotations.CacheConcurrencyStrategy;
import org.hibernate.boot.CacheRegionDefinition;
import org.hibernate.boot.annotations.source.internal.AnnotationsHelper;
import org.hibernate.boot.annotations.source.spi.AnnotationUsage;
import org.hibernate.cache.spi.access.AccessType;
import org.hibernate.internal.util.StringHelper;

/**
 * Models the caching options for an entity, natural-id, or collection.
 *
 * @author Steve Ebersole
 * @author Hardy Ferentschik
 */
public class Caching {
	private boolean enabled;

	private String region;
	private AccessType accessType;
	private boolean cacheLazyProperties;

	/**
	 * Generally used for disabled caching
	 */
	public Caching(boolean enabled) {
		this.enabled = enabled;
	}

	public Caching(
			AnnotationUsage<Cache> cacheAnnotation,
			AccessType implicitCacheAccessType,
			String implicitRegionName) {
		this.enabled = true;

		if ( cacheAnnotation == null ) {
			region = implicitRegionName;
			accessType = implicitCacheAccessType;
			cacheLazyProperties = true;
		}
		else {
			region = AnnotationsHelper.getValue( cacheAnnotation.getAttributeValue( "region" ), implicitRegionName );
			accessType = interpretAccessType( cacheAnnotation.getAttributeValue( "usage" ), implicitCacheAccessType );
			cacheLazyProperties = AnnotationsHelper.getValue(
					cacheAnnotation.getAttributeValue( "includeLazy" ),
					() -> {
						final String include = cacheAnnotation.getAttributeValue( "include" ).asString();
						assert "all".equals( include ) || "non-lazy".equals( include );
						return include.equals( "all" );
					}
			);
		}
	}

	private static AccessType interpretAccessType(AnnotationUsage.AttributeValue usageValue, AccessType implicitValue) {
		if ( usageValue != null ) {
			final CacheConcurrencyStrategy strategy = usageValue.getValue();
			return strategy.toAccessType();
		}
		return implicitValue;
	}

	public boolean isEnabled() {
		return enabled;
	}

	public String getRegion() {
		return region;
	}

	public void setRegion(String region) {
		this.region = region;
	}

	public AccessType getAccessType() {
		return accessType;
	}

	public void setAccessType(AccessType accessType) {
		this.accessType = accessType;
	}

	public boolean isCacheLazyProperties() {
		return cacheLazyProperties;
	}

	public void setCacheLazyProperties(boolean cacheLazyProperties) {
		this.cacheLazyProperties = cacheLazyProperties;
	}

	public void overlay(CacheRegionDefinition overrides) {
		if ( overrides == null ) {
			return;
		}

		enabled = true;
		accessType = AccessType.fromExternalName( overrides.getUsage() );
		if ( StringHelper.isEmpty( overrides.getRegion() ) ) {
			region = overrides.getRegion();
		}
		// ugh, primitive boolean
		cacheLazyProperties = overrides.isCacheLazy();
	}

	public void overlay(Caching overrides) {
		if ( overrides == null ) {
			return;
		}

		this.enabled = overrides.enabled;
		this.accessType = overrides.accessType;
		this.region = overrides.region;
		this.cacheLazyProperties = overrides.cacheLazyProperties;
	}

	@Override
	public String toString() {
		return "Caching{region='" + region + '\''
				+ ", accessType=" + accessType
				+ ", cacheLazyProperties=" + cacheLazyProperties
				+ ", enabled=" + enabled + '}';
	}

}
