/*
 * Hibernate, Relational Persistence for Idiomatic Java
 *
 * License: GNU Lesser General Public License (LGPL), version 2.1 or later.
 * See the lgpl.txt file in the root directory or <http://www.gnu.org/licenses/lgpl-2.1.html>.
 */
package org.hibernate.type.spi;

import java.util.Optional;

import org.hibernate.HibernateException;
import org.hibernate.Incubating;
import org.hibernate.internal.util.compare.EqualsHelper;
import org.hibernate.metamodel.model.domain.spi.VersionSupport;
import org.hibernate.sql.JdbcValueMapper;
import org.hibernate.sql.ast.produce.metamodel.spi.BasicValuedExpressableType;
import org.hibernate.sql.ast.tree.spi.expression.Expression;
import org.hibernate.sql.ast.tree.spi.expression.StandardJdbcParameter;
import org.hibernate.type.Type;
import org.hibernate.type.descriptor.java.spi.BasicJavaDescriptor;
import org.hibernate.type.descriptor.sql.spi.SqlTypeDescriptor;

/**
 * Redefines the Type contract in terms of simple/basic value types which is
 * a mapping from a Java type (JavaTypeDescriptor) to a single SQL type
 * (SqlTypeDescriptor).
 *
 * @author Steve Ebersole
 *
 * @since 6.0
 */
@Incubating( since = "6.0" )
public interface BasicType<T>
		extends Type<T>, BasicValuedExpressableType<T>, JdbcValueMapper, javax.persistence.metamodel.BasicType<T> {
	@Override
	BasicJavaDescriptor<T> getJavaTypeDescriptor();

	/**
	 * The descriptor of the SQL type part of this basic-type
	 */
	SqlTypeDescriptor getSqlTypeDescriptor();

	@Override
	default Expression toJdbcParameters() {
		return new StandardJdbcParameter( this );
	}

	@Override
	default PersistenceType getPersistenceType() {
		return PersistenceType.BASIC;
	}

	@Override
	default boolean areEqual(T x, T y) throws HibernateException {
		return EqualsHelper.areEqual( x, y );
	}

	@Override
	default int getNumberOfJdbcParametersToBind() {
		return 1;
	}

	@Override
	default Class<T> getJavaType() {
		return getJavaTypeDescriptor().getJavaType();
	}

	default Optional<VersionSupport<T>> getVersionSupport() {
		return Optional.empty();
	}
}
