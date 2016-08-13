/*
 * Hibernate, Relational Persistence for Idiomatic Java
 *
 * License: GNU Lesser General Public License (LGPL), version 2.1 or later.
 * See the lgpl.txt file in the root directory or <http://www.gnu.org/licenses/lgpl-2.1.html>.
 */
package org.hibernate.hql.internal.ast.tree;

import org.hibernate.engine.spi.SessionFactoryImplementor;
import org.hibernate.type.StandardBasicTypes;
import org.hibernate.type.mapper.spi.Type;
import org.hibernate.type.spi.JdbcLiteralFormatter;

/**
 * Represents a boolean literal within a query.
 *
 * @author Steve Ebersole
 */
public class BooleanLiteralNode extends LiteralNode implements ExpectedTypeAwareNode {
	private Type expectedType;

	public Type getDataType() {
		return getExpectedType() == null ? StandardBasicTypes.BOOLEAN : getExpectedType();
	}

	public Boolean getValue() {
		return getType() == TRUE ;
	}

	@Override
	public void setText(String s) {
		super.setText( s );
	}

	@Override
	public void setExpectedType(Type expectedType) {
		this.expectedType = expectedType;
	}

	@Override
	public Type getExpectedType() {
		return expectedType;
	}

	@Override
	@SuppressWarnings( {"unchecked"})
	public String getRenderText(SessionFactoryImplementor sessionFactory) {
		final boolean literalValue = getValue();

		final JdbcLiteralFormatter jdbcLiteralFormatter = getDataType().getJdbcLiteralFormatter();
		if ( jdbcLiteralFormatter == null ) {
			return sessionFactory.getJdbcServices().getJdbcEnvironment().getDialect().toBooleanValueString( literalValue );
		}

		return jdbcLiteralFormatter.toJdbcLiteral( literalValue, sessionFactory.getJdbcServices().getJdbcEnvironment().getDialect() );
	}
}
