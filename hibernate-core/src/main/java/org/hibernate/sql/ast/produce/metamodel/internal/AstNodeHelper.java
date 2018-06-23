/*
 * Hibernate, Relational Persistence for Idiomatic Java
 *
 * License: GNU Lesser General Public License (LGPL), version 2.1 or later
 * See the lgpl.txt file in the root directory or http://www.gnu.org/licenses/lgpl-2.1.html
 */
package org.hibernate.sql.ast.produce.metamodel.internal;

import java.util.ArrayList;
import java.util.List;

import org.hibernate.engine.spi.SharedSessionContractImplementor;
import org.hibernate.metamodel.model.domain.spi.Navigable;
import org.hibernate.sql.ast.produce.spi.ColumnReferenceQualifier;
import org.hibernate.sql.ast.tree.spi.expression.ColumnReference;
import org.hibernate.sql.ast.tree.spi.expression.Expression;
import org.hibernate.sql.ast.tree.spi.expression.ParameterSpec;
import org.hibernate.sql.ast.tree.spi.expression.SqlTuple;
import org.hibernate.sql.ast.tree.spi.expression.StandardJdbcParameter;
import org.hibernate.sql.ast.tree.spi.predicate.InListPredicate;
import org.hibernate.sql.ast.tree.spi.predicate.Predicate;
import org.hibernate.sql.ast.tree.spi.predicate.RelationalPredicate;
import org.hibernate.sql.exec.internal.StandardJdbcParameterBindings;
import org.hibernate.sql.results.spi.SqlSelectionResolutionContext;

/**
 * Helper for generating SQL-AST nodes
 *
 * todo (6.0) : consider exposing some of these from Navigable - similar to what we do for SQM building (#createSqmExpression, etc).
 *
 * @author Steve Ebersole
 */
public class AstNodeHelper {
	public static Expression createExpression(
			ColumnReferenceQualifier qualifier,
			SqlSelectionResolutionContext sqlExpressionContext,
			Navigable<?> navigable) {
		final List<ColumnReference> columnReferences = navigable.resolveColumnReferences(
				qualifier,
				sqlExpressionContext
		);

		if ( columnReferences.size() == 1 ) {
			return columnReferences.get( 0 );
		}
		else {
			return new SqlTuple( (List) columnReferences );
		}
	}

	public static Predicate createRestriction(
			int numberOfTimes,
			ColumnReferenceQualifier qualifier,
			SqlSelectionResolutionContext sqlExpressionContext,
			Navigable<?> navigable) {
		final Expression columnReferences = createExpression( qualifier, sqlExpressionContext, navigable );
		final int numberOfColumns = columnReferences instanceof SqlTuple
				? ( (SqlTuple) columnReferences ).getExpressions().size()
				: 1;

		if ( numberOfTimes == 1 ) {
			return new RelationalPredicate(
					RelationalPredicate.Operator.EQUAL,
					columnReferences,
					generateJdbcParameters( navigable, numberOfColumns )
			);
		}
		else {
			final InListPredicate predicate = new InListPredicate( columnReferences );
			for ( int i = 0; i < numberOfTimes; i++ ) {
				predicate.addExpression( generateJdbcParameters( navigable, numberOfColumns ) );
			}
			return predicate;
		}
	}

	private static Expression generateJdbcParameters(Navigable<?> navigable, int numberOfColumns) {
		final List<ParameterSpec> parameterSpecs = new ArrayList<>();
		navigable.toJdbcParameters(
				(column, parameterSpec) -> parameterSpecs.add( parameterSpec )
		);

		assert numberOfColumns == parameterSpecs.size();

		if ( numberOfColumns == 1 ) {
			return parameterSpecs.get( 0 );
		}
		else {
			return new SqlTuple( (List) parameterSpecs );
		}
	}

	public static StandardJdbcParameterBindings toJdbcParamBindings(
			Navigable<?> navigable,
			Object value,
			SharedSessionContractImplementor session) {

	}

	public static StandardJdbcParameterBindings toJdbcParamBindings(Object[] values, SharedSessionContractImplementor session) {
		final StandardJdbcParameterBindings.Builder jdbcParamBindingsBuilder = new StandardJdbcParameterBindings.Builder();
		entityDescriptor.getHierarchy().getIdentifierDescriptor().dehydrate(
				id,
				(jdbcValue, boundColumn, jdbcValueMapper) -> jdbcParamBindingsBuilder.add(
						new StandardJdbcParameter( jdbcValueMapper ),
						jdbcValue
				),
				session
		);
		return jdbcParamBindingsBuilder.build();
	}
}
