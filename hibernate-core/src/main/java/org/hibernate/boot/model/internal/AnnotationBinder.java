/*
 * Hibernate, Relational Persistence for Idiomatic Java
 *
 * License: GNU Lesser General Public License (LGPL), version 2.1 or later.
 * See the lgpl.txt file in the root directory or http://www.gnu.org/licenses/lgpl-2.1.html.
 */
package org.hibernate.boot.model.internal;

import java.lang.reflect.ParameterizedType;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

import org.hibernate.AnnotationException;
import org.hibernate.Internal;
import org.hibernate.MappingException;
import org.hibernate.annotations.CollectionTypeRegistration;
import org.hibernate.annotations.CompositeTypeRegistration;
import org.hibernate.annotations.ConverterRegistration;
import org.hibernate.annotations.EmbeddableInstantiatorRegistration;
import org.hibernate.annotations.FetchMode;
import org.hibernate.annotations.FetchProfile;
import org.hibernate.annotations.FetchProfile.FetchOverride;
import org.hibernate.annotations.FetchProfiles;
import org.hibernate.annotations.FilterDef;
import org.hibernate.annotations.GenericGenerator;
import org.hibernate.annotations.Imported;
import org.hibernate.annotations.JavaTypeRegistration;
import org.hibernate.annotations.JdbcTypeRegistration;
import org.hibernate.annotations.ParamDef;
import org.hibernate.annotations.TypeRegistration;
import org.hibernate.boot.internal.GenerationStrategyInterpreter;
import org.hibernate.boot.model.IdentifierGeneratorDefinition;
import org.hibernate.boot.model.convert.spi.ConverterDescriptor;
import org.hibernate.boot.model.convert.spi.RegisteredConversion;
import org.hibernate.boot.models.categorize.spi.GlobalRegistrations;
import org.hibernate.boot.registry.StandardServiceRegistry;
import org.hibernate.boot.registry.classloading.spi.ClassLoaderService;
import org.hibernate.boot.spi.BootstrapContext;
import org.hibernate.boot.spi.InFlightMetadataCollector;
import org.hibernate.boot.spi.MetadataBuildingContext;
import org.hibernate.dialect.Dialect;
import org.hibernate.engine.spi.FilterDefinition;
import org.hibernate.internal.CoreMessageLogger;
import org.hibernate.internal.util.GenericsHelper;
import org.hibernate.internal.util.StringHelper;
import org.hibernate.metamodel.mapping.JdbcMapping;
import org.hibernate.models.spi.AnnotationTarget;
import org.hibernate.models.spi.AnnotationUsage;
import org.hibernate.models.spi.ClassDetails;
import org.hibernate.resource.beans.internal.FallbackBeanInstanceProducer;
import org.hibernate.resource.beans.spi.ManagedBean;
import org.hibernate.resource.beans.spi.ManagedBeanRegistry;
import org.hibernate.type.BasicType;
import org.hibernate.type.CustomType;
import org.hibernate.type.descriptor.converter.internal.JpaAttributeConverterImpl;
import org.hibernate.type.descriptor.java.BasicJavaType;
import org.hibernate.type.descriptor.java.JavaType;
import org.hibernate.type.descriptor.java.spi.JavaTypeRegistry;
import org.hibernate.type.descriptor.jdbc.JdbcType;
import org.hibernate.type.descriptor.jdbc.JdbcTypeIndicators;
import org.hibernate.type.internal.ConvertedBasicTypeImpl;
import org.hibernate.type.spi.TypeConfiguration;
import org.hibernate.usertype.UserType;

import jakarta.persistence.AttributeConverter;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.Inheritance;
import jakarta.persistence.InheritanceType;
import jakarta.persistence.MappedSuperclass;
import jakarta.persistence.NamedNativeQuery;
import jakarta.persistence.NamedQuery;
import jakarta.persistence.NamedStoredProcedureQuery;
import jakarta.persistence.SequenceGenerator;
import jakarta.persistence.SqlResultSetMapping;
import jakarta.persistence.Table;
import jakarta.persistence.TableGenerator;

