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
import org.hibernate.type.descriptor.java.MutabilityPlan;
import org.hibernate.type.descriptor.java.spi.BasicJavaDescriptor;
import org.hibernate.type.descriptor.sql.spi.SqlTypeDescriptor;
import org.hibernate.type.spi.TypeConfiguration;

/**
 * @author Steve Ebersole
 */
public class InferredMappingBuilder<J> {
	private final TypeConfiguration typeConfiguration;

	private BasicJavaDescriptor<J> domainJtd;
	private BasicJavaDescriptor<?> relationalJtd;
	private SqlTypeDescriptor relationalStd;
	private BasicValueConverter valueConverter;
	private MutabilityPlan mutabilityPlan;

	public InferredMappingBuilder(
			BasicJavaDescriptor<J> explicitJtd,
			SqlTypeDescriptor explicitStd,
			TypeConfiguration typeConfiguration) {
		this.typeConfiguration = typeConfiguration;

		this.domainJtd = explicitJtd;
		this.relationalStd = explicitStd;
	}

	public BasicValueMapper<J> build() {
		final SqlExpressableType sqlExpressableType = relationalStd.getSqlExpressableType(
				relationalJtd ,
				typeConfiguration
		);

		return new StandardBasicValueMapper<>(
				domainJtd,
				sqlExpressableType,
				valueConverter,
				mutabilityPlan
		);
	}

	public TypeConfiguration getTypeConfiguration() {
		return typeConfiguration;
	}

	public BasicJavaDescriptor<?> getDomainJtd() {
		return domainJtd;
	}

	@SuppressWarnings("unchecked")
	public void setDomainJtd(BasicJavaDescriptor domainJtd) {
		this.domainJtd = domainJtd;
	}

	public BasicJavaDescriptor<?> getRelationalJtd() {
		return relationalJtd;
	}

	public void setRelationalJtd(BasicJavaDescriptor<?> relationalJtd) {
		this.relationalJtd = relationalJtd;
	}

	public SqlTypeDescriptor getRelationalStd() {
		return relationalStd;
	}

	public void setRelationalStd(SqlTypeDescriptor relationalStd) {
		this.relationalStd = relationalStd;
	}

	public BasicValueConverter getValueConverter() {
		return valueConverter;
	}

	public void setValueConverter(BasicValueConverter valueConverter) {
		this.valueConverter = valueConverter;
	}

	public MutabilityPlan getMutabilityPlan() {
		return mutabilityPlan;
	}

	public void setMutabilityPlan(MutabilityPlan mutabilityPlan) {
		this.mutabilityPlan = mutabilityPlan;
	}
}
