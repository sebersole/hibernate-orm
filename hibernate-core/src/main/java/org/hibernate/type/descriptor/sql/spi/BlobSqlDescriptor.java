/*
 * Hibernate, Relational Persistence for Idiomatic Java
 *
 * License: GNU Lesser General Public License (LGPL), version 2.1 or later.
 * See the lgpl.txt file in the root directory or <http://www.gnu.org/licenses/lgpl-2.1.html>.
 */
package org.hibernate.type.descriptor.sql.spi;

import java.sql.Blob;
import java.sql.CallableStatement;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Types;

import org.hibernate.NotYetImplementedFor6Exception;
import org.hibernate.engine.jdbc.BinaryStream;
import org.hibernate.sql.AbstractJdbcValueBinder;
import org.hibernate.sql.AbstractJdbcValueExtractor;
import org.hibernate.sql.JdbcValueExtractor;
import org.hibernate.sql.JdbcValueMapper;
import org.hibernate.sql.exec.spi.ExecutionContext;
import org.hibernate.sql.results.spi.JdbcValuesSourceProcessingState;
import org.hibernate.sql.results.spi.SqlSelection;
import org.hibernate.type.descriptor.java.spi.BasicJavaDescriptor;
import org.hibernate.type.descriptor.java.spi.JavaTypeDescriptor;
import org.hibernate.type.spi.TypeConfiguration;

/**
 * Descriptor for {@link Types#BLOB BLOB} handling.
 *
 * @author Steve Ebersole
 * @author Gail Badner
 * @author Brett Meyer
 */
public abstract class BlobSqlDescriptor implements SqlTypeDescriptor  {

	private BlobSqlDescriptor() {
	}

	@Override
	public int getJdbcTypeCode() {
		return Types.BLOB;
	}

	@Override
	public boolean canBeRemapped() {
		return true;
	}

	@Override
	public <T> JdbcLiteralFormatter<T> getJdbcLiteralFormatter(JavaTypeDescriptor<T> javaTypeDescriptor) {
		// literal values for BLOB data is not supported.
		return null;
	}

	@Override
	public <X> JdbcValueMapper getJdbcValueMapper(BasicJavaDescriptor<X> javaTypeDescriptor) {
		// todo (6.0) : the binding handling for blob, clob and nclob can all be significantly simplified
		throw new NotYetImplementedFor6Exception();
	}

	@Override
	public <X> JdbcValueExtractor<X> getExtractor(final BasicJavaDescriptor<X> javaTypeDescriptor) {
		return new AbstractJdbcValueExtractor<X>( javaTypeDescriptor, this ) {
			@Override
			protected X doExtract(ResultSet rs, SqlSelection sqlSelection, JdbcValuesSourceProcessingState processingState) throws SQLException {
				return javaTypeDescriptor.wrap( rs.getBlob( sqlSelection.getJdbcResultSetIndex() ), processingState.getSession() );
			}

			@Override
			protected X doExtract(CallableStatement statement, SqlSelection sqlSelection, JdbcValuesSourceProcessingState processingState) throws SQLException {
				return javaTypeDescriptor.wrap( statement.getBlob( sqlSelection.getJdbcResultSetIndex() ), processingState.getSession() );
			}

			@Override
			protected X doExtract(CallableStatement statement, String name, JdbcValuesSourceProcessingState processingState)
					throws SQLException {
				return javaTypeDescriptor.wrap( statement.getBlob( name ), processingState.getSession() );
			}
		};
	}

	protected abstract <X> AbstractJdbcValueBinder<X> getBlobBinder(final JavaTypeDescriptor<X> javaTypeDescriptor);

	@Override
	public <X> AbstractJdbcValueBinder<X> getBinder(final BasicJavaDescriptor<X> javaTypeDescriptor) {
		return getBlobBinder( javaTypeDescriptor );
	}

