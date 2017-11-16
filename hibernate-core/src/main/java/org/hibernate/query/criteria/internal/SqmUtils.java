/*
 * Hibernate, Relational Persistence for Idiomatic Java
 *
 * License: GNU Lesser General Public License (LGPL), version 2.1 or later.
 * See the lgpl.txt file in the root directory or <http://www.gnu.org/licenses/lgpl-2.1.html>.
 */
package org.hibernate.query.criteria.internal;

import javax.persistence.criteria.JoinType;
import javax.persistence.criteria.Predicate;

import org.hibernate.query.criteria.JpaPredicate;
import org.hibernate.query.criteria.internal.predicate.JpaAndPredicate;
import org.hibernate.query.criteria.internal.predicate.JpaOrPredicate;
import org.hibernate.query.criteria.internal.predicate.JpaPredicateImplementor;
import org.hibernate.query.sqm.tree.SqmJoinType;
import org.hibernate.query.sqm.tree.predicate.AndSqmPredicate;
import org.hibernate.query.sqm.tree.predicate.OrSqmPredicate;
import org.hibernate.query.sqm.tree.predicate.SqmPredicate;

public abstract class SqmUtils {

	public static JpaPredicateImplementor getAndPredicate(Predicate... restrictions) {
		if (restrictions.length == 0) {
			return null;
		}

		JpaPredicateImplementor rhs = (JpaPredicateImplementor) restrictions[restrictions.length - 1];
		QueryContext context = rhs.context();
		for (int i = restrictions.length - 2; i >= 0; i--) {
			JpaPredicateImplementor lhs = (JpaPredicateImplementor) restrictions[i];
			rhs = new JpaAndPredicate( lhs, rhs, context );
		}

		return rhs;
	}

	public static JpaPredicateImplementor getOrPredicate(Predicate... restrictions) {
		if (restrictions.length == 0) {
			return null;
		}

		JpaPredicateImplementor rhs = (JpaPredicateImplementor) restrictions[restrictions.length - 1];
		QueryContext context = rhs.context();
		for (int i = restrictions.length - 2; i >= 0; i--) {
			JpaPredicateImplementor lhs = (JpaPredicateImplementor) restrictions[i];
			rhs = new JpaOrPredicate( lhs, rhs, context );
		}

		return rhs;
	}

	public static SqmJoinType getSqmJoinType(JoinType jt) {
		switch ( jt ) {
			case LEFT: return SqmJoinType.LEFT;
			case INNER: return SqmJoinType.INNER;
			case RIGHT: return SqmJoinType.RIGHT;
		}

		throw new IllegalArgumentException( "Unknown join type: " + jt );
	}
}
