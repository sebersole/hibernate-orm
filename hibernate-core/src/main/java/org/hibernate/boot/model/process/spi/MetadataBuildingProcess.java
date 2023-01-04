/*
 * Hibernate, Relational Persistence for Idiomatic Java
 *
 * License: GNU Lesser General Public License (LGPL), version 2.1 or later.
 * See the lgpl.txt file in the root directory or <http://www.gnu.org/licenses/lgpl-2.1.html>.
 */
package org.hibernate.boot.model.process.spi;

import java.sql.Types;
import java.time.Instant;
import java.time.OffsetDateTime;
import java.time.ZonedDateTime;
import java.util.Collection;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import org.hibernate.boot.MetadataSources;
import org.hibernate.boot.internal.InFlightMetadataCollectorImpl;
import org.hibernate.boot.internal.MetadataBuildingContextRootImpl;
import org.hibernate.boot.jandex.internal.JandexIndexAugmenter;
import org.hibernate.boot.jaxb.internal.MappingBinder;
import org.hibernate.boot.model.TypeContributions;
import org.hibernate.boot.model.TypeContributor;
import org.hibernate.boot.model.process.internal.ManagedResourcesImpl;
import org.hibernate.boot.model.process.internal.ScanningCoordinator;
import org.hibernate.boot.model.source.internal.annotations.AnnotationMetadataSourceProcessorImpl;
import org.hibernate.boot.model.source.internal.hbm.EntityHierarchyBuilder;
import org.hibernate.boot.model.source.internal.hbm.EntityHierarchySourceImpl;
import org.hibernate.boot.model.source.internal.hbm.HbmMetadataSourceProcessorImpl;
import org.hibernate.boot.model.source.internal.hbm.MappingDocument;
import org.hibernate.boot.model.source.internal.hbm.ModelBinder;
import org.hibernate.boot.model.source.spi.MetadataSourceProcessor;
import org.hibernate.boot.registry.StandardServiceRegistry;
import org.hibernate.boot.registry.classloading.spi.ClassLoaderService;
import org.hibernate.boot.spi.AdditionalJaxbMappingProducer;
import org.hibernate.boot.spi.BootstrapContext;
import org.hibernate.boot.spi.MetadataBuildingOptions;
import org.hibernate.boot.spi.MetadataContributor;
import org.hibernate.boot.spi.MetadataImplementor;
import org.hibernate.cfg.AvailableSettings;
import org.hibernate.cfg.MetadataSourceType;
import org.hibernate.dialect.Dialect;
import org.hibernate.engine.config.spi.ConfigurationService;
import org.hibernate.engine.config.spi.StandardConverters;
import org.hibernate.engine.jdbc.spi.JdbcServices;
import org.hibernate.type.BasicType;
import org.hibernate.type.BasicTypeRegistry;
import org.hibernate.type.SqlTypes;
import org.hibernate.type.descriptor.java.spi.JavaTypeRegistry;
import org.hibernate.type.descriptor.jdbc.JdbcType;
import org.hibernate.type.descriptor.jdbc.JsonJdbcType;
import org.hibernate.type.descriptor.jdbc.XmlAsStringJdbcType;
import org.hibernate.type.descriptor.jdbc.spi.JdbcTypeRegistry;
import org.hibernate.type.descriptor.sql.DdlType;
import org.hibernate.type.descriptor.sql.internal.DdlTypeImpl;
import org.hibernate.type.descriptor.sql.spi.DdlTypeRegistry;
import org.hibernate.type.internal.NamedBasicTypeImpl;
import org.hibernate.type.spi.TypeConfiguration;

import org.jboss.jandex.IndexView;
import org.jboss.logging.Logger;

import static org.hibernate.internal.util.config.ConfigurationHelper.getPreferredSqlTypeCodeForArray;
import static org.hibernate.internal.util.config.ConfigurationHelper.getPreferredSqlTypeCodeForDuration;
import static org.hibernate.internal.util.config.ConfigurationHelper.getPreferredSqlTypeCodeForInstant;
import static org.hibernate.internal.util.config.ConfigurationHelper.getPreferredSqlTypeCodeForUuid;

