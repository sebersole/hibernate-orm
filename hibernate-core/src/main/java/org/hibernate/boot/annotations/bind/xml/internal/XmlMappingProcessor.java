/*
 * Hibernate, Relational Persistence for Idiomatic Java
 *
 * License: GNU Lesser General Public License (LGPL), version 2.1 or later.
 * See the lgpl.txt file in the root directory or http://www.gnu.org/licenses/lgpl-2.1.html.
 */
package org.hibernate.boot.annotations.bind.xml.internal;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.hibernate.MappingException;
import org.hibernate.boot.annotations.bind.internal.global.TypeContributionProcessor;
import org.hibernate.boot.annotations.bind.xml.spi.XmlClassAnnotationProcessingContext;
import org.hibernate.boot.annotations.bind.xml.spi.XmlDocumentAnnotationProcessingContext;
import org.hibernate.boot.annotations.source.internal.AnnotationAttributeValueImpl;
import org.hibernate.boot.annotations.source.internal.AnnotationUsageImpl;
import org.hibernate.boot.annotations.source.internal.dynamic.ClassDetailsImpl;
import org.hibernate.boot.annotations.source.internal.dynamic.DynamicMemberDetails;
import org.hibernate.boot.annotations.source.internal.dynamic.FieldDetailsImpl;
import org.hibernate.boot.annotations.source.internal.dynamic.MethodDetailsImpl;
import org.hibernate.boot.annotations.source.spi.AnnotationAttributeValue;
import org.hibernate.boot.annotations.source.spi.AnnotationUsage;
import org.hibernate.boot.annotations.source.spi.ClassDetails;
import org.hibernate.boot.annotations.source.spi.ClassDetailsRegistry;
import org.hibernate.boot.annotations.source.spi.HibernateAnnotations;
import org.hibernate.boot.annotations.source.spi.JpaAnnotations;
import org.hibernate.boot.annotations.source.spi.MemberDetails;
import org.hibernate.boot.annotations.spi.AnnotationProcessingContext;
import org.hibernate.boot.internal.ClassmateContext;
import org.hibernate.boot.jaxb.hbm.spi.JaxbHbmHibernateMapping;
import org.hibernate.boot.jaxb.mapping.JaxbBasic;
import org.hibernate.boot.jaxb.mapping.JaxbConverter;
import org.hibernate.boot.jaxb.mapping.JaxbEntity;
import org.hibernate.boot.jaxb.mapping.JaxbEntityMappings;
import org.hibernate.boot.jaxb.mapping.JaxbGenericIdGenerator;
import org.hibernate.boot.jaxb.mapping.JaxbId;
import org.hibernate.boot.jaxb.mapping.JaxbSequenceGenerator;
import org.hibernate.boot.jaxb.mapping.JaxbTableGenerator;
import org.hibernate.boot.jaxb.mapping.PersistentAttribute;
import org.hibernate.boot.jaxb.spi.BindableMappingDescriptor;
import org.hibernate.boot.jaxb.spi.Binding;
import org.hibernate.boot.model.convert.internal.ClassBasedConverterDescriptor;
import org.hibernate.boot.spi.InFlightMetadataCollector;
import org.hibernate.internal.util.StringHelper;

import jakarta.persistence.AccessType;
import jakarta.persistence.Basic;

import static java.lang.Boolean.TRUE;
import static java.util.Collections.singletonList;
import static org.hibernate.internal.util.StringHelper.isNotEmpty;
import static org.hibernate.internal.util.collections.CollectionHelper.isEmpty;
import static org.hibernate.internal.util.collections.CollectionHelper.isNotEmpty;

/**
 * Processes XML mappings
 *
 * @author Steve Ebersole
 */
public class XmlMappingProcessor {

