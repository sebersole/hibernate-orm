/*
 * Hibernate, Relational Persistence for Idiomatic Java
 *
 * License: GNU Lesser General Public License (LGPL), version 2.1 or later.
 * See the lgpl.txt file in the root directory or <http://www.gnu.org/licenses/lgpl-2.1.html>.
 */
package org.hibernate.dialect;

import java.sql.CallableStatement;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Types;
import java.util.UUID;

import org.hibernate.boot.model.TypeContributions;
import org.hibernate.query.sqm.consume.multitable.internal.StandardIdTableSupport;
import org.hibernate.query.sqm.consume.multitable.spi.IdTableStrategy;
import org.hibernate.query.sqm.consume.multitable.spi.idtable.LocalTempTableExporter;
import org.hibernate.query.sqm.consume.multitable.spi.idtable.LocalTemporaryTableStrategy;
import org.hibernate.service.ServiceRegistry;
import org.hibernate.sql.AbstractJdbcValueBinder;
import org.hibernate.sql.AbstractJdbcValueExtractor;
import org.hibernate.sql.JdbcValueBinder;
import org.hibernate.sql.JdbcValueExtractor;
import org.hibernate.sql.exec.spi.ExecutionContext;
import org.hibernate.sql.results.spi.JdbcValuesSourceProcessingState;
import org.hibernate.sql.results.spi.SqlSelection;
import org.hibernate.type.descriptor.java.spi.BasicJavaDescriptor;
import org.hibernate.type.descriptor.java.spi.JavaTypeDescriptor;
import org.hibernate.type.descriptor.sql.spi.JdbcLiteralFormatter;
import org.hibernate.type.descriptor.sql.spi.SqlTypeDescriptor;
import org.hibernate.type.spi.TypeConfiguration;

/**
 * An SQL dialect for Postgres 8.2 and later, adds support for "if exists" when dropping tables
 * 
 * @author edalquist
 */
public class PostgreSQL82Dialect extends PostgreSQL81Dialect {
	@Override
	public boolean supportsIfExistsBeforeTableName() {
		return true;
	}

	@Override
	public void contributeTypes(TypeContributions typeContributions, ServiceRegistry serviceRegistry) {
		super.contributeTypes( typeContributions, serviceRegistry );

		// HHH-9562
		typeContributions.contributeSqlTypeDescriptor( PostgresUUIDType.INSTANCE );
	}

	@Override
	public IdTableStrategy getDefaultIdTableStrategy() {
		return new LocalTemporaryTableStrategy(
				new StandardIdTableSupport(
						new LocalTempTableExporter() {
							@Override
							public String getCreateCommand() {
								return "create temporary  table";
							}
						}
				)
		);
	}

	@Override
	public String getDropSequenceString(String sequenceName) {
		return "drop sequence if exists " + sequenceName;
	}

	@Override
	public boolean supportsValuesList() {
		return true;
	}

	@Override
	public boolean supportsRowValueConstructorSyntaxInInList() {
		return true;
	}




	private static class PostgresUUIDType implements SqlTypeDescriptor {
		/**
		 * Singleton access
		 */
		public static final PostgresUUIDType INSTANCE = new PostgresUUIDType();

		/**
		 * Postgres reports its UUID type as {@link java.sql.Types#OTHER}.  Unfortunately
		 * it reports a lot of its types as {@link java.sql.Types#OTHER}, making that
		 * value useless for distinguishing one SqlTypeDescriptor from another.
		 * So here we define a "magic value" that is a (hopefully no collisions)
		 * unique key within the {@link org.hibernate.type.descriptor.sql.spi.SqlTypeDescriptorRegistry}
		 */
		public static final int JDBC_TYPE_CODE = 3975;

		@Override
		public int getJdbcTypeCode() {
			return JDBC_TYPE_CODE;
		}

		@Override
		public boolean canBeRemapped() {
			return true;
		}

		@Override
		public <J> BasicJavaDescriptor<J> getJdbcRecommendedJavaTypeMapping(TypeConfiguration typeConfiguration) {
			return (BasicJavaDescriptor<J>) typeConfiguration.getJavaTypeDescriptorRegistry().getOrMakeJavaDescriptor( UUID.class );
		}

		@Override
		public <T> JdbcLiteralFormatter<T> getJdbcLiteralFormatter(JavaTypeDescriptor<T> javaTypeDescriptor) {
			return null;
		}

		@Override
		public <X> JdbcValueBinder<X> getBinder(BasicJavaDescriptor<X> javaTypeDescriptor) {
			return new AbstractJdbcValueBinder<X>( javaTypeDescriptor, this ) {
				@Override
				protected void doBind(PreparedStatement st, X value, int index, ExecutionContext executionContext) throws SQLException {
					st.setObject( index, javaTypeDescriptor.unwrap( value, UUID.class, executionContext.getSession() ), Types.OTHER );
				}

				@Override
				protected void doBind(CallableStatement st, X value, String name, ExecutionContext executionContext)
						throws SQLException {
					st.setObject( name, javaTypeDescriptor.unwrap( value, UUID.class, executionContext.getSession() ), Types.OTHER );
				}
			};
		}

		@Override
		public <X> JdbcValueExtractor<X> getExtractor(BasicJavaDescriptor<X> javaTypeDescriptor) {
			return new AbstractJdbcValueExtractor<X>( javaTypeDescriptor, this ) {
				@Override
				protected X doExtract(ResultSet rs, SqlSelection sqlSelection, JdbcValuesSourceProcessingState processingState)
						throws SQLException {
					return javaTypeDescriptor.wrap( rs.getObject( sqlSelection.getJdbcResultSetIndex() ), processingState.getSession() );
				}

				@Override
				protected X doExtract(CallableStatement statement, SqlSelection sqlSelection, JdbcValuesSourceProcessingState processingState)
						throws SQLException {
					return javaTypeDescriptor.wrap( statement.getObject( sqlSelection.getJdbcResultSetIndex() ), processingState.getSession() );
				}

				@Override
				protected X doExtract(
						CallableStatement statement,
						String name,
						JdbcValuesSourceProcessingState processingState) throws SQLException {
					return javaTypeDescriptor.wrap( statement.getObject( name ), processingState.getSession() );
				}
			};
		}
	}
}
