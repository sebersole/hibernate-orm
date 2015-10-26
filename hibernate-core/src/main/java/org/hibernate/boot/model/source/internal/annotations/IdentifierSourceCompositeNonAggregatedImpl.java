/*
 * Hibernate, Relational Persistence for Idiomatic Java
 *
 * License: GNU Lesser General Public License (LGPL), version 2.1 or later.
 * See the lgpl.txt file in the root directory or <http://www.gnu.org/licenses/lgpl-2.1.html>.
 */
package org.hibernate.boot.model.source.internal.annotations;

import java.util.List;

import org.hibernate.boot.model.process.jandex.spi.JpaDotNames;
import org.hibernate.boot.model.JavaTypeDescriptor;
import org.hibernate.boot.model.MemberDescriptor;
import org.hibernate.boot.model.source.internal.annotations.metadata.attribute.AssociationOverride;
import org.hibernate.boot.model.source.internal.annotations.metadata.attribute.AttributeOverride;
import org.hibernate.boot.model.source.internal.annotations.metadata.attribute.EmbeddedContainer;
import org.hibernate.boot.model.source.spi.IdentifierGenerationInformation;
import org.hibernate.boot.model.source.internal.annotations.metadata.type.EmbeddableTypeMetadata;
import org.hibernate.boot.model.source.internal.annotations.util.AttributeSourceBuildingHelper;
import org.hibernate.boot.model.source.spi.AnnotationAttributeSource;
import org.hibernate.boot.model.source.spi.AttributePath;
import org.hibernate.boot.model.source.spi.AttributeRole;
import org.hibernate.boot.model.source.spi.AttributeSource;
import org.hibernate.boot.model.source.spi.EmbeddableSource;
import org.hibernate.boot.model.source.spi.IdentifierSourceCompositeNonAggregated;
import org.hibernate.boot.model.source.spi.LocalMetadataBuildingContext;
import org.hibernate.boot.model.source.spi.NaturalIdMutability;
import org.hibernate.boot.model.source.spi.SingularAttributeSource;
import org.hibernate.id.EntityIdentifierNature;

import org.jboss.jandex.AnnotationInstance;
import org.jboss.jandex.ClassInfo;
import org.jboss.logging.Logger;

