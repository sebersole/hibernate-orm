/*
 * Hibernate, Relational Persistence for Idiomatic Java
 *
 * License: GNU Lesser General Public License (LGPL), version 2.1 or later
 * See the lgpl.txt file in the root directory or http://www.gnu.org/licenses/lgpl-2.1.html
 */
package org.hibernate.testing.cache;

import java.io.Serializable;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

import org.hibernate.cache.CacheException;
import org.hibernate.cache.spi.UpdateTimestampsRegion;
import org.hibernate.engine.spi.SharedSessionContractImplementor;
import org.hibernate.internal.CoreLogging;
import org.hibernate.internal.CoreMessageLogger;

/**
 * @author Steve Ebersole
 */
public class UpdateTimestampsRegionImpl implements UpdateTimestampsRegion {
	private static final CoreMessageLogger LOG = CoreLogging.messageLogger( UpdateTimestampsRegionImpl.class );
	private static final boolean DEBUG_ENABLED = LOG.isDebugEnabled();

	private final CachingRegionFactory regionFactory;

	private final ConcurrentHashMap cacheDataMap;

	public UpdateTimestampsRegionImpl(
			CachingRegionFactory regionFactory) {
		this.regionFactory = regionFactory;

		this.cacheDataMap = new ConcurrentHashMap(  );
	}

	@Override
	public void preInvalidate(Serializable[] spaces, SharedSessionContractImplementor session) throws CacheException {
		final boolean stats = session.getFactory().getStatistics().isStatisticsEnabled();

		final Long ts = regionFactory.nextTimestamp() + CachingRegionFactory.TIMEOUT;

		for ( Serializable space : spaces ) {
			if ( DEBUG_ENABLED ) {
				LOG.debugf( "Pre-invalidating space [%s], timestamp: %s", space, ts );
			}

			try {
				session.getEventListenerManager().cachePutStart();
				cacheDataMap.put( space, ts );
			}
			finally {
				session.getEventListenerManager().cachePutEnd();

				if ( stats ) {
					session.getFactory().getStatistics().updateTimestampsCachePut();
				}
			}
		}
	}

	@Override
	public void invalidate(Serializable[] spaces, SharedSessionContractImplementor session) throws CacheException {
		final boolean stats = session.getFactory().getStatistics().isStatisticsEnabled();

		final Long ts = regionFactory.nextTimestamp();

		for (Serializable space : spaces) {
			if ( DEBUG_ENABLED ) {
				LOG.debugf( "Invalidating space [%s], timestamp: %s", space, ts );
			}

			try {
				session.getEventListenerManager().cachePutStart();
				cacheDataMap.put( space, ts );
			}
			finally {
				session.getEventListenerManager().cachePutEnd();

				if ( stats ) {
					session.getFactory().getStatistics().updateTimestampsCachePut();
				}
			}
		}
	}

	@Override
	public boolean isUpToDate(
			Set<Serializable> spaces,
			Long timestamp,
			SharedSessionContractImplementor session) throws CacheException {
		final boolean stats = session.getFactory().getStatistics().isStatisticsEnabled();

		for ( Serializable space : spaces ) {
			final Long lastUpdate = getLastUpdateTimestampForSpace( space, session );
			if ( lastUpdate == null ) {
				if ( stats ) {
					session.getFactory().getStatistics().updateTimestampsCacheMiss();
				}
				//the last update timestamp was lost from the cache
				//(or there were no updates since startup!)
				//updateTimestamps.put( space, new Long( updateTimestamps.nextTimestamp() ) );
				//result = false; // safer
			}
			else {
				if ( DEBUG_ENABLED ) {
					LOG.debugf(
							"[%s] last update timestamp: %s",
							space,
							lastUpdate + ", result set timestamp: " + timestamp
					);
				}
				if ( stats ) {
					session.getFactory().getStatistics().updateTimestampsCacheHit();
				}
				if ( lastUpdate >= timestamp ) {
					return false;
				}
			}
		}
		return true;
	}

	private Long getLastUpdateTimestampForSpace(Serializable space, SharedSessionContractImplementor session) {
		Long ts = null;
		try {
			session.getEventListenerManager().cacheGetStart();
			ts = (Long) cacheDataMap.get( space );
		}
		finally {
			session.getEventListenerManager().cacheGetEnd( ts != null );
		}
		return ts;
	}

	@Override
	public void destroy() throws CacheException {
		cacheDataMap.clear();
	}
}
