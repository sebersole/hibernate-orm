/*
 * Hibernate, Relational Persistence for Idiomatic Java
 *
 * License: GNU Lesser General Public License (LGPL), version 2.1 or later.
 * See the lgpl.txt file in the root directory or http://www.gnu.org/licenses/lgpl-2.1.html.
 */
package org.hibernate.boot.annotations.bind.internal;

import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.hibernate.boot.annotations.source.spi.AnnotationTarget;
import org.hibernate.boot.annotations.source.spi.ClassDetails;
import org.hibernate.boot.annotations.source.spi.ClassDetailsRegistry;
import org.hibernate.boot.annotations.spi.AnnotationProcessingContext;
import org.hibernate.boot.annotations.xml.spi.XmlMappingAnnotationProcessingContext;
import org.hibernate.boot.internal.ClassmateContext;
import org.hibernate.boot.jaxb.mapping.JaxbCollectionUserTypeRegistration;
import org.hibernate.boot.jaxb.mapping.JaxbCompositeUserTypeRegistration;
import org.hibernate.boot.jaxb.mapping.JaxbConfigurationParameter;
import org.hibernate.boot.jaxb.mapping.JaxbConverterRegistration;
import org.hibernate.boot.jaxb.mapping.JaxbEmbeddableInstantiatorRegistration;
import org.hibernate.boot.jaxb.mapping.JaxbEntityMappings;
import org.hibernate.boot.jaxb.mapping.JaxbJavaTypeRegistration;
import org.hibernate.boot.jaxb.mapping.JaxbJdbcTypeRegistration;
import org.hibernate.boot.jaxb.mapping.JaxbUserTypeRegistration;
import org.hibernate.boot.model.convert.spi.RegisteredConversion;
import org.hibernate.boot.spi.InFlightMetadataCollector;
import org.hibernate.boot.spi.InFlightMetadataCollector.CollectionTypeRegistrationDescriptor;
import org.hibernate.internal.util.StringHelper;
import org.hibernate.internal.util.collections.CollectionHelper;
import org.hibernate.metamodel.CollectionClassification;
import org.hibernate.metamodel.spi.EmbeddableInstantiator;
import org.hibernate.resource.beans.internal.FallbackBeanInstanceProducer;
import org.hibernate.type.descriptor.java.JavaType;
import org.hibernate.type.descriptor.jdbc.JdbcType;
import org.hibernate.usertype.CompositeUserType;
import org.hibernate.usertype.UserCollectionType;
import org.hibernate.usertype.UserType;

import jakarta.persistence.AttributeConverter;

import static org.hibernate.boot.annotations.bind.internal.BindingHelper.extractValue;
import static org.hibernate.boot.annotations.source.spi.HibernateAnnotations.COLLECTION_TYPE_REG;
import static org.hibernate.boot.annotations.source.spi.HibernateAnnotations.COMPOSITE_TYPE_REG;
import static org.hibernate.boot.annotations.source.spi.HibernateAnnotations.CONVERTER_REG;
import static org.hibernate.boot.annotations.source.spi.HibernateAnnotations.EMBEDDABLE_INSTANTIATOR_REG;
import static org.hibernate.boot.annotations.source.spi.HibernateAnnotations.JAVA_TYPE_REG;
import static org.hibernate.boot.annotations.source.spi.HibernateAnnotations.JDBC_TYPE_REG;
import static org.hibernate.boot.annotations.source.spi.HibernateAnnotations.TYPE_REG;

/**
 * Processes global type-contribution metadata
 *
 * @see org.hibernate.annotations.JavaTypeRegistration
 * @see org.hibernate.annotations.JdbcTypeRegistration
 * @see org.hibernate.annotations.ConverterRegistration
 * @see org.hibernate.annotations.EmbeddableInstantiatorRegistration
 * @see org.hibernate.annotations.TypeRegistration
 * @see org.hibernate.annotations.CompositeTypeRegistration
 * @see org.hibernate.annotations.CollectionTypeRegistration
 *
 * @author Steve Ebersole
 */
