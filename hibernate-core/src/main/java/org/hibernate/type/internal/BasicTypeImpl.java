/*
 * Hibernate, Relational Persistence for Idiomatic Java
 *
 * License: GNU Lesser General Public License (LGPL), version 2.1 or later
 * See the lgpl.txt file in the root directory or http://www.gnu.org/licenses/lgpl-2.1.html
 */
package org.hibernate.type.internal;

import java.util.function.Consumer;

import org.hibernate.engine.spi.SharedSessionContractImplementor;
import org.hibernate.sql.SqlExpressableType;
import org.hibernate.sql.ast.Clause;
import org.hibernate.type.descriptor.java.spi.BasicJavaDescriptor;
import org.hibernate.type.descriptor.sql.spi.SqlTypeDescriptor;
import org.hibernate.type.spi.BasicType;
import org.hibernate.type.spi.TypeConfiguration;

/**
 * @author Steve Ebersole
 */
public class BasicTypeImpl<T> implements BasicType<T> {
	// todo (6.0) : only support BasicType as a user-extension point (along with UserType)
	//		^^ meaning always scoped to a TypeConfiguration
	//
	// todo (6.0) : relatedly (^^), have StandardBasicTypes contain reference tuples:
	//			1) Java type (Class)
	//			2) SQL type code (int)
	//			3) MutabilityPlan
	// 		- used to lazily resolve BasicValueMappers for these StandardBasicTypes reference

	private final BasicJavaDescriptor javaDescriptor;
	private final SqlTypeDescriptor sqlTypeDescriptor;

	/**
	 * todo (6.0) : is this "static reference"-safe?
	 * 		- in other words, will this work with
	 */
	private SqlExpressableType sqlExpressableType;

	@SuppressWarnings("unchecked")
	public BasicTypeImpl(
			BasicJavaDescriptor javaDescriptor,
			SqlTypeDescriptor sqlTypeDescriptor) {
		this.javaDescriptor = javaDescriptor;
		this.sqlTypeDescriptor = sqlTypeDescriptor;
	}

	@Override
	@SuppressWarnings("unchecked")
	public BasicJavaDescriptor<T> getJavaTypeDescriptor() {
		return javaDescriptor;
	}

	@Override
	public SqlTypeDescriptor getSqlTypeDescriptor() {
		return sqlTypeDescriptor;
	}

//	@Override
//	public Optional<VersionSupport<T>> getVersionSupport() {
//		return Optional.ofNullable( versionSupport );
//	}

	@Override
	public SqlExpressableType getSqlExpressableType() {
		return sqlExpressableType;
	}

	private SqlExpressableType resolveJdbcValueMapper(TypeConfiguration typeConfiguration) {
		if ( sqlExpressableType == null ) {
			sqlExpressableType = getSqlTypeDescriptor().getSqlExpressableType( getJavaTypeDescriptor(), typeConfiguration );
		}

		return sqlExpressableType;
	}

	@Override
	public void visitJdbcTypes(
			Consumer<SqlExpressableType> action,
			Clause clause,
			TypeConfiguration typeConfiguration) {
		action.accept( resolveJdbcValueMapper( typeConfiguration ) );
	}

	@Override
	public Object unresolve(Object value, SharedSessionContractImplementor session) {
		return value;
	}
}