/**
 * Represents the process of transforming a {@link MetadataSources}
 * reference into a {@link org.hibernate.boot.Metadata} reference.  Allows for 2 different process paradigms:<ul>
 *     <li>
 *         Single step : as defined by the {@link #build} method; internally leverages the 2-step paradigm
 *     </li>
 *     <li>
 *         Two step : a first step coordinates resource scanning and some other preparation work; a second step
 *         builds the {@link org.hibernate.boot.Metadata}.  A hugely important distinction in the need for the
 *         steps is that the first phase should strive to not load user entity/component classes so that we can still
 *         perform enhancement on them later.  This approach caters to the 2-phase bootstrap we use in regards
 *         to WildFly Hibernate-JPA integration.  The first step is defined by {@link #prepare} which returns
 *         a {@link ManagedResources} instance.  The second step is defined by calling
 *         {@link #complete}
 *     </li>
 * </ul>
 *
 * @author Steve Ebersole
 */
public class MetadataBuildingProcess {
	private static final Logger log = Logger.getLogger( MetadataBuildingProcess.class );

	/**
	 * Unified single phase for MetadataSources to Metadata process
	 *
	 * @param sources The MetadataSources
	 * @param options The building options
	 *
	 * @return The built Metadata
	 */
	public static MetadataImplementor build(
			final MetadataSources sources,
			final BootstrapContext bootstrapContext,
			final MetadataBuildingOptions options) {
		return complete( prepare( sources, bootstrapContext ), bootstrapContext, options );
	}

	/**
	 * First step of two-phase for MetadataSources to Metadata process
	 *
	 * @param sources The MetadataSources
	 * @param bootstrapContext The bootstrapContext
	 *
	 * @return Token/memento representing all known users resources (classes, packages, mapping files, etc).
	 */
	public static ManagedResources prepare(
			final MetadataSources sources,
			final BootstrapContext bootstrapContext) {
		final ManagedResourcesImpl managedResources = new ManagedResourcesImpl( sources, bootstrapContext );
		final ConfigurationService configService = bootstrapContext.getServiceRegistry().getService( ConfigurationService.class );
		final boolean xmlMappingEnabled = configService.getSetting(
				AvailableSettings.XML_MAPPING_ENABLED,
				StandardConverters.BOOLEAN,
				true
		);
		ScanningCoordinator.INSTANCE.coordinateScan(
				managedResources,
				bootstrapContext,
				xmlMappingEnabled ? sources.getXmlMappingBinderAccess() : null
		);
		managedResources.finishPreparation();
		return managedResources;
	}

