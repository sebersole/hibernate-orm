/*
 * Hibernate, Relational Persistence for Idiomatic Java
 *
 * License: GNU Lesser General Public License (LGPL), version 2.1 or later.
 * See the lgpl.txt file in the root directory or <http://www.gnu.org/licenses/lgpl-2.1.html>.
 */
package org.hibernate.hql.internal.ast.tree;

import java.util.Locale;

import org.hibernate.QueryException;
import org.hibernate.engine.spi.SessionFactoryImplementor;
import org.hibernate.type.descriptor.spi.java.JavaTypeDescriptor;
import org.hibernate.type.mapper.spi.Type;
import org.hibernate.type.spi.JdbcLiteralFormatter;

/**
 * A node representing a static Java constant.
 *
 * @author Steve Ebersole
 */
public class JavaConstantNode extends Node implements ExpectedTypeAwareNode, SessionFactoryAwareNode {
	private SessionFactoryImplementor factory;

	private String constantExpression;
	private Object constantValue;

	private Type expectedType;

//	@Override
//	public void setText(String s) {
//		// for some reason the antlr.CommonAST initialization routines force
//		// this method to get called twice.  The first time with an empty string
//		if ( StringHelper.isNotEmpty( s ) ) {
//			constantExpression = s;
//			constantValue = ReflectHelper.getConstantValue( s, factory.getServiceRegistry().getService( ClassLoaderService.class ) );
//			heuristicType = factory.getTypeResolver().heuristicType( constantValue.getClass().getName() );
//			super.setText( s );
//		}
//	}

	@Override
	public void setExpectedType(Type expectedType) {
		this.expectedType = expectedType;
	}

	@Override
	public Type getExpectedType() {
		return expectedType;
	}

	@Override
	public void setSessionFactory(SessionFactoryImplementor factory) {
		this.factory = factory;
	}

	@Override
	@SuppressWarnings("unchecked")
	public String getRenderText(SessionFactoryImplementor sessionFactory) {
		JdbcLiteralFormatter jdbcLiteralFormatter = null;
		if ( expectedType != null ) {
			jdbcLiteralFormatter = expectedType.getJdbcLiteralFormatter();
		}
		else {
			final JavaTypeDescriptor javaTypeDescriptor = sessionFactory.getMetamodel()
					.getTypeConfiguration()
					.getJavaTypeDescriptorRegistry()
					.getDescriptor( constantValue.getClass() );
			if ( javaTypeDescriptor != null ) {
				jdbcLiteralFormatter = javaTypeDescriptor.getJdbcLiteralFormatter();
			}
		}

		if ( jdbcLiteralFormatter == null ) {
			throw new QueryException(
					String.format(
							Locale.ENGLISH,
							"Could not determine JdbcLiteralFormatter to use for constantExpression : %s",
							constantExpression
					)
			);
		}

		return jdbcLiteralFormatter.toJdbcLiteral( constantValue, sessionFactory.getJdbcServices().getJdbcEnvironment().getDialect() );
	}
}
