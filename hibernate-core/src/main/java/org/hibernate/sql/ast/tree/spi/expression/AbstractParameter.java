/*
 * Hibernate, Relational Persistence for Idiomatic Java
 *
 * License: GNU Lesser General Public License (LGPL), version 2.1 or later
 * See the lgpl.txt file in the root directory or http://www.gnu.org/licenses/lgpl-2.1.html
 */
package org.hibernate.sql.ast.tree.spi.expression;

import java.sql.PreparedStatement;
import java.sql.SQLException;

import org.hibernate.sql.SqlExpressableType;
import org.hibernate.sql.ast.produce.metamodel.spi.BasicValuedExpressableType;
import org.hibernate.sql.ast.Clause;
import org.hibernate.sql.exec.ExecutionException;
import org.hibernate.sql.exec.spi.ExecutionContext;
import org.hibernate.sql.exec.spi.JdbcParameter;
import org.hibernate.sql.exec.spi.JdbcParameterBinder;
import org.hibernate.sql.exec.spi.JdbcParameterBinding;
import org.hibernate.sql.results.internal.ScalarQueryResultImpl;
import org.hibernate.sql.results.internal.SqlSelectionImpl;
import org.hibernate.sql.results.spi.QueryResult;
import org.hibernate.sql.results.spi.QueryResultCreationContext;
import org.hibernate.sql.results.spi.QueryResultProducer;
import org.hibernate.sql.results.spi.SqlSelection;
import org.hibernate.type.descriptor.java.spi.BasicJavaDescriptor;
import org.hibernate.type.spi.TypeConfiguration;

/**
 * @author Steve Ebersole
 */
public abstract class AbstractParameter
		implements GenericParameter, JdbcParameter, JdbcParameterBinder, QueryResultProducer {

	// todo (6.0) : should not extend QueryResultProducer
	//		QueryResultProducer is a domain query level thing, whereas this parameter is part of the SQL AST

	private final SqlExpressableType type;
	private final Clause clause;
	private final TypeConfiguration typeConfiguration;

	public AbstractParameter(
			SqlExpressableType type,
			Clause clause,
			TypeConfiguration typeConfiguration) {
		this.type = type;
		this.clause = clause;
		this.typeConfiguration = typeConfiguration;
	}

	@Override
	public SqlExpressableType getExpressableType() {
		return type;
	}

	@Override
	public JdbcParameterBinder getParameterBinder() {
		return this;
	}

	@Override
	public int bindParameterValue(
			PreparedStatement statement,
			int startPosition,
			ExecutionContext executionContext) throws SQLException {
		final JdbcParameterBinding binding = executionContext.getJdbcParameterBindings().getBinding( AbstractParameter.this );
		if ( binding == null ) {
			throw new ExecutionException( "JDBC parameter value not bound - " + this );
		}

		final SqlExpressableType bindType = binding.getBindType();
		if ( bindType == null ) {
			throw new ExecutionException( "JDBC parameter bind type unresolved - " + this );
		}

		bindType.getJdbcValueBinder().bind(
				statement,
				startPosition,
				binding.getBindValue(),
				executionContext
		);

		return 1;
	}


	// todo (6.0) : both of the methods below are another manifestation of only really allowing basic (single column) valued parameters


	@Override
	public QueryResult createQueryResult(String resultVariable, QueryResultCreationContext creationContext) {
		return new ScalarQueryResultImpl(
				resultVariable,
				creationContext.getSqlSelectionResolver().resolveSqlSelection(
						this,
						type.getJavaTypeDescriptor(),
						creationContext.getSessionFactory().getTypeConfiguration()
				),
				getExpressableType()
		);
	}

	@Override
	public SqlSelection createSqlSelection(
			int jdbcPosition,
			BasicJavaDescriptor javaTypeDescriptor,
			TypeConfiguration typeConfiguration) {
		// todo (6.0) : we should really just access the parameter bind value here rather than reading from ResultSet
		//		should be more performant - double so if we can resolve the bind here
		//		and encode it into the SqlSelectionReader
		//
		//		see `org.hibernate.sql.ast.tree.spi.expression.AbstractLiteral.createSqlSelection`

		return new SqlSelectionImpl(
				jdbcPosition,
				this,
				( (BasicValuedExpressableType) getType() ).getBasicType().getSqlExpressableType( typeConfiguration )
		);
	}
}
