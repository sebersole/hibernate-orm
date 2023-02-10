/*
 * Hibernate, Relational Persistence for Idiomatic Java
 *
 * License: GNU Lesser General Public License (LGPL), version 2.1 or later.
 * See the lgpl.txt file in the root directory or http://www.gnu.org/licenses/lgpl-2.1.html.
 */
package org.hibernate.boot.annotations.bind.internal;

import java.util.HashSet;
import java.util.Locale;
import java.util.Map;
import java.util.Set;

import org.hibernate.boot.annotations.source.spi.AnnotationTarget;
import org.hibernate.boot.annotations.source.spi.AnnotationUsage;
import org.hibernate.boot.annotations.source.spi.ClassDetails;
import org.hibernate.boot.annotations.source.spi.HibernateAnnotations;
import org.hibernate.boot.annotations.source.spi.JpaAnnotations;
import org.hibernate.boot.annotations.spi.AnnotationProcessingContext;
import org.hibernate.boot.model.convert.spi.RegisteredConversion;
import org.hibernate.boot.spi.InFlightMetadataCollector;
import org.hibernate.metamodel.CollectionClassification;
import org.hibernate.metamodel.spi.EmbeddableInstantiator;
import org.hibernate.resource.beans.internal.FallbackBeanInstanceProducer;
import org.hibernate.type.descriptor.java.JavaType;
import org.hibernate.type.descriptor.jdbc.JdbcType;
import org.hibernate.usertype.CompositeUserType;
import org.hibernate.usertype.UserCollectionType;
import org.hibernate.usertype.UserType;

import jakarta.persistence.AttributeConverter;

import static org.hibernate.boot.annotations.bind.internal.BindingHelper.extractParameterMap;
import static org.hibernate.boot.annotations.bind.internal.BindingHelper.extractValue;
import static org.hibernate.boot.annotations.source.spi.HibernateAnnotations.COLLECTION_TYPE_REG;
import static org.hibernate.boot.annotations.source.spi.HibernateAnnotations.COMPOSITE_TYPE_REG;
import static org.hibernate.boot.annotations.source.spi.HibernateAnnotations.CONVERTER_REG;
import static org.hibernate.boot.annotations.source.spi.HibernateAnnotations.EMBEDDABLE_INSTANTIATOR_REG;
import static org.hibernate.boot.annotations.source.spi.HibernateAnnotations.GENERIC_GENERATOR;
import static org.hibernate.boot.annotations.source.spi.HibernateAnnotations.JAVA_TYPE_REG;
import static org.hibernate.boot.annotations.source.spi.HibernateAnnotations.JDBC_TYPE_REG;
import static org.hibernate.boot.annotations.source.spi.HibernateAnnotations.TYPE_REG;
import static org.hibernate.boot.annotations.source.spi.JpaAnnotations.NAMED_ENTITY_GRAPH;
import static org.hibernate.boot.annotations.source.spi.JpaAnnotations.SEQUENCE_GENERATOR;
import static org.hibernate.boot.annotations.source.spi.JpaAnnotations.TABLE_GENERATOR;

/**
 * Processes "global" annotations which can be applied at a number of levels,
 * but are always considered global in scope
 *
 * @author Steve Ebersole
 */
public class GlobalAnnotationProcessor {
	private final AnnotationProcessingContext processingContext;
	private final Set<String> processedGlobalAnnotationSources = new HashSet<>();

	public GlobalAnnotationProcessor(AnnotationProcessingContext processingContext) {
		this.processingContext = processingContext;
	}

	public void processGlobalAnnotation(AnnotationTarget annotationTarget) {
		if ( processedGlobalAnnotationSources.contains( annotationTarget.getName() ) ) {
			return;
		}
		processedGlobalAnnotationSources.add( annotationTarget.getName() );

		processTypeContributions( annotationTarget );
		processGenerators( annotationTarget );
		processNamedQueries( annotationTarget );
		processNamedEntityGraphs( annotationTarget );
		processFilterDefinitions( annotationTarget );
	}

