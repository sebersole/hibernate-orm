/*
 * Hibernate, Relational Persistence for Idiomatic Java
 *
 * License: GNU Lesser General Public License (LGPL), version 2.1 or later.
 * See the lgpl.txt file in the root directory or <http://www.gnu.org/licenses/lgpl-2.1.html>.
 */
package org.hibernate.type.mapper.spi.basic;

import java.util.Comparator;
import java.util.HashMap;
import java.util.Map;
import javax.persistence.EnumType;

import org.hibernate.HibernateException;
import org.hibernate.type.converter.spi.AttributeConverterDefinition;
import org.hibernate.type.spi.TypeConfiguration;
import org.hibernate.type.descriptor.spi.JdbcRecommendedSqlTypeMappingContext;
import org.hibernate.type.descriptor.spi.TypeDescriptorRegistryAccess;
import org.hibernate.type.descriptor.spi.java.basic.BigDecimalTypeDescriptor;
import org.hibernate.type.descriptor.spi.java.basic.BigIntegerTypeDescriptor;
import org.hibernate.type.descriptor.spi.java.basic.BlobTypeDescriptor;
import org.hibernate.type.descriptor.spi.java.basic.BooleanTypeDescriptor;
import org.hibernate.type.descriptor.spi.java.basic.ByteArrayTypeDescriptor;
import org.hibernate.type.descriptor.spi.java.basic.ByteTypeDescriptor;
import org.hibernate.type.descriptor.spi.java.basic.CalendarDateTypeDescriptor;
import org.hibernate.type.descriptor.spi.java.basic.CalendarTimeTypeDescriptor;
import org.hibernate.type.descriptor.spi.java.basic.CharacterArrayTypeDescriptor;
import org.hibernate.type.descriptor.spi.java.basic.CharacterTypeDescriptor;
import org.hibernate.type.descriptor.spi.java.basic.ClassTypeDescriptor;
import org.hibernate.type.descriptor.spi.java.basic.ClobTypeDescriptor;
import org.hibernate.type.descriptor.spi.java.basic.CurrencyTypeDescriptor;
import org.hibernate.type.descriptor.spi.java.basic.DoubleTypeDescriptor;
import org.hibernate.type.descriptor.spi.java.basic.DurationJavaDescriptor;
import org.hibernate.type.descriptor.spi.java.basic.FloatTypeDescriptor;
import org.hibernate.type.descriptor.spi.ImmutableMutabilityPlan;
import org.hibernate.type.descriptor.spi.java.basic.InstantJavaDescriptor;
import org.hibernate.type.descriptor.spi.java.basic.IntegerTypeDescriptor;
import org.hibernate.type.descriptor.spi.java.JavaTypeDescriptor;
import org.hibernate.type.descriptor.spi.java.basic.JdbcDateTypeDescriptor;
import org.hibernate.type.descriptor.spi.java.basic.JdbcTimeTypeDescriptor;
import org.hibernate.type.descriptor.spi.java.basic.JdbcTimestampTypeDescriptor;
import org.hibernate.type.descriptor.spi.java.basic.LocalDateJavaDescriptor;
import org.hibernate.type.descriptor.spi.java.basic.LocalDateTimeJavaDescriptor;
import org.hibernate.type.descriptor.spi.java.basic.LocalTimeJavaDescriptor;
import org.hibernate.type.descriptor.spi.java.basic.LocaleTypeDescriptor;
import org.hibernate.type.descriptor.spi.java.basic.LongTypeDescriptor;
import org.hibernate.type.descriptor.spi.MutabilityPlan;
import org.hibernate.type.descriptor.spi.java.basic.NClobTypeDescriptor;
import org.hibernate.type.descriptor.spi.java.basic.OffsetDateTimeJavaDescriptor;
import org.hibernate.type.descriptor.spi.java.basic.OffsetTimeJavaDescriptor;
import org.hibernate.type.descriptor.spi.java.basic.PrimitiveByteArrayTypeDescriptor;
import org.hibernate.type.descriptor.spi.java.basic.PrimitiveCharacterArrayTypeDescriptor;
import org.hibernate.type.descriptor.spi.java.basic.SerializableTypeDescriptor;
import org.hibernate.type.descriptor.spi.java.basic.ShortTypeDescriptor;
import org.hibernate.type.descriptor.spi.java.basic.StringTypeDescriptor;
import org.hibernate.type.descriptor.spi.java.basic.TemporalJavaTypeDescriptor;
import org.hibernate.type.descriptor.spi.java.basic.TimeZoneTypeDescriptor;
import org.hibernate.type.descriptor.spi.java.basic.UUIDTypeDescriptor;
import org.hibernate.type.descriptor.spi.java.basic.UrlTypeDescriptor;
import org.hibernate.type.descriptor.spi.java.basic.ZonedDateTimeJavaDescriptor;
import org.hibernate.type.descriptor.spi.sql.BigIntTypeDescriptor;
import org.hibernate.type.descriptor.spi.sql.BinaryTypeDescriptor;
import org.hibernate.type.descriptor.spi.sql.CharTypeDescriptor;
import org.hibernate.type.descriptor.spi.sql.DateTypeDescriptor;
import org.hibernate.type.descriptor.spi.sql.LongNVarcharTypeDescriptor;
import org.hibernate.type.descriptor.spi.sql.LongVarbinaryTypeDescriptor;
import org.hibernate.type.descriptor.spi.sql.LongVarcharTypeDescriptor;
import org.hibernate.type.descriptor.spi.sql.NCharTypeDescriptor;
import org.hibernate.type.descriptor.spi.sql.NVarcharTypeDescriptor;
import org.hibernate.type.descriptor.spi.sql.NumericTypeDescriptor;
import org.hibernate.type.descriptor.spi.sql.SmallIntTypeDescriptor;
import org.hibernate.type.descriptor.spi.sql.SqlTypeDescriptor;
import org.hibernate.type.descriptor.spi.sql.TimeTypeDescriptor;
import org.hibernate.type.descriptor.spi.sql.TimestampTypeDescriptor;
import org.hibernate.type.descriptor.spi.sql.TinyIntTypeDescriptor;
import org.hibernate.type.descriptor.spi.sql.VarbinaryTypeDescriptor;
import org.hibernate.type.descriptor.spi.sql.VarcharTypeDescriptor;

