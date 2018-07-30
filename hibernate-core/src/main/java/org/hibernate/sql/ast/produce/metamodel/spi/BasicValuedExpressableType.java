/*
 * Hibernate, Relational Persistence for Idiomatic Java
 *
 * License: GNU Lesser General Public License (LGPL), version 2.1 or later
 * See the lgpl.txt file in the root directory or http://www.gnu.org/licenses/lgpl-2.1.html
 */
package org.hibernate.sql.ast.produce.metamodel.spi;

import java.sql.CallableStatement;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.util.function.Predicate;
import javax.persistence.TemporalType;

import org.hibernate.NotYetImplementedFor6Exception;
import org.hibernate.metamodel.model.domain.spi.AllowableFunctionReturnType;
import org.hibernate.metamodel.model.domain.spi.AllowableParameterType;
import org.hibernate.metamodel.model.domain.spi.StateArrayContributor;
import org.hibernate.sql.JdbcValueMapper;
import org.hibernate.sql.exec.spi.ExecutionContext;
import org.hibernate.type.descriptor.java.spi.BasicJavaDescriptor;
import org.hibernate.type.descriptor.spi.Util;
import org.hibernate.type.descriptor.spi.ValueBinder;
import org.hibernate.type.descriptor.sql.spi.SqlTypeDescriptor;
import org.hibernate.type.spi.BasicType;
import org.hibernate.type.spi.TypeConfiguration;

/**
 * @author Steve Ebersole
 */
public interface BasicValuedExpressableType<J>
		extends ExpressableType<J>, AllowableParameterType<J>, AllowableFunctionReturnType<J> {
	BasicType<J> getBasicType();

	@Override
	default PersistenceType getPersistenceType() {
		return PersistenceType.BASIC;
	}

	@Override
	BasicJavaDescriptor<J> getJavaTypeDescriptor();

	default SqlTypeDescriptor getSqlTypeDescriptor() {
		throw new NotYetImplementedFor6Exception();
	}

	@Override
	default int getNumberOfJdbcParametersNeeded() {
		return 1;
	}

	@Override
	@SuppressWarnings("unchecked")
	default ValueBinder getValueBinder(
			Predicate<StateArrayContributor> inclusionChecker,
			TypeConfiguration typeConfiguration) {
		return new ValueBinder() {
			@Override
			public int getNumberOfJdbcParametersNeeded() {
				return 1;
			}

			@Override
			public void bind(PreparedStatement st, int position, Object value, ExecutionContext executionContext) throws SQLException {
				final JdbcValueMapper jdbcValueMapper = getSqlTypeDescriptor().getJdbcValueMapper(
						getJavaTypeDescriptor(),
						executionContext.getSession().getFactory().getTypeConfiguration()
				);
				jdbcValueMapper.getJdbcValueBinder().bind( st, position, value, executionContext );
			}

			@Override
			public void bind(
					PreparedStatement st,
					String name,
					Object value,
					ExecutionContext executionContext) throws SQLException {
				final CallableStatement callable = Util.asCallableStatementForNamedParam( st );
				final JdbcValueMapper jdbcValueMapper = getSqlTypeDescriptor().getJdbcValueMapper(
						getJavaTypeDescriptor(),
						executionContext.getSession().getFactory().getTypeConfiguration()
				);
				jdbcValueMapper.getJdbcValueBinder().bind( callable, name, value, executionContext );
			}
		};
	}

	default AllowableParameterType resolveTemporalPrecision(
			TemporalType temporalType,
			TypeConfiguration typeConfiguration) {
		return getBasicType().resolveTemporalPrecision( temporalType, typeConfiguration );
	}

	// todo (6.0) : moved this down to BasicValuedNavigable#getSqlTypeDescriptor
	//		uncomment if we find this is needed as part of being queryable
	//SqlTypeDescriptor getSqlTypeDescriptor();
}
