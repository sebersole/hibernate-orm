/*
 * Hibernate, Relational Persistence for Idiomatic Java
 *
 * License: GNU Lesser General Public License (LGPL), version 2.1 or later.
 * See the lgpl.txt file in the root directory or <http://www.gnu.org/licenses/lgpl-2.1.html>.
 */
package org.hibernate.query.criteria.internal.path;

import java.util.List;

import javax.persistence.criteria.Expression;
import javax.persistence.criteria.Predicate;
import javax.persistence.metamodel.ListAttribute;
import javax.persistence.metamodel.PluralAttribute;

import org.hibernate.query.criteria.JpaExpression;
import org.hibernate.query.criteria.JpaListJoin;
import org.hibernate.query.criteria.internal.expression.JpaListIndexExpressionBasic;
import org.hibernate.query.criteria.internal.expression.JpaListIndexExpressionEmbedded;
import org.hibernate.query.criteria.internal.expression.JpaListIndexExpressionEntity;
import org.hibernate.query.sqm.tree.expression.domain.SqmPluralAttributeReference;
import org.hibernate.query.sqm.tree.from.SqmAttributeJoin;

/**
 * Implementor of JpaListJoin.
 *
 * @author Christian Beikov
 */
public interface JpaListJoinImplementor<Z, E> extends JpaListJoin<Z, E>, JpaPluralJoinImplementor<Z, List<E>, E> {

	@Override
	default JpaListJoin<Z, E> on(Expression<Boolean> restriction) {
		JpaPluralJoinImplementor.super.on( restriction );
		return this;
	}

	@Override
	default JpaListJoin<Z, E> on(Predicate... restrictions) {
		JpaPluralJoinImplementor.super.on( restrictions );
		return this;
	}

	@Override
	default JpaExpression<Integer> index() {
		SqmPluralAttributeReference attributeReference = (SqmPluralAttributeReference) ((SqmAttributeJoin) this).getAttributeReference();
		switch (getModel().getElementType().getPersistenceType()) {
			case BASIC:
				return new JpaListIndexExpressionBasic( attributeReference, context() );
			case ENTITY:
				return new JpaListIndexExpressionEntity( attributeReference, context() );
			case EMBEDDABLE:
				return new JpaListIndexExpressionEmbedded( attributeReference, context() );
		}

		throw new IllegalArgumentException( "Unknown element type: " + getModel().getElementType().getPersistenceType() );
	}

	@Override
	@SuppressWarnings("unchecked")
	default ListAttribute<? super Z, E> getModel() {
		return (ListAttribute<? super Z, E>) ((SqmAttributeJoin) this).getAttributeReference().getReferencedNavigable();
	}
}
