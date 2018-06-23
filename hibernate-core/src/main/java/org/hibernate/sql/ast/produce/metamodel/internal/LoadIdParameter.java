/*
 * Hibernate, Relational Persistence for Idiomatic Java
 *
 * License: GNU Lesser General Public License (LGPL), version 2.1 or later
 * See the lgpl.txt file in the root directory or http://www.gnu.org/licenses/lgpl-2.1.html
 */
package org.hibernate.sql.ast.produce.metamodel.internal;

import org.hibernate.sql.JdbcValueMapper;
import org.hibernate.sql.ast.consume.spi.SqlAstWalker;
import org.hibernate.sql.ast.tree.spi.expression.AbstractParameter;

/**
 * @author Steve Ebersole
 */
public class LoadIdParameter extends AbstractParameter {

	// todo (6.0) (domain-jdbc) : should be moved domain query package (org.hibernate.query.?)

	private final int idValueIndex;

	public LoadIdParameter(JdbcValueMapper mapper) {
		this( 0, mapper );

	}

	public LoadIdParameter(int idValueIndex, JdbcValueMapper mapper) {
		super( mapper );
		this.idValueIndex = idValueIndex;
	}

	@Override
	public void accept(SqlAstWalker sqlTreeWalker) {
		sqlTreeWalker.visitGenericParameter( this );
	}
}
