/*
 * Hibernate, Relational Persistence for Idiomatic Java
 *
 * License: GNU Lesser General Public License (LGPL), version 2.1 or later.
 * See the lgpl.txt file in the root directory or <http://www.gnu.org/licenses/lgpl-2.1.html>.
 */
package org.hibernate.query.criteria;

import java.util.Collection;
import javax.persistence.criteria.CollectionJoin;
import javax.persistence.criteria.Expression;
import javax.persistence.criteria.Join;
import javax.persistence.criteria.Predicate;

/**
 * Hibernate extensions to the JPA CollectionJoin.
 *
 * @author Christian Beikov
 */
public interface JpaCollectionJoin<Z, E> extends CollectionJoin<Z, E>, JpaPluralJoin<Z, Collection<E>, E> {
	JpaCollectionJoin<Z, E> on(Expression<Boolean> restriction);

	JpaCollectionJoin<Z, E> on(Predicate... restrictions);
}
