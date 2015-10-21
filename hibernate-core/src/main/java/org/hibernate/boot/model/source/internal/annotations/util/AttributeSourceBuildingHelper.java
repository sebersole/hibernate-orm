/*
 * Hibernate, Relational Persistence for Idiomatic Java
 *
 * License: GNU Lesser General Public License (LGPL), version 2.1 or later.
 * See the lgpl.txt file in the root directory or <http://www.gnu.org/licenses/lgpl-2.1.html>.
 */
package org.hibernate.boot.model.source.internal.annotations.util;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

import org.hibernate.AssertionFailure;
import org.hibernate.boot.model.source.internal.annotations.AttributeSource;
import org.hibernate.boot.model.source.internal.annotations.MapsIdSource;
import org.hibernate.boot.model.source.internal.annotations.PluralAttributeSource;
import org.hibernate.boot.model.source.internal.annotations.SingularAttributeSource;
import org.hibernate.boot.model.source.internal.annotations.SingularAttributeSourceAny;
import org.hibernate.boot.model.source.internal.annotations.SingularAttributeSourceBasic;
import org.hibernate.boot.model.source.internal.annotations.SingularAttributeSourceEmbedded;
import org.hibernate.boot.model.source.internal.annotations.SingularAttributeSourceToOne;
import org.hibernate.boot.model.source.internal.annotations.impl.PluralAttributeSourceImpl;
import org.hibernate.boot.model.source.internal.annotations.impl.SingularAttributeSourceBasicImpl;
import org.hibernate.boot.model.source.internal.annotations.impl.SingularAttributeSourceEmbeddedImpl;
import org.hibernate.boot.model.source.internal.annotations.impl.SingularAttributeSourceManyToOneImpl;
import org.hibernate.boot.model.source.internal.annotations.metadata.attribute.BasicAttribute;
import org.hibernate.boot.model.source.internal.annotations.metadata.attribute.EmbeddedAttribute;
import org.hibernate.boot.model.source.internal.annotations.metadata.attribute.OverrideAndConverterCollector;
import org.hibernate.boot.model.source.internal.annotations.metadata.attribute.PersistentAttribute;
import org.hibernate.boot.model.source.internal.annotations.metadata.attribute.PluralAttribute;
import org.hibernate.boot.model.source.internal.annotations.metadata.attribute.SingularAssociationAttribute;
import org.hibernate.boot.model.source.internal.annotations.metadata.type.EntityTypeMetadata;
import org.hibernate.boot.model.source.internal.annotations.metadata.type.IdentifiableTypeMetadata;
import org.hibernate.boot.model.source.internal.annotations.metadata.type.ManagedTypeMetadata;
import org.hibernate.boot.model.source.internal.annotations.metadata.type.MappedSuperclassTypeMetadata;
import org.hibernate.boot.model.source.internal.annotations.metadata.type.RootEntityTypeMetadata;
import org.hibernate.cfg.NotYetImplementedException;
import org.hibernate.internal.CoreLogging;
import org.hibernate.internal.CoreMessageLogger;
import org.hibernate.internal.util.StringHelper;

import org.jboss.jandex.AnnotationValue;

/**
 * Utilities for building attribute source objects.
 *
 * @author Steve Ebersole
 * @author Strong Liu
 */
public class AttributeSourceBuildingHelper {
	private static final CoreMessageLogger LOG = CoreLogging.messageLogger( AttributeSourceBuildingHelper.class );

	// todo : the walking supers bits here is due to the total lack of understanding of MappedSuperclasses in Binder...

	public static List<AttributeSource> buildAttributeSources(
			ManagedTypeMetadata managedTypeMetadata,
			AttributeBuilder attributeBuilder) {
		final List<AttributeSource> result = new ArrayList<AttributeSource>();

		if ( EntityTypeMetadata.class.isInstance( managedTypeMetadata ) ) {
			final EntityTypeMetadata entityTypeMetadata = (EntityTypeMetadata) managedTypeMetadata;

			IdentifiableTypeMetadata currentSuperType = entityTypeMetadata.getSuperType();

			while ( currentSuperType != null && MappedSuperclassTypeMetadata.class.isInstance( currentSuperType ) ) {
				collectAttributeSources( result, currentSuperType, entityTypeMetadata, attributeBuilder );

				currentSuperType = currentSuperType.getSuperType();
			}
		}

		collectAttributeSources( result, managedTypeMetadata, managedTypeMetadata, attributeBuilder );

		return result;
	}