import static java.util.Collections.emptyMap;
import static org.hibernate.boot.model.internal.AnnotatedClassType.ENTITY;
import static org.hibernate.boot.model.internal.GeneratorBinder.buildGenerators;
import static org.hibernate.boot.model.internal.InheritanceState.getInheritanceStateOfSuperEntity;
import static org.hibernate.boot.model.internal.InheritanceState.getSuperclassInheritanceState;
import static org.hibernate.internal.CoreLogging.messageLogger;
import static org.hibernate.mapping.MetadataSource.ANNOTATIONS;

/**
 * Reads annotations from Java classes and produces the Hibernate configuration-time metamodel,
 * that is, the objects defined in the package {@link org.hibernate.mapping}.
 *
 * @implNote This class is stateless, unlike most of the other "binders".
 *
 * @author Emmanuel Bernard
 * @author Hardy Ferentschik
 */
public final class AnnotationBinder {
	private static final CoreMessageLogger LOG = messageLogger( AnnotationBinder.class );

	private AnnotationBinder() {}

	public static void bindDefaults(MetadataBuildingContext context) {
		// todo (jpa32) - remove this.  left for now for easy debugging
		final Map<?,?> defaults = context.getBootstrapContext().getReflectionManager().getDefaults();
		final GlobalRegistrations globalRegistrations = context.getMetadataCollector().getGlobalRegistrations();

		// id generators ~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~

		globalRegistrations.getSequenceGeneratorRegistrations().forEach( (name, generatorRegistration) -> {
			final IdentifierGeneratorDefinition.Builder definitionBuilder = new IdentifierGeneratorDefinition.Builder();
			GenerationStrategyInterpreter.STRATEGY_INTERPRETER.interpretSequenceGenerator(
					generatorRegistration.configuration(),
					definitionBuilder
			);
			final IdentifierGeneratorDefinition idGenDef = definitionBuilder.build();
			if ( LOG.isTraceEnabled() ) {
				LOG.tracef( "Adding global sequence generator with name: %s", name );
			}
			context.getMetadataCollector().addDefaultIdentifierGenerator( idGenDef );
		} );

		globalRegistrations.getTableGeneratorRegistrations().forEach( (name, generatorRegistration) -> {
			final IdentifierGeneratorDefinition.Builder definitionBuilder = new IdentifierGeneratorDefinition.Builder();
			GenerationStrategyInterpreter.STRATEGY_INTERPRETER.interpretTableGenerator(
					generatorRegistration.configuration(),
					definitionBuilder
			);
			final IdentifierGeneratorDefinition idGenDef = definitionBuilder.build();
			if ( LOG.isTraceEnabled() ) {
				LOG.tracef( "Adding global table generator with name: %s", name );
			}
			context.getMetadataCollector().addDefaultIdentifierGenerator( idGenDef );
		} );


		// result-set-mappings ~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~

		globalRegistrations.getSqlResultSetMappingRegistrations().forEach( (name, mappingRegistration) -> {
			QueryBinder.bindSqlResultSetMapping( mappingRegistration.configuration(), context, true );
		} );


		// queries ~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~

		globalRegistrations.getNamedQueryRegistrations().forEach( (name, queryRegistration) -> {
			QueryBinder.bindQuery( queryRegistration.configuration(), context, true );
		} );

		globalRegistrations.getNamedNativeQueryRegistrations().forEach( (name, queryRegistration) -> {
			QueryBinder.bindNativeQuery( queryRegistration.configuration(), context, true );
		} );

		globalRegistrations.getNamedStoredProcedureQueryRegistrations().forEach( (name, queryRegistration) -> {
			QueryBinder.bindNamedStoredProcedureQuery( queryRegistration.configuration(), context, true );
		} );
	}

	public static void bindPackage(ClassLoaderService cls, String packageName, MetadataBuildingContext context) {
		final Package pack = cls.packageForNameOrNull( packageName );
		if ( pack == null ) {
			return;
		}
		final ClassDetails packageInfoClassDetails = context.getMetadataCollector()
				.getSourceModelBuildingContext()
				.getClassDetailsRegistry()
				.resolveClassDetails( pack.getName() + ".package-info" );

		handleIdGenerators( packageInfoClassDetails, context );

		bindTypeDescriptorRegistrations( packageInfoClassDetails, context );
		bindEmbeddableInstantiatorRegistrations( packageInfoClassDetails, context );
		bindUserTypeRegistrations( packageInfoClassDetails, context );
		bindCompositeUserTypeRegistrations( packageInfoClassDetails, context );
		bindConverterRegistrations( packageInfoClassDetails, context );

		bindGenericGenerators( packageInfoClassDetails, context );
		bindQueries( packageInfoClassDetails, context );
		bindFilterDefs( packageInfoClassDetails, context );
	}

