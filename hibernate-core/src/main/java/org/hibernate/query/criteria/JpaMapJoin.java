/*
 * Hibernate, Relational Persistence for Idiomatic Java
 *
 * License: GNU Lesser General Public License (LGPL), version 2.1 or later.
 * See the lgpl.txt file in the root directory or <http://www.gnu.org/licenses/lgpl-2.1.html>.
 */
package org.hibernate.query.criteria;

import java.util.Map;
import javax.persistence.criteria.Expression;
import javax.persistence.criteria.MapJoin;
import javax.persistence.criteria.Predicate;

/**
 * Hibernate extensions to the JPA MapJoin.
 *
 * @author Christian Beikov
 */
public interface JpaMapJoin<Z, K, V> extends MapJoin<Z, K, V>, JpaPluralJoin<Z, Map<K, V>, V> {
	JpaMapJoin<Z, K, V> on(Expression<Boolean> restriction);

	JpaMapJoin<Z, K, V> on(Predicate... restrictions);

	JpaPath<K> key();

	JpaPath<V> value();

	JpaExpression<Map.Entry<K, V>> entry();
}