	private static void collectAttributeSources(
			List<AttributeSource> result,
			ManagedTypeMetadata managedTypeMetadata,
			OverrideAndConverterCollector overrideAndConverterCollector,
			AttributeBuilder attributeBuilder) {
		for ( PersistentAttribute attribute : managedTypeMetadata.getPersistentAttributeMap().values() ) {
			switch ( attribute.getAttributeNature() ) {
				case BASIC: {
					result.add(
							attributeBuilder.buildBasicAttribute(
									(BasicAttribute) attribute,
									overrideAndConverterCollector
							)
					);
					break;
				}
				case EMBEDDED: {
					result.add(
							attributeBuilder.buildEmbeddedAttribute(
									(EmbeddedAttribute) attribute,
									overrideAndConverterCollector
							)
					);
					break;
				}
				case TO_ONE: {
					result.add(
							attributeBuilder.buildToOneAttribute(
									(SingularAssociationAttribute) attribute,
									overrideAndConverterCollector
							)
					);
					break;
				}
				case PLURAL: {
					result.add(
							attributeBuilder.buildPluralAttribute(
									(PluralAttribute) attribute,
									overrideAndConverterCollector
							)
					);
					break;
				}
				case ANY: {
					result.add(
							attributeBuilder.buildAnyAttribute(
									attribute,
									overrideAndConverterCollector
							)
					);
					break;
				}
				default: {
					throw managedTypeMetadata.getLocalBindingContext().makeMappingException(
							"Unexpected PersistentAttribute nature encountered : " + attribute.getAttributeNature()
					);
				}
			}
		}
	}

	public static List<SingularAttributeSource> buildIdentifierAttributeSources(
			RootEntityTypeMetadata rootEntityTypeMetadata,
			AttributeBuilder attributeBuilder) {
		final List<SingularAttributeSource> result = new ArrayList<SingularAttributeSource>();

// we already specially collect identifier attributes
//		IdentifiableTypeMetadata currentSuperType = rootEntityTypeMetadata.getSuperType();
//		while ( currentSuperType != null && MappedSuperclassTypeMetadata.class.isInstance( currentSuperType ) ) {
//			collectIdentifierAttributeSources( result, currentSuperType, rootEntityTypeMetadata, attributeBuilder );
//
//			currentSuperType = currentSuperType.getSuperType();
//		}

		collectIdentifierAttributeSources( result, rootEntityTypeMetadata, rootEntityTypeMetadata, attributeBuilder );

		return result;
	}

	private static void collectIdentifierAttributeSources(
			List<SingularAttributeSource> result,
			IdentifiableTypeMetadata identifiableTypeMetadata,
			OverrideAndConverterCollector overrideAndConverterCollector,
			AttributeBuilder attributeBuilder) {
		for ( PersistentAttribute attribute : identifiableTypeMetadata.getIdentifierAttributes() ) {
			switch ( attribute.getAttributeNature() ) {
				case BASIC: {
					result.add(
							attributeBuilder.buildBasicAttribute(
									(BasicAttribute) attribute,
									overrideAndConverterCollector
							)
					);
					break;
				}
				case EMBEDDED: {
					result.add(
							attributeBuilder.buildEmbeddedAttribute(
									(EmbeddedAttribute) attribute,
									overrideAndConverterCollector
							)
					);
					break;
				}
				case TO_ONE: {
					result.add(
							attributeBuilder.buildToOneAttribute(
									(SingularAssociationAttribute) attribute,
									overrideAndConverterCollector
							)
					);
					break;
				}
				case PLURAL: {
					throw identifiableTypeMetadata.getLocalBindingContext().makeMappingException(
							"Plural attribute cannot be part of identifier : " + attribute.getBackingMember().toString()
					);
				}
				case ANY: {
					throw identifiableTypeMetadata.getLocalBindingContext().makeMappingException(
							"Hibernate ANY mapping cannot be part of identifier : " + attribute.getBackingMember().toString()
					);
				}
				default: {
					throw identifiableTypeMetadata.getLocalBindingContext().makeMappingException(
							"Unexpected PersistentAttribute nature encountered : " + attribute.getAttributeNature()
					);
				}
			}
		}
	}

