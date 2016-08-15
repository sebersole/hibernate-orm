/*
 * Hibernate, Relational Persistence for Idiomatic Java
 *
 * License: GNU Lesser General Public License (LGPL), version 2.1 or later.
 * See the lgpl.txt file in the root directory or <http://www.gnu.org/licenses/lgpl-2.1.html>.
 */
package org.hibernate.type.mapper.spi.basic;

import java.sql.Types;
import java.util.Comparator;
import javax.persistence.AttributeConverter;

import org.hibernate.type.descriptor.spi.java.JavaTypeDescriptor;
import org.hibernate.type.descriptor.spi.java.TemporalTypeDescriptor;
import org.hibernate.type.descriptor.spi.sql.SqlTypeDescriptor;
import org.hibernate.type.spi.TypeConfiguration;
import org.hibernate.type.descriptor.spi.MutabilityPlan;

/**
 * @author Steve Ebersole
 */
public abstract class AbstractTemporalTypeImpl<T> extends AbstractBasicTypeImpl<T> implements TemporalType<T> {

	@Override
	@SuppressWarnings("unchecked")
	public <X> TemporalType<X> resolveTypeForPrecision(javax.persistence.TemporalType precision, TypeConfiguration typeConfiguration) {
		if ( precision == getPrecision() ) {
			return (TemporalType<X>) this;
		}

		final TemporalTypeDescriptor<X> treatedJavaTypeDescriptor = getJavaTypeDescriptor().resolveTypeForPrecision(
				precision,
				typeConfiguration
		);
		final SqlTypeDescriptor treatedSqlTypeDescriptor = determineSqlTypeDescriptor(
				precision,
				typeConfiguration
		);

		final AttributeConverterDefinition converterDefinition;

		if ( getAttributeConverterDefinition() == null ) {
			converterDefinition = null;
		}
		else {
			converterDefinition = new AttributeConverterDefinition() {
				@Override
				public AttributeConverter getAttributeConverter() {
					return getAttributeConverterDefinition().getAttributeConverter();
				}

				@Override
				public JavaTypeDescriptor<?> getDomainType() {
					return getJavaTypeDescriptor();
				}

				@Override
				public JavaTypeDescriptor<?> getJdbcType() {
					return getColumnMapping().getSqlTypeDescriptor().getJdbcRecommendedJavaTypeMapping(
							typeConfiguration.getBasicTypeRegistry().getTypeDescriptorRegistryAccess().getTypeConfiguration()
					);
				}
			};
		}

		return (TemporalType<X>) typeConfiguration.getBasicTypeRegistry().resolveBasicType(
				new BasicTypeParameters<X>() {
					@Override
					public JavaTypeDescriptor<X> getJavaTypeDescriptor() {
						return treatedJavaTypeDescriptor;
					}

					@Override
					public SqlTypeDescriptor getSqlTypeDescriptor() {
						return treatedSqlTypeDescriptor;
					}

					@Override
					public AttributeConverterDefinition getAttributeConverterDefinition() {
						return converterDefinition;
					}

					@Override
					public MutabilityPlan<X> getMutabilityPlan() {
						return getJavaTypeDescriptor().getMutabilityPlan();
					}

					@Override
					public Comparator<X> getComparator() {
						return getJavaTypeDescriptor().getComparator();
					}

					@Override
					public javax.persistence.TemporalType getTemporalPrecision() {
						return precision;
					}
				},
				typeConfiguration.getBasicTypeRegistry().getBaseJdbcRecommendedSqlTypeMappingContext()
		);
	}

	private SqlTypeDescriptor determineSqlTypeDescriptor(
			javax.persistence.TemporalType precision,
			TypeConfiguration typeConfiguration) {
		switch ( precision ) {
			case TIMESTAMP: {
				return typeConfiguration.getSqlTypeDescriptorRegistry().getDescriptor( Types.TIMESTAMP );
			}
			case DATE: {
				return typeConfiguration.getSqlTypeDescriptorRegistry().getDescriptor( Types.DATE );
			}
			case TIME: {
				return typeConfiguration.getSqlTypeDescriptorRegistry().getDescriptor( Types.TIME );
			}
			default: {
				throw new IllegalArgumentException( "Unexpected javax.persistence.TemporalType [" + precision + "]; expecting TIMESTAMP, DATE or TIME" );
			}
		}
	}
}