	/**
	 * Second step of two-phase for MetadataSources to Metadata process
	 *
	 * @param managedResources The token/memento from 1st phase
	 * @param options The building options
	 *
	 * @return Token/memento representing all known users resources (classes, packages, mapping files, etc).
	 */
	public static MetadataImplementor complete(
			final ManagedResources managedResources,
			final BootstrapContext bootstrapContext,
			final MetadataBuildingOptions options) {
		final InFlightMetadataCollectorImpl metadataCollector = new InFlightMetadataCollectorImpl(
				bootstrapContext,
				options
		);

		handleTypes( bootstrapContext, options );

		final ClassLoaderService classLoaderService = options.getServiceRegistry().getService( ClassLoaderService.class );

		final MetadataBuildingContextRootImpl rootMetadataBuildingContext = new MetadataBuildingContextRootImpl(
				"orm",
				bootstrapContext,
				options,
				metadataCollector
		);

		managedResources.getAttributeConverterDescriptors().forEach( metadataCollector::addAttributeConverter );

		bootstrapContext.getTypeConfiguration().scope( rootMetadataBuildingContext );

		final IndexView jandexView = JandexIndexAugmenter.buildAugmentedIndex(
				managedResources,
				rootMetadataBuildingContext
		);


		// ~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~
		// Set up the processors and start binding
		//		NOTE : this becomes even more simplified after we move purely
		// 		to unified model

		final MetadataSourceProcessor processor = new MetadataSourceProcessor() {
			private final MetadataSourceProcessor hbmProcessor =
						options.isXmlMappingEnabled()
							? new HbmMetadataSourceProcessorImpl( managedResources, rootMetadataBuildingContext )
							: new NoOpMetadataSourceProcessorImpl();

			private final AnnotationMetadataSourceProcessorImpl annotationProcessor = new AnnotationMetadataSourceProcessorImpl(
					managedResources,
					rootMetadataBuildingContext,
					jandexView
			);

			@Override
			public void prepare() {
				hbmProcessor.prepare();
				annotationProcessor.prepare();
			}

			@Override
			public void processTypeDefinitions() {
				hbmProcessor.processTypeDefinitions();
				annotationProcessor.processTypeDefinitions();
			}

			@Override
			public void processQueryRenames() {
				hbmProcessor.processQueryRenames();
				annotationProcessor.processQueryRenames();
			}

			@Override
			public void processNamedQueries() {
				hbmProcessor.processNamedQueries();
				annotationProcessor.processNamedQueries();
			}

			@Override
			public void processAuxiliaryDatabaseObjectDefinitions() {
				hbmProcessor.processAuxiliaryDatabaseObjectDefinitions();
				annotationProcessor.processAuxiliaryDatabaseObjectDefinitions();
			}

			@Override
			public void processIdentifierGenerators() {
				hbmProcessor.processIdentifierGenerators();
				annotationProcessor.processIdentifierGenerators();
			}

			@Override
			public void processFilterDefinitions() {
				hbmProcessor.processFilterDefinitions();
				annotationProcessor.processFilterDefinitions();
			}

			@Override
			public void processFetchProfiles() {
				hbmProcessor.processFetchProfiles();
				annotationProcessor.processFetchProfiles();
			}

			@Override
			public void prepareForEntityHierarchyProcessing() {
				for ( MetadataSourceType metadataSourceType : options.getSourceProcessOrdering() ) {
					if ( metadataSourceType == MetadataSourceType.HBM ) {
						hbmProcessor.prepareForEntityHierarchyProcessing();
					}

					if ( metadataSourceType == MetadataSourceType.CLASS ) {
						annotationProcessor.prepareForEntityHierarchyProcessing();
					}
				}
			}

			@Override
			public void processEntityHierarchies(Set<String> processedEntityNames) {
				for ( MetadataSourceType metadataSourceType : options.getSourceProcessOrdering() ) {
					if ( metadataSourceType == MetadataSourceType.HBM ) {
						hbmProcessor.processEntityHierarchies( processedEntityNames );
					}

					if ( metadataSourceType == MetadataSourceType.CLASS ) {
						annotationProcessor.processEntityHierarchies( processedEntityNames );
					}
				}
			}

			@Override
			public void postProcessEntityHierarchies() {
				for ( MetadataSourceType metadataSourceType : options.getSourceProcessOrdering() ) {
					if ( metadataSourceType == MetadataSourceType.HBM ) {
						hbmProcessor.postProcessEntityHierarchies();
					}

					if ( metadataSourceType == MetadataSourceType.CLASS ) {
						annotationProcessor.postProcessEntityHierarchies();
					}
				}
			}

			@Override
			public void processResultSetMappings() {
				hbmProcessor.processResultSetMappings();
				annotationProcessor.processResultSetMappings();
			}

			@Override
			public void finishUp() {
				hbmProcessor.finishUp();
				annotationProcessor.finishUp();
			}
		};

		processor.prepare();

		processor.processTypeDefinitions();
		processor.processQueryRenames();
		processor.processAuxiliaryDatabaseObjectDefinitions();

		processor.processIdentifierGenerators();
		processor.processFilterDefinitions();
		processor.processFetchProfiles();

		final Set<String> processedEntityNames = new HashSet<>();
		processor.prepareForEntityHierarchyProcessing();
		processor.processEntityHierarchies( processedEntityNames );
		processor.postProcessEntityHierarchies();

		processor.processResultSetMappings();

		for ( MetadataContributor contributor : classLoaderService.loadJavaServices( MetadataContributor.class ) ) {
			log.tracef( "Calling MetadataContributor : %s", contributor );
			contributor.contribute( metadataCollector, jandexView );
		}

		metadataCollector.processSecondPasses( rootMetadataBuildingContext );

		// Make sure collections are fully bound before processing named queries as hbm result set mappings require it
		processor.processNamedQueries();

		processor.finishUp();

		if ( options.isXmlMappingEnabled() ) {
			//noinspection deprecation
			final Iterable<AdditionalJaxbMappingProducer> producers = classLoaderService.loadJavaServices( AdditionalJaxbMappingProducer.class );
			if ( producers != null ) {
				final EntityHierarchyBuilder hierarchyBuilder = new EntityHierarchyBuilder();
				final MappingBinder mappingBinder = new MappingBinder(
						classLoaderService,
						new MappingBinder.Options() {
							@Override
							public boolean validateMappings() {
								return false;
							}

							@Override
							public boolean transformHbmMappings() {
								return false;
							}
						}
				);
				//noinspection deprecation
				for ( AdditionalJaxbMappingProducer producer : producers ) {
					log.tracef( "Calling AdditionalJaxbMappingProducer : %s", producer );
					Collection<MappingDocument> additionalMappings = producer.produceAdditionalMappings(
							metadataCollector,
							jandexView,
							mappingBinder,
							rootMetadataBuildingContext
					);
					for ( MappingDocument mappingDocument : additionalMappings ) {
						hierarchyBuilder.indexMappingDocument( mappingDocument );
					}
				}

				ModelBinder binder = ModelBinder.prepare( rootMetadataBuildingContext );
				for ( EntityHierarchySourceImpl entityHierarchySource : hierarchyBuilder.buildHierarchies() ) {
					binder.bindEntityHierarchy( entityHierarchySource );
				}
			}
		}

		applyExtraQueryImports( managedResources, metadataCollector );

		return metadataCollector.buildMetadataInstance( rootMetadataBuildingContext );
	}