	public static List<MapsIdSource> buildMapsIdSources(
			RootEntityTypeMetadata entityTypeMetadata,
			IdentifierPathAttributeBuilder attributeBuilder) {
		final List<MapsIdSource> result = new ArrayList<MapsIdSource>();
		for ( final SingularAssociationAttribute attribute : entityTypeMetadata.getMapsIdAttributes() ) {
			final SingularAttributeSourceToOne attributeSource = attributeBuilder.buildToOneAttribute(
					attribute,
					entityTypeMetadata
			);

			final AnnotationValue mapsIdNameValue = attribute.getMapsIdAnnotation().value();
			final String mappedIdAttributeName = mapsIdNameValue == null
					? null
					: StringHelper.nullIfEmpty( mapsIdNameValue.asString() );
			result.add(
					new MapsIdSource() {
						@Override
						public String getMappedIdAttributeName() {
							return mappedIdAttributeName;
						}

						@Override
						public SingularAttributeSourceToOne getAssociationAttributeSource() {
							return attributeSource;
						}
					}
			);
		}
		return result;
	}

	public static interface AttributeBuilder {
		SingularAttributeSourceBasic buildBasicAttribute(
				BasicAttribute attribute,
				OverrideAndConverterCollector overrideAndConverterCollector);

		SingularAttributeSourceEmbedded buildEmbeddedAttribute(
				EmbeddedAttribute attribute,
				OverrideAndConverterCollector overrideAndConverterCollector);

		SingularAttributeSourceToOne buildToOneAttribute(
				SingularAssociationAttribute attribute,
				OverrideAndConverterCollector overrideAndConverterCollector);

		PluralAttributeSource buildPluralAttribute(
				PluralAttribute attribute,
				OverrideAndConverterCollector overrideAndConverterCollector);

		SingularAttributeSourceAny buildAnyAttribute(
				PersistentAttribute attribute,
				OverrideAndConverterCollector overrideAndConverterCollector);

		PluralAttributeSource buildManyToAnyAttribute(
				PersistentAttribute attribute,
				OverrideAndConverterCollector overrideAndConverterCollector);
	}


	public static class StandardAttributeBuilder implements AttributeBuilder {
		/**
		 * Singleton access
		 */
		public static final StandardAttributeBuilder INSTANCE = new StandardAttributeBuilder();

		@Override
		public SingularAttributeSourceBasic buildBasicAttribute(
				BasicAttribute attribute,
				OverrideAndConverterCollector overrideAndConverterCollector) {
			return new SingularAttributeSourceBasicImpl( attribute, overrideAndConverterCollector );
		}

		@Override
		public SingularAttributeSourceEmbedded buildEmbeddedAttribute(
				EmbeddedAttribute attribute,
				OverrideAndConverterCollector overrideAndConverterCollector) {
			return new SingularAttributeSourceEmbeddedImpl( attribute, false, false );
		}

		@Override
		public SingularAttributeSourceToOne buildToOneAttribute(
				SingularAssociationAttribute attribute,
				OverrideAndConverterCollector overrideAndConverterCollector) {
			if ( attribute.getMappedByAttributeName() == null ) {
				if ( attribute.getToOneNature() == SingularAssociationAttribute.ToOneNature.MANY_TO_ONE ) {
					return new SingularAttributeSourceManyToOneImpl( attribute, overrideAndConverterCollector );
				}
//				else {
//					return new SingularAttributeSourceOneToOneImpl( attribute, overrideAndConverterCollector );
//				}
			}
//			else {
//				return new ToOneMappedByAttributeSourceImpl( attribute, overrideAndConverterCollector );
//			}

			throw new NotYetImplementedException( "not yet implemented" );
		}