	private static void handleIdGenerators(ClassDetails packageInfoClassDetails, MetadataBuildingContext context) {
		packageInfoClassDetails.forEachAnnotationUsage( SequenceGenerator.class, (usage) -> {
			IdentifierGeneratorDefinition idGen = GeneratorBinder.buildIdGenerator( usage, context );
			context.getMetadataCollector().addIdentifierGenerator( idGen );
			if ( LOG.isTraceEnabled() ) {
				LOG.tracev( "Add sequence generator with name: {0}", idGen.getName() );
			}
		} );

		packageInfoClassDetails.forEachAnnotationUsage( TableGenerator.class, (usage) -> {
			IdentifierGeneratorDefinition idGen = GeneratorBinder.buildIdGenerator( usage, context );
			context.getMetadataCollector().addIdentifierGenerator( idGen );
			if ( LOG.isTraceEnabled() ) {
				LOG.tracev( "Add table generator with name: {0}", idGen.getName() );
			}
		} );
	}

	private static void bindGenericGenerators(AnnotationTarget annotatedElement, MetadataBuildingContext context) {
		annotatedElement.forEachAnnotationUsage( GenericGenerator.class, (usage) -> {
			bindGenericGenerator( usage, context );
		} );
	}

	private static void bindGenericGenerator(AnnotationUsage<GenericGenerator> def, MetadataBuildingContext context) {
		context.getMetadataCollector().addIdentifierGenerator( GeneratorBinder.buildIdGenerator( def, context ) );
	}

	public static void bindQueries(AnnotationTarget annotationTarget, MetadataBuildingContext context) {
		bindNamedJpaQueries( annotationTarget, context );
		bindNamedHibernateQueries( annotationTarget, context );
	}

	private static void bindNamedHibernateQueries(AnnotationTarget annotationTarget, MetadataBuildingContext context) {
		annotationTarget.forEachAnnotationUsage( org.hibernate.annotations.NamedQuery.class, (usage) -> QueryBinder.bindQuery(
				usage,
				context
		) );

		annotationTarget.forEachAnnotationUsage( org.hibernate.annotations.NamedNativeQuery.class, (usage) -> QueryBinder.bindNativeQuery(
				usage,
				context
		) );
	}

	private static void bindNamedJpaQueries(AnnotationTarget annotationTarget, MetadataBuildingContext context) {
		annotationTarget.forEachAnnotationUsage( SqlResultSetMapping.class, (usage) -> QueryBinder.bindSqlResultSetMapping(
				usage,
				context,
				false
		) );

		annotationTarget.forEachAnnotationUsage( NamedQuery.class, (usage) -> QueryBinder.bindQuery(
				usage,
				context,
				false
		) );

		annotationTarget.forEachAnnotationUsage( NamedNativeQuery.class, (usage) -> QueryBinder.bindNativeQuery(
				usage,
				context,
				false
		) );

		annotationTarget.forEachAnnotationUsage( NamedStoredProcedureQuery.class, (usage) -> QueryBinder.bindNamedStoredProcedureQuery(
				usage,
				context,
				false
		) );
	}

	/**
	 * Bind an annotated class. A subclass must be bound <em>after</em> its superclass.
	 *
	 * @param classDetails entity to bind as {@code XClass} instance
	 * @param inheritanceStatePerClass Metadata about the inheritance relationships for all mapped classes
	 *
	 * @throws MappingException in case there is a configuration error
	 */
	public static void bindClass(
			ClassDetails classDetails,
			Map<ClassDetails, InheritanceState> inheritanceStatePerClass,
			MetadataBuildingContext context) throws MappingException {

		detectMappedSuperclassProblems( classDetails );

		bindQueries( classDetails, context );
		handleImport( classDetails, context );
		//bindFilterDefs( classDetails, context );
		bindTypeDescriptorRegistrations( classDetails, context );
		bindEmbeddableInstantiatorRegistrations( classDetails, context );
		bindUserTypeRegistrations( classDetails, context );
		bindCompositeUserTypeRegistrations( classDetails, context );
		bindConverterRegistrations( classDetails, context );

		// try to find class level generators
		final Map<String, IdentifierGeneratorDefinition> generators = buildGenerators( classDetails, context );
		if ( context.getMetadataCollector().getClassType( classDetails ) == ENTITY ) {
			EntityBinder.bindEntityClass( classDetails, inheritanceStatePerClass, generators, context );
		}
	}

