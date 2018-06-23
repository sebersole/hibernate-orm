/*
 * Hibernate, Relational Persistence for Idiomatic Java
 *
 * License: GNU Lesser General Public License (LGPL), version 2.1 or later.
 * See the lgpl.txt file in the root directory or <http://www.gnu.org/licenses/lgpl-2.1.html>.
 */
package org.hibernate.type.descriptor.sql.spi;

import java.sql.CallableStatement;
import java.sql.Clob;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Types;

import org.hibernate.NotYetImplementedFor6Exception;
import org.hibernate.engine.jdbc.CharacterStream;
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
import org.hibernate.type.spi.TypeConfiguration;

/**
 * Descriptor for {@link Types#CLOB CLOB} handling.
 *
 * @author Steve Ebersole
 * @author Gail Badner
 */
public abstract class ClobSqlDescriptor implements SqlTypeDescriptor {
	@Override
	public int getJdbcTypeCode() {
		return Types.CLOB;
	}

	@Override
	public boolean canBeRemapped() {
		return true;
	}

	@Override
	public <T> JdbcLiteralFormatter<T> getJdbcLiteralFormatter(JavaTypeDescriptor<T> javaTypeDescriptor) {
		// literal values for (N)CLOB data is not supported.
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
				return javaTypeDescriptor.wrap( rs.getClob( sqlSelection.getJdbcResultSetIndex() ), processingState.getSession() );
			}

			@Override
			protected X doExtract(CallableStatement statement, SqlSelection sqlSelection, JdbcValuesSourceProcessingState processingState)
					throws SQLException {
				return javaTypeDescriptor.wrap( statement.getClob( sqlSelection.getJdbcResultSetIndex() ), processingState.getSession() );
			}

