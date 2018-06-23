/*
 * Hibernate, Relational Persistence for Idiomatic Java
 *
 * License: GNU Lesser General Public License (LGPL), version 2.1 or later
 * See the lgpl.txt file in the root directory or http://www.gnu.org/licenses/lgpl-2.1.html
 */
package org.hibernate.sql.ast.tree.spi.expression;

import java.util.List;

import org.hibernate.sql.JdbcValueMapper;
import org.hibernate.sql.ast.consume.spi.SqlAstWalker;
import org.hibernate.sql.ast.produce.spi.SqlExpressable;
import org.hibernate.sql.results.spi.SqlSelection;

/**
 * Specialized node for handling (domain) query parameters that resolve
 * to more-than-one JDBC parameter.  E.g.
 *
 * ----
 * 		// DOMAIN:
 * 		Query q = s.createQuery( "... from Person p where p.name = :name" );
 * 		q.setParameter( "name", new Name("John", "Doe") );
 *
 * 		// SQL:
 * 		"... from person p where (p.fname, p.lname) = (?,?)"
 * ----
 *
 * This node represents the `(?,?)` fragment.  The purpose being that it allows
 * direct reference back to the corresponding domain query bind values.
 *
 * @author Steve Ebersole
 */
public class ParameterSpecGroup implements Expression {
	private final List<ParameterSpec<?>> groupedParameters;

	public ParameterSpecGroup(List<ParameterSpec<?>> groupedParameters) {
		this.groupedParameters = groupedParameters;
	}

	public List<ParameterSpec<?>> getGroupedParameters() {
		return groupedParameters;
	}

	@Override
	public void accept(SqlAstWalker sqlTreeWalker) {
		sqlTreeWalker.visitParameterGroup( this );
	}

	@Override
	public JdbcValueMapper getJdbcValueMapper() {
		throw new UnsupportedOperationException();
	}

	@Override
	public SqlSelection createSqlSelection(int jdbcPosition) {
		throw new UnsupportedOperationException();
	}

	@Override
	public SqlExpressable getExpressable() {
		throw new UnsupportedOperationException();
	}
}