	private static void handleImport(ClassDetails annotatedClass, MetadataBuildingContext context) {
		if ( annotatedClass.hasAnnotationUsage( Imported.class ) ) {
			final String qualifiedName = annotatedClass.getName();
			final String name = StringHelper.unqualify( qualifiedName );
			final String rename = annotatedClass.getAnnotationUsage( Imported.class ).getString( "rename" );
			context.getMetadataCollector().addImport( rename.isEmpty() ? name : rename, qualifiedName );
		}
	}

	private static void detectMappedSuperclassProblems(ClassDetails annotatedClass) {
		if ( annotatedClass.hasAnnotationUsage( MappedSuperclass.class ) ) {
			//@Entity and @MappedSuperclass on the same class leads to a NPE down the road
			if ( annotatedClass.hasAnnotationUsage( Entity.class ) ) {
				throw new AnnotationException( "Type '" + annotatedClass.getName()
						+ "' is annotated both '@Entity' and '@MappedSuperclass'" );
			}
			if ( annotatedClass.hasAnnotationUsage( Table.class ) ) {
				throw new AnnotationException( "Mapped superclass '" + annotatedClass.getName()
						+ "' may not specify a '@Table'" );
			}
			if ( annotatedClass.hasAnnotationUsage( Inheritance.class ) ) {
				LOG.unsupportedMappedSuperclassWithEntityInheritance( annotatedClass.getName() );
			}
		}
	}

	private static void bindTypeDescriptorRegistrations(
			AnnotationTarget annotatedElement,
			MetadataBuildingContext context) {
		final ManagedBeanRegistry managedBeanRegistry = context.getBootstrapContext()
				.getServiceRegistry()
				.getService( ManagedBeanRegistry.class );

		annotatedElement.forEachAnnotationUsage( JavaTypeRegistration.class, (usage) -> {
			handleJavaTypeRegistration( context, managedBeanRegistry, usage );
		} );

		annotatedElement.forEachAnnotationUsage( JdbcTypeRegistration.class, (usage) -> {
			handleJdbcTypeRegistration( context, managedBeanRegistry, usage );
		} );

		annotatedElement.forEachAnnotationUsage( CollectionTypeRegistration.class, (usage) -> {
			context.getMetadataCollector().addCollectionTypeRegistration( usage );
		} );
	}

	private static void handleJdbcTypeRegistration(
			MetadataBuildingContext context,
			ManagedBeanRegistry managedBeanRegistry,
			AnnotationUsage<JdbcTypeRegistration> annotation) {
		final Class<? extends JdbcType> jdbcTypeClass = annotation.getClassDetails( "value" ).toJavaClass();
		final JdbcType jdbcType = !context.getBuildingOptions().isAllowExtensionsInCdi()
				? FallbackBeanInstanceProducer.INSTANCE.produceBeanInstance( jdbcTypeClass )
				: managedBeanRegistry.getBean( jdbcTypeClass ).getBeanInstance();
		final Integer registrationCode = annotation.getInteger( "registrationCode" );
		final int typeCode = registrationCode == Integer.MIN_VALUE
				? jdbcType.getDefaultSqlTypeCode()
				: registrationCode;
		context.getMetadataCollector().addJdbcTypeRegistration( typeCode, jdbcType );
	}

