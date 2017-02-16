/*
 * Hibernate, Relational Persistence for Idiomatic Java
 *
 * License: GNU Lesser General Public License (LGPL), version 2.1 or later.
 * See the lgpl.txt file in the root directory or <http://www.gnu.org/licenses/lgpl-2.1.html>.
 */
package org.hibernate.testing.cache;

import java.util.HashMap;
import java.util.Properties;

import org.hibernate.boot.spi.SessionFactoryOptions;
import org.hibernate.cache.CacheException;
import org.hibernate.cache.internal.DefaultCacheKeysFactory;
import org.hibernate.cache.spi.CacheKeysFactory;
import org.hibernate.cache.spi.CacheableRegion;
import org.hibernate.cache.spi.CacheableRegionNameMapping;
import org.hibernate.cache.spi.QueryResultsRegion;
import org.hibernate.cache.spi.RegionBuildingContext;
import org.hibernate.cache.spi.RegionFactory;
import org.hibernate.cache.spi.UpdateTimestampsRegion;
import org.hibernate.cache.spi.access.AccessType;

import org.jboss.logging.Logger;

/**
 * @author Strong Liu
 */
public class CachingRegionFactory implements RegionFactory {
	private static final Logger LOG = Logger.getLogger( CachingRegionFactory.class.getName() );

	public static String DEFAULT_ACCESSTYPE = "DefaultAccessType";
	public static int TIMEOUT = Timestamper.ONE_MS * 60000;  //60s


	// to support globally switching the CacheKeysFactory to use for testing
	private final CacheKeysFactory cacheKeysFactory;
	private final HashMap<String,CacheableRegionImpl> namedRegionMap = new HashMap<>();

	private SessionFactoryOptions settings;
	private Properties properties;

	public CachingRegionFactory() {
		this( DefaultCacheKeysFactory.INSTANCE, null );
	}

	public CachingRegionFactory(CacheKeysFactory cacheKeysFactory) {
		this( cacheKeysFactory, null );
	}

	public CachingRegionFactory(Properties properties) {
		this( DefaultCacheKeysFactory.INSTANCE, properties );
	}

	public CachingRegionFactory(CacheKeysFactory cacheKeysFactory, Properties properties) {
		LOG.warn( "CachingRegionFactory should be only used for testing." );
		this.cacheKeysFactory = cacheKeysFactory;
		this.properties = properties;
	}

	public SessionFactoryOptions getSettings() {
		return settings;
	}

	public CacheKeysFactory getCacheKeysFactory() {
		return cacheKeysFactory;
	}

	@Override
	public void start(SessionFactoryOptions settings, Properties properties) throws CacheException {
		this.settings = settings;
		this.properties = properties;
	}

	@Override
	public void stop() {
	}

	@Override
	public boolean isMinimalPutsEnabledByDefault() {
		return false;
	}

	@Override
	public AccessType getDefaultAccessType() {
		if ( properties != null && properties.get( DEFAULT_ACCESSTYPE ) != null ) {
			return AccessType.fromExternalName( properties.getProperty( DEFAULT_ACCESSTYPE ) );
		}
		return AccessType.READ_WRITE;
	}


	@Override
	public CacheableRegion buildCacheableRegion(
			String regionName,
			CacheableRegionNameMapping regionNameMapping,
			RegionBuildingContext buildingContext) {
		final CacheableRegionImpl existing = namedRegionMap.get( regionName );
		if ( existing != null ) {
			return existing;
		}

		final CacheableRegionImpl region = new CacheableRegionImpl( this, regionName, regionNameMapping, buildingContext );
		namedRegionMap.put( regionName, region );
		return region;
	}

	@Override
	public QueryResultsRegion buildQueryResultsRegion(String regionName, RegionBuildingContext buildingContext) {
		return new QueryResultsRegionImpl( this, regionName );
	}

	@Override
	public UpdateTimestampsRegion buildUpdateTimestampsRegion(RegionBuildingContext buildingContext) {
		return new UpdateTimestampsRegionImpl( this );
	}

	@Override
	public long nextTimestamp() {
		return Timestamper.next();
	}
}
