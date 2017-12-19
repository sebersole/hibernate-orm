/*
 * Hibernate, Relational Persistence for Idiomatic Java
 *
 * License: GNU Lesser General Public License (LGPL), version 2.1 or later.
 * See the lgpl.txt file in the root directory or <http://www.gnu.org/licenses/lgpl-2.1.html>.
 */
package org.hibernate.query.criteria;

import java.util.Set;
import javax.persistence.criteria.Expression;
import javax.persistence.criteria.Predicate;
import javax.persistence.criteria.SetJoin;

/**
 * Hibernate extensions to the JPA SetJoin.
 *
 * @author Christian Beikov
 */
public interface JpaSetJoin<Z, E> extends SetJoin<Z, E>, JpaPluralJoin<Z, Set<E>, E> {
	JpaSetJoin<Z, E> on(Expression<Boolean> restriction);

	JpaSetJoin<Z, E> on(Predicate... restrictions);
}