	private static void handleJavaTypeRegistration(
			MetadataBuildingContext context,
			ManagedBeanRegistry managedBeanRegistry,
			AnnotationUsage<JavaTypeRegistration> annotation) {
		final Class<? extends BasicJavaType<?>> javaTypeClass = annotation.getClassDetails( "descriptorClass" ).toJavaClass();
		final BasicJavaType<?> javaType = !context.getBuildingOptions().isAllowExtensionsInCdi()
				? FallbackBeanInstanceProducer.INSTANCE.produceBeanInstance( javaTypeClass )
				: managedBeanRegistry.getBean( javaTypeClass ).getBeanInstance();
		context.getMetadataCollector().addJavaTypeRegistration(
				annotation.getClassDetails( "javaType" ).toJavaClass(),
				javaType
		);
	}

	private static void bindEmbeddableInstantiatorRegistrations(
			AnnotationTarget annotatedElement,
			MetadataBuildingContext context) {
		annotatedElement.forEachAnnotationUsage( EmbeddableInstantiatorRegistration.class, (usage) -> {
			handleEmbeddableInstantiatorRegistration( context, usage );
		} );
	}

	private static void handleEmbeddableInstantiatorRegistration(
			MetadataBuildingContext context,
			AnnotationUsage<EmbeddableInstantiatorRegistration> annotation) {
		context.getMetadataCollector().registerEmbeddableInstantiator(
				annotation.getClassDetails( "embeddableClass" ).toJavaClass(),
				annotation.getClassDetails( "instantiator" ).toJavaClass()
		);
	}

	private static void bindCompositeUserTypeRegistrations(
			AnnotationTarget annotatedElement,
			MetadataBuildingContext context) {
		annotatedElement.forEachAnnotationUsage( CompositeTypeRegistration.class, (usage) -> {
			handleCompositeUserTypeRegistration( context, usage );
		} );
	}

	private static void bindUserTypeRegistrations(
			AnnotationTarget annotatedElement,
			MetadataBuildingContext context) {
		annotatedElement.forEachAnnotationUsage( TypeRegistration.class, (usage) -> {
			handleUserTypeRegistration( context, usage );
		} );
	}

	private static void handleUserTypeRegistration(
			MetadataBuildingContext context,
			AnnotationUsage<TypeRegistration> compositeTypeRegistration) {
		// TODO: check that the two classes agree, i.e. that
		//       the user type knows how to handle the type
		context.getMetadataCollector().registerUserType(
				compositeTypeRegistration.getClassDetails( "basicClass" ).toJavaClass(),
				compositeTypeRegistration.getClassDetails( "userType" ).toJavaClass()
		);
	}

	private static void handleCompositeUserTypeRegistration(
			MetadataBuildingContext context,
			AnnotationUsage<CompositeTypeRegistration> compositeTypeRegistration) {
		// TODO: check that the two classes agree, i.e. that
		//       the user type knows how to handle the type
		context.getMetadataCollector().registerCompositeUserType(
				compositeTypeRegistration.getClassDetails( "embeddableClass" ).toJavaClass(),
				compositeTypeRegistration.getClassDetails( "userType" ).toJavaClass()
		);
	}

	private static void bindConverterRegistrations(AnnotationTarget container, MetadataBuildingContext context) {
		container.forEachAnnotationUsage( ConverterRegistration.class, (usage) -> {
			handleConverterRegistration( usage, context );
		} );
	}

	private static void handleConverterRegistration(AnnotationUsage<ConverterRegistration> registration, MetadataBuildingContext context) {
		final InFlightMetadataCollector metadataCollector = context.getMetadataCollector();
		metadataCollector.getConverterRegistry().addRegisteredConversion( new RegisteredConversion(
				registration.getClassDetails( "domainType" ).toJavaClass(),
				registration.getClassDetails( "converter" ).toJavaClass(),
				registration.getBoolean( "autoApply" ),
				context
		) );
	}

	public static void bindFilterDefs(AnnotationTarget annotatedElement, MetadataBuildingContext context) {
		annotatedElement.forEachAnnotationUsage( FilterDef.class, (usage) -> {
			bindFilterDef( usage, context );
		} );
	}