			@Override
			protected X doExtract(CallableStatement statement, String name, JdbcValuesSourceProcessingState processingState)
					throws SQLException {
				return javaTypeDescriptor.wrap( statement.getClob( name ), processingState.getSession() );
			}
		};
	}

	protected abstract <X> AbstractJdbcValueBinder<X> getClobBinder(JavaTypeDescriptor<X> javaTypeDescriptor);

	@Override
	public <X> JdbcValueBinder<X> getBinder(BasicJavaDescriptor<X> javaTypeDescriptor) {
		return getClobBinder( javaTypeDescriptor );
	}


	public static final ClobSqlDescriptor DEFAULT = new ClobSqlDescriptor() {
		@Override
		public <T> BasicJavaDescriptor<T> getJdbcRecommendedJavaTypeMapping(TypeConfiguration typeConfiguration) {
			return (BasicJavaDescriptor<T>) typeConfiguration.getJavaTypeDescriptorRegistry().getDescriptor( Clob.class );
		}

		@Override
		public <X> AbstractJdbcValueBinder<X> getClobBinder(final JavaTypeDescriptor<X> javaTypeDescriptor) {
			return new AbstractJdbcValueBinder<X>( javaTypeDescriptor, this ) {
				@Override
				protected void doBind(PreparedStatement st, X value, int index, ExecutionContext executionContext)
						throws SQLException {
					if ( executionContext.getSession().useStreamForLobBinding() ) {
						STREAM_BINDING.getClobBinder( javaTypeDescriptor ).bind(
								st,
								value,
								index,
								executionContext
						);
					}
					else {
						CLOB_BINDING.getClobBinder( javaTypeDescriptor ).bind( st, value, index, executionContext );
					}
				}

				@Override
				protected void doBind(CallableStatement st, X value, String name, ExecutionContext executionContext)
						throws SQLException {
					if ( executionContext.getSession().useStreamForLobBinding() ) {
						STREAM_BINDING.getClobBinder( javaTypeDescriptor ).bind( st, value, name, executionContext );
					}
					else {
						CLOB_BINDING.getClobBinder( javaTypeDescriptor ).bind( st, value, name, executionContext );
					}
				}
			};
		}
	};

	public static final ClobSqlDescriptor CLOB_BINDING = new ClobSqlDescriptor() {
		@Override
		public <T> BasicJavaDescriptor<T> getJdbcRecommendedJavaTypeMapping(TypeConfiguration typeConfiguration) {
			return (BasicJavaDescriptor<T>) typeConfiguration.getJavaTypeDescriptorRegistry().getDescriptor( Clob.class );
		}

		@Override
		public <X> AbstractJdbcValueBinder<X> getClobBinder(final JavaTypeDescriptor<X> javaTypeDescriptor) {
			return new AbstractJdbcValueBinder<X>( javaTypeDescriptor, this ) {
				@Override
				protected void doBind(PreparedStatement st, X value, int index, ExecutionContext executionContext)
						throws SQLException {
					st.setClob( index, javaTypeDescriptor.unwrap( value, Clob.class, executionContext.getSession() ) );
				}

				@Override
				protected void doBind(CallableStatement st, X value, String name, ExecutionContext executionContext)
						throws SQLException {
					st.setClob( name, javaTypeDescriptor.unwrap( value, Clob.class, executionContext.getSession() ) );
				}
			};
		}
	};

	public static final ClobSqlDescriptor STREAM_BINDING = new ClobSqlDescriptor() {
		@Override
		public <T> BasicJavaDescriptor<T> getJdbcRecommendedJavaTypeMapping(TypeConfiguration typeConfiguration) {
			return (BasicJavaDescriptor<T>) typeConfiguration.getJavaTypeDescriptorRegistry().getDescriptor( String.class );
		}

		@Override
		public <X> AbstractJdbcValueBinder<X> getClobBinder(final JavaTypeDescriptor<X> javaTypeDescriptor) {
			return new AbstractJdbcValueBinder<X>( javaTypeDescriptor, this ) {
				@Override
				protected void doBind(PreparedStatement st, X value, int index, ExecutionContext executionContext)
						throws SQLException {
					final CharacterStream characterStream = javaTypeDescriptor.unwrap(
							value,
							CharacterStream.class,
							executionContext.getSession()
					);
					st.setCharacterStream( index, characterStream.asReader(), characterStream.getLength() );
				}

				@Override
				protected void doBind(CallableStatement st, X value, String name, ExecutionContext executionContext)
						throws SQLException {
					final CharacterStream characterStream = javaTypeDescriptor.unwrap(
							value,
							CharacterStream.class,
							executionContext.getSession()
					);
					st.setCharacterStream( name, characterStream.asReader(), characterStream.getLength() );
				}
			};
		}
	};

	public static final ClobSqlDescriptor STREAM_BINDING_EXTRACTING = new ClobSqlDescriptor() {
		@Override
		public <T> BasicJavaDescriptor<T> getJdbcRecommendedJavaTypeMapping(TypeConfiguration typeConfiguration) {
			return (BasicJavaDescriptor<T>) typeConfiguration.getJavaTypeDescriptorRegistry().getDescriptor( String.class );
		}

		@Override
		public <X> AbstractJdbcValueBinder<X> getClobBinder(final JavaTypeDescriptor<X> javaTypeDescriptor) {
			return new AbstractJdbcValueBinder<X>( javaTypeDescriptor, this ) {
				@Override
				protected void doBind(PreparedStatement st, X value, int index, ExecutionContext executionContext)
						throws SQLException {
					final CharacterStream characterStream = javaTypeDescriptor.unwrap(
							value,
							CharacterStream.class,
							executionContext.getSession()
					);
					st.setCharacterStream( index, characterStream.asReader(), characterStream.getLength() );
				}

				@Override
				protected void doBind(CallableStatement st, X value, String name, ExecutionContext executionContext)
						throws SQLException {
					final CharacterStream characterStream = javaTypeDescriptor.unwrap(
							value,
							CharacterStream.class,
							executionContext.getSession()
					);
					st.setCharacterStream( name, characterStream.asReader(), characterStream.getLength() );
				}
			};
		}

		@Override
		public <X> JdbcValueExtractor<X> getExtractor(final BasicJavaDescriptor<X> javaTypeDescriptor) {
			return new AbstractJdbcValueExtractor<X>( javaTypeDescriptor, this ) {
				@Override
				protected X doExtract(ResultSet rs, SqlSelection sqlSelection, JdbcValuesSourceProcessingState processingState) throws SQLException {
					return javaTypeDescriptor.wrap(
							rs.getCharacterStream( sqlSelection.getJdbcResultSetIndex() ),
							processingState.getSession()
					);
				}

				@Override
				protected X doExtract(CallableStatement statement, SqlSelection sqlSelection, JdbcValuesSourceProcessingState processingState)
						throws SQLException {
					return javaTypeDescriptor.wrap(
							statement.getCharacterStream( sqlSelection.getJdbcResultSetIndex() ),
							processingState.getSession()
					);
				}

				@Override
				protected X doExtract(CallableStatement statement, String name, JdbcValuesSourceProcessingState processingState)
						throws SQLException {
					return javaTypeDescriptor.wrap( statement.getCharacterStream( name ), processingState.getSession() );
				}
			};
		}
	};

}