/**
 * Registry for BasicType instances for lookup.
 *
 * @author Steve Ebersole
 * @author Chris Cranford
 */
public class BasicTypeRegistry {
	private final Map<RegistryKey,BasicType> registrations = new HashMap<>();

	private final TypeConfiguration typeConfiguration;
	private final JdbcRecommendedSqlTypeMappingContext baseJdbcRecommendedSqlTypeMappingContext;

	public BasicTypeRegistry(TypeConfiguration typeConfiguration) {
		this.typeConfiguration = typeConfiguration;
		this.baseJdbcRecommendedSqlTypeMappingContext = new JdbcRecommendedSqlTypeMappingContext() {
			@Override
			public boolean isNationalized() {
				return false;
			}

			@Override
			public boolean isLob() {
				return false;
			}

			@Override
			public EnumType getEnumeratedType() {
				return EnumType.STRING;
			}

			@Override
			public TypeConfiguration getTypeConfiguration() {
				return typeConfiguration;
			}
		};
//		registerBasicTypes();
	}

	public TypeDescriptorRegistryAccess getTypeDescriptorRegistryAccess() {
		return typeConfiguration;
	}

	public JdbcRecommendedSqlTypeMappingContext getBaseJdbcRecommendedSqlTypeMappingContext() {
		return baseJdbcRecommendedSqlTypeMappingContext;
	}

	public <T> BasicType<T> getRegisteredBasicType(RegistryKey registryKey) {
		return registrations.get( registryKey );
	}

