/*
 * Hibernate, Relational Persistence for Idiomatic Java
 *
 * License: GNU Lesser General Public License (LGPL), version 2.1 or later.
 * See the lgpl.txt file in the root directory or <http://www.gnu.org/licenses/lgpl-2.1.html>.
 */
package org.hibernate.boot.model.source.internal.annotations.metadata.attribute;

import java.util.EnumSet;
import java.util.Map;
import javax.persistence.AccessType;

import org.hibernate.boot.jandex.spi.HibernateDotNames;
import org.hibernate.boot.jandex.spi.JpaDotNames;
import org.hibernate.boot.model.MemberDescriptor;
import org.hibernate.boot.model.source.internal.annotations.metadata.type.EntityBindingContext;
import org.hibernate.boot.model.source.internal.annotations.metadata.type.ManagedTypeMetadata;
import org.hibernate.boot.model.source.internal.annotations.util.AnnotationBindingHelper;
import org.hibernate.boot.model.source.spi.AttributePath;
import org.hibernate.boot.model.source.spi.AttributeRole;
import org.hibernate.cfg.NotYetImplementedException;
import org.hibernate.internal.util.StringHelper;

import org.jboss.jandex.AnnotationInstance;
import org.jboss.jandex.DotName;
import org.jboss.logging.Logger;

/**
 * @author Steve Ebersole
 */
public class AttributeFactory {
	private static final Logger LOG = Logger.getLogger( AttributeFactory.class );

	public static PersistentAttribute buildAttribute(
			ManagedTypeMetadata container,
			MemberDescriptor member,
			EntityBindingContext bindingContext) {
		return new AttributeFactory( container, member, bindingContext )
				.createPersistentAttribute( container );
	}

	// passed state
	private final MemberDescriptor member;
	private final AccessType accessType;
	private final EntityBindingContext bindingContext;

	// work state
	private final PersistentAttribute.AttributeNature attributeNature;
	private final AttributePath attributePath;
	private final AttributeRole attributeRole;
	private final Map<DotName,AnnotationInstance> memberAnnotationMap;
	private final String accessorStrategy;

	private AttributeFactory(
			ManagedTypeMetadata container,
			MemberDescriptor member,
			EntityBindingContext bindingContext) {
		this.member = member;
		this.bindingContext = bindingContext;

		this.attributePath = container.getAttributePathBase().append( member.attributeName() );
		this.attributeRole = container.getAttributeRoleBase().append( member.attributeName() );

		this.memberAnnotationMap = bindingContext.getMemberAnnotationInstances( member );

		this.accessType = determineAttributeAccessType( container.getClassLevelAccessType() );
		this.accessorStrategy = determineAttributeLevelAccessorStrategy();
		this.attributeNature = determineAttributeNature();
	}

	private AccessType determineAttributeAccessType(AccessType classLevelAccessType) {
		final AnnotationInstance explicitAccessAnnotation = memberAnnotationMap.get( JpaDotNames.ACCESS );
		if ( explicitAccessAnnotation != null ) {
			return bindingContext.getTypedValueExtractor( AccessType.class )
					.extract( explicitAccessAnnotation, "value" );
		}

		return classLevelAccessType;
	}

	private String determineAttributeLevelAccessorStrategy() {
		// first and foremost, does the attribute define a local accessor strategy
		final AnnotationInstance attributeAccessorAnnotation = memberAnnotationMap.get( HibernateDotNames.ATTRIBUTE_ACCESSOR );
		if ( attributeAccessorAnnotation != null ) {
			String explicitAccessorStrategy = attributeAccessorAnnotation.value().asString();
			if ( StringHelper.isEmpty( explicitAccessorStrategy ) ) {
				LOG.warnf( "Attribute [%s] specified @AttributeAccessor with empty value", member );
			}
			else {
				return explicitAccessorStrategy;
			}
		}

		// finally use the attribute AccessType as default...
		return accessType.name().toLowerCase();
	}

