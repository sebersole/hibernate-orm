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
import org.hibernate.boot.annotations.xml.XmlMappingAnnotationProcessingContextImpl;
import org.hibernate.boot.annotations.xml.spi.XmlMappingAnnotationProcessingContext;
import org.hibernate.boot.internal.ClassmateContext;
import org.hibernate.boot.jaxb.hbm.spi.JaxbHbmHibernateMapping;
import org.hibernate.boot.jaxb.mapping.JaxbConverter;
import org.hibernate.boot.jaxb.mapping.JaxbEntityMappings;
import org.hibernate.boot.jaxb.mapping.JaxbGenericIdGenerator;
import org.hibernate.boot.jaxb.mapping.JaxbSequenceGenerator;
import org.hibernate.boot.jaxb.mapping.JaxbTableGenerator;
import org.hibernate.boot.jaxb.spi.BindableMappingDescriptor;
import org.hibernate.boot.jaxb.spi.Binding;
import org.hibernate.boot.model.convert.internal.ClassBasedConverterDescriptor;
import org.hibernate.boot.spi.InFlightMetadataCollector;
import org.hibernate.internal.util.collections.CollectionHelper;

import static java.lang.Boolean.TRUE;

/**
 * @author Steve Ebersole
 */
public class XmlMappingProcessor {

	/**
	 * Entry into processing a collection of XML mappings
	 */
	public static void processXmlMappings(
			Collection<Binding<BindableMappingDescriptor>> xmlMappingBindings,
			AnnotationProcessingContext processingContext) {
		if ( !processingContext.getMetadataBuildingContext().getBuildingOptions().isXmlMappingEnabled() ) {
			return;
		}

		if ( CollectionHelper.isEmpty( xmlMappingBindings ) ) {
			return;
		}

		for ( Binding<BindableMappingDescriptor> xmlBinding : xmlMappingBindings ) {
			if ( xmlBinding.getRoot() instanceof JaxbHbmHibernateMapping ) {
				throw new UnsupportedOperationException( "`hbm.xml` processing not supported" );
			}

			processXmlMapping( xmlBinding, processingContext );
		}
	}

	/**
	 * Processes a single of XML mapping.
	 *
	 * @apiNote Generally called via {@link #processXmlMappings}, but exposed for tests
	 */
	public static void processXmlMapping(
			Binding<BindableMappingDescriptor> binding,
			AnnotationProcessingContext processingContext) {

		final JaxbEntityMappings root = (JaxbEntityMappings) binding.getRoot();

		final XmlMappingProcessor processor = new XmlMappingProcessor( new XmlMappingAnnotationProcessingContextImpl(
				root,
				binding.getOrigin(),
				processingContext
		) );

		processor.processGlobals( root );
	}

	private final XmlMappingAnnotationProcessingContext processingContext;

	private final ClassDetailsRegistry classDetailsRegistry;
	private final InFlightMetadataCollector metadataCollector;
	private final ClassmateContext classmateContext;

	private XmlMappingProcessor(XmlMappingAnnotationProcessingContext processingContext) {
		this.processingContext = processingContext;

		this.classDetailsRegistry = processingContext.getClassDetailsRegistry();
		this.metadataCollector = processingContext.getMetadataBuildingContext().getMetadataCollector();
		this.classmateContext = processingContext.getMetadataBuildingContext()
				.getBootstrapContext()
				.getClassmateContext();

	}

	private void processGlobals(JaxbEntityMappings root) {
		// NOTE : for most globals, we will just process and register the appropriate
		// references rather than deal with conversions to AnnotationUsage and
		// have the annotation-based processors pick those up

		TypeContributionProcessor.processTypeContributions( root, processingContext );

		processConverters( root.getConverters() );

		processSequenceGenerators( root.getSequenceGenerators() );
		processTableGenerators( root.getTableGenerators() );
		processGenericGenerators( root.getGenericGenerators() );
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