	@SuppressWarnings("unchecked")
	public <T> BasicType<T> resolveBasicType(
			BasicTypeParameters<T> parameters,
			JdbcRecommendedSqlTypeMappingContext jdbcTypeResolutionContext) {
		if ( parameters == null ) {
			throw new IllegalArgumentException( "BasicTypeParameters must not be null" );
		}

		// IMPL NOTE : resolving a BasicType follows very different algorithms based on what
		// specific information is available (non-null) from the BasicTypeParameters.  To help
		// facilitate that, we try to break this down into a number of sub-methods for some
		// high-level differences

		if ( parameters.getTemporalPrecision() != null ) {
			return resolveBasicTypeWithTemporalPrecision( parameters, jdbcTypeResolutionContext );
		}

		if ( parameters.getAttributeConverterDefinition() != null ) {
			return resolveConvertedBasicType( parameters, jdbcTypeResolutionContext );
		}


		JavaTypeDescriptor<T> javaTypeDescriptor = parameters.getJavaTypeDescriptor();
		SqlTypeDescriptor sqlTypeDescriptor = parameters.getSqlTypeDescriptor();

		if ( javaTypeDescriptor == null ) {
			if ( sqlTypeDescriptor == null ) {
				throw new IllegalArgumentException( "BasicTypeParameters must define either a JavaTypeDescriptor or a SqlTypeDescriptor (if not providing AttributeConverter)" );
			}
			javaTypeDescriptor = sqlTypeDescriptor.getJdbcRecommendedJavaTypeMapping( jdbcTypeResolutionContext.getTypeConfiguration() );
		}

		if ( sqlTypeDescriptor == null ) {
			sqlTypeDescriptor = javaTypeDescriptor.getJdbcRecommendedSqlType( jdbcTypeResolutionContext );
		}

		final RegistryKey key = RegistryKey.from( javaTypeDescriptor, sqlTypeDescriptor, null );
		BasicType impl = registrations.get( key );
		if ( !isMatch( impl, parameters ) ) {
			MutabilityPlan<T> mutabilityPlan = parameters.getMutabilityPlan();
			if ( mutabilityPlan == null ) {
				mutabilityPlan = javaTypeDescriptor.getMutabilityPlan();
			}

			Comparator<T> comparator = parameters.getComparator();
			if ( comparator == null ) {
				comparator = javaTypeDescriptor.getComparator();
			}

			if ( TemporalJavaTypeDescriptor.class.isInstance( javaTypeDescriptor ) ) {
				impl = new TemporalTypeImpl( (TemporalJavaTypeDescriptor) javaTypeDescriptor, sqlTypeDescriptor, mutabilityPlan, comparator );
			}
			else {
				impl = new BasicTypeImpl( javaTypeDescriptor, sqlTypeDescriptor, mutabilityPlan, comparator );
			}

			registrations.put( key, impl );
		}

		return impl;
	}

	private <T> boolean isMatch(BasicType<T> impl, BasicTypeParameters<T> parameters) {
		if ( impl == null ) {
			return false;
		}

		if ( parameters.getJavaTypeDescriptor() != null ) {
			if ( impl.getJavaTypeDescriptor() != parameters.getJavaTypeDescriptor() ) {
				return false;
			}
		}

		if ( parameters.getSqlTypeDescriptor() != null ) {
			if ( impl.getColumnMapping().getSqlTypeDescriptor() != parameters.getSqlTypeDescriptor() ) {
				return false;
			}
		}

		if ( parameters.getMutabilityPlan() != null ) {
			if ( impl.getMutabilityPlan() != parameters.getMutabilityPlan() ) {
				return false;
			}
		}

		if ( parameters.getComparator() != null ) {
			if ( impl.getComparator() != parameters.getComparator() ) {
				return false;
			}
		}

		return true;
	}

	/**
	 * Builds a BasicType when we have temporal precision (JPA's TemporalType) associated
	 * with the request
	 */
	@SuppressWarnings("unchecked")
	private <T> BasicType<T> resolveBasicTypeWithTemporalPrecision(
			BasicTypeParameters<T> parameters,
			JdbcRecommendedSqlTypeMappingContext jdbcTypeResolutionContext) {
		assert parameters != null;
		assert parameters.getTemporalPrecision() != null;

		final BasicType baseType = resolveBasicType(
				new BasicTypeParameters<T>() {
					@Override
					public JavaTypeDescriptor<T> getJavaTypeDescriptor() {
						return parameters.getJavaTypeDescriptor();
					}

					@Override
					public SqlTypeDescriptor getSqlTypeDescriptor() {
						return parameters.getSqlTypeDescriptor();
					}

					@Override
					public AttributeConverterDefinition getAttributeConverterDefinition() {
						return parameters.getAttributeConverterDefinition();
					}

					@Override
					public MutabilityPlan<T> getMutabilityPlan() {
						return parameters.getMutabilityPlan();
					}

					@Override
					public Comparator<T> getComparator() {
						return parameters.getComparator();
					}

					@Override
					public javax.persistence.TemporalType getTemporalPrecision() {
						return null;
					}
				},
				jdbcTypeResolutionContext
		);

		if ( !TemporalType.class.isInstance( baseType ) ) {
			throw new IllegalArgumentException( "Expecting a TemporalType, but found [" + baseType + "]" );
		}

		return ( (TemporalType<T>) baseType ).resolveTypeForPrecision(
				parameters.getTemporalPrecision(),
				typeConfiguration
		);
	}

