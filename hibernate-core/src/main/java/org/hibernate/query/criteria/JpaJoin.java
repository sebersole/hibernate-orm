/*
 * Hibernate, Relational Persistence for Idiomatic Java
 *
 * License: GNU Lesser General Public License (LGPL), version 2.1 or later.
 * See the lgpl.txt file in the root directory or <http://www.gnu.org/licenses/lgpl-2.1.html>.
 */
package org.hibernate.query.criteria;

import javax.persistence.criteria.Expression;
import javax.persistence.criteria.Join;
import javax.persistence.criteria.Predicate;

/**
 * Hibernate extensions to the JPA Join.
 *
 * @author Christian Beikov
 */
public interface JpaJoin<Z, X> extends Join<Z, X>, JpaFrom<Z, X> {

	JpaJoin<Z, X> setFetch(boolean fetch);

	boolean isFetch();

	// Covariant overrides

	JpaJoin<Z, X> on(Expression<Boolean> restriction);

	JpaJoin<Z, X> on(Predicate... restrictions);

	JpaPredicate getOn();

	JpaFrom<?, Z> getParent();
}
