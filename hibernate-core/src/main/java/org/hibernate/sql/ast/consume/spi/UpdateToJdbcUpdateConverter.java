/*
 * Hibernate, Relational Persistence for Idiomatic Java
 *
 * License: GNU Lesser General Public License (LGPL), version 2.1 or later
 * See the lgpl.txt file in the root directory or http://www.gnu.org/licenses/lgpl-2.1.html
 */
package org.hibernate.sql.ast.consume.spi;

import java.util.List;
import java.util.Set;

import org.hibernate.metamodel.model.relational.spi.PhysicalTable;
import org.hibernate.sql.ast.tree.spi.UpdateStatement;
import org.hibernate.sql.ast.tree.spi.assign.Assignment;
import org.hibernate.sql.exec.spi.JdbcParameterBinder;
import org.hibernate.sql.exec.spi.JdbcUpdate;
import org.hibernate.sql.exec.spi.ParameterBindingContext;

/**
 * @author Steve Ebersole
 */
public class UpdateToJdbcUpdateConverter
		extends AbstractSqlAstToJdbcOperationConverter
		implements SqlMutationToJdbcMutationConverter {

	// todo (6.0) : do we need to limit rendering the qualification alias for columns?
	//		we could also control this when we build the SQL AST

	public static JdbcUpdate createJdbcUpdate(
			UpdateStatement sqlAst,
			ParameterBindingContext parameterBindingContext) {
		final UpdateToJdbcUpdateConverter walker = new UpdateToJdbcUpdateConverter( parameterBindingContext );
		walker.processUpdateStatement( sqlAst );
		return new JdbcUpdate() {
			@Override
			public String getSql() {
				return walker.getSql();
			}

			@Override
			public List<JdbcParameterBinder> getParameterBinders() {
				return walker.getParameterBinders();
			}

			@Override
			public Set<String> getAffectedTableNames() {
				return walker.getAffectedTableNames();
			}
		};
	}

	public UpdateToJdbcUpdateConverter(ParameterBindingContext parameterBindingContext) {
		super( parameterBindingContext );
	}

	private void processUpdateStatement(UpdateStatement updateStatement) {
		appendSql( "update " );

		final PhysicalTable targetTable = (PhysicalTable) updateStatement.getTargetTable().getTable();
		final String tableName = getSessionFactory().getJdbcServices()
				.getJdbcEnvironment()
				.getQualifiedObjectNameFormatter()
				.format(
						targetTable.getQualifiedTableName(),
						getSessionFactory().getJdbcServices().getJdbcEnvironment().getDialect()
				);

		appendSql( tableName );

		boolean firstPass = true;
		for ( Assignment assignment : updateStatement.getAssignments() ) {
			if ( firstPass ) {
				appendSql( " set " );
				firstPass = false;
			}
			else {
				appendSql( ", " );
			}

			visitAssignment( assignment );
		}

		if ( updateStatement.getRestriction() != null ) {
			appendSql( " where " );
			updateStatement.getRestriction().accept( this );
		}
	}


	@Override
	@SuppressWarnings("unchecked")
	public void visitAssignment(Assignment assignment) {
		assignment.getColumnReference().accept( this );
		appendSql( " = " );
		assignment.getAssignedValue().accept( this );
	}
}
