/*
 * Hibernate, Relational Persistence for Idiomatic Java
 *
 * License: GNU Lesser General Public License (LGPL), version 2.1 or later.
 * See the lgpl.txt file in the root directory or <http://www.gnu.org/licenses/lgpl-2.1.html>.
 */
package org.hibernate.type;

import java.io.Serializable;
import java.math.BigDecimal;
import java.math.BigInteger;
import java.sql.Blob;
import java.sql.Clob;
import java.sql.NClob;
import java.time.Duration;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.OffsetDateTime;
import java.time.OffsetTime;
import java.time.ZonedDateTime;
import java.util.Calendar;
import java.util.Currency;
import java.util.Date;
import java.util.HashSet;
import java.util.Locale;
import java.util.Set;
import java.util.TimeZone;
import java.util.UUID;

import org.hibernate.type.descriptor.spi.sql.SqlTypeDescriptor;
import org.hibernate.type.mapper.spi.basic.BasicType;
import org.hibernate.type.mapper.spi.basic.BigDecimalType;
import org.hibernate.type.mapper.spi.basic.BigIntegerType;
import org.hibernate.type.mapper.spi.basic.BinaryType;
import org.hibernate.type.mapper.spi.basic.BlobType;
import org.hibernate.type.mapper.spi.basic.BooleanType;
import org.hibernate.type.mapper.spi.basic.ByteType;
import org.hibernate.type.mapper.spi.basic.CalendarDateType;
import org.hibernate.type.mapper.spi.basic.CalendarType;
import org.hibernate.type.mapper.spi.basic.CharArrayType;
import org.hibernate.type.mapper.spi.basic.CharacterArrayType;
import org.hibernate.type.mapper.spi.basic.CharacterType;
import org.hibernate.type.mapper.spi.basic.ClassType;
import org.hibernate.type.mapper.spi.basic.ClobType;
import org.hibernate.type.mapper.spi.basic.CurrencyType;
import org.hibernate.type.mapper.spi.basic.DateType;
import org.hibernate.type.mapper.spi.basic.DoubleType;
import org.hibernate.type.mapper.spi.basic.DurationType;
import org.hibernate.type.mapper.spi.basic.FloatType;
import org.hibernate.type.mapper.spi.basic.ImageType;
import org.hibernate.type.mapper.spi.basic.IntegerType;
import org.hibernate.type.mapper.spi.basic.LocalDateTimeType;
import org.hibernate.type.mapper.spi.basic.LocalDateType;
import org.hibernate.type.mapper.spi.basic.LocalTimeType;
import org.hibernate.type.mapper.spi.basic.LocaleType;
import org.hibernate.type.mapper.spi.basic.LongType;
import org.hibernate.type.mapper.spi.basic.MaterializedBlobType;
import org.hibernate.type.mapper.spi.basic.MaterializedClobType;
import org.hibernate.type.mapper.spi.basic.MaterializedNClobType;
import org.hibernate.type.mapper.spi.basic.NClobType;
import org.hibernate.type.mapper.spi.basic.NTextType;
import org.hibernate.type.mapper.spi.basic.NumericBooleanType;
import org.hibernate.type.mapper.spi.basic.OffsetDateTimeType;
import org.hibernate.type.mapper.spi.basic.OffsetTimeType;
import org.hibernate.type.mapper.spi.basic.SerializableType;
import org.hibernate.type.mapper.spi.basic.ShortType;
import org.hibernate.type.mapper.spi.basic.StringType;
import org.hibernate.type.mapper.spi.basic.TextType;
import org.hibernate.type.mapper.spi.basic.TimeType;
import org.hibernate.type.mapper.spi.basic.TimeZoneType;
import org.hibernate.type.mapper.spi.basic.TimestampType;
import org.hibernate.type.mapper.spi.basic.TrueFalseType;
import org.hibernate.type.mapper.spi.basic.UUIDBinaryType;
import org.hibernate.type.mapper.spi.basic.UUIDCharType;
import org.hibernate.type.mapper.spi.basic.UrlType;
import org.hibernate.type.mapper.spi.basic.WrapperBinaryType;
import org.hibernate.type.mapper.spi.basic.YesNoType;
import org.hibernate.type.mapper.spi.basic.ZonedDateTimeType;

/**
 * Centralizes access to the standard set of basic {@link Type types}.
 * <p/>
 * Type mappings can be adjusted per {@link org.hibernate.SessionFactory}.  These adjusted mappings can be accessed
 * from the {@link org.hibernate.TypeHelper} instance obtained via {@link org.hibernate.SessionFactory#getTypeHelper()}
 *
 * @see org.hibernate.TypeHelper
 * @see org.hibernate.SessionFactory#getTypeHelper()
 *
 * @author Gavin King
 * @author Steve Ebersole
 */