	private static void applyExtraQueryImports(
			ManagedResources managedResources,
			InFlightMetadataCollectorImpl metadataCollector) {
		final Map<String, Class<?>> extraQueryImports = managedResources.getExtraQueryImports();
		if ( extraQueryImports == null || extraQueryImports.isEmpty() ) {
			return;
		}

		for ( Map.Entry<String, Class<?>> entry : extraQueryImports.entrySet() ) {
			metadataCollector.addImport( entry.getKey(), entry.getValue().getName() );
		}
	}

//	todo (7.0) : buildJandexInitializer
//	private static JandexInitManager buildJandexInitializer(
//			MetadataBuildingOptions options,
//			ClassLoaderAccess classLoaderAccess) {
//		final boolean autoIndexMembers = ConfigurationHelper.getBoolean(
//				org.hibernate.cfg.AvailableSettings.ENABLE_AUTO_INDEX_MEMBER_TYPES,
//				options.getServiceRegistry().getService( ConfigurationService.class ).getSettings(),
//				false
//		);
//
//		return new JandexInitManager( options.getJandexView(), classLoaderAccess, autoIndexMembers );
//	}

	private static void handleTypes(BootstrapContext bootstrapContext, MetadataBuildingOptions options) {
		final ClassLoaderService classLoaderService = options.getServiceRegistry().getService( ClassLoaderService.class );

		final TypeConfiguration typeConfiguration = bootstrapContext.getTypeConfiguration();
		final StandardServiceRegistry serviceRegistry = bootstrapContext.getServiceRegistry();
		final JdbcTypeRegistry jdbcTypeRegistry = typeConfiguration.getJdbcTypeRegistry();
		final TypeContributions typeContributions = () -> typeConfiguration;

		// add Dialect contributed types
		final Dialect dialect = options.getServiceRegistry().getService( JdbcServices.class ).getDialect();
		dialect.contribute( typeContributions, options.getServiceRegistry() );
		// Capture the dialect configured JdbcTypes so that we can detect if a TypeContributor overwrote them,
		// which has precedence over the fallback and preferred type registrations
		final JdbcType dialectUuidDescriptor = jdbcTypeRegistry.findDescriptor( SqlTypes.UUID );
		final JdbcType dialectArrayDescriptor = jdbcTypeRegistry.findDescriptor( SqlTypes.ARRAY );
		final JdbcType dialectIntervalDescriptor = jdbcTypeRegistry.findDescriptor( SqlTypes.INTERVAL_SECOND );

		// add TypeContributor contributed types.
		for ( TypeContributor contributor : classLoaderService.loadJavaServices( TypeContributor.class ) ) {
			contributor.contribute( typeContributions, options.getServiceRegistry() );
		}

		// add fallback type descriptors
		final int preferredSqlTypeCodeForUuid = getPreferredSqlTypeCodeForUuid( serviceRegistry );
		if ( preferredSqlTypeCodeForUuid != SqlTypes.UUID ) {
			adaptToPreferredSqlTypeCode(
					jdbcTypeRegistry,
					dialectUuidDescriptor,
					SqlTypes.UUID,
					preferredSqlTypeCodeForUuid
			);
		}
		else {
			addFallbackIfNecessary( jdbcTypeRegistry, SqlTypes.UUID, SqlTypes.BINARY );
		}

		final int preferredSqlTypeCodeForArray = getPreferredSqlTypeCodeForArray( serviceRegistry );
		if ( preferredSqlTypeCodeForArray != SqlTypes.ARRAY ) {
			adaptToPreferredSqlTypeCode(
					jdbcTypeRegistry,
					dialectArrayDescriptor,
					SqlTypes.ARRAY,
					preferredSqlTypeCodeForArray
			);
		}
		else {
			addFallbackIfNecessary( jdbcTypeRegistry, SqlTypes.ARRAY, SqlTypes.VARBINARY );
		}

		final int preferredSqlTypeCodeForDuration = getPreferredSqlTypeCodeForDuration( serviceRegistry );
		if ( preferredSqlTypeCodeForDuration != SqlTypes.INTERVAL_SECOND ) {
			adaptToPreferredSqlTypeCode(
					jdbcTypeRegistry,
					dialectIntervalDescriptor,
					SqlTypes.INTERVAL_SECOND,
					preferredSqlTypeCodeForDuration
			);
		}
		else {
			addFallbackIfNecessary( jdbcTypeRegistry, SqlTypes.INTERVAL_SECOND, SqlTypes.NUMERIC );
		}

		addFallbackIfNecessary( jdbcTypeRegistry, SqlTypes.INET, SqlTypes.VARBINARY );
		addFallbackIfNecessary( jdbcTypeRegistry, SqlTypes.GEOMETRY, SqlTypes.VARBINARY );
		addFallbackIfNecessary( jdbcTypeRegistry, SqlTypes.POINT, SqlTypes.VARBINARY );
		addFallbackIfNecessary( jdbcTypeRegistry, SqlTypes.GEOGRAPHY, SqlTypes.GEOMETRY );

		jdbcTypeRegistry.addDescriptorIfAbsent( JsonJdbcType.INSTANCE );
		jdbcTypeRegistry.addDescriptorIfAbsent( XmlAsStringJdbcType.INSTANCE );

		addFallbackIfNecessary( jdbcTypeRegistry, SqlTypes.MATERIALIZED_BLOB, SqlTypes.BLOB );
		addFallbackIfNecessary( jdbcTypeRegistry, SqlTypes.MATERIALIZED_CLOB, SqlTypes.CLOB );
		addFallbackIfNecessary( jdbcTypeRegistry, SqlTypes.MATERIALIZED_NCLOB, SqlTypes.NCLOB );

		final DdlTypeRegistry ddlTypeRegistry = typeConfiguration.getDdlTypeRegistry();
		// Fallback to the biggest varchar DdlType when json is requested
		ddlTypeRegistry.addDescriptorIfAbsent(
				new DdlTypeImpl(
						SqlTypes.JSON,
						ddlTypeRegistry.getTypeName( SqlTypes.VARCHAR, null, null, null ),
						dialect
				)
		);
		ddlTypeRegistry.addDescriptorIfAbsent(
				new DdlTypeImpl(
						SqlTypes.SQLXML,
						ddlTypeRegistry.getTypeName( SqlTypes.VARCHAR, null, null, null ),
						dialect
				)
		);
		// Fallback to the geometry DdlType when geography is requested
		final DdlType geometryType = ddlTypeRegistry.getDescriptor( SqlTypes.GEOMETRY );
		if ( geometryType != null ) {
			ddlTypeRegistry.addDescriptorIfAbsent(
					new DdlTypeImpl(
							SqlTypes.GEOGRAPHY,
							geometryType.getTypeName( null, null, null ),
							dialect
					)
			);
		}

		// add explicit application registered types
		typeConfiguration.addBasicTypeRegistrationContributions( options.getBasicTypeRegistrations() );

		final JdbcType timestampWithTimeZoneOverride = getTimestampWithTimeZoneOverride( options, jdbcTypeRegistry );
		if ( timestampWithTimeZoneOverride != null ) {
			adaptToDefaultTimeZoneStorage( typeConfiguration, timestampWithTimeZoneOverride );
		}
		final int preferredSqlTypeCodeForInstant = getPreferredSqlTypeCodeForInstant( serviceRegistry );
		if ( preferredSqlTypeCodeForInstant != SqlTypes.TIMESTAMP_UTC ) {
			adaptToPreferredSqlTypeCodeForInstant( typeConfiguration, jdbcTypeRegistry, preferredSqlTypeCodeForInstant );
		}
	}

