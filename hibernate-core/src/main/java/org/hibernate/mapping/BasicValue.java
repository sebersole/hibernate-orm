/*
 * Hibernate, Relational Persistence for Idiomatic Java
 *
 * License: GNU Lesser General Public License (LGPL), version 2.1 or later
 * See the lgpl.txt file in the root directory or http://www.gnu.org/licenses/lgpl-2.1.html
 */
package org.hibernate.mapping;

import javax.persistence.AttributeConverter;
import javax.persistence.EnumType;
import javax.persistence.TemporalType;

import org.hibernate.HibernateException;
import org.hibernate.MappingException;
import org.hibernate.boot.model.convert.internal.ClassBasedConverterDescriptor;
import org.hibernate.boot.model.convert.spi.ConverterDescriptor;
import org.hibernate.boot.model.domain.BasicValueMapping;
import org.hibernate.boot.model.domain.JavaTypeMapping;
import org.hibernate.boot.model.relational.MappedTable;
import org.hibernate.boot.model.type.spi.BasicTypeResolver;
import org.hibernate.boot.registry.classloading.spi.ClassLoaderService;
import org.hibernate.boot.spi.MetadataBuildingContext;
import org.hibernate.cfg.BasicTypeResolverConvertibleSupport;
import org.hibernate.internal.CoreLogging;
import org.hibernate.internal.CoreMessageLogger;
import org.hibernate.internal.util.ReflectHelper;
import org.hibernate.internal.util.config.ConfigurationHelper;
import org.hibernate.metamodel.model.convert.internal.NamedEnumValueConverter;
import org.hibernate.metamodel.model.convert.internal.OrdinalEnumValueConverter;
import org.hibernate.metamodel.model.convert.spi.BasicValueConverter;
import org.hibernate.metamodel.model.creation.spi.RuntimeModelCreationContext;
import org.hibernate.type.descriptor.java.internal.EnumJavaDescriptor;
import org.hibernate.type.descriptor.java.spi.BasicJavaDescriptor;
import org.hibernate.type.descriptor.java.spi.JavaTypeDescriptor;
import org.hibernate.type.descriptor.java.spi.TemporalJavaDescriptor;
import org.hibernate.type.descriptor.sql.spi.SqlTypeDescriptor;
import org.hibernate.type.spi.BasicType;

/**
 * @author Steve Ebersole
 * @author Andrea Boriero
 */
public class BasicValue extends SimpleValue implements BasicValueMapping {
	private static final CoreMessageLogger log = CoreLogging.messageLogger( BasicValue.class );

	private boolean isNationalized;
	private boolean isLob;
	private EnumType enumType;
	private TemporalType temporalType;
	private boolean mutable;
	private SqlTypeDescriptor sqlType;

	private ConverterDescriptor attributeConverterDescriptor;

	private BasicTypeResolver basicTypeResolver;
	private JavaTypeMapping javaTypeMapping;
	private BasicType basicType;

	public BasicValue(MetadataBuildingContext buildingContext, MappedTable table) {
		super( buildingContext, table );

		this.enumType = buildingContext.getBuildingOptions().getImplicitEnumType();
	}

	@Override
	public JavaTypeMapping getJavaTypeMapping() {
		// todo (6.0) - this seems hackish as a replacement for {@link #getJavaTypeDescriptor()}.
		if ( javaTypeMapping == null ) {
			final BasicType basicType = resolveType();
			javaTypeMapping = new JavaTypeMapping() {
				@Override
				public String getTypeName() {
					return basicType.getJavaType().getTypeName();
				}

				@Override
				public JavaTypeDescriptor resolveJavaTypeDescriptor() {
					return basicType.getJavaTypeDescriptor();
				}
			};
		}

		return javaTypeMapping;
	}

	@Override
	public ConverterDescriptor getAttributeConverterDescriptor() {
		return attributeConverterDescriptor;
	}