public class TypeContributionProcessor {
	public static void processTypeContributions(
			AnnotationTarget annotationTarget,
			AnnotationProcessingContext processingContext) {
		final TypeContributionProcessor processor = new TypeContributionProcessor( processingContext );
		processor.processJavaTypeRegistrations( annotationTarget );
		processor.processJdbcTypeRegistrations( annotationTarget );
		processor.processAttributeConverterRegistrations( annotationTarget );
		processor.processUserTypeRegistrations( annotationTarget );
		processor.processCompositeUserTypeRegistrations( annotationTarget );
		processor.processCollectionTypeRegistrations( annotationTarget );
		processor.processEmbeddableInstantiatorRegistrations( annotationTarget );
	}

	public static void processTypeContributions(
			JaxbEntityMappings root,
			XmlMappingAnnotationProcessingContext processingContext) {
		final TypeContributionProcessor processor = new TypeContributionProcessor( processingContext );
		processor.processJavaTypeRegistrations( root.getJavaTypeRegistrations() );
		processor.processJdbcTypeRegistrations( root.getJdbcTypeRegistrations() );
		processor.processAttributeConverterRegistrations( root.getConverterRegistrations() );
		processor.processUserTypeRegistrations( root.getUserTypeRegistrations() );
		processor.processCompositeUserTypeRegistrations( root.getCompositeUserTypeRegistrations() );
		processor.processCollectionTypeRegistrations( root.getCollectionUserTypeRegistrations() );
		processor.processEmbeddableInstantiatorRegistrations( root.getEmbeddableInstantiatorRegistrations() );
	}

	private final AnnotationProcessingContext processingContext;

	private final ClassDetailsRegistry classDetailsRegistry;
	private final InFlightMetadataCollector metadataCollector;
	private final ClassmateContext classmateContext;

	public TypeContributionProcessor(AnnotationProcessingContext processingContext) {
		this.processingContext = processingContext;

		this.classDetailsRegistry = processingContext.getClassDetailsRegistry();
		this.metadataCollector = processingContext.getMetadataBuildingContext().getMetadataCollector();
		this.classmateContext = processingContext.getMetadataBuildingContext()
				.getBootstrapContext()
				.getClassmateContext();
	}

