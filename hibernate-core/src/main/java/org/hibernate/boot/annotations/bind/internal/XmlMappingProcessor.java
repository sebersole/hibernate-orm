/*
 * Hibernate, Relational Persistence for Idiomatic Java
 *
 * License: GNU Lesser General Public License (LGPL), version 2.1 or later.
 * See the lgpl.txt file in the root directory or http://www.gnu.org/licenses/lgpl-2.1.html.
 */
package org.hibernate.boot.annotations.bind.internal;

import java.util.Collection;
import java.util.List;

import org.hibernate.boot.annotations.source.spi.ClassDetails;
import org.hibernate.boot.annotations.source.spi.ClassDetailsRegistry;
import org.hibernate.boot.annotations.spi.AnnotationProcessingContext;
import org.hibernate.boot.internal.ClassmateContext;
import org.hibernate.boot.jaxb.Origin;
import org.hibernate.boot.jaxb.mapping.JaxbConverter;
import org.hibernate.boot.jaxb.mapping.JaxbConverterRegistration;
import org.hibernate.boot.jaxb.mapping.JaxbEntityMappings;
import org.hibernate.boot.jaxb.mapping.JaxbGenericIdGenerator;
import org.hibernate.boot.jaxb.mapping.JaxbJavaTypeRegistration;
import org.hibernate.boot.jaxb.mapping.JaxbJdbcTypeRegistration;
import org.hibernate.boot.jaxb.mapping.JaxbSequenceGenerator;
import org.hibernate.boot.jaxb.mapping.JaxbTableGenerator;
import org.hibernate.boot.jaxb.mapping.JaxbUserTypeRegistration;
import org.hibernate.boot.jaxb.spi.BindableMappingDescriptor;
import org.hibernate.boot.jaxb.spi.Binding;
import org.hibernate.boot.model.convert.internal.ClassBasedConverterDescriptor;
import org.hibernate.boot.model.convert.spi.RegisteredConversion;
import org.hibernate.boot.spi.InFlightMetadataCollector;
import org.hibernate.internal.util.StringHelper;
import org.hibernate.internal.util.collections.CollectionHelper;
import org.hibernate.resource.beans.internal.FallbackBeanInstanceProducer;
import org.hibernate.type.descriptor.java.JavaType;
import org.hibernate.type.descriptor.jdbc.JdbcType;

import static java.lang.Boolean.TRUE;
import static org.hibernate.boot.annotations.AnnotationSourceLogging.ANNOTATION_SOURCE_LOGGER;
import static org.hibernate.boot.annotations.AnnotationSourceLogging.ANNOTATION_SOURCE_LOGGER_DEBUG_ENABLED;

/**
 * @author Steve Ebersole
 */
public class XmlMappingProcessor {

	public static void processXmlMappings(
			Collection<Binding<BindableMappingDescriptor>> xmlMappingBindings,
			AnnotationProcessingContext processingContext) {
		if ( !processingContext.getMetadataBuildingContext().getBuildingOptions().isXmlMappingEnabled() ) {
			return;
		}

		if ( CollectionHelper.isEmpty( xmlMappingBindings ) ) {
			return;
		}

		final XmlMappingProcessor xmlMappingProcessor = new XmlMappingProcessor( processingContext );
		xmlMappingBindings.forEach( xmlMappingProcessor::processXmlMapping );
	}

	private final AnnotationProcessingContext processingContext;

	private final ClassDetailsRegistry classDetailsRegistry;
	private final InFlightMetadataCollector metadataCollector;
	private final ClassmateContext classmateContext;

	public XmlMappingProcessor(AnnotationProcessingContext processingContext) {
		this.processingContext = processingContext;

		this.classDetailsRegistry = processingContext.getClassDetailsRegistry();
		this.metadataCollector = processingContext.getMetadataBuildingContext().getMetadataCollector();
		this.classmateContext = processingContext.getMetadataBuildingContext()
				.getBootstrapContext()
				.getClassmateContext();

	}

	public void processXmlMapping(Binding<BindableMappingDescriptor> binding) {
		if ( binding.getRoot() instanceof JaxbEntityMappings ) {
			processMappingXml( binding );
		}
		else {
			processHbmXml( binding );
		}
	}

	private void processMappingXml(Binding<BindableMappingDescriptor> binding) {
		if ( ANNOTATION_SOURCE_LOGGER_DEBUG_ENABLED ) {
			ANNOTATION_SOURCE_LOGGER.debugf(
					"Processing mapping XML binding : %s (%s)",
					binding.getOrigin().getName(),
					binding.getOrigin().getType()
			);
		}

		final JaxbEntityMappings root = (JaxbEntityMappings) binding.getRoot();
		processGlobals( root, binding.getOrigin() );
	}