@SuppressWarnings( {"UnusedDeclaration"})
public final class StandardBasicTypes {
	private StandardBasicTypes() {
	}

	private static final Set<SqlTypeDescriptor> SQL_TYPE_DESCRIPTORS = new HashSet<SqlTypeDescriptor>();

	// ~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~
	// boolean data

	/**
	 * The standard Hibernate type for mapping {@link Boolean} to JDBC {@link java.sql.Types#BIT BIT}.
	 *
	 * @see BooleanType
	 */
	public static final BasicType<Boolean> BOOLEAN = BooleanType.INSTANCE;

	/**
	 * The standard Hibernate type for mapping {@link Boolean} to JDBC {@link java.sql.Types#INTEGER INTEGER}.
	 *
	 * @see NumericBooleanType
	 */
	public static final BasicType<Boolean> NUMERIC_BOOLEAN = NumericBooleanType.INSTANCE;

	/**
	 * The standard Hibernate type for mapping {@link Boolean} to JDBC {@link java.sql.Types#CHAR CHAR(1)} (using 'T'/'F').
	 *
	 * @see TrueFalseType
	 */
	public static final BasicType<Boolean> TRUE_FALSE = TrueFalseType.INSTANCE;

	/**
	 * The standard Hibernate type for mapping {@link Boolean} to JDBC {@link java.sql.Types#CHAR CHAR(1)} (using 'Y'/'N').
	 *
	 * @see YesNoType
	 */
	public static final BasicType<Boolean> YES_NO = YesNoType.INSTANCE;


	// ~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~
	// byte/binary data

	/**
	 * The standard Hibernate type for mapping {@link Byte} to JDBC {@link java.sql.Types#TINYINT TINYINT}.
	 */
	public static final BasicType<Byte> BYTE = ByteType.INSTANCE;

	/**
	 * The standard Hibernate type for mapping {@code byte[]} to JDBC {@link java.sql.Types#VARBINARY VARBINARY}.
	 *
	 * @see BinaryType
	 */
	public static final BasicType<byte[]> BINARY = BinaryType.INSTANCE;

	/**
	 * The standard Hibernate type for mapping {@link Byte Byte[]} to JDBC {@link java.sql.Types#VARBINARY VARBINARY}.
	 *
	 * @see WrapperBinaryType
	 */
	public static final BasicType<Byte[]> WRAPPER_BINARY = WrapperBinaryType.INSTANCE;

	/**
	 * The standard Hibernate type for mapping {@code byte[]} to JDBC {@link java.sql.Types#LONGVARBINARY LONGVARBINARY}.
	 *
	 * @see ImageType
	 * @see #MATERIALIZED_BLOB
	 */
	public static final BasicType<byte[]> IMAGE = ImageType.INSTANCE;

	/**
	 * The standard Hibernate type for mapping {@link java.sql.Blob} to JDBC {@link java.sql.Types#BLOB BLOB}.
	 *
	 * @see BlobType
	 * @see #MATERIALIZED_BLOB
	 */
	public static final BasicType<Blob> BLOB = BlobType.INSTANCE;

	/**
	 * The standard Hibernate type for mapping {@code byte[]} to JDBC {@link java.sql.Types#BLOB BLOB}.
	 *
	 * @see MaterializedBlobType
	 * @see #MATERIALIZED_BLOB
	 * @see #IMAGE
	 */
	public static final BasicType<byte[]> MATERIALIZED_BLOB = MaterializedBlobType.INSTANCE;


	// ~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~
	// numeric data

	/**
	 * The standard Hibernate type for mapping {@link Short} to JDBC {@link java.sql.Types#SMALLINT SMALLINT}.
	 *
	 * @see ShortType
	 */
	public static final BasicType<Short> SHORT = ShortType.INSTANCE;

	/**
	 * The standard Hibernate type for mapping {@link Integer} to JDBC {@link java.sql.Types#INTEGER INTEGER}.
	 *
	 * @see IntegerType
	 */
	public static final BasicType<Integer> INTEGER = IntegerType.INSTANCE;

	/**
	 * The standard Hibernate type for mapping {@link Long} to JDBC {@link java.sql.Types#BIGINT BIGINT}.
	 *
	 * @see LongType
	 */
	public static final BasicType<Long> LONG = LongType.INSTANCE;

	/**
	 * The standard Hibernate type for mapping {@link Float} to JDBC {@link java.sql.Types#FLOAT FLOAT}.
	 *
	 * @see FloatType
	 */
	public static final BasicType<Float> FLOAT = FloatType.INSTANCE;

