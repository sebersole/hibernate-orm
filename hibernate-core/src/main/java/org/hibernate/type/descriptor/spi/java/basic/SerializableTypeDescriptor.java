/*
 * Hibernate, Relational Persistence for Idiomatic Java
 *
 * License: GNU Lesser General Public License (LGPL), version 2.1 or later.
 * See the lgpl.txt file in the root directory or <http://www.gnu.org/licenses/lgpl-2.1.html>.
 */
package org.hibernate.type.descriptor.spi.java.basic;

import java.io.ByteArrayInputStream;
import java.io.InputStream;
import java.io.Serializable;
import java.sql.Blob;
import java.sql.SQLException;
import java.sql.Types;

import org.hibernate.HibernateException;
import org.hibernate.engine.jdbc.BinaryStream;
import org.hibernate.engine.jdbc.internal.BinaryStreamImpl;
import org.hibernate.internal.util.SerializationHelper;
import org.hibernate.type.descriptor.internal.java.DataHelper;
import org.hibernate.type.descriptor.internal.java.MutabilityPlanSerializableImpl;
import org.hibernate.type.descriptor.spi.JdbcRecommendedSqlTypeMappingContext;
import org.hibernate.type.descriptor.spi.MutabilityPlan;
import org.hibernate.type.descriptor.spi.WrapperOptions;
import org.hibernate.type.descriptor.spi.sql.SqlTypeDescriptor;

/**
 * Descriptor for general {@link Serializable} handling.
 *
 * @author Steve Ebersole
 * @author Brett meyer
 */
public class SerializableTypeDescriptor<T extends Serializable> extends AbstractTypeDescriptorBasicImpl<T> {
	public static final SerializableTypeDescriptor<Serializable> INSTANCE = new SerializableTypeDescriptor<>( Serializable.class );

	@SuppressWarnings({ "unchecked" })
	public SerializableTypeDescriptor(Class<T> type) {
		super(
				type,
				Serializable.class.equals( type )
						? (MutabilityPlan<T>) MutabilityPlanSerializableImpl.INSTANCE
						: new MutabilityPlanSerializableImpl<>( type )
		);
	}

	public String toString(T value) {
		return PrimitiveByteArrayTypeDescriptor.INSTANCE.toString( toBytes( value ) );
	}

	public T fromString(String string) {
		return fromBytes( PrimitiveByteArrayTypeDescriptor.INSTANCE.fromString( string ) );
	}

	@Override
	public boolean areEqual(T one, T another) {
		if ( one == another ) {
			return true;
		}
		if ( one == null || another == null ) {
			return false;
		}
		return one.equals( another )
				|| PrimitiveByteArrayTypeDescriptor.INSTANCE.areEqual( toBytes( one ), toBytes( another ) );
	}

	@Override
	public SqlTypeDescriptor getJdbcRecommendedSqlType(JdbcRecommendedSqlTypeMappingContext context) {
		return context.getTypeConfiguration().getSqlTypeDescriptorRegistry().getDescriptor( Types.VARBINARY );
	}

	@Override
	public int extractHashCode(T value) {
		return PrimitiveByteArrayTypeDescriptor.INSTANCE.extractHashCode( toBytes( value ) );
	}

	@SuppressWarnings({ "unchecked" })
	public <X> X unwrap(T value, Class<X> type, WrapperOptions options) {
		if ( value == null ) {
			return null;
		}
		else if ( type.isInstance( value ) ) {
			return (X) value;
		}
		else if ( byte[].class.isAssignableFrom( type ) ) {
			return (X) toBytes( value );
		}
		else if ( InputStream.class.isAssignableFrom( type ) ) {
			return (X) new ByteArrayInputStream( toBytes( value ) );
		}
		else if ( BinaryStream.class.isAssignableFrom( type ) ) {
			return (X) new BinaryStreamImpl( toBytes( value ) );
		}
		else if ( Blob.class.isAssignableFrom( type )) {
			return (X) options.getLobCreator().createBlob( toBytes(value) );
		}
		
		throw unknownUnwrap( type );
	}

	@SuppressWarnings("unchecked")
	public <X> T wrap(X value, WrapperOptions options) {
		if ( value == null ) {
			return null;
		}
		else if ( byte[].class.isInstance( value ) ) {
			return fromBytes( (byte[]) value );
		}
		else if ( InputStream.class.isInstance( value ) ) {
			return fromBytes( DataHelper.extractBytes( (InputStream) value ) );
		}
		else if ( Blob.class.isInstance( value )) {
			try {
				return fromBytes( DataHelper.extractBytes( ( (Blob) value ).getBinaryStream() ) );
			}
			catch ( SQLException e ) {
				throw new HibernateException( e);
			}
		}
		else if ( getJavaTypeClass().isInstance( value ) ) {
			return (T) value;
		}
		throw unknownWrap( value.getClass() );
	}

	protected byte[] toBytes(T value) {
		return SerializationHelper.serialize( value );
	}

	@SuppressWarnings({ "unchecked" })
	protected T fromBytes(byte[] bytes) {
		return (T) SerializationHelper.deserialize( bytes, getJavaTypeClass().getClassLoader() );
	}
}
