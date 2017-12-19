/*
 * Hibernate, Relational Persistence for Idiomatic Java
 *
 * License: GNU Lesser General Public License (LGPL), version 2.1 or later.
 * See the lgpl.txt file in the root directory or <http://www.gnu.org/licenses/lgpl-2.1.html>.
 */
package org.hibernate.query.criteria;

import javax.persistence.criteria.FetchParent;
import javax.persistence.criteria.JoinType;
import javax.persistence.metamodel.PluralAttribute;
import javax.persistence.metamodel.SingularAttribute;

/**
 * Hibernate extensions to the JPA FetchParent.
 *
 * @author Christian Beikov
 */
public interface JpaFetchParent<Z, X> extends FetchParent<Z, X> {
	java.util.Set<JpaFetch<X, ?>> getJpaFetches();

	<Y> JpaFetch<X, Y> fetch(SingularAttribute<? super X, Y> attribute);

	<Y> JpaFetch<X, Y> fetch(SingularAttribute<? super X, Y> attribute, JoinType jt);

	<Y> JpaFetch<X, Y> fetch(PluralAttribute<? super X, ?, Y> attribute);

	<Y> JpaFetch<X, Y> fetch(PluralAttribute<? super X, ?, Y> attribute, JoinType jt);


	//String-based:

	@SuppressWarnings("hiding")
	<X, Y> JpaFetch<X, Y> fetch(String attributeName);

	@SuppressWarnings("hiding")
	<X, Y> JpaFetch<X, Y> fetch(String attributeName, JoinType jt);

}
