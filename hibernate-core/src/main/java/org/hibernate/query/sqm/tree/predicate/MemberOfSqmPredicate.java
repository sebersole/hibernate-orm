/*
 * Hibernate, Relational Persistence for Idiomatic Java
 *
 * License: GNU Lesser General Public License (LGPL), version 2.1 or later
 * See the lgpl.txt file in the root directory or http://www.gnu.org/licenses/lgpl-2.1.html
 */
package org.hibernate.query.sqm.tree.predicate;

import org.hibernate.query.sqm.consume.spi.SemanticQueryWalker;
import org.hibernate.query.sqm.tree.expression.SqmExpression;
import org.hibernate.query.sqm.tree.expression.domain.SqmPluralAttributeReference;

/**
 * @author Steve Ebersole
 */
public class MemberOfSqmPredicate extends AbstractNegatableSqmPredicate {
	private final SqmExpression expression;
	private final SqmPluralAttributeReference pluralAttributeReference;

	public MemberOfSqmPredicate(
			SqmExpression expression,
			SqmPluralAttributeReference pluralAttributeReference) {
		this( expression, pluralAttributeReference, false );
	}

	public MemberOfSqmPredicate(
			SqmExpression expression,
			SqmPluralAttributeReference pluralAttributeReference,
			boolean negated) {
		super( negated );
		this.expression = expression;

		assert pluralAttributeReference != null;
		this.pluralAttributeReference = pluralAttributeReference;
	}

	public SqmExpression getExpression() {
		return expression;
	}

	public SqmPluralAttributeReference getPluralAttributeReference() {
		return pluralAttributeReference;
	}

	@Override
	public <T> T accept(SemanticQueryWalker<T> walker) {
		return walker.visitMemberOfPredicate( this );
	}
}