	/**
	 * Builds a BasicType when we have an AttributeConverter associated with the request
	 */
	@SuppressWarnings("unchecked")
	private <T> BasicType<T> resolveConvertedBasicType(
			BasicTypeParameters<T> parameters,
			JdbcRecommendedSqlTypeMappingContext jdbcTypeResolutionContext) {
		assert parameters != null;
		assert parameters.getAttributeConverterDefinition() != null;

		final JavaTypeDescriptor converterDefinedDomainTypeDescriptor = parameters.getAttributeConverterDefinition().getDomainType();
		final JavaTypeDescriptor converterDefinedJdbcTypeDescriptor = parameters.getAttributeConverterDefinition().getJdbcType();

		JavaTypeDescriptor javaTypeDescriptor = parameters.getJavaTypeDescriptor();
		if ( javaTypeDescriptor == null ) {
			javaTypeDescriptor = converterDefinedDomainTypeDescriptor;
		}
		else {
			// todo : check that they match?
		}

		SqlTypeDescriptor sqlTypeDescriptor = parameters.getSqlTypeDescriptor();
		if ( sqlTypeDescriptor == null ) {
			sqlTypeDescriptor = converterDefinedJdbcTypeDescriptor.getJdbcRecommendedSqlType( jdbcTypeResolutionContext );
		}

		final RegistryKey key = RegistryKey.from( javaTypeDescriptor, sqlTypeDescriptor, parameters.getAttributeConverterDefinition() );
		final BasicType existing = registrations.get( key );
		if ( isMatch( existing, parameters ) ) {
			return existing;
		}

		MutabilityPlan<T> mutabilityPlan = parameters.getMutabilityPlan();
		if ( mutabilityPlan == null ) {
			mutabilityPlan = javaTypeDescriptor.getMutabilityPlan();
		}

		Comparator<T> comparator = parameters.getComparator();
		if ( comparator == null ) {
			comparator = javaTypeDescriptor.getComparator();
		}

		final BasicType<T> impl;
		if ( TemporalJavaTypeDescriptor.class.isInstance( javaTypeDescriptor ) ) {
			final TemporalJavaTypeDescriptor javaTemporalTypeDescriptor = (TemporalJavaTypeDescriptor) javaTypeDescriptor;
			impl = new TemporalTypeImpl(
					javaTemporalTypeDescriptor,
					sqlTypeDescriptor,
					mutabilityPlan,
					comparator,
					parameters.getAttributeConverterDefinition()
			);
		}
		else {
			impl = new BasicTypeImpl(
					javaTypeDescriptor,
					sqlTypeDescriptor,
					mutabilityPlan,
					comparator,
					parameters.getAttributeConverterDefinition()
			);
		}
		registrations.put( key, impl );
		return impl;
	}

	public void register(BasicType type, RegistryKey registryKey) {
		if ( registryKey == null ) {
			throw new HibernateException( "Cannot register a type with a null registry key." );
		}
		if ( type == null ) {
			throw new HibernateException( "Cannot register a null type." );
		}
		registrations.put( registryKey, type );
	}

