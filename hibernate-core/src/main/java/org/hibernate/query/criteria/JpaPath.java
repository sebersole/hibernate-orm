/*
 * Hibernate, Relational Persistence for Idiomatic Java
 *
 * License: GNU Lesser General Public License (LGPL), version 2.1 or later.
 * See the lgpl.txt file in the root directory or <http://www.gnu.org/licenses/lgpl-2.1.html>.
 */
package org.hibernate.query.criteria;

import javax.persistence.criteria.Path;
import javax.persistence.metamodel.MapAttribute;
import javax.persistence.metamodel.PluralAttribute;
import javax.persistence.metamodel.SingularAttribute;

/**
 * Hibernate extensions to the JPA Path.
 *
 * @author Christian Beikov
 */
public interface JpaPath<X> extends Path<X>, JpaExpression<X> {
	JpaPath<?> getParentPath();

	<Y> JpaPath<Y> get(SingularAttribute<? super X, Y> attribute);

	<E, C extends java.util.Collection<E>> JpaExpression<C> get(PluralAttribute<X, C, E> collection);

	<K, V, M extends java.util.Map<K, V>> JpaExpression<M> get(MapAttribute<X, K, V> map);

	JpaExpression<Class<? extends X>> type();


	//String-based:

	<Y> JpaPath<Y> get(String attributeName);
}