	private static void adaptToPreferredSqlTypeCode(
			JdbcTypeRegistry jdbcTypeRegistry,
			JdbcType dialectUuidDescriptor,
			int defaultSqlTypeCode,
			int preferredSqlTypeCode) {
		if ( jdbcTypeRegistry.findDescriptor( defaultSqlTypeCode ) == dialectUuidDescriptor ) {
			jdbcTypeRegistry.addDescriptor(
					defaultSqlTypeCode,
					jdbcTypeRegistry.getDescriptor( preferredSqlTypeCode )
			);
		}
		// else warning?
	}

	private static void adaptToPreferredSqlTypeCodeForInstant(
			TypeConfiguration typeConfiguration,
			JdbcTypeRegistry jdbcTypeRegistry,
			int preferredSqlTypeCodeForInstant) {
		final JavaTypeRegistry javaTypeRegistry = typeConfiguration.getJavaTypeRegistry();
		final BasicTypeRegistry basicTypeRegistry = typeConfiguration.getBasicTypeRegistry();
		final BasicType<?> instantType = new NamedBasicTypeImpl<>(
				javaTypeRegistry.getDescriptor( Instant.class ),
				jdbcTypeRegistry.getDescriptor( preferredSqlTypeCodeForInstant ),
				"instant"
		);
		basicTypeRegistry.register(
				instantType,
				"org.hibernate.type.InstantType",
				Instant.class.getSimpleName(),
				Instant.class.getName()
		);
	}