	private PersistentAttribute.AttributeNature determineAttributeNature() {
		final EnumSet<PersistentAttribute.AttributeNature> natures = EnumSet.noneOf( PersistentAttribute.AttributeNature.class );

		// ~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~
		// first, look for explicit nature annotations

		final AnnotationInstance basic = memberAnnotationMap.get( JpaDotNames.BASIC );
		if ( basic != null ) {
			natures.add( PersistentAttribute.AttributeNature.BASIC );
		}

		final AnnotationInstance embeddedId = memberAnnotationMap.get( JpaDotNames.EMBEDDED_ID );
		final AnnotationInstance embedded = memberAnnotationMap.get( JpaDotNames.EMBEDDED );
		if ( embeddedId != null || embedded != null ) {
			natures.add( PersistentAttribute.AttributeNature.EMBEDDED );
		}

		final AnnotationInstance any = memberAnnotationMap.get( HibernateDotNames.ANY );
		if ( any != null ) {
			natures.add( PersistentAttribute.AttributeNature.ANY );
		}

		final AnnotationInstance oneToOne = memberAnnotationMap.get( JpaDotNames.ONE_TO_ONE );
		final AnnotationInstance manyToOne = memberAnnotationMap.get( JpaDotNames.MANY_TO_ONE );
		if ( oneToOne != null || manyToOne != null ) {
			natures.add( PersistentAttribute.AttributeNature.TO_ONE );
		}

		final AnnotationInstance oneToMany = memberAnnotationMap.get( JpaDotNames.ONE_TO_MANY );
		final AnnotationInstance manyToMany = memberAnnotationMap.get( JpaDotNames.MANY_TO_MANY );
		final AnnotationInstance elementCollection = memberAnnotationMap.get( JpaDotNames.ELEMENT_COLLECTION );
		if ( oneToMany != null || manyToMany != null || elementCollection != null ) {
			natures.add( PersistentAttribute.AttributeNature.PLURAL );
		}

		final AnnotationInstance manyToAny = memberAnnotationMap.get( HibernateDotNames.MANY_TO_ANY );
		if ( manyToAny != null ) {
			natures.add( PersistentAttribute.AttributeNature.PLURAL );
		}


		// For backward compatibility, we're allowing attributes of an
		// @Embeddable type to leave off @Embedded.  Check the type's
		// annotations.  (see HHH-7678)
		// However, it's important to ignore this if the field is
		// annotated with @EmbeddedId.
		if ( embedded == null && embeddedId == null ) {
			if ( AnnotationBindingHelper.isEmbeddableType( member.type(), bindingContext ) ) {
				LOG.warnf(
						"Class %s was annotated as @Embeddable. However a persistent attribute [%s] " +
								"of this type was found that did not contain the @Embedded (or @EmbeddedId) annotation.  " +
								"This may cause compatibility issues",
						bindingContext.getLoggableContextName(),
						attributeRole.toString()
				);
				natures.add( PersistentAttribute.AttributeNature.EMBEDDED );
			}
		}


		// ~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~
		// then we look at annotations that can be used to infer natures

		if ( memberAnnotationMap.containsKey( JpaDotNames.TEMPORAL )
				|| memberAnnotationMap.containsKey( JpaDotNames.LOB )
				|| memberAnnotationMap.containsKey( JpaDotNames.ENUMERATED )
				|| memberAnnotationMap.containsKey( HibernateDotNames.TYPE ) ) {
			// technically these could describe the elements of a "element collection"
			// but without requiring the @ElementCollection annotation we
			// run into problems mapping things like our "materialized LOB"
			// support where @Lob might be combined with an array
			//
			// All in all, supporting that inference would require a lot of checks.
			// Not sure its worth the effort.  For future reference the checks would
			// be along the lines of:
			// 		in order for this to indicate a persistent (element) collection
			//		we'd have to unequivocally know the collection element type (or
			//		array component type) and that type would need to be consistent
			//		with the inferred type.  For example, given a collection marked
			// 		@Lob, in order for us to interpret that as indicating a
			//		LOB-based ElementCollection we would need to be able to verify
			//		that the elements of the Collection are in fact Lobs.
			if ( elementCollection == null ) {
				natures.add( PersistentAttribute.AttributeNature.BASIC );
			}
		}

		if ( memberAnnotationMap.containsKey( HibernateDotNames.COLLECTION_ID )
				|| memberAnnotationMap.containsKey( HibernateDotNames.COLLECTION_TYPE )
				|| memberAnnotationMap.containsKey( HibernateDotNames.LIST_INDEX_BASE )
				|| memberAnnotationMap.containsKey( HibernateDotNames.MAP_KEY_TYPE )
				|| memberAnnotationMap.containsKey( JpaDotNames.MAP_KEY )
				|| memberAnnotationMap.containsKey( JpaDotNames.MAP_KEY_CLASS )
				|| memberAnnotationMap.containsKey( JpaDotNames.MAP_KEY_COLUMN )
				|| memberAnnotationMap.containsKey( JpaDotNames.MAP_KEY_JOIN_COLUMN )
				|| memberAnnotationMap.containsKey( JpaDotNames.MAP_KEY_JOIN_COLUMNS ) ) {
			natures.add( PersistentAttribute.AttributeNature.PLURAL );
		}

		// todo : other "inferences"?

		int size = natures.size();
		switch ( size ) {
			case 0: {
				return PersistentAttribute.AttributeNature.BASIC;
			}
			case 1: {
				return natures.iterator().next();
			}
			default: {
				throw bindingContext.makeMappingException(
						"Attribute [" + member.attributeName() + "] resolved to multiple natures : " +
								natures.toString()
				);
			}
		}
	}

	private PersistentAttribute createPersistentAttribute(ManagedTypeMetadata managedTypeMetadata) {
		switch ( attributeNature ) {
			case BASIC: {
				return new BasicAttribute(
						managedTypeMetadata,
						member.attributeName(),
						attributePath,
						attributeRole,
						member,
						accessType,
						accessorStrategy
				);
			}
			case EMBEDDED: {
				// NOTE that this models the Embedded, not the Embeddable!
				return new EmbeddedAttribute(
						managedTypeMetadata,
						member.attributeName(),
						attributePath,
						attributeRole,
						member,
						accessType,
						accessorStrategy
				);
			}
			case TO_ONE: {
				return new SingularAssociationAttribute(
						managedTypeMetadata,
						member.attributeName(),
						attributePath,
						attributeRole,
						member,
						accessType,
						accessorStrategy
				);
			}
			case PLURAL: {
				return new PluralAttribute(
						managedTypeMetadata,
						member.attributeName(),
						attributePath,
						attributeRole,
						member,
						accessType,
						accessorStrategy
				);
			}
			case ANY: {
				throw new NotYetImplementedException( "ANY mappings not yet implemented" );
			}
		}

		return null;
	}

}
