/*
 * Hibernate, Relational Persistence for Idiomatic Java
 *
 * License: GNU Lesser General Public License (LGPL), version 2.1 or later
 * See the lgpl.txt file in the root directory or http://www.gnu.org/licenses/lgpl-2.1.html
 */
package org.hibernate.sql.ast.tree.spi.expression;

import org.hibernate.metamodel.model.domain.spi.AllowableFunctionReturnType;
import org.hibernate.sql.ast.consume.spi.SqlAstWalker;
import org.hibernate.sql.ast.produce.metamodel.spi.BasicValuedExpressableType;
import org.hibernate.sql.ast.tree.spi.TrimSpecification;
import org.hibernate.sql.results.internal.SqlSelectionImpl;
import org.hibernate.sql.results.spi.SqlSelection;
import org.hibernate.type.descriptor.java.spi.BasicJavaDescriptor;
import org.hibernate.type.spi.StandardSpiBasicTypes;
import org.hibernate.type.spi.TypeConfiguration;

/**
 * @author Steve Ebersole
 */
public class TrimFunction extends AbstractStandardFunction {
	private final TrimSpecification specification;
	private final Expression trimCharacter;
	private final Expression source;

	public TrimFunction(
			TrimSpecification specification,
			Expression trimCharacter,
			Expression source) {
		this.specification = specification;
		this.trimCharacter = trimCharacter;
		this.source = source;
	}

	public TrimSpecification getSpecification() {
		return specification;
	}

	public Expression getTrimCharacter() {
		return trimCharacter;
	}

	public Expression getSource() {
		return source;
	}

	@Override
	public void accept(SqlAstWalker sqlTreeWalker) {
		sqlTreeWalker.visitTrimFunction( this );
	}

	@Override
	public AllowableFunctionReturnType getType() {
		return StandardSpiBasicTypes.STRING;
	}

	@Override
	public SqlSelection createSqlSelection(
			int jdbcPosition,
			BasicJavaDescriptor javaTypeDescriptor,
			TypeConfiguration typeConfiguration) {
		return new SqlSelectionImpl(
				jdbcPosition,
				this,
				( (BasicValuedExpressableType) getType() ).getBasicType().getJdbcValueMapper( typeConfiguration )
		);
	}
}
