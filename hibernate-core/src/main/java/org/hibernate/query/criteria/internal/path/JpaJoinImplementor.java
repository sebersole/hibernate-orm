/*
 * Hibernate, Relational Persistence for Idiomatic Java
 *
 * License: GNU Lesser General Public License (LGPL), version 2.1 or later.
 * See the lgpl.txt file in the root directory or <http://www.gnu.org/licenses/lgpl-2.1.html>.
 */
package org.hibernate.query.criteria.internal.path;

import javax.persistence.criteria.Expression;
import javax.persistence.criteria.Predicate;
import javax.persistence.metamodel.Attribute;

import org.hibernate.query.criteria.JpaFrom;
import org.hibernate.query.criteria.JpaJoin;
import org.hibernate.query.criteria.JpaPredicate;
import org.hibernate.query.criteria.internal.SqmUtils;
import org.hibernate.query.sqm.tree.from.SqmAttributeJoin;
import org.hibernate.query.sqm.tree.from.SqmQualifiedJoin;
import org.hibernate.query.sqm.tree.predicate.SqmPredicate;

/**
 * Implementor of JpaJoin.
 *
 * @author Christian Beikov
 */
public interface JpaJoinImplementor<Z, X> extends JpaJoin<Z, X>, JpaFromImplementor<Z, X>, SqmQualifiedJoin {

	@Override
	default JpaJoin<Z, X> setFetch(boolean fetch) {
		((SqmAttributeJoin) this).setFetched(fetch);
		return this;
	}

	@Override
	default boolean isFetch() {
		return ((SqmAttributeJoin) this).isFetched();
	}

	@Override
	default JpaJoin<Z, X> on(Expression<Boolean> restriction) {
		setOnClausePredicate( (SqmPredicate) restriction );
		return this;
	}

	@Override
	default JpaJoin<Z, X> on(Predicate... restrictions) {
		setOnClausePredicate((SqmPredicate) SqmUtils.getAndPredicate( restrictions ));
		return this;
	}

	@Override
	default JpaPredicate getOn() {
		return (JpaPredicate) getOnClausePredicate();
	}

	@Override
	@SuppressWarnings("unchecked")
	default JpaFrom<?, Z> getParent() {
		return (JpaFrom<?, Z>) ((SqmAttributeJoin) this).getLhs();
	}

	@Override
	@SuppressWarnings("unchecked")
	default Attribute<? super Z, ?> getAttribute() {
		return (Attribute<? super Z, ?>) ((SqmAttributeJoin) this).getAttributeReference().getReferencedNavigable();
	}

	// TODO: refactor name of SqmJoin#getJoinType() to getSqmJoinType() and convert here
//	@Override
//	default JoinType getJoinType() {
//		return null;
//	}
}
