/*
 * Hibernate, Relational Persistence for Idiomatic Java
 *
 * License: GNU Lesser General Public License (LGPL), version 2.1 or later.
 * See the lgpl.txt file in the root directory or <http://www.gnu.org/licenses/lgpl-2.1.html>.
 */
package org.hibernate.query.criteria;

import java.util.List;
import javax.persistence.criteria.Expression;
import javax.persistence.criteria.ListJoin;
import javax.persistence.criteria.Predicate;

/**
 * Hibernate extensions to the JPA ListJoin.
 *
 * @author Christian Beikov
 */
public interface JpaListJoin<Z, E> extends ListJoin<Z, E>, JpaPluralJoin<Z, List<E>, E> {
	JpaListJoin<Z, E> on(Expression<Boolean> restriction);

	JpaListJoin<Z, E> on(Predicate... restrictions);

	JpaExpression<Integer> index();
}