	private static void bindFilterDef(AnnotationUsage<FilterDef> filterDef, MetadataBuildingContext context) {
		final String name = filterDef.getString( "name" );
		if ( context.getMetadataCollector().getFilterDefinition( name ) != null ) {
			throw new AnnotationException( "Multiple '@FilterDef' annotations define a filter named '" + name + "'" );
		}

		final Map<String, JdbcMapping> paramJdbcMappings;
		final List<AnnotationUsage<ParamDef>> explicitParameters = filterDef.getList( "parameters" );
		if ( explicitParameters.isEmpty() ) {
			paramJdbcMappings = emptyMap();
		}
		else {
			paramJdbcMappings = new HashMap<>();
			for ( AnnotationUsage<ParamDef> explicitParameter : explicitParameters ) {
				final String parameterName = explicitParameter.getString( "name" );
				final ClassDetails typeClassDetails = explicitParameter.getClassDetails( "type" );
				final JdbcMapping jdbcMapping = resolveFilterParamType( typeClassDetails.toJavaClass(), context );
				if ( jdbcMapping == null ) {
					throw new MappingException(
							String.format(
									Locale.ROOT,
									"Unable to resolve type specified for parameter (%s) defined for @FilterDef (%s)",
									parameterName,
									name
							)
					);
				}
				paramJdbcMappings.put( parameterName, jdbcMapping );
			}
		}

		final FilterDefinition filterDefinition = new FilterDefinition(
				name,
				filterDef.getString( "defaultCondition" ),
				paramJdbcMappings
		);

		LOG.debugf( "Binding filter definition: %s", filterDefinition.getFilterName() );
		context.getMetadataCollector().addFilterDefinition( filterDefinition );
	}

	@SuppressWarnings("unchecked")
	public static JdbcMapping resolveFilterParamType(Class<?> type, MetadataBuildingContext context) {
		if ( UserType.class.isAssignableFrom( type ) ) {
			return resolveUserType( (Class<UserType<?>>) type, context );
		}
		else if ( AttributeConverter.class.isAssignableFrom( type ) ) {
			return resolveAttributeConverter( (Class<AttributeConverter<?,?>>) type, context );
		}
		else if ( JavaType.class.isAssignableFrom( type ) ) {
			return resolveJavaType( (Class<JavaType<?>>) type, context );
		}
		else {
			return resolveBasicType( type, context );
		}
	}

	@Internal
	public static BasicType<Object> resolveBasicType(Class<?> type, MetadataBuildingContext context) {
		final TypeConfiguration typeConfiguration = context.getBootstrapContext().getTypeConfiguration();
		final JavaType<Object> jtd = typeConfiguration.getJavaTypeRegistry().findDescriptor( type );
		if ( jtd != null ) {
			final JdbcType jdbcType = jtd.getRecommendedJdbcType(
					new JdbcTypeIndicators() {
						@Override
						public TypeConfiguration getTypeConfiguration() {
							return typeConfiguration;
						}

						@Override
						public int getPreferredSqlTypeCodeForBoolean() {
							return context.getPreferredSqlTypeCodeForBoolean();
						}

						@Override
						public int getPreferredSqlTypeCodeForDuration() {
							return context.getPreferredSqlTypeCodeForDuration();
						}

						@Override
						public int getPreferredSqlTypeCodeForUuid() {
							return context.getPreferredSqlTypeCodeForUuid();
						}

						@Override
						public int getPreferredSqlTypeCodeForInstant() {
							return context.getPreferredSqlTypeCodeForInstant();
						}

						@Override
						public int getPreferredSqlTypeCodeForArray() {
							return context.getPreferredSqlTypeCodeForArray();
						}

						@Override
						public Dialect getDialect() {
							return context.getMetadataCollector().getDatabase().getDialect();
						}
					}
			);
			return typeConfiguration.getBasicTypeRegistry().resolve( jtd, jdbcType );
		}
		else {
			return null;
		}
	}

	@Internal
	public static JdbcMapping resolveUserType(Class<UserType<?>> userTypeClass, MetadataBuildingContext context) {
		final UserType<?> userType = !context.getBuildingOptions().isAllowExtensionsInCdi()
				? FallbackBeanInstanceProducer.INSTANCE.produceBeanInstance( userTypeClass )
				: context.getBootstrapContext().getServiceRegistry()
						.requireService( ManagedBeanRegistry.class )
						.getBean( userTypeClass ).getBeanInstance();
		return new CustomType<>( userType, context.getBootstrapContext().getTypeConfiguration() );
	}

