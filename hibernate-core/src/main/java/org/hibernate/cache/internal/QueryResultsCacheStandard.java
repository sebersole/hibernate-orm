/*
 * Hibernate, Relational Persistence for Idiomatic Java
 *
 * License: GNU Lesser General Public License (LGPL), version 2.1 or later.
 * See the lgpl.txt file in the root directory or <http://www.gnu.org/licenses/lgpl-2.1.html>.
 */
package org.hibernate.cache.internal;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import javax.persistence.EntityNotFoundException;

import org.hibernate.HibernateException;
import org.hibernate.UnresolvableObjectException;
import org.hibernate.cache.spi.CacheKeysFactory;
import org.hibernate.cache.spi.QueryKey;
import org.hibernate.cache.spi.QueryResultsCache;
import org.hibernate.cache.spi.QueryResultsRegion;
import org.hibernate.cache.spi.RegionBuildingContext;
import org.hibernate.cache.spi.RegionFactory;
import org.hibernate.cache.spi.UpdateTimestampsRegion;
import org.hibernate.engine.spi.SessionFactoryImplementor;
import org.hibernate.engine.spi.SharedSessionContractImplementor;
import org.hibernate.internal.CoreLogging;
import org.hibernate.internal.CoreMessageLogger;
import org.hibernate.type.TypeHelper;
import org.hibernate.type.spi.Type;

/**
 * The standard implementation of the Hibernate QueryCache interface.  This
 * implementation is very good at recognizing stale query results and
 * and re-running queries when it detects this condition, re-caching the new
 * results.
 *
 * @author Gavin King
 * @author Steve Ebersole
 */
public class QueryResultsCacheStandard implements QueryResultsCache, RegionBuildingContext {
	private static final CoreMessageLogger LOG = CoreLogging.messageLogger( QueryResultsCacheStandard.class );

	private static final boolean DEBUGGING = LOG.isDebugEnabled();
	private static final boolean TRACING = LOG.isTraceEnabled();

	private final SessionFactoryImplementor sessionFactory;
	private final RegionFactory regionFactory;
	private final String cacheRegionPrefix;
	private final String defaultRegionName;

	private final UpdateTimestampsRegion updateTimestampsRegion;
	private final Map<String,QueryResultsRegion> queryResultsRegionMap;

	public QueryResultsCacheStandard(
			SessionFactoryImplementor sessionFactory,
			RegionFactory regionFactory,
			UpdateTimestampsRegion updateTimestampsRegion) {
		this.sessionFactory = sessionFactory;
		this.regionFactory = regionFactory;
		this.cacheRegionPrefix = sessionFactory.getSessionFactoryOptions().getCacheRegionPrefix();
		this.defaultRegionName = resolveRegionName( getClass().getName() );
		this.updateTimestampsRegion = updateTimestampsRegion;
		this.queryResultsRegionMap = new ConcurrentHashMap<>();
	}

	private String resolveRegionName(String regionName) {
		if ( regionName == null ) {
			return defaultRegionName;
		}

		if ( cacheRegionPrefix == null ) {
			return regionName;
		}

		return cacheRegionPrefix + '.' + regionName;
	}

	@Override
	public void destroy() {
		stop();
	}

	@Override
	public void stop() {
		for ( QueryResultsRegion queryResultsRegion : queryResultsRegionMap.values() ) {
			try {
				queryResultsRegion.stop();
			}
			catch (Exception e) {
				LOG.unableToDestroyQueryCache( queryResultsRegion.getName(), e.getMessage() );
			}
		}
	}

	@Override
	public void clear(String regionName) {
		final QueryResultsRegion region = findRegion( regionName );

	}

	private QueryResultsRegion findRegion(String regionName) {
		regionName = resolveRegionName( regionName );
		return queryResultsRegionMap.get( regionName );
	}

