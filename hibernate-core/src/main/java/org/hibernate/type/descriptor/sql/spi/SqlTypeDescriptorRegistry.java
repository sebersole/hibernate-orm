/*
 * Hibernate, Relational Persistence for Idiomatic Java
 *
 * License: GNU Lesser General Public License (LGPL), version 2.1 or later.
 * See the lgpl.txt file in the root directory or <http://www.gnu.org/licenses/lgpl-2.1.html>.
 */
package org.hibernate.type.descriptor.sql.spi;

import java.io.Serializable;
import java.sql.CallableStatement;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.concurrent.ConcurrentHashMap;

import org.hibernate.sql.AbstractJdbcValueBinder;
import org.hibernate.sql.AbstractJdbcValueExtractor;
import org.hibernate.sql.JdbcValueBinder;
import org.hibernate.sql.JdbcValueExtractor;
import org.hibernate.sql.JdbcValueMapper;
import org.hibernate.sql.exec.spi.ExecutionContext;
import org.hibernate.sql.results.spi.JdbcValuesSourceProcessingState;
import org.hibernate.sql.results.spi.SqlSelection;
import org.hibernate.type.descriptor.java.spi.BasicJavaDescriptor;
import org.hibernate.type.descriptor.java.spi.JavaTypeDescriptor;
import org.hibernate.type.descriptor.sql.internal.SqlTypeDescriptorBaseline;
import org.hibernate.type.spi.TypeConfiguration;

import org.jboss.logging.Logger;

/**
 * Basically a map from JDBC type code (int) -> {@link SqlTypeDescriptor}
 *
 * @author Steve Ebersole
 */
public class SqlTypeDescriptorRegistry implements SqlTypeDescriptorBaseline.BaselineTarget {
	private static final Logger log = Logger.getLogger( SqlTypeDescriptorRegistry.class );

	private final TypeConfiguration typeConfiguration;
	private ConcurrentHashMap<Integer, SqlTypeDescriptor> descriptorMap = new ConcurrentHashMap<>();

	public SqlTypeDescriptorRegistry(TypeConfiguration typeConfiguration) {
		this.typeConfiguration = typeConfiguration;
		SqlTypeDescriptorBaseline.prime( this );
	}

	// ~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~
	// baseline descriptors

	@Override
	public void addDescriptor(SqlTypeDescriptor sqlTypeDescriptor) {
		descriptorMap.put( sqlTypeDescriptor.getJdbcTypeCode(), sqlTypeDescriptor );
	}

	public SqlTypeDescriptor getDescriptor(int jdbcTypeCode) {
		SqlTypeDescriptor descriptor = descriptorMap.get( Integer.valueOf( jdbcTypeCode ) );
		if ( descriptor != null ) {
			return descriptor;
		}

		if ( JdbcTypeNameMapper.isStandardTypeCode( jdbcTypeCode ) ) {
			log.debugf(
					"A standard JDBC type code [%s] was not defined in SqlTypeDescriptorRegistry",
					jdbcTypeCode
			);
		}

		// see if the typecode is part of a known type family...
		JdbcTypeFamilyInformation.Family family = JdbcTypeFamilyInformation.INSTANCE.locateJdbcTypeFamilyByTypeCode( jdbcTypeCode );
		if ( family != null ) {
			for ( int potentialAlternateTypeCode : family.getTypeCodes() ) {
				if ( potentialAlternateTypeCode != jdbcTypeCode ) {
					final SqlTypeDescriptor potentialAlternateDescriptor = descriptorMap.get( Integer.valueOf( potentialAlternateTypeCode ) );
					if ( potentialAlternateDescriptor != null ) {
						// todo : add a SqlTypeDescriptor.canBeAssignedFrom method...
						return potentialAlternateDescriptor;
					}

					if ( JdbcTypeNameMapper.isStandardTypeCode( potentialAlternateTypeCode ) ) {
						log.debugf(
								"A standard JDBC type code [%s] was not defined in SqlTypeDescriptorRegistry",
								potentialAlternateTypeCode
						);
					}
				}
			}
		}

		// finally, create a new descriptor mapping to getObject/setObject for this type code...
		final ObjectSqlTypeDescriptor fallBackDescriptor = new ObjectSqlTypeDescriptor( jdbcTypeCode );
		addDescriptor( fallBackDescriptor );
		return fallBackDescriptor;
	}