	@Internal
	public static JdbcMapping resolveAttributeConverter(Class<AttributeConverter<?, ?>> type, MetadataBuildingContext context) {
		final BootstrapContext bootstrapContext = context.getBootstrapContext();
		final ManagedBeanRegistry beanRegistry =
				bootstrapContext.getServiceRegistry().requireService( ManagedBeanRegistry.class );
		final ManagedBean<AttributeConverter<?, ?>> bean = beanRegistry.getBean( type );

		final TypeConfiguration typeConfiguration = bootstrapContext.getTypeConfiguration();
		final JavaTypeRegistry jtdRegistry = typeConfiguration.getJavaTypeRegistry();
		final JavaType<? extends AttributeConverter<?,?>> converterJtd = jtdRegistry.resolveDescriptor( bean.getBeanClass() );

		final ParameterizedType converterParameterizedType = GenericsHelper.extractParameterizedType( bean.getBeanClass() );
		final Class<?> domainJavaClass = GenericsHelper.extractClass( converterParameterizedType.getActualTypeArguments()[0] );
		final Class<?> relationalJavaClass = GenericsHelper.extractClass( converterParameterizedType.getActualTypeArguments()[1] );

		final JavaType<?> domainJtd = jtdRegistry.resolveDescriptor( domainJavaClass );
		final JavaType<?> relationalJtd = jtdRegistry.resolveDescriptor( relationalJavaClass );

		@SuppressWarnings({"rawtypes", "unchecked"})
		final JpaAttributeConverterImpl<?,?> valueConverter =
				new JpaAttributeConverterImpl( bean, converterJtd, domainJtd, relationalJtd );
		return new ConvertedBasicTypeImpl<>(
				ConverterDescriptor.TYPE_NAME_PREFIX
						+ valueConverter.getConverterJavaType().getJavaType().getTypeName(),
				String.format(
						"BasicType adapter for AttributeConverter<%s,%s>",
						domainJtd.getJavaType().getTypeName(),
						relationalJtd.getJavaType().getTypeName()
				),
				relationalJtd.getRecommendedJdbcType( typeConfiguration.getCurrentBaseSqlTypeIndicators() ),
				valueConverter
		);
	}

	@Internal
	public static JdbcMapping resolveJavaType(Class<JavaType<?>> type, MetadataBuildingContext context) {
		final TypeConfiguration typeConfiguration = context.getBootstrapContext().getTypeConfiguration();
		final JavaType<?> jtd = getJavaType( type, context, typeConfiguration );
		final JdbcType jdbcType = jtd.getRecommendedJdbcType( typeConfiguration.getCurrentBaseSqlTypeIndicators() );
		return typeConfiguration.getBasicTypeRegistry().resolve( jtd, jdbcType );
	}

	private static JavaType<?> getJavaType(
			Class<JavaType<?>> javaTypeClass,
			MetadataBuildingContext context,
			TypeConfiguration typeConfiguration) {
		final JavaType<?> registeredJtd = typeConfiguration.getJavaTypeRegistry().findDescriptor( javaTypeClass );
		if ( registeredJtd != null ) {
			return registeredJtd;
		}

		if ( !context.getBuildingOptions().isAllowExtensionsInCdi() ) {
			return FallbackBeanInstanceProducer.INSTANCE.produceBeanInstance( javaTypeClass );
		}

		final StandardServiceRegistry serviceRegistry = context.getBootstrapContext().getServiceRegistry();
		final ManagedBeanRegistry beanRegistry = serviceRegistry.getService( ManagedBeanRegistry.class );
		return beanRegistry.getBean(javaTypeClass).getBeanInstance();
	}

	public static void bindFetchProfilesForClass(ClassDetails annotatedClass, MetadataBuildingContext context) {
		bindFetchProfiles( annotatedClass, context );
	}

	public static void bindFetchProfilesForPackage(ClassLoaderService cls, String packageName, MetadataBuildingContext context) {
		final ClassDetails packageInfoClassDetails = context
				.getMetadataCollector()
				.getClassDetailsRegistry()
				.findClassDetails( packageName + ".package-info" );
		if ( packageInfoClassDetails != null ) {
			bindFetchProfiles( packageInfoClassDetails, context );
		}
	}