	@Override
	public boolean put(
			String regionName,
			QueryKey key,
			org.hibernate.type.Type[] returnTypes,
			List result,
			boolean isNaturalKeyLookup,
			SharedSessionContractImplementor session) throws HibernateException {
		final QueryResultsRegion region = findOrCreateRegion( regionName );

		if ( DEBUGGING ) {
			LOG.debugf( "Caching query results in region: %s; timestamp=%s", region.getName(), session.getTransactionStartTimestamp() );
		}

		final List cacheable = new ArrayList( result.size() + 1 );
		if ( TRACING ) {
			logCachedResultDetails( key, null, returnTypes, cacheable );
		}
		cacheable.add( session.getTransactionStartTimestamp() );

		final boolean isSingleResult = returnTypes.length == 1;
		for ( Object aResult : result ) {
			final Serializable cacheItem = isSingleResult
					? returnTypes[0].disassemble( aResult, session, null )
					: TypeHelper.disassemble( (Object[]) aResult, returnTypes, null, session, null );
			cacheable.add( cacheItem );
			if ( TRACING ) {
				logCachedResultRowDetails( returnTypes, aResult );
			}
		}

		try {
			session.getEventListenerManager().cachePutStart();
			region.put( session, key, cacheable );
		}
		finally {
			session.getEventListenerManager().cachePutEnd();
		}

		return true;
	}

	private QueryResultsRegion findOrCreateRegion(String regionName) {
		final String regionNameToUse = resolveRegionName( regionName );
		return queryResultsRegionMap.computeIfAbsent(
				regionName, k -> regionFactory.buildQueryResultsRegion( regionNameToUse, this )
		);
	}

	@Override
	public List get(
			String regionName,
			QueryKey key,
			org.hibernate.type.Type[] returnTypes,
			boolean isNaturalKeyLookup,
			Set<Serializable> spaces,
			SharedSessionContractImplementor session) throws HibernateException {
		final QueryResultsRegion region = findRegion( regionName );

		if ( DEBUGGING ) {
			LOG.debugf( "Checking cached query results in region: %s", region.getName() );
		}

		final List cacheable = getCachedResults( key, session );
		if ( TRACING ) {
			logCachedResultDetails( key, spaces, returnTypes, cacheable );
		}
		if ( cacheable == null ) {
			if ( DEBUGGING ) {
				LOG.debug( "Query results were not found in cache" );
			}
			return null;
		}

		final Long timestamp = (Long) cacheable.get( 0 );
		if ( !isNaturalKeyLookup && !isUpToDate( spaces, timestamp, session ) ) {
			if ( DEBUGGING ) {
				LOG.debug( "Cached query results were not up-to-date" );
			}
			return null;
		}

		if ( DEBUGGING ) {
			LOG.debug( "Returning cached query results" );
		}
		final boolean singleResult = returnTypes.length == 1;
		for ( int i = 1; i < cacheable.size(); i++ ) {
			if ( singleResult ) {
				returnTypes[0].beforeAssemble( (Serializable) cacheable.get( i ), session );
			}
			else {
				TypeHelper.beforeAssemble( (Serializable[]) cacheable.get( i ), returnTypes, session );
			}
		}

		return assembleCachedResult(
				region,
				key,
				cacheable,
				isNaturalKeyLookup,
				singleResult,
				returnTypes,
				session
		);
	}

	private List assembleCachedResult(
			QueryResultsRegion queryResultsRegion,
			final QueryKey queryResultsKey,
			final List cacheable,
			final boolean isNaturalKeyLookup,
			boolean singleResult,
			final org.hibernate.type.Type[] returnTypes,
			final SharedSessionContractImplementor session) throws HibernateException {

		try {
			final List result = new ArrayList( cacheable.size() - 1 );
			if ( singleResult ) {
				for ( int i = 1; i < cacheable.size(); i++ ) {
					result.add( returnTypes[0].getMutabilityPlan().assemble( (Serializable) cacheable.get( i ) ) );
				}
			}
			else {
				for ( int i = 1; i < cacheable.size(); i++ ) {
					result.add(
							TypeHelper.assemble( (Serializable[]) cacheable.get( i ), returnTypes, session, null )
					);
					if ( TRACING ) {
						logCachedResultRowDetails( returnTypes, result.get( i - 1 ) );
					}
				}
			}
			return result;
		}
		catch ( RuntimeException ex ) {
			if ( isNaturalKeyLookup ) {
				// potentially perform special handling for natural-id look ups.
				if ( UnresolvableObjectException.class.isInstance( ex )
						|| EntityNotFoundException.class.isInstance( ex ) ) {
					if ( DEBUGGING ) {
						LOG.debug( "Unable to reassemble cached natural-id query result" );
					}
					queryResultsRegion.evict( queryResultsKey );

					// EARLY EXIT !
					return null;
				}
			}
			throw ex;
		}
	}

