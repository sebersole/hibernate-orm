/*
 * Hibernate, Relational Persistence for Idiomatic Java
 *
 * License: GNU Lesser General Public License (LGPL), version 2.1 or later.
 * See the lgpl.txt file in the root directory or <http://www.gnu.org/licenses/lgpl-2.1.html>.
 */
package org.hibernate.query.criteria.internal.path;

import java.util.Collection;
import javax.persistence.criteria.Expression;
import javax.persistence.criteria.Predicate;
import javax.persistence.metamodel.CollectionAttribute;

import org.hibernate.query.criteria.JpaCollectionJoin;
import org.hibernate.query.sqm.tree.from.SqmAttributeJoin;

/**
 * Implementor of JpaCollectionJoin.
 *
 * @author Christian Beikov
 */
public interface JpaCollectionJoinImplementor<Z, E> extends JpaCollectionJoin<Z, E>, JpaPluralJoinImplementor<Z, Collection<E>, E> {

	@Override
	default JpaCollectionJoin<Z, E> on(Expression<Boolean> restriction) {
		JpaPluralJoinImplementor.super.on( restriction );
		return this;
	}

	@Override
	default JpaCollectionJoin<Z, E> on(Predicate... restrictions) {
		JpaPluralJoinImplementor.super.on( restrictions );
		return this;
	}

	@Override
	default CollectionAttribute<? super Z, E> getModel() {
		return (CollectionAttribute<? super Z, E>) ((SqmAttributeJoin) this).getAttributeReference().getReferencedNavigable();
	}
}
