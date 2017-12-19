/*
 * Hibernate, Relational Persistence for Idiomatic Java
 *
 * License: GNU Lesser General Public License (LGPL), version 2.1 or later.
 * See the lgpl.txt file in the root directory or <http://www.gnu.org/licenses/lgpl-2.1.html>.
 */
package org.hibernate.query.criteria;

import java.util.Set;
import javax.persistence.criteria.From;
import javax.persistence.criteria.JoinType;
import javax.persistence.metamodel.CollectionAttribute;
import javax.persistence.metamodel.ListAttribute;
import javax.persistence.metamodel.MapAttribute;
import javax.persistence.metamodel.SetAttribute;
import javax.persistence.metamodel.SingularAttribute;

/**
 * Hibernate extensions to the JPA From.
 *
 * @author Christian Beikov
 */
public interface JpaFrom<Z, X> extends From<Z, X>, JpaPath<X>, JpaFetchParent<Z, X> {

	Set<JpaJoin<X, ?>> getJpaJoins();

	JpaFrom<Z, X> getCorrelationParent();

	<Y> JpaJoin<X, Y> join(SingularAttribute<? super X, Y> attribute);

	<Y> JpaJoin<X, Y> join(SingularAttribute<? super X, Y> attribute, JoinType jt);

	<Y> JpaCollectionJoin<X, Y> join(CollectionAttribute<? super X, Y> collection);

	<Y> JpaSetJoin<X, Y> join(SetAttribute<? super X, Y> set);

	<Y> JpaListJoin<X, Y> join(ListAttribute<? super X, Y> list);

	<K, V> JpaMapJoin<X, K, V> join(MapAttribute<? super X, K, V> map);

	<Y> JpaCollectionJoin<X, Y> join(CollectionAttribute<? super X, Y> collection, JoinType jt);

	<Y> JpaSetJoin<X, Y> join(SetAttribute<? super X, Y> set, JoinType jt);

	<Y> JpaListJoin<X, Y> join(ListAttribute<? super X, Y> list, JoinType jt);

	<K, V> JpaMapJoin<X, K, V> join(MapAttribute<? super X, K, V> map, JoinType jt);


	//String-based:

	<X, Y> JpaJoin<X, Y> join(String attributeName);

	<X, Y> JpaCollectionJoin<X, Y> joinCollection(String attributeName);

	<X, Y> JpaSetJoin<X, Y> joinSet(String attributeName);

	<X, Y> JpaListJoin<X, Y> joinList(String attributeName);

	<X, K, V> JpaMapJoin<X, K, V> joinMap(String attributeName);

	<X, Y> JpaJoin<X, Y> join(String attributeName, JoinType jt);

	<X, Y> JpaCollectionJoin<X, Y> joinCollection(String attributeName, JoinType jt);

	<X, Y> JpaSetJoin<X, Y> joinSet(String attributeName, JoinType jt);

	<X, Y> JpaListJoin<X, Y> joinList(String attributeName, JoinType jt);

	<X, K, V> JpaMapJoin<X, K, V> joinMap(String attributeName, JoinType jt);
}