	private List getCachedResults(
			QueryResultsRegion queryResultsRegion,
			QueryKey queryResultKey,
			SharedSessionContractImplementor session) {
		List cacheable = null;
		try {
			session.getEventListenerManager().cacheGetStart();
			cacheable = (List) queryResultsRegion.get( session, queryResultKey );
		}
		finally {
			session.getEventListenerManager().cacheGetEnd( cacheable != null );
		}
		return cacheable;
	}


	protected boolean isUpToDate(Set<Serializable> spaces, Long timestamp, SharedSessionContractImplementor session) {
		if ( DEBUGGING ) {
			LOG.debugf( "Checking query spaces are up-to-date: %s", spaces );
		}
		return updateTimestampsRegion.isUpToDate( spaces, timestamp, session );
	}

	private static void logCachedResultDetails(QueryKey key, Set querySpaces, Type[] returnTypes, List result) {
		if ( !TRACING ) {
			return;
		}
		LOG.trace( "key.hashCode=" + key.hashCode() );
		LOG.trace( "querySpaces=" + querySpaces );
		if ( returnTypes == null || returnTypes.length == 0 ) {
			LOG.trace(
					"Unexpected returnTypes is "
							+ ( returnTypes == null ? "null" : "empty" ) + "! result"
							+ ( result == null ? " is null" : ".size()=" + result.size() )
			);
		}
		else {
			final StringBuilder returnTypeInfo = new StringBuilder();
			for ( Type returnType : returnTypes ) {
				returnTypeInfo.append( "typename=" )
						.append( returnType.getName() )
						.append( " class=" )
						.append( returnType.getReturnedClass().getName() )
						.append( ' ' );
			}
			LOG.trace( "unexpected returnTypes is " + returnTypeInfo.toString() + "! result" );
		}
	}

	private static void logCachedResultRowDetails(Type[] returnTypes, Object result) {
		logCachedResultRowDetails(
				returnTypes,
				( result instanceof Object[] ? (Object[]) result : new Object[] { result } )
		);
	}

	private static void logCachedResultRowDetails(Type[] returnTypes, Object[] tuple) {
		if ( !TRACING ) {
			return;
		}
		if ( tuple == null ) {
			LOG.tracef(
					"tuple is null; returnTypes is %s",
					returnTypes == null ? "null" : "Type[" + returnTypes.length + "]"
			);
			if ( returnTypes != null && returnTypes.length > 1 ) {
				LOG.trace(
						"Unexpected result tuple! tuple is null; should be Object["
								+ returnTypes.length + "]!"
				);
			}
		}
		else {
			if ( returnTypes == null || returnTypes.length == 0 ) {
				LOG.trace(
						"Unexpected result tuple! tuple is null; returnTypes is "
								+ ( returnTypes == null ? "null" : "empty" )
				);
			}
			LOG.tracef(
					"tuple is Object[%s]; returnTypes is %s",
					tuple.length,
					returnTypes == null ? "null" : "Type[" + returnTypes.length + "]"
			);
			if ( returnTypes != null && tuple.length != returnTypes.length ) {
				LOG.trace(
						"Unexpected tuple length! transformer= expected="
								+ returnTypes.length + " got=" + tuple.length
				);
			}
			else {
				for ( int j = 0; j < tuple.length; j++ ) {
					if ( tuple[j] != null && returnTypes != null
							&& ! returnTypes[j].getReturnedClass().isInstance( tuple[j] ) ) {
						LOG.trace(
								"Unexpected tuple value type! transformer= expected="
										+ returnTypes[j].getReturnedClass().getName()
										+ " got="
										+ tuple[j].getClass().getName()
						);
					}
				}
			}
		}
	}


	// ~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~
	// RegionBuildingContext impl


	@Override
	public CacheKeysFactory getEnforcedCacheKeysFactory() {
		return sessionFactory.getSessionFactoryOptions().getEnforcedCacheKeysFactory();
	}

	@Override
	public SessionFactoryImplementor getSessionFactory() {
		return sessionFactory;
	}
}