	public static final BlobSqlDescriptor DEFAULT = new BlobSqlDescriptor() {
		@Override
		public <T> BasicJavaDescriptor<T> getJdbcRecommendedJavaTypeMapping(TypeConfiguration typeConfiguration) {
			return (BasicJavaDescriptor<T>) typeConfiguration.getJavaTypeDescriptorRegistry().getDescriptor( Blob.class );
		}

		@Override
		public <X> AbstractJdbcValueBinder<X> getBlobBinder(final JavaTypeDescriptor<X> javaTypeDescriptor) {
			return new AbstractJdbcValueBinder<X>( javaTypeDescriptor, this ) {
				@Override
				protected void doBind(PreparedStatement st, X value, int index, ExecutionContext executionContext)
						throws SQLException {
					BlobSqlDescriptor descriptor = BLOB_BINDING;
					if ( byte[].class.isInstance( value ) ) {
						// performance shortcut for binding BLOB data in byte[] format
						descriptor = PRIMITIVE_ARRAY_BINDING;
					}
					else if ( executionContext.getSession().useStreamForLobBinding() ) {
						descriptor = STREAM_BINDING;
					}
					descriptor.getBlobBinder( javaTypeDescriptor ).bind( st, value, index, executionContext );
				}

				@Override
				protected void doBind(CallableStatement st, X value, String name, ExecutionContext executionContext)
						throws SQLException {
					BlobSqlDescriptor descriptor = BLOB_BINDING;
					if ( byte[].class.isInstance( value ) ) {
						// performance shortcut for binding BLOB data in byte[] format
						descriptor = PRIMITIVE_ARRAY_BINDING;
					}
					else if ( executionContext.getSession().useStreamForLobBinding() ) {
						descriptor = STREAM_BINDING;
					}
					descriptor.getBlobBinder( javaTypeDescriptor ).bind( st, value, name, executionContext );
				}
			};
		}
	};

	public static final BlobSqlDescriptor PRIMITIVE_ARRAY_BINDING = new BlobSqlDescriptor() {
		@Override
		public <T> BasicJavaDescriptor<T> getJdbcRecommendedJavaTypeMapping(TypeConfiguration typeConfiguration) {
			return (BasicJavaDescriptor<T>) typeConfiguration.getJavaTypeDescriptorRegistry().getDescriptor( byte[].class );
		}

		@Override
		public <X> AbstractJdbcValueBinder<X> getBlobBinder(final JavaTypeDescriptor<X> javaTypeDescriptor) {
			return new AbstractJdbcValueBinder<X>( javaTypeDescriptor, this ) {
				@Override
				public void doBind(PreparedStatement st, X value, int index, ExecutionContext executionContext)
						throws SQLException {
					st.setBytes( index, javaTypeDescriptor.unwrap( value, byte[].class, executionContext.getSession() ) );
				}

				@Override
				protected void doBind(CallableStatement st, X value, String name, ExecutionContext executionContext)
						throws SQLException {
					st.setBytes( name, javaTypeDescriptor.unwrap( value, byte[].class, executionContext.getSession() ) );
				}
			};
		}
	};

	public static final BlobSqlDescriptor BLOB_BINDING = new BlobSqlDescriptor() {
		@Override
		public <T> BasicJavaDescriptor<T> getJdbcRecommendedJavaTypeMapping(TypeConfiguration typeConfiguration) {
			return (BasicJavaDescriptor<T>) typeConfiguration.getJavaTypeDescriptorRegistry().getDescriptor( Blob.class );
		}

		@Override
		public <X> AbstractJdbcValueBinder<X> getBlobBinder(final JavaTypeDescriptor<X> javaTypeDescriptor) {
			return new AbstractJdbcValueBinder<X>( javaTypeDescriptor, this ) {
				@Override
				protected void doBind(PreparedStatement st, X value, int index, ExecutionContext executionContext)
						throws SQLException {
					st.setBlob( index, javaTypeDescriptor.unwrap( value, Blob.class, executionContext.getSession() ) );
				}

				@Override
				protected void doBind(CallableStatement st, X value, String name, ExecutionContext executionContext)
						throws SQLException {
					st.setBlob( name, javaTypeDescriptor.unwrap( value, Blob.class, executionContext.getSession() ) );
				}
			};
		}
	};

	public static final BlobSqlDescriptor STREAM_BINDING = new BlobSqlDescriptor() {
		@Override
		public <T> BasicJavaDescriptor<T> getJdbcRecommendedJavaTypeMapping(TypeConfiguration typeConfiguration) {
			return null;
		}

		@Override
		public <X> AbstractJdbcValueBinder<X> getBlobBinder(final JavaTypeDescriptor<X> javaTypeDescriptor) {
			return new AbstractJdbcValueBinder<X>( javaTypeDescriptor, this ) {
				@Override
				protected void doBind(PreparedStatement st, X value, int index, ExecutionContext executionContext)
						throws SQLException {
					final BinaryStream binaryStream = javaTypeDescriptor.unwrap(
							value,
							BinaryStream.class,
							executionContext.getSession()
					);
					st.setBinaryStream( index, binaryStream.getInputStream(), binaryStream.getLength() );
				}

				@Override
				protected void doBind(CallableStatement st, X value, String name, ExecutionContext executionContext)
						throws SQLException {
					final BinaryStream binaryStream = javaTypeDescriptor.unwrap(
							value,
							BinaryStream.class,
							executionContext.getSession()
					);
					st.setBinaryStream( name, binaryStream.getInputStream(), binaryStream.getLength() );
				}
			};
		}
	};
}
