/*
 * Hibernate, Relational Persistence for Idiomatic Java
 *
 * License: GNU Lesser General Public License (LGPL), version 2.1 or later.
 * See the lgpl.txt file in the root directory or <http://www.gnu.org/licenses/lgpl-2.1.html>.
 */
package org.hibernate.type.descriptor.spi.java;

import java.util.Comparator;
import java.util.Locale;
import java.util.StringTokenizer;
import java.util.function.Consumer;

import org.hibernate.dialect.Dialect;
import org.hibernate.internal.util.StringHelper;
import org.hibernate.type.descriptor.internal.java.ComparatorLocaleImpl;
import org.hibernate.type.descriptor.spi.JdbcRecommendedSqlTypeMappingContext;
import org.hibernate.type.descriptor.spi.WrapperOptions;
import org.hibernate.type.descriptor.spi.sql.SqlTypeDescriptor;
import org.hibernate.type.mapper.spi.basic.StringType;
import org.hibernate.type.spi.JdbcLiteralFormatter;

/**
 * Descriptor for {@link Locale} handling.
 * 
 * @author Steve Ebersole
 */
public class LocaleTypeDescriptor extends AbstractTypeDescriptorBasicImpl<Locale>
		implements JdbcLiteralFormatter<Locale> {
	public static final LocaleTypeDescriptor INSTANCE = new LocaleTypeDescriptor();

	public LocaleTypeDescriptor() {
		super( Locale.class );
	}

	@Override
	public SqlTypeDescriptor getJdbcRecommendedSqlType(JdbcRecommendedSqlTypeMappingContext context) {
		return StringTypeDescriptor.INSTANCE.getJdbcRecommendedSqlType( context );
	}

	@Override
	public Comparator<Locale> getComparator() {
		return ComparatorLocaleImpl.INSTANCE;
	}

	@Override
	public JdbcLiteralFormatter<Locale> getJdbcLiteralFormatter() {
		return this;
	}

	@Override
	public String toJdbcLiteral(Locale value, Dialect dialect) {
		return StringTypeDescriptor.INSTANCE.toJdbcLiteral( toString( value ), dialect );
	}

	public String toString(Locale value) {
		final StringBuilder buffer = new StringBuilder();

		boolean hasLanguage = StringHelper.isNotEmpty( value.getLanguage() );
		boolean hasCountry = StringHelper.isNotEmpty( value.getCountry() );
		boolean hasVariant = StringHelper.isNotEmpty( value.getVariant() );

		if ( hasLanguage ) {
			buffer.append( value.getLanguage() );
		}

		if ( hasCountry || hasVariant ) {
			buffer.append( '_' );
		}

		if ( hasCountry ) {
			buffer.append( value.getCountry() );
		}

		if ( hasVariant ) {
			if ( hasLanguage || hasCountry ) {
				buffer.append( '_' );
			}
			buffer.append( value.getVariant() );
		}

		return buffer.toString();
	}

	public Locale fromString(String string) {
		if ( string == null || string.isEmpty() ) {
			return null;
		}

		final Locale.Builder localeBuilder = new Locale.Builder();

		final StringTokenizer tokenizer = new StringTokenizer( string, "_", true );
		Consumer<String> method = localeBuilder::setLanguage;
		int separatorCount = 0;
		while ( tokenizer.hasMoreTokens() ) {
			final String token = tokenizer.nextToken();
			if ( "_".equals( token ) ) {
				separatorCount++;
				if ( separatorCount == 1 ) {
					method = localeBuilder::setRegion;
				}
				else if ( separatorCount == 2 ) {
					method = localeBuilder::setVariant;
				}
				else {
					break;
				}
			}
			else {
				method.accept( token );
			}
		}

		return localeBuilder.build();
	}

	@SuppressWarnings({ "unchecked" })
	public <X> X unwrap(Locale value, Class<X> type, WrapperOptions options) {
		if ( value == null ) {
			return null;
		}
		if ( String.class.isAssignableFrom( type ) ) {
			return (X) value.toString();
		}
		throw unknownUnwrap( type );
	}

	public <X> Locale wrap(X value, WrapperOptions options) {
		if ( value == null ) {
			return null;
		}
		if ( String.class.isInstance( value ) ) {
			return fromString( (String) value );
		}
		throw unknownWrap( value.getClass() );
	}
}