	private void processGlobals(JaxbEntityMappings root, Origin origin) {
		// NOTE : for most globals, we will just register the appropriate references
		// rather than deal with conversions to AnnotationUsage

		// todo (annotation-source) : delegate this to `GlobalAnnotationProcessor`?

		processJavaTypeRegistrations( root.getJavaTypeRegistrations() );
		processJdbcTypeRegistrations( root.getJdbcTypeRegistrations() );
		processUserTypeRegistrations( root.getUserTypeRegistrations() );
		processConverterRegistrations( root.getConverterRegistrations() );

		processConverters( root.getConverters() );

		processSequenceGenerators( root.getSequenceGenerators() );
		processTableGenerators( root.getTableGenerators() );
		processGenericGenerators( root.getGenericGenerators() );
	}

	private void processJavaTypeRegistrations(List<JaxbJavaTypeRegistration> registrations) {
		if ( CollectionHelper.isEmpty( registrations ) ) {
			return;
		}

		registrations.forEach( (reg) -> {
			final ClassDetails domainClass = classDetailsRegistry.resolveManagedClass( reg.getClazz() );
			final ClassDetails descriptorClassDetails = classDetailsRegistry.resolveManagedClass( reg.getDescriptor() );
			final JavaType<?> descriptor = FallbackBeanInstanceProducer.INSTANCE.produceBeanInstance( descriptorClassDetails.toJavaClass() );

			metadataCollector.addJavaTypeRegistration( domainClass.toJavaClass(), descriptor );
		} );
	}

	private void processJdbcTypeRegistrations(List<JaxbJdbcTypeRegistration> registrations) {
		if ( CollectionHelper.isEmpty( registrations ) ) {
			return;
		}

		registrations.forEach( (reg) -> {
			final Integer code = reg.getCode();
			final ClassDetails descriptorClassDetails = classDetailsRegistry.resolveManagedClass( reg.getDescriptor() );
			final JdbcType descriptor = FallbackBeanInstanceProducer.INSTANCE.produceBeanInstance( descriptorClassDetails.toJavaClass() );

			final int registrationCode = code == null
					? descriptor.getJdbcTypeCode()
					: code;
			metadataCollector.addJdbcTypeRegistration( registrationCode, descriptor );
		} );
	}

	private void processUserTypeRegistrations(List<JaxbUserTypeRegistration> registrations) {
		if ( CollectionHelper.isEmpty( registrations ) ) {
			return;
		}

		registrations.forEach( (reg) -> {
			final ClassDetails domainTypeDetails = classDetailsRegistry.resolveManagedClass( reg.getClazz() );
			final ClassDetails descriptorDetails = classDetailsRegistry.resolveManagedClass( reg.getDescriptor() );

			metadataCollector.registerUserType( domainTypeDetails.toJavaClass(), descriptorDetails.toJavaClass() );
		} );
	}

	private void processConverterRegistrations(List<JaxbConverterRegistration> registrations) {
		if ( CollectionHelper.isEmpty( registrations ) ) {
			return;
		}

		registrations.forEach( (reg) -> {
			final ClassDetails converterDetails = classDetailsRegistry.resolveManagedClass( reg.getConverter() );

			final Class<?> explicitDomainType;
			final String explicitDomainTypeName = reg.getClazz();
			if ( StringHelper.isNotEmpty( explicitDomainTypeName ) ) {
				explicitDomainType = classDetailsRegistry.resolveManagedClass( explicitDomainTypeName ).toJavaClass();
			}
			else {
				explicitDomainType = null;
			}

			final boolean autoApply = reg.isAutoApply();
			metadataCollector.getConverterRegistry().addRegisteredConversion( new RegisteredConversion(
					explicitDomainType,
					converterDetails.toJavaClass(),
					autoApply,
					processingContext.getMetadataBuildingContext()
			) );
		} );
	}

	private void processConverters(List<JaxbConverter> converters) {
		if ( CollectionHelper.isEmpty( converters ) ) {
			return;
		}

		converters.forEach( (converter) -> {
			final ClassDetails converterDetails = classDetailsRegistry.resolveManagedClass( converter.getClazz() );
			final boolean autoApply = converter.isAutoApply() == TRUE;

			metadataCollector.getConverterRegistry().addAttributeConverter( new ClassBasedConverterDescriptor(
					converterDetails.toJavaClass(),
					autoApply,
					classmateContext
			) );
		} );
	}

	private void processSequenceGenerators(List<JaxbSequenceGenerator> generators) {
		if ( CollectionHelper.isEmpty( generators ) ) {
			return;
		}

	}

	private void processTableGenerators(List<JaxbTableGenerator> generators) {
		if ( CollectionHelper.isEmpty( generators ) ) {
			return;
		}

	}

	private void processGenericGenerators(List<JaxbGenericIdGenerator> generators) {
		if ( CollectionHelper.isEmpty( generators ) ) {
			return;
		}

	}

	private void processHbmXml(Binding<BindableMappingDescriptor> binding) {
		throw new UnsupportedOperationException( "hbm.xml binding not supported" );
	}
}