	/**
	 * Entry into processing a collection of XML mappings.
	 *
	 * @implNote It is expected that all known annotated domain classes
	 * (both listed and discovered) have had their ClassDetail references
	 * created by the time we get here
	 */
	public static void processXmlMappings(
			Collection<Binding<BindableMappingDescriptor>> xmlMappingBindings,
			AnnotationProcessingContext processingContext) {
		final boolean xmlMappingEnabled = processingContext.getMetadataBuildingContext()
				.getBuildingOptions()
				.isXmlMappingEnabled();
		if ( !xmlMappingEnabled ) {
			return;
		}

		if ( isEmpty( xmlMappingBindings ) ) {
			return;
		}

		final XmlMappingProcessor xmlMappingProcessor = new XmlMappingProcessor( processingContext );
		for ( Binding<BindableMappingDescriptor> xmlBinding : xmlMappingBindings ) {
			if ( xmlBinding.getRoot() instanceof JaxbHbmHibernateMapping ) {
				xmlMappingProcessor.processHbmXml( xmlBinding );
			}
			else {
				xmlMappingProcessor.processMappingXml( xmlBinding, processingContext );
			}
		}

		xmlMappingProcessor.processClassBindings();
	}

	private final AnnotationProcessingContext processingContext;

	private final ClassDetailsRegistry classDetailsRegistry;
	private final InFlightMetadataCollector metadataCollector;
	private final ClassmateContext classmateContext;

	private final Map<String,ClassBinding> classBindingMap = new HashMap<>();

	private XmlMappingProcessor(AnnotationProcessingContext processingContext) {
		this.processingContext = processingContext;

		this.classDetailsRegistry = processingContext.getClassDetailsRegistry();
		this.metadataCollector = processingContext.getMetadataBuildingContext().getMetadataCollector();
		this.classmateContext = processingContext.getMetadataBuildingContext()
				.getBootstrapContext()
				.getClassmateContext();
	}

	/**
	 * Processes a single of mapping XML document creating entries in `classBindingMap`
	 */
	private void processMappingXml(
			Binding<BindableMappingDescriptor> binding,
			AnnotationProcessingContext processingContext) {
		final JaxbEntityMappings root = (JaxbEntityMappings) binding.getRoot();
		final XmlDocumentAnnotationProcessingContextImpl documentContext = new XmlDocumentAnnotationProcessingContextImpl(
				root,
				binding.getOrigin(),
				processingContext
		);

		processGlobals( root, documentContext );

		prepareClassBindings( root, documentContext );
	}

	private void processGlobals(JaxbEntityMappings root, XmlDocumentAnnotationProcessingContext localContext) {
		// NOTE : for most globals, we will just process and register the appropriate
		// references rather than deal with conversions to AnnotationUsage and
		// have the annotation-based processors pick those up

		TypeContributionProcessor.processTypeContributions( root, localContext );

		processConverters( root.getConverters(), localContext );

		processSequenceGenerators( root.getSequenceGenerators(), localContext );
		processTableGenerators( root.getTableGenerators(), localContext );
		processGenericGenerators( root.getGenericGenerators(), localContext );
	}

