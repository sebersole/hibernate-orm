/*
 * Hibernate, Relational Persistence for Idiomatic Java
 *
 * License: GNU Lesser General Public License (LGPL), version 2.1 or later
 * See the lgpl.txt file in the root directory or http://www.gnu.org/licenses/lgpl-2.1.html
 */
package org.hibernate.query.sqm.produce.function.internal;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import org.hibernate.NotYetImplementedFor6Exception;
import org.hibernate.engine.spi.SessionFactoryImplementor;
import org.hibernate.metamodel.model.domain.spi.AllowableFunctionReturnType;
import org.hibernate.metamodel.model.domain.spi.ConvertibleNavigable;
import org.hibernate.query.sqm.tree.expression.SqmExpression;
import org.hibernate.sql.JdbcValueBinder;
import org.hibernate.sql.JdbcValueExtractor;
import org.hibernate.sql.JdbcValueMapper;
import org.hibernate.sql.ast.consume.spi.SelfRenderingExpression;
import org.hibernate.sql.ast.consume.spi.SqlAppender;
import org.hibernate.sql.ast.consume.spi.SqlAstWalker;
import org.hibernate.sql.ast.produce.metamodel.spi.BasicValuedExpressableType;
import org.hibernate.sql.ast.produce.spi.SqlExpressable;
import org.hibernate.sql.ast.produce.sqm.spi.SqmToSqlAstConverter;
import org.hibernate.sql.ast.tree.spi.expression.Expression;
import org.hibernate.sql.results.internal.ScalarQueryResultImpl;
import org.hibernate.sql.results.internal.SqlSelectionImpl;
import org.hibernate.sql.results.spi.QueryResult;
import org.hibernate.sql.results.spi.QueryResultCreationContext;
import org.hibernate.sql.results.spi.QueryResultProducer;
import org.hibernate.sql.results.spi.Selectable;
import org.hibernate.sql.results.spi.SqlSelection;
import org.hibernate.type.descriptor.java.spi.BasicJavaDescriptor;
import org.hibernate.type.descriptor.sql.spi.SqlTypeDescriptor;

/**
 * Representation of a function call in the SQL AST for impls that know how to
 * render themselves.
 *
 * @author Steve Ebersole
 */
public class SelfRenderingFunctionSqlAstExpression
		implements SelfRenderingExpression, Selectable, SqlExpressable, QueryResultProducer {
	private final SelfRenderingSqmFunction sqmExpression;
	private final List<Expression> sqlAstArguments;

	public SelfRenderingFunctionSqlAstExpression(
			SelfRenderingSqmFunction sqmExpression,
			SqmToSqlAstConverter walker) {
		this.sqmExpression = sqmExpression;
		this.sqlAstArguments = resolveSqlAstArguments( sqmExpression.getSqmArguments(), walker );
	}

	private static List<Expression> resolveSqlAstArguments(List<SqmExpression> sqmArguments, SqmToSqlAstConverter walker) {
		if ( sqmArguments == null || sqmArguments.isEmpty() ) {
			return Collections.emptyList();
		}

		final ArrayList<Expression> sqlAstArguments = new ArrayList<>();
		for ( SqmExpression sqmArgument : sqmArguments ) {
			sqlAstArguments.add( (Expression) sqmArgument.accept( walker ) );
		}
		return sqlAstArguments;
	}

	@Override
	public AllowableFunctionReturnType getType() {
		return sqmExpression.getExpressableType();
	}

	@Override
	public SqlSelection createSqlSelection(int jdbcPosition) {
		return new SqlSelectionImpl(
				jdbcPosition,
				null,
				( (BasicValuedExpressableType) getType() ).getBasicType().getSqlSelectionReader()
		);
	}

	@Override
	public QueryResult createQueryResult(
			String resultVariable,
			QueryResultCreationContext creationContext) {
		return new ScalarQueryResultImpl(
				resultVariable,
				creationContext.getSqlSelectionResolver().resolveSqlSelection( this ),
				sqmExpression.getExpressableType() instanceof ConvertibleNavigable
						? ( (ConvertibleNavigable) sqmExpression.getExpressableType() ).getValueConverter()
						: null
		);
	}

	@Override
	public void renderToSql(
			SqlAppender sqlAppender,
			SqlAstWalker walker,
			SessionFactoryImplementor sessionFactory) {
		sqmExpression.getRenderingSupport().render( sqlAppender, sqlAstArguments,walker, sessionFactory );
	}

	@Override
	public JdbcValueMapper getJdbcValueMapper() {
		return ( (BasicValuedExpressableType) sqmExpression.getJavaTypeDescriptor() ).getBasicType();
	}

	// todo (6.0) : consider ways to allow returning non-basic types from SQM functions
	//		the issue really is the combining SQM function and SQL-AST function into one

	@Override
	public BasicJavaDescriptor getJavaTypeDescriptor() {
		throw new NotYetImplementedFor6Exception();
	}

	@Override
	public SqlTypeDescriptor getSqlTypeDescriptor() {
		throw new NotYetImplementedFor6Exception();
	}

	@Override
	public JdbcValueBinder getJdbcValueBinder() {
		throw new NotYetImplementedFor6Exception();
	}

	@Override
	public JdbcValueExtractor getJdbcValueExtractor() {
		throw new NotYetImplementedFor6Exception();
	}
}
