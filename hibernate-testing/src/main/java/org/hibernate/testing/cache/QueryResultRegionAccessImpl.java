/*
 * Hibernate, Relational Persistence for Idiomatic Java
 *
 * License: GNU Lesser General Public License (LGPL), version 2.1 or later
 * See the lgpl.txt file in the root directory or http://www.gnu.org/licenses/lgpl-2.1.html
 */
package org.hibernate.testing.cache;

import java.io.Serializable;
import java.util.List;
import java.util.Set;

import org.hibernate.HibernateException;
import org.hibernate.cache.CacheException;
import org.hibernate.cache.spi.QueryKey;
import org.hibernate.cache.spi.Region;
import org.hibernate.cache.spi.access.QueryResultRegionAccess;
import org.hibernate.cache.spi.access.SoftLock;
import org.hibernate.cache.spi.access.StatisticsAwareRegionAccess;
import org.hibernate.engine.spi.SharedSessionContractImplementor;
import org.hibernate.stat.spi.StatisticsImplementor;
import org.hibernate.type.spi.Type;

/**
 * @author Steve Ebersole
 */
public class QueryResultRegionAccessImpl extends BaseRegionAccess implements QueryResultRegionAccess, StatisticsAwareRegionAccess {
	private StatisticsImplementor statisticsImplementor;

	QueryResultRegionAccessImpl(RegionImpl region) {
		super( region );
	}

	@Override
	public void injectStatisticsImplementor(StatisticsImplementor statisticsImplementor) {
		this.statisticsImplementor = statisticsImplementor;
	}

	@Override
	protected boolean isDefaultMinimalPutOverride() {
		return getInternalRegion().getRegionFactory().isMinimalPutsEnabledByDefault();
	}

	@Override
	public void clear() throws CacheException {

	}

	@Override
	public boolean put(
			QueryKey key,
			Type[] returnTypes,
			List result,
			boolean isNaturalKeyLookup,
			SharedSessionContractImplementor session) throws HibernateException {
		getInternalRegion().put( session, key, result );
		return true;
	}

	@Override
	public List get(
			QueryKey key,
			Type[] returnTypes,
			boolean isNaturalKeyLookup,
			Set<Serializable> spaces,
			SharedSessionContractImplementor session) throws HibernateException {
		return (List) getInternalRegion().get( session, key );
	}

	@Override
	public void destroy() {

	}
}