	private void processConverters(List<JaxbConverter> converters, XmlDocumentAnnotationProcessingContext localContext) {
		if ( isEmpty( converters ) ) {
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

	private void processSequenceGenerators(
			List<JaxbSequenceGenerator> generators,
			XmlDocumentAnnotationProcessingContext localContext) {
		if ( isEmpty( generators ) ) {
			return;
		}

	}

	private void processTableGenerators(
			List<JaxbTableGenerator> generators,
			XmlDocumentAnnotationProcessingContext localContext) {
		if ( isEmpty( generators ) ) {
			return;
		}

	}

	private void processGenericGenerators(
			List<JaxbGenericIdGenerator> generators,
			XmlDocumentAnnotationProcessingContext localContext) {
		if ( isEmpty( generators ) ) {
			return;
		}

	}

	private void processHbmXml(Binding<BindableMappingDescriptor> binding) {
		throw new UnsupportedOperationException( "`hbm.xml` processing not supported" );
	}

	private void prepareClassBindings(
			JaxbEntityMappings root,
			XmlDocumentAnnotationProcessingContextImpl documentContext) {
		// todo (annotation-source) : this is an overly simplistic implementation.  a more robust
		// 		implementation would need to arrange the classes in order based on inheritance
		//		for the next processing step

		if ( isNotEmpty( root.getEmbeddables() ) ) {
			root.getEmbeddables().forEach( (mapping) -> {
				classBindingMap.put( mapping.getClazz(), new ClassBinding( mapping, documentContext ) );
			} );
		}

		if ( isNotEmpty( root.getMappedSuperclasses() ) ) {
			root.getMappedSuperclasses().forEach( (mapping) -> {
				classBindingMap.put( mapping.getClazz(), new ClassBinding( mapping, documentContext ) );
			} );
		}

		if ( isNotEmpty( root.getEntities() ) ) {
			root.getEntities().forEach( (mapping) -> {
				// todo (annotation-source)  : need to figure out how to best handle entity-name
				//		1. class-name, no entity-name : normal mapping
				//		2. entity-name, no class-name : a Hibernate dynamic (MAP mode) mapping
				//		3. class-name + entity-name : unclear -
				//			a. could be a normal JPA mapping with specific "HQL import name"
				//			b. Hibernate entity-name mapping
				//
				// for the time being, assume "normal" mappings
				classBindingMap.put( mapping.getClazz(), new ClassBinding( mapping, documentContext ) );
			} );
		}
	}

	private void processClassBindings() {
		classBindingMap.forEach( (name, classBinding) -> {
			// todo (annotation-source) : more overly simplistic stuff
			if ( classBinding.getClassNode() instanceof JaxbEntity ) {
				processEntity( (JaxbEntity) classBinding.getClassNode(), classBinding );

			}
			else {
				throw new UnsupportedOperationException( "Not yet implemented" );
			}
		} );
	}

	private void processEntity(JaxbEntity classNode, XmlClassAnnotationProcessingContext processingContext) {
		if ( !processingContext.getDocumentProcessingContext().isComplete() ) {
			throw new UnsupportedOperationException( "Not yet implemented" );
		}

		createCompleteEntity( classNode, processingContext );
	}

	private void createCompleteEntity(JaxbEntity classNode, XmlClassAnnotationProcessingContext processingContext) {
		final String className = StringHelper.qualifyConditionallyIfNot(
				processingContext.getDocumentProcessingContext().getXmlMapping().getPackage(),
				classNode.getClazz()
		);

		final String name = StringHelper.isEmpty( classNode.getName() )
				? className
				: classNode.getName();
		assert classDetailsRegistry.findManagedClass( name ) == null;

		final ClassDetailsImpl classDetails = new ClassDetailsImpl( name, className, null, processingContext );
		applyEntityAnnotation( classDetails, name, processingContext );
		applyTableAnnotation( classDetails, classNode, processingContext );
		applySecondaryTableAnnotations( classDetails, classNode, processingContext );

		processCompleteAttributes( classDetails, classNode, processingContext );
	}

	private void applyEntityAnnotation(
			ClassDetailsImpl classDetails,
			String name,
			XmlClassAnnotationProcessingContext processingContext) {
		classDetails.apply( new AnnotationUsageImpl<>(
				JpaAnnotations.ENTITY,
				classDetails,
				singletonList( new AnnotationAttributeValueImpl(
						JpaAnnotations.ENTITY.getAttribute( "name" ),
						name
				) )
		) );
	}

	private void applyTableAnnotation(
			ClassDetailsImpl classDetails,
			JaxbEntity classNode,
			XmlClassAnnotationProcessingContext processingContext) {
		if ( isNotEmpty( classNode.getTableExpression() ) ) {
			throw new UnsupportedOperationException( "Support for @Subselect not yet implemented" );
		}
		else if ( classNode.getTable() != null ) {
			// @Table
			final List<AnnotationAttributeValue> tableAttributes = new ArrayList<>();
			if ( isNotEmpty( classNode.getTable().getName() ) ) {
				tableAttributes.add( new AnnotationAttributeValueImpl(
						JpaAnnotations.TABLE.getAttribute( "name" ),
						classNode.getTable().getName()
				) );
			}
			if ( isNotEmpty( classNode.getTable().getCatalog() ) ) {
				tableAttributes.add( new AnnotationAttributeValueImpl(
						JpaAnnotations.TABLE.getAttribute( "catalog" ),
						classNode.getTable().getCatalog()
				) );
			}
			if ( isNotEmpty( classNode.getTable().getSchema() ) ) {
				tableAttributes.add( new AnnotationAttributeValueImpl(
						JpaAnnotations.TABLE.getAttribute( "schema" ),
						classNode.getTable().getSchema()
				) );
			}
			classDetails.apply( new AnnotationUsageImpl<>(
					JpaAnnotations.TABLE,
					classDetails,
					tableAttributes
			) );

			// todo (annotation-source) : indexes
			// todo (annotation-source) : unique-constraints

			// @Check
			if ( isNotEmpty( classNode.getTable().getCheck() ) ) {
				classDetails.apply( new AnnotationUsageImpl<>(
						HibernateAnnotations.CHECK,
						classDetails,
						singletonList( new AnnotationAttributeValueImpl(
								HibernateAnnotations.CHECK.getAttribute( "constraints" ),
								classNode.getTable().getCheck()
						) )
				) );
			}

			// @Comment
			if ( isNotEmpty( classNode.getTable().getComment() ) ) {
				classDetails.apply( new AnnotationUsageImpl<>(
						HibernateAnnotations.COMMENT,
						classDetails,
						singletonList( new AnnotationAttributeValueImpl(
								HibernateAnnotations.COMMENT.getValueAttribute(),
								classNode.getTable().getCheck()
						) )
				) );
			}
		}
	}

	private void applySecondaryTableAnnotations(ClassDetailsImpl classDetails, JaxbEntity classNode, XmlClassAnnotationProcessingContext processingContext) {

	}

	private void processCompleteAttributes(
			ClassDetailsImpl classDetails,
			JaxbEntity classNode,
			XmlClassAnnotationProcessingContext processingContext) {
		final AccessType classNodeAccess = classNode.getAccess();

		assert classNode.getAttributes() != null;

		if ( isNotEmpty( classNode.getAttributes().getId() ) ) {
			if ( classNode.getAttributes().getId().size() > 1 ) {
				throw new UnsupportedOperationException( "Support for composite ids not yet implemented" );
			}
			else {
				final JaxbId jaxbId = classNode.getAttributes().getId().get( 0 );
				final DynamicMemberDetails attributeMember = resolveAttributeMember(
						classDetails,
						classNodeAccess,
						jaxbId,
						processingContext
				);
				applyBasicIdentifier( jaxbId, attributeMember, classNode, processingContext );
			}
		}
		else if ( classNode.getAttributes().getEmbeddedId() != null ) {
			throw new UnsupportedOperationException( "Support for composite ids not yet implemented" );
		}
		else {
			throw new MappingException( "Entity did not define identifier" );
		}

		if ( isNotEmpty( classNode.getAttributes().getBasicAttributes() ) ) {
			classNode.getAttributes().getBasicAttributes().forEach( (attrNode) -> {
				final DynamicMemberDetails attributeMember = resolveAttributeMember(
						classDetails,
						classNodeAccess,
						attrNode,
						processingContext
				);
				processCompleteBasicAttribute( attributeMember, attrNode );
			} );
		}
	}

	private static DynamicMemberDetails resolveAttributeMember(
			ClassDetailsImpl classDetails,
			AccessType classAccessType,
			PersistentAttribute attrNode,
			XmlClassAnnotationProcessingContext processingContext) {
		final AccessType accessType = attrNode.getAccess() == null
				? classAccessType
				: attrNode.getAccess();

		if ( accessType == AccessType.FIELD ) {
			final FieldDetailsImpl fieldDetails = new FieldDetailsImpl( attrNode.getName(), processingContext );
			classDetails.addField( fieldDetails );
			return fieldDetails;
		}
		else {
			assert accessType == AccessType.PROPERTY;
			// todo (annotation-source) : figure out the proper attribute name (the `null`)
			//  	- but how?  that requires access to the Class to know for sure
			final MethodDetailsImpl methodDetails = new MethodDetailsImpl( null, attrNode.getName(), processingContext );
			classDetails.addMethod( methodDetails );
			return methodDetails;
		}
	}

	private void applyBasicIdentifier(
			JaxbId jaxbId,
			DynamicMemberDetails attributeMember,
			JaxbEntity classNode,
			XmlClassAnnotationProcessingContext processingContext) {
		attributeMember.apply( new AnnotationUsageImpl<>( JpaAnnotations.ID, attributeMember, Collections.emptyMap() ) );
		attributeMember.apply( new AnnotationUsageImpl<>( JpaAnnotations.BASIC, attributeMember, Collections.emptyMap() ) );
		processPersistentAttribute( attributeMember, jaxbId );

		// todo (annotation-source) : do the others
	}

	private void processCompleteBasicAttribute(DynamicMemberDetails attributeMember, JaxbBasic jaxbBasic) {
		final AnnotationUsage<Basic> basicAnnotation = createBasicAnnotation( jaxbBasic, attributeMember );
		attributeMember.apply( basicAnnotation );

		processPersistentAttribute( attributeMember, jaxbBasic );

		if ( jaxbBasic.isOptimisticLock() ) {
			final Map<String, AnnotationAttributeValue> valueMap = Collections.singletonMap(
					"excluded",
					new AnnotationAttributeValueImpl(
							HibernateAnnotations.OPTIMISTIC_LOCK.getAttribute( "excluded" ),
							!jaxbBasic.isOptimisticLock()
					)
			);
			attributeMember.apply( new AnnotationUsageImpl<>( HibernateAnnotations.OPTIMISTIC_LOCK, attributeMember, valueMap ) );
		}

		if ( jaxbBasic.getColumn() != null ) {
			throw new UnsupportedOperationException( "Not yet implemented" );
		}
		else if ( jaxbBasic.getFormula() != null ) {
			throw new UnsupportedOperationException( "Not yet implemented" );
		}

		if ( jaxbBasic.getConvert() != null ) {
			throw new UnsupportedOperationException( "Not yet implemented" );
		}

		if ( jaxbBasic.getEnumerated() != null ) {
			attributeMember.apply( new AnnotationUsageImpl<>(
					JpaAnnotations.ENUMERATED,
					attributeMember,
					singletonList( new AnnotationAttributeValueImpl(
							JpaAnnotations.ENUMERATED.getValueAttribute(),
							jaxbBasic.getEnumerated()
					) )
			) );
		}

		if ( jaxbBasic.getLob() != null ) {
			attributeMember.apply( new AnnotationUsageImpl<>(
					JpaAnnotations.LOB,
					attributeMember,
					Collections.emptyMap()
			) );
		}

		if ( jaxbBasic.getNationalized() != null ) {
			attributeMember.apply( new AnnotationUsageImpl<>(
					HibernateAnnotations.NATIONALIZED,
					attributeMember,
					Collections.emptyMap()
			) );
		}
	}

	private void processPersistentAttribute(DynamicMemberDetails attributeMember, PersistentAttribute jaxbNode) {
		if ( jaxbNode.getAccess() != null ) {
			attributeMember.apply( new AnnotationUsageImpl<>(
					JpaAnnotations.ACCESS,
					attributeMember,
					singletonList(
							new AnnotationAttributeValueImpl( JpaAnnotations.ACCESS.getValueAttribute(), jaxbNode.getAccess() )
					)
			) );
		}

		final String attributeAccessor = jaxbNode.getAttributeAccessor();
		if ( isNotEmpty( attributeAccessor ) ) {
			attributeMember.apply( new AnnotationUsageImpl<>(
					HibernateAnnotations.ATTRIBUTE_ACCESSOR,
					attributeMember,
					singletonList(
							new AnnotationAttributeValueImpl(
									JpaAnnotations.ACCESS.getAttribute( "strategy" ),
									classDetailsRegistry.resolveManagedClass( attributeAccessor )
							)
					)
			) );
		}
	}

	private AnnotationUsage<Basic> createBasicAnnotation(JaxbBasic jaxbBasic, MemberDetails target) {
		final Map<String, AnnotationAttributeValue> valueMap = new HashMap<>();

		if ( jaxbBasic.getFetch() != null ) {
			valueMap.put(
					"fetch",
					new AnnotationAttributeValueImpl( JpaAnnotations.BASIC.getAttribute( "fetch" ), jaxbBasic.getFetch() )
			);
		}

		if ( jaxbBasic.isOptional() == TRUE ) {
			valueMap.put(
					"optional",
					new AnnotationAttributeValueImpl( JpaAnnotations.BASIC.getAttribute( "optional" ), jaxbBasic.getFetch() )
			);
		}

		return new AnnotationUsageImpl<>( JpaAnnotations.BASIC, target, valueMap );
	}
}