	public static class ObjectSqlTypeDescriptor extends AbstractSqlTypeDescriptor {
		private final int jdbcTypeCode;

		public ObjectSqlTypeDescriptor(int jdbcTypeCode) {
			this.jdbcTypeCode = jdbcTypeCode;
		}

		@Override
		public int getJdbcTypeCode() {
			return jdbcTypeCode;
		}

		@Override
		public <T> BasicJavaDescriptor<T> getJdbcRecommendedJavaTypeMapping(TypeConfiguration typeConfiguration) {
			throw new UnsupportedOperationException( "No recommended Java-type mapping known for JDBC type code [" + jdbcTypeCode + "]" );
		}

		@Override
		public <T> JdbcLiteralFormatter<T> getJdbcLiteralFormatter(JavaTypeDescriptor<T> javaTypeDescriptor) {
			// obviously no literal support here :)
			return null;
		}

		@Override
		public boolean canBeRemapped() {
			return true;
		}

		@Override
		public <X> JdbcValueMapper<X> getJdbcValueMapper(BasicJavaDescriptor<X> javaTypeDescriptor) {
			return determineValueMapper(
					javaTypeDescriptor,
					jtd -> {
						final JdbcValueBinder<X> binder = createBinder( javaTypeDescriptor );
						final JdbcValueExtractor<X> extractor = createExtractor( javaTypeDescriptor );

						return new JdbcValueMapperImpl<>( javaTypeDescriptor, this, extractor, binder );
					}
			);
		}

		private <X> JdbcValueBinder<X> createBinder(BasicJavaDescriptor<X> javaTypeDescriptor) {
			if ( Serializable.class.isAssignableFrom( javaTypeDescriptor.getJavaType() ) ) {
				return VarbinarySqlDescriptor.INSTANCE.getBinder( javaTypeDescriptor );
			}

			return new AbstractJdbcValueBinder<X>( javaTypeDescriptor, this ) {
				@Override
				protected void doBind(PreparedStatement st, X value, int index, ExecutionContext executionContext)
						throws SQLException {
					st.setObject( index, value, jdbcTypeCode );
				}

				@Override
				protected void doBind(CallableStatement st, X value, String name, ExecutionContext executionContext)
						throws SQLException {
					st.setObject( name, value, jdbcTypeCode );
				}
			};
		}

		private  <X> JdbcValueExtractor<X> createExtractor(BasicJavaDescriptor<X> javaTypeDescriptor) {
			if ( Serializable.class.isAssignableFrom( javaTypeDescriptor.getJavaType() ) ) {
				return VarbinarySqlDescriptor.INSTANCE.getExtractor( javaTypeDescriptor );
			}

			return new AbstractJdbcValueExtractor<X>( javaTypeDescriptor, this ) {
				@Override
				protected X doExtract(ResultSet rs, SqlSelection sqlSelection, JdbcValuesSourceProcessingState processingState) throws SQLException {
					return (X) rs.getObject( sqlSelection.getJdbcResultSetIndex() );
				}

				@Override
				protected X doExtract(CallableStatement statement, SqlSelection sqlSelection, JdbcValuesSourceProcessingState processingState) throws SQLException {
					return (X) statement.getObject( sqlSelection.getJdbcResultSetIndex() );
				}

				@Override
				protected X doExtract(CallableStatement statement, String name, JdbcValuesSourceProcessingState processingState) throws SQLException {
					return (X) statement.getObject( name );
				}
			};
		}
	}
}
