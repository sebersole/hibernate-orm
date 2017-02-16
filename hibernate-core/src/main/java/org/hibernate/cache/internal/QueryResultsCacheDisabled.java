/*
 * Hibernate, Relational Persistence for Idiomatic Java
 *
 * License: GNU Lesser General Public License (LGPL), version 2.1 or later
 * See the lgpl.txt file in the root directory or http://www.gnu.org/licenses/lgpl-2.1.html
 */
package org.hibernate.cache.internal;

import java.io.Serializable;
import java.util.List;
import java.util.Set;

import org.hibernate.cache.spi.QueryKey;
import org.hibernate.cache.spi.QueryResultsCache;
import org.hibernate.engine.spi.SharedSessionContractImplementor;
import org.hibernate.type.Type;

/**
 * No-op version of QueryResultsCache for use when query-result caching
 * is disabled.
 *
 * @author Steve Ebersole
 */
public class QueryResultsCacheDisabled implements QueryResultsCache {
	@Override
	public void clear(String region) {
	}

	@Override
	public boolean put(
			String regionName,
			QueryKey key,
			Type[] returnTypes,
			List result,
			boolean isNaturalKeyLookup,
			SharedSessionContractImplementor session) {
		return false;
	}

	@Override
	public List get(
			String regionName,
			QueryKey key,
			Type[] returnTypes,
			boolean isNaturalKeyLookup,
			Set<Serializable> spaces,
			SharedSessionContractImplementor session) {
		return null;
	}

	@Override
	public void destroy() {
	}
}