	private void processTypeContributions(AnnotationTarget annotationTarget) {
		processJavaTypeRegistrations( annotationTarget );
		processJdbcTypeRegistrations( annotationTarget );
		processAttributeConverterRegistrations( annotationTarget );
		processUserTypeRegistrations( annotationTarget );
		processCompositeUserTypeRegistrations( annotationTarget );
		processCollectionTypeRegistrations( annotationTarget );
		processEmbeddableInstantiatorRegistrations( annotationTarget );
	}

	private void processJavaTypeRegistrations(AnnotationTarget annotationTarget) {
		annotationTarget.forEachAnnotation( JAVA_TYPE_REG, (usage) -> {
			final ClassDetails javaType = extractValue( usage, "javaType" );
			final Class<? extends JavaType<?>> descriptorClass = extractClassValue( usage, "descriptorClass" );
			final JavaType<?> descriptor = FallbackBeanInstanceProducer.INSTANCE.produceBeanInstance( descriptorClass );

			processingContext.getMetadataBuildingContext()
					.getMetadataCollector()
					.addJavaTypeRegistration( javaType.toJavaClass(), descriptor );
		} );
	}

	private static <T> Class<T> extractClassValue(AnnotationUsage<?> usage, String attributeName) {
		final ClassDetails descriptorClass = extractValue( usage, attributeName );
		if ( descriptorClass == null ) {
			throw new IllegalStateException(
					String.format(
							Locale.ROOT,
							"Unexpected null attribute value - %s.%s",
							usage.getAnnotationDescriptor().getName(),
							attributeName
					)
			);
		}
		//noinspection unchecked
		return (Class<T>) descriptorClass.toJavaClass();
	}

	private void processJdbcTypeRegistrations(AnnotationTarget annotationTarget) {
		annotationTarget.forEachAnnotation( JDBC_TYPE_REG, (usage) -> {
			final int registrationCode = extractValue( usage, "registrationCode", Integer.MIN_VALUE );
			final Class<? extends JdbcType> jdbcTypeClass = extractClassValue( usage, "value" );

			final JdbcType jdbcType = FallbackBeanInstanceProducer.INSTANCE.produceBeanInstance( jdbcTypeClass );
			final int typeCode = registrationCode == Integer.MIN_VALUE
					? jdbcType.getJdbcTypeCode()
					: registrationCode;

			processingContext.getMetadataBuildingContext()
					.getMetadataCollector()
					.addJdbcTypeRegistration( typeCode, jdbcType );
		} );
	}

	private void processAttributeConverterRegistrations(AnnotationTarget annotationTarget) {
		annotationTarget.forEachAnnotation( CONVERTER_REG, (usage) -> {
			final Class<?> domainType = extractClassValue( usage, "domainType" );
			final Class<? extends AttributeConverter<?,?>> converterType = extractClassValue( usage, "converter" );
			final boolean autoApply = extractValue( usage, "autoApply", true );

			processingContext.getMetadataBuildingContext()
					.getMetadataCollector()
					.getConverterRegistry()
					.addRegisteredConversion( new RegisteredConversion(
							domainType,
							converterType,
							autoApply,
							processingContext.getMetadataBuildingContext()
					) );
		} );
	}

	private void processUserTypeRegistrations(AnnotationTarget annotationTarget) {
		annotationTarget.forEachAnnotation( TYPE_REG, (usage) -> {
			final Class<?> basicClass = extractClassValue( usage, "basicClass" );
			final Class<? extends UserType<?>> userTypeClass = extractClassValue( usage, "userType" );

			processingContext.getMetadataBuildingContext()
					.getMetadataCollector()
					.registerUserType( basicClass, userTypeClass );
		} );
	}

	private void processCompositeUserTypeRegistrations(AnnotationTarget annotationTarget) {
		annotationTarget.forEachAnnotation( COMPOSITE_TYPE_REG, (usage) -> {
			final Class<?> embeddableClass = extractClassValue( usage, "embeddableClass" );
			final Class<? extends CompositeUserType<?>> userTypeClass = extractClassValue( usage, "userType" );

			processingContext.getMetadataBuildingContext()
					.getMetadataCollector()
					.registerCompositeUserType( embeddableClass, userTypeClass );
		} );
	}