/**
* @author Steve Ebersole
*/
class IdentifierSourceCompositeNonAggregatedImpl
		extends AbstractIdentifierSource
		implements IdentifierSourceCompositeNonAggregated {
	private static final Logger log = Logger.getLogger( IdentifierSourceCompositeNonAggregatedImpl.class );

	private final List<SingularAttributeSource> idAttributeSources;
	private final IdClassSource idClassSource;

	public IdentifierSourceCompositeNonAggregatedImpl(RootEntitySourceImpl rootEntitySource) {
		super( rootEntitySource );

		this.idAttributeSources = rootEntitySource.getIdentifierAttributes();

		final ClassInfo idClassClassInfo = resolveIdClassDescriptor();
		if ( idClassClassInfo == null ) {
			log.warnf(
					"Encountered non-aggregated identifier with no IdClass specified; while this is supported, " +
							"its use should be considered deprecated"
			);
			this.idClassSource = null;
		}
		else {
			this.idClassSource = new IdClassSource( rootEntitySource, idClassClassInfo );
		}	}

	private ClassInfo resolveIdClassDescriptor() {
		final AnnotationInstance idClassAnnotation = rootEntitySource().getIdentifiableTypeMetadata()
				.typeAnnotationMap()
				.get( JpaDotNames.ID_CLASS );

		if ( idClassAnnotation == null ) {
			return null;
		}

		if ( idClassAnnotation.value() == null ) {
			return null;
		}

		return rootEntitySource().getLocalBindingContext().getJandexIndex().getClassByName(
				idClassAnnotation.value().asClass().name()
		);
	}

	@Override
	public List<SingularAttributeSource> getAttributeSourcesMakingUpIdentifier() {
		return idAttributeSources;
	}

	@Override
	public EmbeddableSource getIdClassSource() {
		return idClassSource;
	}

	@Override
	public IdentifierGenerationInformation getIndividualAttributeIdentifierGenerationInformation(String identifierAttributeName) {
		// for now, return null.  this is that stupid specj bs
		return null;
	}

	@Override
	public IdentifierGenerationInformation getIdentifierGenerationInformation() {
		// annotations do not currently allow generators to be attached to composite identifiers as a whole
		return null;
	}

	@Override
	public EntityIdentifierNature getNature() {
		return EntityIdentifierNature.NON_AGGREGATED_COMPOSITE;
	}

	private class IdClassSource implements EmbeddableSource, EmbeddedContainer {
		private final ClassInfo idClassDescriptor;
		private final EmbeddableTypeMetadata idClassTypeMetadata;

		private final AttributeRole attributeRoleBase;
		private final AttributePath attributePathBase;

		private List<AttributeSource> attributeSources;

		private IdClassSource(RootEntitySourceImpl rootEntitySource, ClassInfo idClassDescriptor) {
			this.idClassDescriptor = idClassDescriptor;

			this.attributeRoleBase = rootEntitySource.getAttributeRoleBase().append( "<IdClass>" );
			this.attributePathBase = rootEntitySource.getAttributePathBase().append( "<IdClass>" );

			final AnnotationAttributeSource firstIdAttribute = rootEntitySource.getIdentifierAttributes().get( 0 );

			this.idClassTypeMetadata = new EmbeddableTypeMetadata(
					idClassDescriptor,
					this,
					attributeRoleBase,
					attributePathBase,
					firstIdAttribute.getAnnotatedAttribute().getAccessType(),
					null,
					rootEntitySource.getLocalBindingContext().getRootAnnotationBindingContext()
			);

			// todo : locate MapsId annotations and build a specialized AttributeBuilder

			this.attributeSources = AttributeSourceBuildingHelper.buildAttributeSources(
					idClassTypeMetadata,
					AttributeSourceBuildingHelper.IdentifierPathAttributeBuilder.INSTANCE
			);

			if ( log.isDebugEnabled() ) {
				String attributeDescriptors = null;
				for ( AttributeSource attributeSource : attributeSources ) {
					if ( attributeDescriptors == null ) {
						attributeDescriptors = attributeSource.getName();
					}
					else {
						attributeDescriptors += ", " + attributeSource.getName();
					}
				}
				log.debugf(
						"Built IdClassSource : %s : %s",
						idClassTypeMetadata.getClassInfo().name().toString(),
						attributeDescriptors
				);
			}

			// todo : validate the IdClass attributes against the entity's id attributes

			// todo : we need similar (MapsId, validation) in the EmbeddedId case too.
		}

		@Override
		public JavaTypeDescriptor getTypeDescriptor() {
			return new JavaTypeDescriptorImpl( idClassDescriptor );
		}

		@Override
		public String getParentReferenceAttributeName() {
			return null;
		}

		@Override
		public String getTuplizerImplementationName() {
			return idClassTypeMetadata.getCustomTuplizerClassName();
		}

		@Override
		public boolean isDynamic() {
			return false;
		}

		@Override
		public boolean isUnique() {
			return false;
		}

		@Override
		public AttributePath getAttributePathBase() {
			return attributePathBase;
		}

		@Override
		public AttributeRole getAttributeRoleBase() {
			return attributeRoleBase;
		}

		@Override
		public List<AttributeSource> attributeSources() {
			return attributeSources;
		}

		@Override
		public LocalMetadataBuildingContext getLocalMetadataBuildingContext() {
			return idClassTypeMetadata.getLocalBindingContext();
		}

		// EmbeddedContainer impl (most of which we don't care about here

		@Override
		public MemberDescriptor getBackingMember() {
			return null;
		}

		@Override
		public ConvertConversionInfo locateConversionInfo(AttributePath attributePath) {
			return null;
		}

		@Override
		public AttributeOverride locateAttributeOverride(AttributePath attributePath) {
			return null;
		}

		@Override
		public AssociationOverride locateAssociationOverride(
				AttributePath attributePath) {
			return null;
		}

		@Override
		public NaturalIdMutability getContainerNaturalIdMutability() {
			return null;
		}

		@Override
		public boolean getContainerOptionality() {
			return false;
		}

		@Override
		public boolean getContainerUpdatability() {
			return false;
		}

		@Override
		public boolean getContainerInsertability() {
			return false;
		}

		@Override
		public void registerConverter(
				AttributePath attributePath, ConvertConversionInfo conversionInfo) {

		}

		@Override
		public void registerAttributeOverride(
				AttributePath attributePath, AttributeOverride override) {

		}

		@Override
		public void registerAssociationOverride(
				AttributePath attributePath, AssociationOverride override) {

		}
	}


}
