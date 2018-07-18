/*
 * Hibernate, Relational Persistence for Idiomatic Java
 *
 * License: GNU Lesser General Public License (LGPL), version 2.1 or later
 * See the lgpl.txt file in the root directory or http://www.gnu.org/licenses/lgpl-2.1.html
 */
package org.hibernate.sql.ast.produce.ordering.internal;

import java.util.ArrayList;
import java.util.List;

import org.hibernate.engine.spi.SessionFactoryImplementor;
import org.hibernate.query.internal.QueryOptionsImpl;
import org.hibernate.query.spi.QueryOptions;
import org.hibernate.query.sqm.consume.spi.BaseSqmToSqlAstConverter;
import org.hibernate.query.sqm.tree.order.SqmOrderByClause;
import org.hibernate.query.sqm.tree.order.SqmSortSpecification;
import org.hibernate.sql.ast.produce.internal.NonSelectSqlExpressionResolver;
import org.hibernate.sql.ast.produce.spi.SqlAstCreationContext;
import org.hibernate.sql.ast.produce.spi.SqlExpressionResolver;
import org.hibernate.sql.ast.produce.sqm.spi.Callback;
import org.hibernate.sql.ast.tree.spi.sort.SortSpecification;

/**
 * @author Steve Ebersole
 */
public class OrderByFragmentConverter extends BaseSqmToSqlAstConverter implements SqlAstCreationContext {
	@SuppressWarnings("WeakerAccess")
	public static final QueryOptions QUERY_OPTIONS = new QueryOptionsImpl();

	@SuppressWarnings("WeakerAccess")
	public static List<SortSpecification> convertOrderByFragmentSqmTree(
			SqlAstCreationContext sqlAstCreationContext,
			SqmOrderByClause sqmOrderByClause) {
		return new OrderByFragmentConverter( sqlAstCreationContext ).doConversion( sqmOrderByClause );
	}

	private final List<SortSpecification> collectedSortSpecs = new ArrayList<>();
	private final NonSelectSqlExpressionResolver sqlExpressionResolver = new NonSelectSqlExpressionResolver(
			getSqlAstCreationContext().getSessionFactory(),
			() -> null,
			expression -> expression,
			(expression, selection) -> {}
	);


	@SuppressWarnings("WeakerAccess")
	protected OrderByFragmentConverter(SqlAstCreationContext sqlAstCreationContext) {
		super( sqlAstCreationContext, QUERY_OPTIONS );
	}

	@Override
	public SessionFactoryImplementor getSessionFactory() {
		return getSqlAstCreationContext().getSessionFactory();
	}

	@Override
	public Callback getCallback() {
		return afterLoadAction -> {};
	}

	private List<SortSpecification> doConversion(SqmOrderByClause sqmOrderByClause) {
		for ( SqmSortSpecification sqmSortSpecification : sqmOrderByClause.getSortSpecifications() ) {
			collectedSortSpecs.add( visitSortSpecification( sqmSortSpecification ) );
		}

		return collectedSortSpecs;
	}

	@Override
	public SqlExpressionResolver getSqlSelectionResolver() {
		return sqlExpressionResolver;
	}
}