		@Override
		public PluralAttributeSource buildPluralAttribute(
				PluralAttribute attribute,
				OverrideAndConverterCollector overrideAndConverterCollector) {
			switch ( attribute.getCollectionNature() ) {
				case BAG:
				case SET: {
					return new PluralAttributeSourceImpl( attribute, overrideAndConverterCollector );
				}
				case ID_BAG: {
					throw new NotYetImplementedException( "not yet implemented" );
//					return new PluralAttributeIdBagSourceImpl( attribute, overrideAndConverterCollector );
				}
				case MAP: {
					throw new NotYetImplementedException( "not yet implemented" );
//					return new PluralAttributeMapSourceImpl( attribute, overrideAndConverterCollector );
				}
				case ARRAY:
				case LIST: {
					throw new NotYetImplementedException( "not yet implemented" );
//					return new PluralAttributeIndexedSourceImpl( attribute, overrideAndConverterCollector );
				}
				default: {
					throw new AssertionFailure(
							String.format(
									Locale.ENGLISH,
									"Unknown or not-yet-supported plural attribute CollectionNature: %s",
									attribute.getCollectionNature()
							)
					);
				}
			}
		}

		@Override
		public SingularAttributeSourceAny buildAnyAttribute(
				PersistentAttribute attribute,
				OverrideAndConverterCollector overrideAndConverterCollector) {
			throw new NotYetImplementedException();
		}

		@Override
		public PluralAttributeSource buildManyToAnyAttribute(
				PersistentAttribute attribute,
				OverrideAndConverterCollector overrideAndConverterCollector) {
			throw new NotYetImplementedException();
		}
	}

	public static class PluralAttributesDisallowedAttributeBuilder extends StandardAttributeBuilder {
		/**
		 * Singleton access
		 */
		public static final PluralAttributesDisallowedAttributeBuilder INSTANCE = new PluralAttributesDisallowedAttributeBuilder();

		@Override
		public SingularAttributeSourceEmbedded buildEmbeddedAttribute(
				EmbeddedAttribute attribute,
				OverrideAndConverterCollector overrideAndConverterCollector) {
			return new SingularAttributeSourceEmbeddedImpl( attribute, false, true );
		}

		@Override
		public PluralAttributeSource buildPluralAttribute(
				PluralAttribute attribute,
				OverrideAndConverterCollector overrideAndConverterCollector) {
			throw attribute.getContext().makeMappingException(
					"Plural attributes not allowed in this context : " + attribute.getBackingMember().toString()
			);
		}

		@Override
		public PluralAttributeSource buildManyToAnyAttribute(
				PersistentAttribute attribute,
				OverrideAndConverterCollector overrideAndConverterCollector) {
			throw attribute.getContext().makeMappingException(
					"Plural attributes not allowed in this context : " + attribute.getBackingMember().toString()
			);
		}
	}

	public static class IdentifierPathAttributeBuilder extends PluralAttributesDisallowedAttributeBuilder {
		/**
		 * Singleton access
		 */
		public static final IdentifierPathAttributeBuilder INSTANCE = new IdentifierPathAttributeBuilder();

		@Override
		public PluralAttributeSource buildPluralAttribute(
				PluralAttribute attribute,
				OverrideAndConverterCollector overrideAndConverterCollector) {
			throw attribute.getContext().makeMappingException(
					"Plural attribute cannot be part of identifier : " + attribute.getBackingMember().toString()
			);
		}

		@Override
		public PluralAttributeSource buildManyToAnyAttribute(
				PersistentAttribute attribute,
				OverrideAndConverterCollector overrideAndConverterCollector) {
			throw attribute.getContext().makeMappingException(
					"Plural attribute cannot be part of identifier : " + attribute.getBackingMember().toString()
			);
		}

		@Override
		public SingularAttributeSourceAny buildAnyAttribute(
				PersistentAttribute attribute,
				OverrideAndConverterCollector overrideAndConverterCollector) {
			throw attribute.getContext().makeMappingException(
					"Any mapping cannot be part of identifier : " + attribute.getBackingMember().toString()
			);
		}

		@Override
		public SingularAttributeSourceEmbedded buildEmbeddedAttribute(
				EmbeddedAttribute attribute,
				OverrideAndConverterCollector overrideAndConverterCollector) {
			return new SingularAttributeSourceEmbeddedImpl( attribute, true, false );
		}
	}

}