	@SuppressWarnings("unchecked")
	private void registerBasicTypes() {
		registerBasicType( BooleanTypeDescriptor.INSTANCE, org.hibernate.type.descriptor.spi.sql.BooleanTypeDescriptor.INSTANCE );
		registerBasicType( IntegerTypeDescriptor.INSTANCE, org.hibernate.type.descriptor.spi.sql.BooleanTypeDescriptor.INSTANCE );
		registerBasicType( new BooleanTypeDescriptor( 'T', 'F' ), CharTypeDescriptor.INSTANCE );
		registerBasicType( BooleanTypeDescriptor.INSTANCE, CharTypeDescriptor.INSTANCE );

		registerBasicType( ByteTypeDescriptor.INSTANCE, TinyIntTypeDescriptor.INSTANCE );
		registerBasicType( CharacterTypeDescriptor.INSTANCE, CharTypeDescriptor.INSTANCE );
		registerBasicType( ShortTypeDescriptor.INSTANCE, SmallIntTypeDescriptor.INSTANCE );
		registerBasicType( IntegerTypeDescriptor.INSTANCE, org.hibernate.type.descriptor.spi.sql.IntegerTypeDescriptor.INSTANCE );
		registerBasicType( LongTypeDescriptor.INSTANCE, BigIntTypeDescriptor.INSTANCE );
		registerBasicType( FloatTypeDescriptor.INSTANCE, org.hibernate.type.descriptor.spi.sql.FloatTypeDescriptor.INSTANCE );
		registerBasicType( DoubleTypeDescriptor.INSTANCE, org.hibernate.type.descriptor.spi.sql.DoubleTypeDescriptor.INSTANCE );
		registerBasicType( BigDecimalTypeDescriptor.INSTANCE, NumericTypeDescriptor.INSTANCE );
		registerBasicType( BigIntegerTypeDescriptor.INSTANCE, NumericTypeDescriptor.INSTANCE );

		registerBasicType( StringTypeDescriptor.INSTANCE, VarcharTypeDescriptor.INSTANCE );
		registerBasicType( StringTypeDescriptor.INSTANCE, NVarcharTypeDescriptor.INSTANCE );
		registerBasicType( CharacterTypeDescriptor.INSTANCE, NCharTypeDescriptor.INSTANCE );
		registerBasicType( UrlTypeDescriptor.INSTANCE, VarcharTypeDescriptor.INSTANCE );

		registerBasicType( DurationJavaDescriptor.INSTANCE, BigIntTypeDescriptor.INSTANCE );

		registerTemporalType( InstantJavaDescriptor.INSTANCE, TimestampTypeDescriptor.INSTANCE );
		registerTemporalType( LocalDateTimeJavaDescriptor.INSTANCE, TimestampTypeDescriptor.INSTANCE );
		registerTemporalType( LocalDateJavaDescriptor.INSTANCE, DateTypeDescriptor.INSTANCE );
		registerTemporalType( LocalTimeJavaDescriptor.INSTANCE, TimeTypeDescriptor.INSTANCE );
		registerTemporalType( OffsetDateTimeJavaDescriptor.INSTANCE, TimestampTypeDescriptor.INSTANCE );
		registerTemporalType( OffsetTimeJavaDescriptor.INSTANCE, TimeTypeDescriptor.INSTANCE );
		registerTemporalType( ZonedDateTimeJavaDescriptor.INSTANCE, TimestampTypeDescriptor.INSTANCE );

		registerTemporalType( JdbcDateTypeDescriptor.INSTANCE, DateTypeDescriptor.INSTANCE );
		registerTemporalType( JdbcTimeTypeDescriptor.INSTANCE, TimeTypeDescriptor.INSTANCE );
		registerTemporalType( JdbcTimestampTypeDescriptor.INSTANCE, TimestampTypeDescriptor.INSTANCE );
		registerTemporalType( CalendarTimeTypeDescriptor.INSTANCE, TimestampTypeDescriptor.INSTANCE );
		registerTemporalType( CalendarDateTypeDescriptor.INSTANCE, DateTypeDescriptor.INSTANCE );

		registerBasicType( LocaleTypeDescriptor.INSTANCE, VarcharTypeDescriptor.INSTANCE );
		registerBasicType( CurrencyTypeDescriptor.INSTANCE, VarcharTypeDescriptor.INSTANCE );
		registerBasicType( TimeZoneTypeDescriptor.INSTANCE, VarcharTypeDescriptor.INSTANCE );
		registerBasicType( ClassTypeDescriptor.INSTANCE, VarcharTypeDescriptor.INSTANCE );
		registerBasicType( UUIDTypeDescriptor.INSTANCE, BinaryTypeDescriptor.INSTANCE );
		registerBasicType( UUIDTypeDescriptor.INSTANCE, VarcharTypeDescriptor.INSTANCE );

		registerBasicType( PrimitiveByteArrayTypeDescriptor.INSTANCE, VarbinaryTypeDescriptor.INSTANCE );
		registerBasicType( ByteArrayTypeDescriptor.INSTANCE, VarbinaryTypeDescriptor.INSTANCE );
		registerBasicType( PrimitiveByteArrayTypeDescriptor.INSTANCE, LongVarbinaryTypeDescriptor.INSTANCE );
		registerBasicType( PrimitiveCharacterArrayTypeDescriptor.INSTANCE, VarcharTypeDescriptor.INSTANCE );
		registerBasicType( CharacterArrayTypeDescriptor.INSTANCE, VarcharTypeDescriptor.INSTANCE );
		registerBasicType( StringTypeDescriptor.INSTANCE, LongVarcharTypeDescriptor.INSTANCE );
		registerBasicType( StringTypeDescriptor.INSTANCE, LongNVarcharTypeDescriptor.INSTANCE );
		registerBasicType( BlobTypeDescriptor.INSTANCE, org.hibernate.type.descriptor.spi.sql.BlobTypeDescriptor.DEFAULT );
		registerBasicType( PrimitiveByteArrayTypeDescriptor.INSTANCE, org.hibernate.type.descriptor.spi.sql.BlobTypeDescriptor.DEFAULT );
		registerBasicType( ClobTypeDescriptor.INSTANCE, org.hibernate.type.descriptor.spi.sql.ClobTypeDescriptor.DEFAULT );
		registerBasicType( NClobTypeDescriptor.INSTANCE, org.hibernate.type.descriptor.spi.sql.NClobTypeDescriptor.DEFAULT );
		registerBasicType( StringTypeDescriptor.INSTANCE, org.hibernate.type.descriptor.spi.sql.ClobTypeDescriptor.DEFAULT );
		registerBasicType( StringTypeDescriptor.INSTANCE, org.hibernate.type.descriptor.spi.sql.NClobTypeDescriptor.DEFAULT );
		registerBasicType( SerializableTypeDescriptor.INSTANCE, VarbinaryTypeDescriptor.INSTANCE );

		// todo : ObjectType
		// composed of these two types.
		// StringType.INSTANCE = ( StringTypeDescriptor.INSTANCE, VarcharTypeDescriptor.INSTANCE )
		// SerializableType.INSTANCE = ( SerializableTypeDescriptor.INSTANCE, VarbinaryTypeDescriptor.INSTANCE )
		// based on AnyType

		// Immutable types
		registerTemporalType( JdbcDateTypeDescriptor.INSTANCE, DateTypeDescriptor.INSTANCE, ImmutableMutabilityPlan.INSTANCE );
		registerTemporalType( JdbcTimeTypeDescriptor.INSTANCE, TimeTypeDescriptor.INSTANCE, ImmutableMutabilityPlan.INSTANCE );
		registerTemporalType( JdbcTimestampTypeDescriptor.INSTANCE, TimestampTypeDescriptor.INSTANCE, ImmutableMutabilityPlan.INSTANCE );
		registerTemporalType( CalendarTimeTypeDescriptor.INSTANCE, TimestampTypeDescriptor.INSTANCE, ImmutableMutabilityPlan.INSTANCE );
		registerTemporalType( CalendarDateTypeDescriptor.INSTANCE, DateTypeDescriptor.INSTANCE, ImmutableMutabilityPlan.INSTANCE );
		registerBasicType( PrimitiveByteArrayTypeDescriptor.INSTANCE, VarbinaryTypeDescriptor.INSTANCE, ImmutableMutabilityPlan.INSTANCE );
		registerBasicType( SerializableTypeDescriptor.INSTANCE, VarbinaryTypeDescriptor.INSTANCE, ImmutableMutabilityPlan.INSTANCE );
	}