	private static void bindFetchProfiles(AnnotationTarget annotatedElement, MetadataBuildingContext context) {
		annotatedElement.forEachAnnotationUsage( FetchProfile.class, (usage) -> {
			bindFetchProfile( usage, context );
		} );
	}

	private static void bindFetchProfile(AnnotationUsage<FetchProfile> fetchProfile, MetadataBuildingContext context) {
		final String name = fetchProfile.getString( "name" );
		if ( reuseOrCreateFetchProfile( context, name ) ) {
			final List<AnnotationUsage<FetchOverride>> fetchOverrides = fetchProfile.getList( "fetchOverrides" );
			for ( AnnotationUsage<FetchOverride> fetchOverride : fetchOverrides ) {
				final FetchType type = fetchOverride.getEnum( "fetch" );
				final FetchMode mode = fetchOverride.getEnum( "mode" );
				if ( type == FetchType.LAZY && mode == FetchMode.JOIN ) {
					throw new AnnotationException(
							"Fetch profile '" + name
									+ "' has a '@FetchOverride' with 'fetch=LAZY' and 'mode=JOIN'"
									+ " (join fetching is eager by nature)"
					);
				}
				context.getMetadataCollector().addSecondPass( new FetchOverrideSecondPass( name, fetchOverride, context ) );
			}
		}
		// otherwise, it's a fetch profile defined in XML, and it overrides
		// the annotations, so we simply ignore this annotation completely
	}

	private static boolean reuseOrCreateFetchProfile(MetadataBuildingContext context, String name) {
		// We tolerate multiple @FetchProfile annotations for same named profile
		org.hibernate.mapping.FetchProfile existing = context.getMetadataCollector().getFetchProfile( name );
		if ( existing == null ) {
			// no existing profile, so create a new one
			org.hibernate.mapping.FetchProfile profile =
					new org.hibernate.mapping.FetchProfile( name, ANNOTATIONS );
			context.getMetadataCollector().addFetchProfile( profile );
			return true;
		}
		else {
			return existing.getSource() == ANNOTATIONS;
		}
	}

	/**
	 * For the mapped entities build some temporary data-structure containing information about the
	 * inheritance status of a class.
	 *
	 * @param orderedClasses Order list of all annotated entities and their mapped superclasses
	 *
	 * @return A map of {@code InheritanceState}s keyed against their {@code XClass}.
	 */
	public static Map<ClassDetails, InheritanceState> buildInheritanceStates(
			List<ClassDetails> orderedClasses,
			MetadataBuildingContext buildingContext) {
		final Map<ClassDetails, InheritanceState> inheritanceStatePerClass = new HashMap<>( orderedClasses.size() );
		for ( ClassDetails clazz : orderedClasses ) {
			final InheritanceState superclassState = getSuperclassInheritanceState( clazz, inheritanceStatePerClass );
			final InheritanceState state = new InheritanceState( clazz, inheritanceStatePerClass, buildingContext );
			if ( superclassState != null ) {
				//the classes are ordered thus preventing an NPE
				superclassState.setHasSiblings( true );
				final InheritanceState superEntityState = getInheritanceStateOfSuperEntity( clazz, inheritanceStatePerClass );
				state.setHasParents( superEntityState != null );
				logMixedInheritance( clazz, superclassState, state );
				if ( superclassState.getType() != null ) {
					state.setType( superclassState.getType() );
				}
			}
			switch ( buildingContext.getMetadataCollector().getClassType( clazz ) ) {
				case ENTITY:
				case MAPPED_SUPERCLASS:
				case EMBEDDABLE:
					inheritanceStatePerClass.put( clazz, state );
			}
		}
		return inheritanceStatePerClass;
	}

	private static void logMixedInheritance(ClassDetails classDetails, InheritanceState superclassState, InheritanceState state) {
		if ( state.getType() != null && superclassState.getType() != null ) {
			final boolean nonDefault = InheritanceType.SINGLE_TABLE != state.getType();
			final boolean mixingStrategy = state.getType() != superclassState.getType();
			if ( nonDefault && mixingStrategy ) {
				//TODO: why on earth is this not an error!
				LOG.invalidSubStrategy( classDetails.getName() );
			}
		}
	}
}