	private void processJavaTypeRegistrations(AnnotationTarget annotationTarget) {
		annotationTarget.forEachAnnotation( JAVA_TYPE_REG, (usage) -> {
			final ClassDetails javaType = extractValue( usage, "javaType" );
			final Class<? extends JavaType<?>> descriptorClass = BindingHelper.extractClassValue( usage, "descriptorClass" );
			final JavaType<?> descriptor = FallbackBeanInstanceProducer.INSTANCE.produceBeanInstance( descriptorClass );

			metadataCollector.addJavaTypeRegistration( javaType.toJavaClass(), descriptor );
		} );
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

	private void processJdbcTypeRegistrations(AnnotationTarget annotationTarget) {
		annotationTarget.forEachAnnotation( JDBC_TYPE_REG, (usage) -> {
			final int registrationCode = extractValue( usage, "registrationCode", Integer.MIN_VALUE );
			final Class<? extends JdbcType> jdbcTypeClass = BindingHelper.extractClassValue( usage, "value" );

			final JdbcType jdbcType = FallbackBeanInstanceProducer.INSTANCE.produceBeanInstance( jdbcTypeClass );
			final int typeCode = registrationCode == Integer.MIN_VALUE
					? jdbcType.getJdbcTypeCode()
					: registrationCode;

			metadataCollector.addJdbcTypeRegistration( typeCode, jdbcType );
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

	private void processAttributeConverterRegistrations(AnnotationTarget annotationTarget) {
		annotationTarget.forEachAnnotation( CONVERTER_REG, (usage) -> {
			final Class<?> domainType = BindingHelper.extractClassValue( usage, "domainType" );
			final Class<? extends AttributeConverter<?,?>> converterType = BindingHelper.extractClassValue( usage, "converter" );
			final boolean autoApply = extractValue( usage, "autoApply", true );

			metadataCollector.getConverterRegistry().addRegisteredConversion( new RegisteredConversion(
					domainType,
					converterType,
					autoApply,
					processingContext.getMetadataBuildingContext()
			) );
		} );
	}

	private void processAttributeConverterRegistrations(List<JaxbConverterRegistration> registrations) {
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

	private void processEmbeddableInstantiatorRegistrations(AnnotationTarget annotationTarget) {
		annotationTarget.forEachAnnotation( EMBEDDABLE_INSTANTIATOR_REG, (usage) -> {
			final Class<?> embeddableClass = BindingHelper.extractClassValue( usage, "embeddableClass" );
			final Class<? extends EmbeddableInstantiator> instantiatorClass = BindingHelper.extractClassValue( usage, "instantiator" );

			metadataCollector.registerEmbeddableInstantiator( embeddableClass, instantiatorClass );
		} );
	}

	private void processEmbeddableInstantiatorRegistrations(List<JaxbEmbeddableInstantiatorRegistration> registrations) {
		if ( CollectionHelper.isEmpty( registrations ) ) {
			return;
		}

		registrations.forEach( (reg) -> {
			final ClassDetails embeddableClassDetails = classDetailsRegistry.resolveManagedClass( reg.getEmbeddableClass() );
			final ClassDetails instantiatorClassDetails = classDetailsRegistry.resolveManagedClass( reg.getInstantiator() );
			metadataCollector.registerEmbeddableInstantiator( embeddableClassDetails.toJavaClass(), instantiatorClassDetails.toJavaClass() );
		} );
	}

	private void processUserTypeRegistrations(AnnotationTarget annotationTarget) {
		annotationTarget.forEachAnnotation( TYPE_REG, (usage) -> {
			final Class<?> basicClass = BindingHelper.extractClassValue( usage, "basicClass" );
			final Class<? extends UserType<?>> userTypeClass = BindingHelper.extractClassValue( usage, "userType" );

			metadataCollector.registerUserType( basicClass, userTypeClass );
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

	private void processCompositeUserTypeRegistrations(AnnotationTarget annotationTarget) {
		annotationTarget.forEachAnnotation( COMPOSITE_TYPE_REG, (usage) -> {
			final Class<?> embeddableClass = BindingHelper.extractClassValue( usage, "embeddableClass" );
			final Class<? extends CompositeUserType<?>> userTypeClass = BindingHelper.extractClassValue( usage, "userType" );

			metadataCollector.registerCompositeUserType( embeddableClass, userTypeClass );
		} );
	}

	private void processCompositeUserTypeRegistrations(List<JaxbCompositeUserTypeRegistration> registrations) {
		if ( CollectionHelper.isEmpty( registrations ) ) {
			return;
		}

		registrations.forEach( (reg) -> {
			final ClassDetails domainTypeDetails = classDetailsRegistry.resolveManagedClass( reg.getClazz() );
			final ClassDetails descriptorDetails = classDetailsRegistry.resolveManagedClass( reg.getDescriptor() );

			metadataCollector.registerCompositeUserType( domainTypeDetails.toJavaClass(), descriptorDetails.toJavaClass() );
		} );
	}

	private void processCollectionTypeRegistrations(AnnotationTarget annotationTarget) {
		annotationTarget.forEachAnnotation( COLLECTION_TYPE_REG, (usage) -> {
			final CollectionClassification classification = extractValue( usage, "classification" );
			final Class<? extends UserCollectionType> type = BindingHelper.extractClassValue( usage, "type" );
			final Map<String,String> parameterMap = extractParameterMap( extractValue( usage, "parameters" ) );

			metadataCollector.addCollectionTypeRegistration(
					classification,
					new CollectionTypeRegistrationDescriptor( type, parameterMap )
			);
		} );
	}

	private void processCollectionTypeRegistrations(List<JaxbCollectionUserTypeRegistration> registrations) {
		if ( CollectionHelper.isEmpty( registrations ) ) {
			return;
		}

		registrations.forEach( (reg) -> {
			final ClassDetails descriptorDetails = classDetailsRegistry.resolveManagedClass( reg.getDescriptor() );
			final Map<String,String> parameterMap = extractParameterMap( reg.getParameters() );

			metadataCollector.addCollectionTypeRegistration(
					reg.getClassification(),
					new CollectionTypeRegistrationDescriptor( descriptorDetails.toJavaClass(), parameterMap )
			);
		} );
	}

	private Map<String, String> extractParameterMap(List<JaxbConfigurationParameter> parameters) {
		if ( CollectionHelper.isEmpty( parameters ) ) {
			return Collections.emptyMap();
		}

		final Map<String,String> result = new HashMap<>();
		parameters.forEach( parameter -> result.put( parameter.getName(), parameter.getValue() ) );
		return result;
	}
}
