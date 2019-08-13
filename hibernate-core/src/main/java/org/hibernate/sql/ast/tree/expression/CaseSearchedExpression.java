/*
 * Hibernate, Relational Persistence for Idiomatic Java
 *
 * License: GNU Lesser General Public License (LGPL), version 2.1 or later
 * See the lgpl.txt file in the root directory or http://www.gnu.org/licenses/lgpl-2.1.html
 */

package org.hibernate.sql.ast.tree.expression;

import java.util.ArrayList;
import java.util.List;

import org.hibernate.NotYetImplementedFor6Exception;
import org.hibernate.metamodel.model.mapping.spi.ValueMapping;
import org.hibernate.sql.ast.ValueMappingExpressable;
import org.hibernate.sql.ast.spi.SqlAstWalker;
import org.hibernate.sql.ast.tree.predicate.Predicate;
import org.hibernate.sql.results.spi.DomainResult;
import org.hibernate.sql.results.spi.DomainResultCreationState;
import org.hibernate.sql.results.spi.DomainResultProducer;

/**
 * @author Steve Ebersole
 */
public class CaseSearchedExpression implements Expression, ValueMappingExpressable, DomainResultProducer {
	private final ValueMappingExpressable type;

	private List<WhenFragment> whenFragments = new ArrayList<>();
	private Expression otherwise;

	public CaseSearchedExpression(ValueMappingExpressable type) {
		this.type = type;
	}

	public List<WhenFragment> getWhenFragments() {
		return whenFragments;
	}

	public Expression getOtherwise() {
		return otherwise;
	}

	public void when(Predicate predicate, Expression result) {
		whenFragments.add( new WhenFragment( predicate, result ) );
	}

	public void otherwise(Expression otherwiseExpression) {
		this.otherwise = otherwiseExpression;
		// todo : inject implied type?
	}

	@Override
	public DomainResult createDomainResult(
			String resultVariable,
			DomainResultCreationState creationState) {
		throw new NotYetImplementedFor6Exception( getClass() );

//		return new BasicResultImpl(
//				resultVariable,
//				creationState.getSqlExpressionResolver().resolveSqlSelection(
//						this,
//						getType().getJavaTypeDescriptor(),
//						creationState.getSqlAstCreationState().getCreationContext().getDomainModel().getTypeConfiguration()
//				),
//				getType()
//		);
	}

	@Override
	public void accept(SqlAstWalker walker) {
		walker.visitCaseSearchedExpression( this );
	}

	@Override
	public ValueMapping getExpressableValueMapping() {
		return null;
	}

	@Override
	public ValueMappingExpressable getExpressionType() {
		if ( type == null ) {
			return this;
		}
		return type;
	}

	public static class WhenFragment {
		private final Predicate predicate;
		private final Expression result;

		public WhenFragment(Predicate predicate, Expression result) {
			this.predicate = predicate;
			this.result = result;
		}

		public Predicate getPredicate() {
			return predicate;
		}

		public Expression getResult() {
			return result;
		}
	}
}
