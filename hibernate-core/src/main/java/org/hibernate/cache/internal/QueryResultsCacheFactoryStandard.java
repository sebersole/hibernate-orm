/*
 * Hibernate, Relational Persistence for Idiomatic Java
 *
 * License: GNU Lesser General Public License (LGPL), version 2.1 or later
 * See the lgpl.txt file in the root directory or http://www.gnu.org/licenses/lgpl-2.1.html
 */
package org.hibernate.cache.internal;

import org.hibernate.cache.spi.QueryResultsCache;
import org.hibernate.cache.spi.QueryResultsCacheFactory;
import org.hibernate.cache.spi.RegionFactory;
import org.hibernate.cache.spi.UpdateTimestampsRegion;
import org.hibernate.engine.spi.SessionFactoryImplementor;

/**
 * @author Steve Ebersole
 */
public class QueryResultsCacheFactoryStandard implements QueryResultsCacheFactory {
	/**
	 * Singleton access
	 */
	public static final QueryResultsCacheFactoryStandard INSTANCE = new QueryResultsCacheFactoryStandard();

	private QueryResultsCacheFactoryStandard() {
	}

	@Override
	public QueryResultsCache createQueryResultsCache(
			SessionFactoryImplementor sessionFactory,
			RegionFactory regionFactory,
			UpdateTimestampsRegion updateTimestampsRegion) {
		if ( !sessionFactory.getSessionFactoryOptions().isQueryCacheEnabled() ) {
			return new QueryResultsCacheDisabled();
		}
		return new QueryResultsCacheStandard( sessionFactory, regionFactory, updateTimestampsRegion );
	}
}