	private static void adaptToDefaultTimeZoneStorage(
			TypeConfiguration typeConfiguration,
			JdbcType timestampWithTimeZoneOverride) {
		final JavaTypeRegistry javaTypeRegistry = typeConfiguration.getJavaTypeRegistry();
		final BasicTypeRegistry basicTypeRegistry = typeConfiguration.getBasicTypeRegistry();
		final BasicType<?> offsetDateTimeType = new NamedBasicTypeImpl<>(
				javaTypeRegistry.getDescriptor( OffsetDateTime.class ),
				timestampWithTimeZoneOverride,
				"OffsetDateTime"
		);
		final BasicType<?> zonedDateTimeType = new NamedBasicTypeImpl<>(
				javaTypeRegistry.getDescriptor( ZonedDateTime.class ),
				timestampWithTimeZoneOverride,
				"ZonedDateTime"
		);
		basicTypeRegistry.register(
				offsetDateTimeType,
				"org.hibernate.type.OffsetDateTimeType",
				OffsetDateTime.class.getSimpleName(),
				OffsetDateTime.class.getName()
		);
		basicTypeRegistry.register(
				zonedDateTimeType,
				"org.hibernate.type.ZonedDateTimeType",
				ZonedDateTime.class.getSimpleName(),
				ZonedDateTime.class.getName()
		);
	}

	private static JdbcType getTimestampWithTimeZoneOverride(MetadataBuildingOptions options, JdbcTypeRegistry jdbcTypeRegistry) {
		switch ( options.getDefaultTimeZoneStorage() ) {
			case NORMALIZE:
				// For NORMALIZE, we replace the standard types that use TIMESTAMP_WITH_TIMEZONE to use TIMESTAMP
				return jdbcTypeRegistry.getDescriptor( Types.TIMESTAMP );
			case NORMALIZE_UTC:
				// For NORMALIZE_UTC, we replace the standard types that use TIMESTAMP_WITH_TIMEZONE to use TIMESTAMP_UTC
				return jdbcTypeRegistry.getDescriptor( SqlTypes.TIMESTAMP_UTC );
			default:
				return null;
		}
	}

	private static void addFallbackIfNecessary(
			JdbcTypeRegistry jdbcTypeRegistry,
			int typeCode,
			int fallbackTypeCode) {
		if ( !jdbcTypeRegistry.hasRegisteredDescriptor( typeCode ) ) {
			jdbcTypeRegistry.addDescriptor( typeCode, jdbcTypeRegistry.getDescriptor( fallbackTypeCode ) );
		}
	}
}
