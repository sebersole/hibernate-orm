/*
 * Hibernate, Relational Persistence for Idiomatic Java
 *
 * License: GNU Lesser General Public License (LGPL), version 2.1 or later.
 * See the lgpl.txt file in the root directory or <http://www.gnu.org/licenses/lgpl-2.1.html>.
 */
package org.hibernate.query.criteria.internal.path;

import java.util.Map;
import javax.persistence.criteria.Expression;
import javax.persistence.criteria.Predicate;
import javax.persistence.metamodel.MapAttribute;

import org.hibernate.query.criteria.JpaExpression;
import org.hibernate.query.criteria.JpaMapJoin;
import org.hibernate.query.criteria.JpaPath;
import org.hibernate.query.sqm.tree.from.SqmAttributeJoin;

/**
 * Implementor of JpaMapJoin.
 *
 * @author Christian Beikov
 */
public interface JpaMapJoinImplementor<Z, K, V> extends JpaMapJoin<Z, K, V>, JpaPluralJoinImplementor<Z, Map<K, V>, V> {
	@Override
	default JpaMapJoin<Z, K, V> on(Expression<Boolean> restriction) {
		JpaPluralJoinImplementor.super.on( restriction );
		return this;
	}

	@Override
	default JpaMapJoin<Z, K, V> on(Predicate... restrictions) {
		JpaPluralJoinImplementor.super.on( restrictions );
		return this;
	}

	@Override
	default JpaPath<K> key() {
		return null;
	}

	@Override
	default JpaPath<V> value() {
		return null;
	}

	@Override
	default JpaExpression<Map.Entry<K, V>> entry() {
		return null;
	}

	@Override
	default MapAttribute<? super Z, K, V> getModel() {
		return (MapAttribute<? super Z, K, V>) ((SqmAttributeJoin) this).getAttributeReference().getReferencedNavigable();
	}
}
