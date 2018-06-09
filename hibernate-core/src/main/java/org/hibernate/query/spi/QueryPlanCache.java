/*
 * Hibernate, Relational Persistence for Idiomatic Java
 *
 * License: GNU Lesser General Public License (LGPL), version 2.1 or later
 * See the lgpl.txt file in the root directory or http://www.gnu.org/licenses/lgpl-2.1.html
 */
package org.hibernate.query.spi;

/**
 * A cache for QueryPlans used (and produced) by the translation
 * and execution of a query.
 *
 * @author Steve Ebersole
 */
public interface QueryPlanCache {
	interface Key {
	}

	SelectQueryPlan getSelectQueryPlan(Key key);
	void cacheSelectQueryPlan(Key key, SelectQueryPlan plan);

	NonSelectQueryPlan getNonSelectQueryPlan(Key key);
	void cacheNonSelectQueryPlan(Key key, NonSelectQueryPlan plan);

	void close();
}