	private void registerBasicType(JavaTypeDescriptor javaTypeDescriptor, SqlTypeDescriptor sqlTypeDescriptor) {
		registerBasicType( javaTypeDescriptor, sqlTypeDescriptor, null );
	}

	@SuppressWarnings("unchecked")
	private void registerBasicType(JavaTypeDescriptor javaTypeDescriptor,
			SqlTypeDescriptor sqlTypeDescriptor,
			MutabilityPlan mutabilityPlan) {
		final BasicType type = new BasicTypeImpl(
				javaTypeDescriptor,
				sqlTypeDescriptor,
				mutabilityPlan,
				null
		);
		register( type, RegistryKey.from( javaTypeDescriptor, sqlTypeDescriptor, null ) );
	}

	private void registerTemporalType(TemporalJavaTypeDescriptor temporalTypeDescriptor, SqlTypeDescriptor sqlTypeDescriptor) {
		registerTemporalType( temporalTypeDescriptor, sqlTypeDescriptor, null );
	}

	@SuppressWarnings("unchecked")
	private void registerTemporalType(TemporalJavaTypeDescriptor temporalTypeDescriptor,
			SqlTypeDescriptor sqlTypeDescriptor,
			MutabilityPlan mutabilityPlan) {
		final TemporalType type = new TemporalTypeImpl(
				temporalTypeDescriptor,
				sqlTypeDescriptor,
				mutabilityPlan,
				null
		);
		register( type, RegistryKey.from( temporalTypeDescriptor, sqlTypeDescriptor, null ) );
	}
}
