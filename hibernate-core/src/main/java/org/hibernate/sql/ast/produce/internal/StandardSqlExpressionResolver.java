/*
 * Hibernate, Relational Persistence for Idiomatic Java
 *
 * License: GNU Lesser General Public License (LGPL), version 2.1 or later
 * See the lgpl.txt file in the root directory or http://www.gnu.org/licenses/lgpl-2.1.html
 */
package org.hibernate.sql.ast.produce.internal;

import java.util.HashMap;
import java.util.Map;
import java.util.function.BiConsumer;
import java.util.function.Function;
import java.util.function.Supplier;

import org.hibernate.sql.ast.produce.spi.ColumnReferenceQualifier;
import org.hibernate.sql.ast.produce.spi.NonQualifiableSqlExpressable;
import org.hibernate.sql.ast.produce.spi.QualifiableSqlExpressable;
import org.hibernate.sql.ast.produce.spi.SqlExpressable;
import org.hibernate.sql.ast.produce.spi.SqlExpressionResolver;
import org.hibernate.sql.ast.tree.spi.QuerySpec;
import org.hibernate.sql.ast.tree.spi.expression.Expression;
import org.hibernate.sql.results.internal.EmptySqlSelection;
import org.hibernate.sql.results.spi.SqlSelection;
import org.hibernate.type.descriptor.java.spi.BasicJavaDescriptor;
import org.hibernate.type.spi.TypeConfiguration;

/**
 * @author Steve Ebersole
 */
public class StandardSqlExpressionResolver implements SqlExpressionResolver {
	private final Supplier<QuerySpec> querySpecSupplier;
	private final Function<Expression, Expression> normalizer;
	private final BiConsumer<Expression,SqlSelection> selectionConsumer;

	private Map<SqlExpressable,SqlSelection> sqlSelectionMap;

	public StandardSqlExpressionResolver(
			Supplier<QuerySpec> querySpecSupplier,
			Function<Expression, Expression> normalizer,
			BiConsumer<Expression, SqlSelection> selectionConsumer) {
		this.querySpecSupplier = querySpecSupplier;
		this.normalizer = normalizer;
		this.selectionConsumer = selectionConsumer;
	}

	@Override
	public Expression resolveSqlExpression(
			ColumnReferenceQualifier qualifier,
			QualifiableSqlExpressable sqlSelectable) {
		return normalizer.apply( qualifier.qualify( sqlSelectable ) );
	}

	@Override
	public Expression resolveSqlExpression(NonQualifiableSqlExpressable sqlSelectable) {
		return normalizer.apply( sqlSelectable.createExpression() );
	}

	@Override
	public SqlSelection resolveSqlSelection(
			Expression expression,
			BasicJavaDescriptor javaTypeDescriptor,
			TypeConfiguration typeConfiguration) {
		final SqlSelection existing;
		if ( sqlSelectionMap == null ) {
			sqlSelectionMap = new HashMap<>();
			existing = null;
		}
		else {
			existing = sqlSelectionMap.get( expression.getExpressable() );
		}

		if ( existing != null ) {
			return existing;
		}


		final SqlSelection sqlSelection = expression.createSqlSelection(
				sqlSelectionMap.size(),
				javaTypeDescriptor,
				typeConfiguration
		);

		sqlSelectionMap.put( expression.getExpressable(), sqlSelection );
		selectionConsumer.accept( expression, sqlSelection );

		final QuerySpec querySpec = querySpecSupplier.get();
		querySpec.getSelectClause().addSqlSelection( sqlSelection );

		return sqlSelection;
	}

	@Override
	public SqlSelection emptySqlSelection() {
		final EmptySqlSelection selection = new EmptySqlSelection( sqlSelectionMap.size() );
		sqlSelectionMap.put(
				() -> null,
				selection
		);

		final QuerySpec querySpec = querySpecSupplier.get();
		querySpec.getSelectClause().addSqlSelection( selection );

		return selection;
	}
}
