/*
 * Hibernate, Relational Persistence for Idiomatic Java
 *
 * License: GNU Lesser General Public License (LGPL), version 2.1 or later
 * See the lgpl.txt file in the root directory or http://www.gnu.org/licenses/lgpl-2.1.html
 */
package org.hibernate.type.internal;

import org.hibernate.boot.spi.MetadataBuildingContext;
import org.hibernate.metamodel.model.convert.spi.BasicValueConverter;
import org.hibernate.metamodel.model.domain.spi.BasicValueMapper;
import org.hibernate.sql.SqlExpressableType;
import org.hibernate.type.descriptor.java.MutabilityPlan;
import org.hibernate.type.descriptor.java.spi.BasicJavaDescriptor;
import org.hibernate.type.descriptor.sql.spi.SqlTypeDescriptor;
import org.hibernate.usertype.UserType;

/**
 * @author Steve Ebersole
 */
public class UserTypeMapper implements BasicValueMapper {
	private final BasicValueConverter converter;
	private final MutabilityPlan mutabilityPlan;

	private final BasicJavaDescriptor javaTypeDescriptor;
	private final SqlExpressableType sqlExpressableType;

	public UserTypeMapper(
			UserType userType,
			BasicJavaDescriptor explicitJtd,
			SqlTypeDescriptor explicitStd,
			BasicValueConverter converter,
			MutabilityPlan explicitMutabilityPlan,
			MetadataBuildingContext context) {
		this.converter = converter;

		this.javaTypeDescriptor = determineJavaTypeDescriptor(
				userType,
				explicitJtd,
				context
		);
		this.sqlExpressableType = determineSqlExpressableType(
				userType,
				explicitStd,
				javaTypeDescriptor,
				context
		);

		this.mutabilityPlan = explicitMutabilityPlan != null
				? explicitMutabilityPlan
				: new UserTypeMutabilityPlanAdapter( userType );
	}

	private static BasicJavaDescriptor determineJavaTypeDescriptor(
			UserType userType,
			BasicJavaDescriptor explicitJtd,
			MetadataBuildingContext context) {
		if ( explicitJtd != null ) {
			return explicitJtd;
		}

		final Class<?> domainJavaType = userType.returnedClass();
		return (BasicJavaDescriptor) context.getBootstrapContext()
				.getTypeConfiguration()
				.getJavaTypeDescriptorRegistry()
				.getDescriptor( domainJavaType );
	}

	private static SqlExpressableType determineSqlExpressableType(
			UserType userType,
			SqlTypeDescriptor explicitStd,
			BasicJavaDescriptor jtd,
			MetadataBuildingContext context) {
		SqlTypeDescriptor stdToUse = explicitStd;

		if ( stdToUse == null ) {
			stdToUse = context.getBootstrapContext()
					.getTypeConfiguration()
					.getSqlTypeDescriptorRegistry()
					.getDescriptor( userType.sqlTypeCode() );
		}

		return new UserTypeSqlExpressableTypeAdapter( jtd, stdToUse, userType );

	}

	@Override
	public BasicJavaDescriptor<?> getDomainJtd() {
		return javaTypeDescriptor;
	}

	@Override
	public SqlExpressableType getSqlExpressableType() {
		return sqlExpressableType;
	}

	@Override
	public BasicValueConverter getValueConverter() {
		return converter;
	}

	@Override
	public MutabilityPlan getMutabilityPlan() {
		return mutabilityPlan;
	}
}
