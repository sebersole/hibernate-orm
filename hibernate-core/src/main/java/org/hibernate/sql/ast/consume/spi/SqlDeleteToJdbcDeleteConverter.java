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
import org.hibernate.sql.ast.produce.spi.SqlAstDeleteDescriptor;
import org.hibernate.sql.ast.tree.spi.DeleteStatement;
import org.hibernate.sql.exec.spi.JdbcMutation;
import org.hibernate.sql.exec.spi.JdbcParameterBinder;
import org.hibernate.sql.exec.spi.ParameterBindingContext;

/**
 * @author Steve Ebersole
 */
public class SqlDeleteToJdbcDeleteConverter
		extends AbstractSqlAstToJdbcOperationConverter
		implements SqlMutationToJdbcMutationConverter {

	public static JdbcMutation interpret(
			SqlAstDeleteDescriptor sqlAst,
			ParameterBindingContext parameterBindingContext) {
		final SqlDeleteToJdbcDeleteConverter walker = new SqlDeleteToJdbcDeleteConverter( parameterBindingContext );

		walker.processDeleteStatement( sqlAst.getSqlAstStatement() );

		return new JdbcMutation() {
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

	public SqlDeleteToJdbcDeleteConverter(ParameterBindingContext parameterBindingContext) {
		super( parameterBindingContext );
	}

	private void processDeleteStatement(DeleteStatement deleteStatement) {
		appendSql( "delete " );

		final PhysicalTable targetTable = (PhysicalTable) deleteStatement.getTargetTable().getTable();
		final String tableName = getSessionFactory().getJdbcServices()
				.getJdbcEnvironment()
				.getQualifiedObjectNameFormatter()
				.format(
						targetTable.getQualifiedTableName(),
						getSessionFactory().getJdbcServices().getJdbcEnvironment().getDialect()
				);

		appendSql( tableName );

		if ( deleteStatement.getRestriction() != null ) {
			appendSql( " where " );
			deleteStatement.getRestriction().accept( this );
		}
	}

}