	public boolean isNationalized() {
		return isNationalized;
	}

	public boolean isLob() {
		return isLob;
	}

	public void setJpaAttributeConverterDescriptor(ConverterDescriptor attributeConverterDescriptor) {
		this.attributeConverterDescriptor = attributeConverterDescriptor;
	}

	public void setBasicTypeResolver(BasicTypeResolver basicTypeResolver) {
		this.basicTypeResolver = basicTypeResolver;
	}

	public void makeNationalized() {
		this.isNationalized = true;
	}

	public void makeLob() {
		this.isLob = true;
	}

	@Override
	public void addColumn(Column column) {
		if ( getMappedColumns().size() > 0 ) {
			throw new MappingException( "Attempt to add additional MappedColumn to BasicValueMapping " + column.getName() );
		}
		super.addColumn( column );
	}

	@Override
	protected void setTypeDescriptorResolver(Column column) {
		column.setTypeDescriptorResolver( new BasicValueTypeDescriptorResolver( ) );
	}

	@Override
	public Object accept(ValueVisitor visitor) {
		return visitor.accept(this);
	}

	public class BasicValueTypeDescriptorResolver implements TypeDescriptorResolver {
		@Override
		public SqlTypeDescriptor resolveSqlTypeDescriptor() {
			return resolveType().getSqlTypeDescriptor();
		}

		@Override
		public JavaTypeDescriptor resolveJavaTypeDescriptor() {
			return resolveType().getJavaTypeDescriptor();
		}
	}

	@Override
	public void addFormula(Formula formula) {
		if ( getMappedColumns().size() > 0 ) {
			throw new MappingException( "Attempt to add additional MappedColumn to BasicValueMapping" );
		}
		super.addFormula( formula );
	}

	public void setTypeName(String typeName) {
		if ( typeName != null && typeName.startsWith( ConverterDescriptor.TYPE_NAME_PREFIX ) ) {
			final String converterClassName = typeName.substring( ConverterDescriptor.TYPE_NAME_PREFIX.length() );
			final ClassLoaderService cls = getMetadataBuildingContext()
					.getMetadataCollector()
					.getMetadataBuildingOptions()
					.getServiceRegistry()
					.getService( ClassLoaderService.class );
			try {
				final Class<AttributeConverter> converterClass = cls.classForName( converterClassName );
				attributeConverterDescriptor = new ClassBasedConverterDescriptor(
						converterClass,
						false,
						getMetadataBuildingContext().getBootstrapContext().getClassmateContext()
				);
				return;
			}
			catch (Exception e) {
				log.logBadHbmAttributeConverterType( typeName, e.getMessage() );
			}
		}

		super.setTypeName( typeName );
	}

	@Override
	public BasicType resolveType() {
		if ( basicType == null ) {
			basicType = basicTypeResolver.resolveBasicType();
		}
		return basicType;
	}

	@Override
	public boolean isTypeSpecified() {
		// We mandate a BasicTypeResolver, so this is always true.
		return true;
	}

	@Override
	public void setTypeUsingReflection(String className, String propertyName) throws MappingException {
		// todo (6.0) - this check seems silly
		//		Several places call this method and its possible there are situations where the className
		//		is null because we're dealing with non-pojo cases.  In those cases, we cannot use reflection
		//		to determine type, so we don't overwrite the BasicTypeResolver that is already set.
		//
		//		Ideally can we remove this method call and somehow bake this into `#setType` ?
		if ( className != null ) {
			this.basicTypeResolver = new BasicTypeResolverUsingReflection(
					getMetadataBuildingContext(),
					getAttributeConverterDescriptor(),
					className,
					propertyName,
					isLob(),
					isNationalized()
			);
		}
	}

	public static class BasicTypeResolverUsingReflection extends BasicTypeResolverConvertibleSupport {
		private final JavaTypeDescriptor javaTypeDescriptor;
		private final SqlTypeDescriptor sqlTypeDescriptor;
		private final boolean isLob;
		private final boolean isNationalized;