	/**
	 * The standard Hibernate type for mapping {@link Double} to JDBC {@link java.sql.Types#DOUBLE DOUBLE}.
	 *
	 * @see DoubleType
	 */
	public static final BasicType<Double> DOUBLE = DoubleType.INSTANCE;

	/**
	 * The standard Hibernate type for mapping {@link java.math.BigInteger} to JDBC {@link java.sql.Types#NUMERIC NUMERIC}.
	 *
	 * @see BigIntegerType
	 */
	public static final BasicType<BigInteger> BIG_INTEGER = BigIntegerType.INSTANCE;

	/**
	 * The standard Hibernate type for mapping {@link java.math.BigDecimal} to JDBC {@link java.sql.Types#NUMERIC NUMERIC}.
	 *
	 * @see BigDecimalType
	 */
	public static final BasicType<BigDecimal> BIG_DECIMAL = BigDecimalType.INSTANCE;


	// ~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~
	// String / character data

	/**
	 * The standard Hibernate type for mapping {@link Character} to JDBC {@link java.sql.Types#CHAR CHAR(1)}.
	 *
	 * @see CharacterType
	 */
	public static final BasicType<Character> CHARACTER = CharacterType.INSTANCE;

	/**
	 * The standard Hibernate type for mapping {@link String} to JDBC {@link java.sql.Types#VARCHAR VARCHAR}.
	 *
	 * @see StringType
	 */
	public static final BasicType<String> STRING = StringType.INSTANCE;

	/**
	 * The standard Hibernate type for mapping {@code char[]} to JDBC {@link java.sql.Types#VARCHAR VARCHAR}.
	 *
	 * @see CharArrayType
	 */
	public static final BasicType<char[]> CHAR_ARRAY = CharArrayType.INSTANCE;

	/**
	 * The standard Hibernate type for mapping {@link Character Character[]} to JDBC
	 * {@link java.sql.Types#VARCHAR VARCHAR}.
	 *
	 * @see CharacterArrayType
	 */
	public static final BasicType<Character[]> CHARACTER_ARRAY = CharacterArrayType.INSTANCE;

	/**
	 * The standard Hibernate type for mapping {@link String} to JDBC {@link java.sql.Types#LONGVARCHAR LONGVARCHAR}.
	 * <p/>
	 * Similar to a {@link #MATERIALIZED_CLOB}
	 *
	 * @see TextType
	 */
	public static final BasicType<String> TEXT = TextType.INSTANCE;

	/**
	 * The standard Hibernate type for mapping {@link String} to JDBC {@link java.sql.Types#LONGNVARCHAR LONGNVARCHAR}.
	 * <p/>
	 * Similar to a {@link #MATERIALIZED_NCLOB}
	 *
	 * @see NTextType
	 */
	public static final BasicType<String> NTEXT = NTextType.INSTANCE;

	/**
	 * The standard Hibernate type for mapping {@link java.sql.Clob} to JDBC {@link java.sql.Types#CLOB CLOB}.
	 *
	 * @see ClobType
	 * @see #MATERIALIZED_CLOB
	 */
	public static final BasicType<Clob> CLOB = ClobType.INSTANCE;

	/**
	 * The standard Hibernate type for mapping {@link java.sql.NClob} to JDBC {@link java.sql.Types#NCLOB NCLOB}.
	 *
	 * @see NClobType
	 * @see #MATERIALIZED_NCLOB
	 */
	public static final BasicType<NClob> NCLOB = NClobType.INSTANCE;

	/**
	 * The standard Hibernate type for mapping {@link String} to JDBC {@link java.sql.Types#CLOB CLOB}.
	 *
	 * @see MaterializedClobType
	 * @see #MATERIALIZED_CLOB
	 * @see #TEXT
	 */
	public static final BasicType<String> MATERIALIZED_CLOB = MaterializedClobType.INSTANCE;

	/**
	 * The standard Hibernate type for mapping {@link String} to JDBC {@link java.sql.Types#NCLOB NCLOB}.
	 *
	 * @see MaterializedNClobType
	 * @see #MATERIALIZED_CLOB
	 * @see #NTEXT
	 */
	public static final BasicType<String> MATERIALIZED_NCLOB = MaterializedNClobType.INSTANCE;


	// ~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~
	// Date / time data

	/**
	 * The standard Hibernate type for mapping {@link java.util.Date} ({@link java.sql.Time}) to JDBC
	 * {@link java.sql.Types#TIME TIME}.
	 *
	 * @see TimeType
	 */
	public static final BasicType<Date> TIME = TimeType.INSTANCE;

