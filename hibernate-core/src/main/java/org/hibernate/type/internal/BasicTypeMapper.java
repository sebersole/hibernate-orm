/*
 * Hibernate, Relational Persistence for Idiomatic Java
 *
 * License: GNU Lesser General Public License (LGPL), version 2.1 or later
 * See the lgpl.txt file in the root directory or http://www.gnu.org/licenses/lgpl-2.1.html
 */
package org.hibernate.type.internal;

import org.hibernate.metamodel.model.convert.spi.BasicValueConverter;
import org.hibernate.metamodel.model.domain.spi.BasicValueMapper;
import org.hibernate.sql.SqlExpressableType;
import org.hibernate.type.BasicType;
import org.hibernate.type.descriptor.java.MutabilityPlan;
import org.hibernate.type.descriptor.java.spi.BasicJavaDescriptor;

/**
 * BasicValueMapper bridging to legacy {@link BasicType}
 *
 * @author Steve Ebersole
 */
@SuppressWarnings("unchecked")
public class BasicTypeMapper<J> implements BasicValueMapper<J> {
	private final BasicType basicType;
	private final BasicValueConverter valueConverter;

	private final BasicJavaDescriptor<J> domainJtd;
	private final SqlExpressableType jdbcType;

	private final MutabilityPlan<J> mutabilityPlan;

	public BasicTypeMapper(
			BasicType basicType,
			BasicValueConverter<J,?> valueConverter,
			MutabilityPlan<J> mutabilityPlan) {
		this.basicType = basicType;
		this.mutabilityPlan = mutabilityPlan;
		this.valueConverter = valueConverter;

		final BasicJavaDescriptor relationalJtd;

		if ( valueConverter == null ) {
			this.domainJtd = basicType.getJavaTypeDescriptor();
			relationalJtd = this.domainJtd;
		}
		else {
			this.domainJtd = valueConverter.getDomainJavaDescriptor();
			relationalJtd = basicType.getJavaTypeDescriptor();
		}

		this.jdbcType = new BasicSqlExpressableTypeAdapter(
				relationalJtd,
				basicType.getSqlTypeDescriptor(),
				basicType
		);

	}

	@Override
	public BasicJavaDescriptor<J> getDomainJtd() {
		return domainJtd;
	}

	@Override
	public SqlExpressableType getSqlExpressableType() {
		return jdbcType;
	}

	@Override
	public BasicValueConverter getValueConverter() {
		return valueConverter;
	}

	@Override
	public MutabilityPlan<J> getMutabilityPlan() {
		return mutabilityPlan;
	}

	@Override
	public String toString() {
		return "BasicTypeMapper(" + basicType + ')';
	}
}