		public BasicTypeResolverUsingReflection(
				MetadataBuildingContext buildingContext,
				ConverterDescriptor converterDefinition,
				String className,
				String propertyName,
				boolean isLob,
				boolean isNationalized) {
			super( buildingContext, converterDefinition );
			this.isLob = isLob;
			this.isNationalized = isNationalized;

			if ( converterDefinition == null ) {
				final Class attributeType = ReflectHelper.reflectedPropertyClass(
						className,
						propertyName,
						buildingContext.getBootstrapContext().getServiceRegistry().getService( ClassLoaderService.class )
				);
				javaTypeDescriptor = buildingContext.getBootstrapContext()
						.getTypeConfiguration()
						.getJavaTypeDescriptorRegistry()
						.getDescriptor( attributeType );
				sqlTypeDescriptor = javaTypeDescriptor
						.getJdbcRecommendedSqlType(
								buildingContext.getBootstrapContext()
										.getTypeConfiguration()
										.getBasicTypeRegistry()
										.getBaseJdbcRecommendedSqlTypeMappingContext()
						);
			}
			else {
				final Class<?> domainJavaType = converterDefinition.getDomainValueResolvedType().getErasedType();
				javaTypeDescriptor = buildingContext.getBootstrapContext()
						.getTypeConfiguration()
						.getJavaTypeDescriptorRegistry()
						.getDescriptor( domainJavaType );

				final Class<?> relationalJavaType = converterDefinition.getRelationalValueResolvedType().getErasedType();
				final JavaTypeDescriptor<?> relationalJavaDescriptor = buildingContext.getBootstrapContext()
						.getTypeConfiguration()
						.getJavaTypeDescriptorRegistry()
						.getDescriptor( relationalJavaType );
				sqlTypeDescriptor = relationalJavaDescriptor.getJdbcRecommendedSqlType(
						buildingContext.getBootstrapContext()
								.getTypeConfiguration()
								.getBasicTypeRegistry()
								.getBaseJdbcRecommendedSqlTypeMappingContext()
				);
			}
		}

		@Override
		public BasicJavaDescriptor getJavaTypeDescriptor() {
			return (BasicJavaDescriptor) javaTypeDescriptor;
		}

		@Override
		public SqlTypeDescriptor getSqlTypeDescriptor() {
			return sqlTypeDescriptor;
		}

		@Override
		public boolean isNationalized() {
			return isNationalized;
		}

		@Override
		public boolean isLob() {
			return isLob;
		}

		@Override
		public int getPreferredSqlTypeCodeForBoolean() {
			return ConfigurationHelper
					.getPreferredSqlTypeCodeForBoolean(
							getBuildingContext().getBootstrapContext().getServiceRegistry()
					);
		}
	}

	@Override
	@SuppressWarnings("unchecked")
	public BasicValueConverter resolveValueConverter(
			RuntimeModelCreationContext creationContext,
			BasicType basicType) {
		if ( getAttributeConverterDescriptor() != null ) {
			return getAttributeConverterDescriptor().createJpaAttributeConverter( creationContext );
		}

//		final JavaTypeDescriptor jtd = javaTypeMapping.resolveJavaTypeDescriptor();
		final JavaTypeDescriptor jtd = basicType.getJavaTypeDescriptor();

		if ( jtd instanceof EnumJavaDescriptor ) {
			switch ( enumType ) {
				case STRING: {
					return new NamedEnumValueConverter( (EnumJavaDescriptor) jtd );
				}
				case ORDINAL: {
					return new OrdinalEnumValueConverter( (EnumJavaDescriptor) jtd );
				}
				default: {
					throw new HibernateException( "Unknown EnumType : " + enumType );
				}
			}
		}

		// todo (6.0) : other conversions?
		// 		- how is temporalType going to be handled?  during resolution of BasicType?

		return null;
	}
}
