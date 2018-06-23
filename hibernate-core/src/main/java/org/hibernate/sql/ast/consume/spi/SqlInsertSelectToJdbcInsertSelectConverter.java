/*
 * Hibernate, Relational Persistence for Idiomatic Java
 *
 * License: GNU Lesser General Public License (LGPL), version 2.1 or later
 * See the lgpl.txt file in the root directory or http://www.gnu.org/licenses/lgpl-2.1.html
 */
package org.hibernate.sql.ast.consume.spi;

import java.util.List;
import java.util.Set;

import org.hibernate.sql.ast.consume.SyntaxException;
import org.hibernate.sql.ast.tree.spi.InsertSelectStatement;
import org.hibernate.sql.ast.tree.spi.assign.Assignment;
import org.hibernate.sql.ast.tree.spi.expression.ParameterSpec;
import org.hibernate.sql.exec.spi.ExecutionContext;
import org.hibernate.sql.exec.spi.JdbcInsertSelect;

/**
 * @author Steve Ebersole
 */
public class SqlInsertSelectToJdbcInsertSelectConverter
		extends AbstractSqlAstToJdbcOperationConverter
		implements SqlMutationToJdbcMutationConverter {

	/**
	 * Perform interpretation of a select query, returning the SqlSelectInterpretation
	 *
	 * @return The interpretation result
	 */
	public static JdbcInsertSelect interpret(
			InsertSelectStatement sqlAst,
			ExecutionContext executionContext) {
		final SqlInsertSelectToJdbcInsertSelectConverter walker = new SqlInsertSelectToJdbcInsertSelectConverter( executionContext );
		walker.visitInsertSelectStatement( sqlAst );
		return new JdbcInsertSelect() {
			@Override
			public String getSql() {
				return walker.getSql();
			}

			@Override
			public List<ParameterSpec> getJdbcParameters() {
				return walker.getParameterSpecs();
			}

			@Override
			public Set<String> getAffectedTableNames() {
				return walker.getAffectedTableNames();
			}
		};
	}


	private SqlInsertSelectToJdbcInsertSelectConverter(ExecutionContext executionContext) {
		super( executionContext.getSessionFactory() );
	}

	private void visitInsertSelectStatement(InsertSelectStatement sqlAst) {
		appendSql( "insert into " );
		sqlAst.getTargetTable().accept( this );

		// todo (6.0) : for now we do not provide an explicit target columns (VALUES) list - we should...

		visitQuerySpec( sqlAst.getSourceSelectStatement() );
	}

	@Override
	public void visitAssignment(Assignment assignment) {
		throw new SyntaxException( "Encountered assignment clause as part of INSERT-SELECT statement" );
	}
}
