/*
 * Hibernate, Relational Persistence for Idiomatic Java
 *
 * License: GNU Lesser General Public License (LGPL), version 2.1 or later
 * See the lgpl.txt file in the root directory or http://www.gnu.org/licenses/lgpl-2.1.html
 */
package org.hibernate.sql.ast.produce.metamodel.internal;

import org.hibernate.metamodel.model.domain.spi.AllowableParameterType;
import org.hibernate.query.spi.QueryParameterBinding;
import org.hibernate.sql.ast.Clause;
import org.hibernate.sql.ast.consume.spi.SqlAstWalker;
import org.hibernate.sql.ast.tree.spi.expression.AbstractParameter;
import org.hibernate.sql.exec.spi.ParameterBindingContext;
import org.hibernate.type.spi.TypeConfiguration;

/**
 * @author Steve Ebersole
 */
public class LoadIdParameter extends AbstractParameter {
	private final int idValueIndex;

	public LoadIdParameter(AllowableParameterType type, TypeConfiguration typeConfiguration) {
		this( 0, type, typeConfiguration );

	}

	public LoadIdParameter(int idValueIndex, AllowableParameterType restrictedNavigable, TypeConfiguration typeConfiguration) {
		super( restrictedNavigable, Clause.WHERE, typeConfiguration );
		this.idValueIndex = idValueIndex;
	}

	@Override
	public void accept(SqlAstWalker sqlTreeWalker) {
		sqlTreeWalker.visitGenericParameter( this );
	}

	@Override
	@SuppressWarnings("unchecked")
	public QueryParameterBinding resolveBinding(ParameterBindingContext context) {
		return new LoadIdParameterBinding(
				context.getLoadIdentifiers(),
				getType(),
				idValueIndex
		);
	}

	@Override
	protected void warnNoBinding() {
		throw new IllegalStateException(  );
	}

	@Override
	protected void unresolvedType() {
		throw new IllegalStateException(  );
	}

	@Override
	protected void warnNullBindValue() {
//		throw new IllegalStateException(  );
	}
}