	private void processCollectionTypeRegistrations(AnnotationTarget annotationTarget) {
		annotationTarget.forEachAnnotation( COLLECTION_TYPE_REG, (usage) -> {
			final CollectionClassification classification = extractValue( usage, "classification" );
			final Class<? extends UserCollectionType> type = extractClassValue( usage, "type" );
			final Map<String,String> parameterMap = extractParameterMap( extractValue( usage, "parameters" ) );

			final InFlightMetadataCollector.CollectionTypeRegistrationDescriptor registrationDescriptor = new InFlightMetadataCollector.CollectionTypeRegistrationDescriptor( type, parameterMap );

			processingContext.getMetadataBuildingContext()
					.getMetadataCollector()
					.addCollectionTypeRegistration( classification, registrationDescriptor );
		} );
	}

	private void processEmbeddableInstantiatorRegistrations(AnnotationTarget annotationTarget) {
		annotationTarget.forEachAnnotation( EMBEDDABLE_INSTANTIATOR_REG, (usage) -> {
			final Class<?> embeddableClass = extractClassValue( usage, "embeddableClass" );
			final Class<? extends EmbeddableInstantiator> instantiatorClass = extractClassValue( usage, "instantiator" );

			processingContext.getMetadataBuildingContext()
					.getMetadataCollector()
					.registerEmbeddableInstantiator( embeddableClass, instantiatorClass );
		} );
	}

	private void processGenerators(AnnotationTarget annotationTarget) {
		processSequenceGenerators( annotationTarget );
		processTableGenerators( annotationTarget );
		processGenericGenerators( annotationTarget );
	}

	private void processSequenceGenerators(AnnotationTarget annotationTarget) {
		annotationTarget.forEachAnnotation( SEQUENCE_GENERATOR, (usage) -> {
			// todo (annotation-source) : implement
		} );
	}

	private void processTableGenerators(AnnotationTarget annotationTarget) {
		annotationTarget.forEachAnnotation( TABLE_GENERATOR, (usage) -> {
			// todo (annotation-source) : implement
		} );
	}

	private void processGenericGenerators(AnnotationTarget annotationTarget) {
		annotationTarget.forEachAnnotation( GENERIC_GENERATOR, (usage) -> {
			// todo (annotation-source) : implement
		} );
	}

	private void processNamedQueries(AnnotationTarget annotationTarget) {
		processNamedQuery( annotationTarget );
		processNamedNativeQuery( annotationTarget );
		processNamedProcedureQuery( annotationTarget );
	}

	private void processNamedQuery(AnnotationTarget annotationTarget) {
		annotationTarget.forEachAnnotation( JpaAnnotations.NAMED_QUERY, (usage) -> {
			// todo (annotation-source) : implement
		} );

		annotationTarget.forEachAnnotation( HibernateAnnotations.NAMED_QUERY, (usage) -> {
			// todo (annotation-source) : implement
		} );
	}

	private void processNamedNativeQuery(AnnotationTarget annotationTarget) {
		annotationTarget.forEachAnnotation( JpaAnnotations.NAMED_NATIVE_QUERY, (usage) -> {
			// todo (annotation-source) : implement
		} );

		annotationTarget.forEachAnnotation( HibernateAnnotations.NAMED_NATIVE_QUERY, (usage) -> {
			// todo (annotation-source) : implement
		} );
	}


	private void processNamedProcedureQuery(AnnotationTarget annotationTarget) {
		annotationTarget.forEachAnnotation( JpaAnnotations.NAMED_STORED_PROCEDURE_QUERY, (usage) -> {
			// todo (annotation-source) : implement
		} );
	}

	private void processNamedEntityGraphs(AnnotationTarget annotationTarget) {
		annotationTarget.forEachAnnotation( NAMED_ENTITY_GRAPH, (usage) -> {
			// todo (annotation-source) : implement
		} );
	}

	private void processFilterDefinitions(AnnotationTarget annotationTarget) {
		annotationTarget.forEachAnnotation( HibernateAnnotations.FILTER_DEF, (usage) -> {
			// todo (annotation-source) : implement
		} );
	}
}
