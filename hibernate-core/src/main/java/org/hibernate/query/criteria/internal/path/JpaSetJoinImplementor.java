/*
 * Hibernate, Relational Persistence for Idiomatic Java
 *
 * License: GNU Lesser General Public License (LGPL), version 2.1 or later.
 * See the lgpl.txt file in the root directory or <http://www.gnu.org/licenses/lgpl-2.1.html>.
 */
package org.hibernate.query.criteria.internal.path;

import java.util.Set;
import javax.persistence.criteria.Expression;
import javax.persistence.criteria.Predicate;
import javax.persistence.metamodel.SetAttribute;

import org.hibernate.query.criteria.JpaSetJoin;
import org.hibernate.query.sqm.tree.from.SqmAttributeJoin;

/**
 * Implementor of JpaSetJoin.
 *
 * @author Christian Beikov
 */
public interface JpaSetJoinImplementor<Z, E> extends JpaSetJoin<Z, E>, JpaPluralJoinImplementor<Z, Set<E>, E> {

	@Override
	default JpaSetJoin<Z, E> on(Expression<Boolean> restriction) {
		JpaPluralJoinImplementor.super.on( restriction );
		return this;
	}

	@Override
	default JpaSetJoin<Z, E> on(Predicate... restrictions) {
		JpaPluralJoinImplementor.super.on( restrictions );
		return this;
	}

	@Override
	default SetAttribute<? super Z, E> getModel() {
		return (SetAttribute<? super Z, E>) ((SqmAttributeJoin) this).getAttributeReference().getReferencedNavigable();
	}
}