	/**
	 * The standard Hibernate type for mapping {@link java.util.Date} ({@link java.sql.Date}) to JDBC
	 * {@link java.sql.Types#DATE DATE}.
	 *
	 * @see TimeType
	 */
	public static final BasicType<Date> DATE = DateType.INSTANCE;

	/**
	 * The standard Hibernate type for mapping {@link java.util.Date} ({@link java.sql.Timestamp}) to JDBC
	 * {@link java.sql.Types#TIMESTAMP TIMESTAMP}.
	 *
	 * @see TimeType
	 */
	public static final BasicType<Date> TIMESTAMP = TimestampType.INSTANCE;

	/**
	 * The standard Hibernate type for mapping {@link java.util.Calendar} to JDBC
	 * {@link java.sql.Types#TIMESTAMP TIMESTAMP}.
	 *
	 * @see CalendarType
	 */
	public static final BasicType<Calendar> CALENDAR = CalendarType.INSTANCE;

	/**
	 * The standard Hibernate type for mapping {@link java.util.Calendar} to JDBC
	 * {@link java.sql.Types#DATE DATE}.
	 *
	 * @see CalendarDateType
	 */
	public static final BasicType<Calendar> CALENDAR_DATE = CalendarDateType.INSTANCE;

	public static final BasicType<Duration> DURATION = DurationType.INSTANCE;
	public static final BasicType<LocalDateTime> LOCAL_DATE_TIME = LocalDateTimeType.INSTANCE;
	public static final BasicType<LocalDate> LOCAL_DATE = LocalDateType.INSTANCE;
	public static final BasicType<LocalTime> LOCAL_TIME = LocalTimeType.INSTANCE;
	public static final BasicType<OffsetDateTime> OFFSET_DATE_TIME = OffsetDateTimeType.INSTANCE;
	public static final BasicType<OffsetTime> OFFSET_TIME = OffsetTimeType.INSTANCE;
	public static final BasicType<ZonedDateTime> ZONED_DATE_TIME = ZonedDateTimeType.INSTANCE;


	// ~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~
	// UUID data

	/**
	 * The standard Hibernate type for mapping {@link java.util.UUID} to JDBC {@link java.sql.Types#BINARY BINARY}.
	 *
	 * @see UUIDBinaryType
	 */
	public static final BasicType<UUID> UUID_BINARY = UUIDBinaryType.INSTANCE;

	/**
	 * The standard Hibernate type for mapping {@link java.util.UUID} to JDBC {@link java.sql.Types#CHAR CHAR}.
	 *
	 * @see UUIDCharType
	 */
	public static final BasicType<UUID> UUID_CHAR = UUIDCharType.INSTANCE;


	// ~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~
	// Misc data

	/**
	 * The standard Hibernate type for mapping {@link Class} to JDBC {@link java.sql.Types#VARCHAR VARCHAR}.
	 *
	 * @see ClassType
	 */
	public static final BasicType<Class> CLASS = ClassType.INSTANCE;

	/**
	 * The standard Hibernate type for mapping {@link java.util.Currency} to JDBC {@link java.sql.Types#VARCHAR VARCHAR}.
	 *
	 * @see CurrencyType
	 */
	public static final BasicType<Currency> CURRENCY = CurrencyType.INSTANCE;

	/**
	 * The standard Hibernate type for mapping {@link java.util.Locale} to JDBC {@link java.sql.Types#VARCHAR VARCHAR}.
	 *
	 * @see LocaleType
	 */
	public static final BasicType<Locale> LOCALE = LocaleType.INSTANCE;

	/**
	 * The standard Hibernate type for mapping {@link java.io.Serializable} to JDBC {@link java.sql.Types#VARBINARY VARBINARY}.
	 * <p/>
	 * See especially the discussion wrt {@link ClassLoader} determination on {@link SerializableType}
	 *
	 * @see SerializableType
	 */
	public static final BasicType<Serializable> SERIALIZABLE = SerializableType.INSTANCE;

	/**
	 * The standard Hibernate type for mapping {@link java.util.TimeZone} to JDBC {@link java.sql.Types#VARCHAR VARCHAR}.
	 *
	 * @see TimeZoneType
	 */
	public static final BasicType<TimeZone> TIMEZONE = TimeZoneType.INSTANCE;

	/**
	 * The standard Hibernate type for mapping {@link java.net.URL} to JDBC {@link java.sql.Types#VARCHAR VARCHAR}.
	 *
	 * @see UrlType
	 */
	public static final BasicType<java.net.URL> URL = UrlType.INSTANCE;


}
