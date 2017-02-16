/*
 * Hibernate, Relational Persistence for Idiomatic Java
 *
 * License: GNU Lesser General Public License (LGPL), version 2.1 or later
 * See the lgpl.txt file in the root directory or http://www.gnu.org/licenses/lgpl-2.1.html
 */
package org.hibernate.testing.cache;

import org.hibernate.cache.spi.QueryResultsRegion;

/**
 * Testing implementation of QueryResultsRegion
 *
 * @author Steve Ebersole
 */
public class QueryResultsRegionImpl extends AbstractDirectAccessRegion implements QueryResultsRegion {
	public QueryResultsRegionImpl(CachingRegionFactory regionFactory, String name) {
		super( regionFactory, name );
	}
}
