/*
 * Hibernate, Relational Persistence for Idiomatic Java
 *
 * License: GNU Lesser General Public License (LGPL), version 2.1 or later.
 * See the lgpl.txt file in the root directory or <http://www.gnu.org/licenses/lgpl-2.1.html>.
 */

/*
 * Hibernate, Relational Persistence for Idiomatic Java
 *
 * Copyright (c) 2014, Red Hat Inc. or third-party contributors as
 * indicated by the @author tags or express copyright attribution
 * statements applied by the authors.  All third-party contributions are
 * distributed under license by Red Hat Inc.
 *
 * This copyrighted material is made available to anyone wishing to use, modify,
 * copy, or redistribute it subject to the terms and conditions of the GNU
 * Lesser General Public License, as published by the Free Software Foundation.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY
 * or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU Lesser General Public License
 * for more details.
 *
 * You should have received a copy of the GNU Lesser General Public License
 * along with this distribution; if not, write to:
 * Free Software Foundation, Inc.
 * 51 Franklin Street, Fifth Floor
 * Boston, MA  02110-1301  USA
 */
package org.hibernate.boot.model.source.internal.annotations.metadata.util;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import javax.persistence.CascadeType;
import javax.persistence.FetchType;

import org.hibernate.annotations.FetchMode;
import org.hibernate.annotations.LazyToOneOption;
import org.hibernate.annotations.NotFoundAction;
import org.hibernate.boot.jandex.spi.HibernateDotNames;
import org.hibernate.boot.jandex.spi.JpaDotNames;
import org.hibernate.boot.model.MemberDescriptor;
import org.hibernate.boot.model.source.internal.annotations.AnnotationBindingContext;
import org.hibernate.boot.model.source.internal.annotations.metadata.attribute.Column;
import org.hibernate.boot.model.source.internal.annotations.metadata.type.EntityBindingContext;
import org.hibernate.boot.model.source.internal.annotations.util.AnnotationBindingHelper;
import org.hibernate.boot.model.source.internal.hbm.FetchCharacteristicsSingularAssociationImpl;
import org.hibernate.boot.model.source.spi.FetchCharacteristicsSingularAssociation;
import org.hibernate.engine.FetchStyle;
import org.hibernate.engine.FetchTiming;
import org.hibernate.internal.CoreLogging;
import org.hibernate.internal.CoreMessageLogger;

import org.jboss.jandex.AnnotationInstance;
import org.jboss.jandex.AnnotationValue;
import org.jboss.jandex.ClassInfo;
import org.jboss.jandex.DotName;
import org.jboss.jandex.Type;

/**
 * A helper with utilities for working with associations
 *
 * @author Steve Ebersole
 */
public class AssociationHelper {
	private static final CoreMessageLogger log = CoreLogging.messageLogger( AssociationHelper.class );

	private AssociationHelper() {
	}


	/**
	 * Determine the concrete "target type" name
	 *
	 * @param backingMember The attribute member
	 * @param associationAnnotation The annotation that identifies the "association"
	 * @param defaultTargetType The default type for the target.  For singular attributes, this is the
	 * attribute type; for plural attributes it is the element type.
	 * @param context The binding context
	 *
	 * @return The concrete type name
	 */
	public static ClassInfo determineTarget(
			MemberDescriptor backingMember,
			AnnotationInstance associationAnnotation,
			Type defaultTargetType,
			EntityBindingContext context) {
		final AnnotationInstance targetAnnotation = context.getMemberAnnotationInstances( backingMember ).get(
				HibernateDotNames.TARGET
		);
		if ( targetAnnotation != null ) {
			final DotName dotName = context.getTypedValueExtractor( Type.class ).extract( targetAnnotation, "value" ).name();
			return context.getJandexIndex().getClassByName( dotName );
		}

		final AnnotationValue targetEntityValue = associationAnnotation.value( "targetEntity" );
		if ( targetEntityValue != null ) {
			final DotName name = DotName.createSimple( targetEntityValue.asString() );
			return context.getJandexIndex().getClassByName( name );
		}

		if ( defaultTargetType == null ) {
			throw context.makeMappingException(
					"Could not determine target for association : " + backingMember.toString()
			);
		}

		return context.getJandexIndex().getClassByName( defaultTargetType.name() );
	}

	/**
	 * Determine the name of the attribute that is the other side of this association, which
	 * contains mapping metadata.
	 *
	 * @param associationAnnotation The annotation that identifies the "association"
	 *
	 * @return The specified mapped-by attribute name, or {@code null} if none specified
	 */
	public static String determineMappedByAttributeName(AnnotationInstance associationAnnotation) {
		if ( associationAnnotation == null ) {
			return null;
		}

		final AnnotationValue mappedByAnnotationValue = associationAnnotation.value( "mappedBy" );
		if ( mappedByAnnotationValue == null ) {
			return null;
		}

		return mappedByAnnotationValue.asString();
	}

	/**
	 * Given a member that is fetchable, determine the indicated FetchStyle
	 *
	 * @param backingMember The fetchable attribute member
	 *
	 * @return The indicated FetchStyle, or {@code null} if no indication was given
	 */
	public static FetchStyle determineFetchStyle(MemberDescriptor backingMember, AnnotationBindingContext context) {
		final AnnotationInstance fetchAnnotation = context.getMemberAnnotationInstances( backingMember ).get(
				HibernateDotNames.FETCH
		);
		if ( fetchAnnotation == null ) {
			return null;
		}

		final org.hibernate.annotations.FetchMode annotationFetchMode = org.hibernate.annotations.FetchMode.valueOf(
				fetchAnnotation.value().asEnum()
		);
		return EnumConversionHelper.annotationFetchModeToFetchStyle( annotationFetchMode );
	}

	/**
	 * Determine whether the given (fetchable?) association is considered lazy.
	 *
	 * @param associationAnnotation The annotation that identifies the "association"
	 * @param lazyAnnotation The Hibernate-specific lazy annotation for attributes of the given nature.  Generally
	 * {@link org.hibernate.annotations.LazyCollection} or {@link org.hibernate.annotations.LazyToOne}
	 * @param backingMember The fetchable attribute member
	 * @param fetchStyle The specified fetch style
	 *
	 * @return whether its considered lazy, duh :)
	 */
	public static boolean determineWhetherIsLazy(
			AnnotationInstance associationAnnotation,
			AnnotationInstance lazyAnnotation,
			MemberDescriptor backingMember,
			FetchStyle fetchStyle,
			boolean isCollection,
			AnnotationBindingContext context) {

		final Map<DotName, AnnotationInstance> memberAnnotationMap = context.getMemberAnnotationInstances( backingMember );

		// first precedence
		// 		- join fetches cannot be lazy : fetch style
		if ( fetchStyle != null ) {
			if ( fetchStyle == FetchStyle.JOIN ) {
				return false;
			}
		}

		// second precedence
		// 		- join fetches cannot be lazy : fetch annotation
		final AnnotationInstance fetchAnnotation = memberAnnotationMap.get( HibernateDotNames.FETCH );
		if ( fetchAnnotation != null ) {
			if ( FetchMode.valueOf( fetchAnnotation.value().asEnum() ) == FetchMode.JOIN ) {
				return false;
			}
		}

		// 3rd precedence
		final AnnotationValue fetchValue = associationAnnotation.value( "fetch" );
		if ( fetchValue != null ) {
			return FetchType.LAZY == FetchType.valueOf( fetchValue.asEnum() );
		}

		// 4th precedence
		if ( lazyAnnotation != null ) {
			final AnnotationValue value = lazyAnnotation.value();
			if ( value != null ) {
				return !"FALSE".equals( value.asEnum() );
			}
		}

		// by default collections are lazy, to-ones are not
		return isCollection;
	}


	public static boolean determineOptionality(AnnotationInstance associationAnnotation) {
		// todo : is this only valid for singular association attributes?

		boolean optional = true;

		AnnotationValue optionalValue = associationAnnotation.value( "optional" );
		if ( optionalValue != null ) {
			optional = optionalValue.asBoolean();
		}

		return optional;
	}

	public static boolean determineWhetherToUnwrapProxy(MemberDescriptor backingMember, AnnotationBindingContext context) {
		final AnnotationInstance lazyToOne = context.getMemberAnnotationInstances( backingMember ).get(
				HibernateDotNames.LAZY_TO_ONE
		);
		return lazyToOne != null && LazyToOneOption.valueOf( lazyToOne.value().asEnum() ) == LazyToOneOption.NO_PROXY;

	}

	public static Set<CascadeType> determineCascadeTypes(AnnotationInstance associationAnnotation) {
		Set<CascadeType> cascadeTypes = new HashSet<CascadeType>();
		AnnotationValue cascadeValue = associationAnnotation.value( "cascade" );
		if ( cascadeValue != null ) {
			String[] cascades = cascadeValue.asEnumArray();
			for ( String s : cascades ) {
				cascadeTypes.add( Enum.valueOf( CascadeType.class, s ) );
			}
		}
		return cascadeTypes;
	}

	public static Set<org.hibernate.annotations.CascadeType> determineHibernateCascadeTypes(
			MemberDescriptor backingMember,
			AnnotationBindingContext context) {
		final AnnotationInstance cascadeAnnotation = context.getMemberAnnotationInstances( backingMember ).get(
				HibernateDotNames.CASCADE
		);

		if ( cascadeAnnotation != null ) {
			final AnnotationValue cascadeValue = cascadeAnnotation.value();
			if ( cascadeValue != null ) {
				final String[] cascades = cascadeValue.asEnumArray();
				if ( cascades != null && cascades.length > 0 ) {
					final Set<org.hibernate.annotations.CascadeType> cascadeTypes
							= new HashSet<org.hibernate.annotations.CascadeType>();
					for ( String cascade : cascades ) {
						cascadeTypes.add(
								org.hibernate.annotations.CascadeType.valueOf( cascade )
						);
					}
					return cascadeTypes;
				}
			}
		}

		return Collections.emptySet();
	}

	@SuppressWarnings("SimplifiableIfStatement")
	public static boolean determineOrphanRemoval(AnnotationInstance associationAnnotation) {
		final AnnotationValue orphanRemovalValue = associationAnnotation.value( "orphanRemoval" );
		if ( orphanRemovalValue != null ) {
			return orphanRemovalValue.asBoolean();
		}
		return false;
	}

	public static AnnotationInstance locateMapsId(
			MemberDescriptor member,
			EntityBindingContext context) {
		final AnnotationInstance mapsIdAnnotation = context.getMemberAnnotationInstances( member ).get( JpaDotNames.MAPS_ID );
		if ( mapsIdAnnotation == null ) {
			return null;
		}
		return mapsIdAnnotation;
	}

	public static boolean determineWhetherToIgnoreNotFound(MemberDescriptor backingMember, AnnotationBindingContext context) {
		final AnnotationInstance notFoundAnnotation = context.getMemberAnnotationInstances( backingMember ).get(
				HibernateDotNames.NOT_FOUND
		);
		if ( notFoundAnnotation != null ) {
			final AnnotationValue actionValue = notFoundAnnotation.value( "action" );
			if ( actionValue != null ) {
				return NotFoundAction.valueOf( actionValue.asEnum() ) == NotFoundAction.IGNORE;
			}
		}

		return false;
	}



	public static void processJoinColumnAnnotations(
			MemberDescriptor backingMember,
			ArrayList<Column> joinColumnValues,
			EntityBindingContext context) {
		final Map<DotName, AnnotationInstance> memberAnnotationMap = context.getMemberAnnotationInstances( backingMember );
		final Collection<AnnotationInstance> joinColumnAnnotations = AnnotationBindingHelper.getCombinedAnnotations(
				memberAnnotationMap,
				JpaDotNames.JOIN_COLUMN,
				JpaDotNames.JOIN_COLUMNS,
				context
		);
		for ( AnnotationInstance joinColumnAnnotation : joinColumnAnnotations ) {
			joinColumnValues.add( new Column( joinColumnAnnotation ) );
		}

		// @JoinColumn as part of @CollectionTable
		AnnotationInstance collectionTableAnnotation = memberAnnotationMap.get( JpaDotNames.COLLECTION_TABLE );
		if ( collectionTableAnnotation != null ) {
			final AnnotationInstance[] joinColumnsList = context.getTypedValueExtractor( AnnotationInstance[].class ).extract(
					collectionTableAnnotation,
					"joinColumns"
			);
			for ( AnnotationInstance annotation : joinColumnsList ) {
				joinColumnValues.add( new Column( annotation ) );
			}
		}
	}

	public static void processJoinTableAnnotations(
			MemberDescriptor backingMember,
			ArrayList<Column> joinColumnValues,
			ArrayList<Column> inverseJoinColumnValues,
			EntityBindingContext context) {
		final Map<DotName,AnnotationInstance> memberAnnotationMap = context.getMemberAnnotationInstances( backingMember );

		// @JoinColumn as part of @JoinTable
		final AnnotationInstance joinTableAnnotation = memberAnnotationMap.get( JpaDotNames.JOIN_TABLE );
		if ( joinTableAnnotation == null ) {
			return;
		}

		// first process the `joinColumns` attribute
		final AnnotationInstance[] joinColumns = context.getTypedValueExtractor( AnnotationInstance[].class ).extract(
				joinTableAnnotation,
				"joinColumns"
		);
		for ( AnnotationInstance annotation : joinColumns ) {
			joinColumnValues.add( new Column( annotation ) );
		}

		// then the `inverseJoinColumns` attribute
		final AnnotationInstance[] inverseJoinColumns = context.getTypedValueExtractor( AnnotationInstance[].class ).extract(
				joinTableAnnotation,
				"inverseJoinColumns"
		);
		for ( AnnotationInstance annotation : inverseJoinColumns ) {
			inverseJoinColumnValues.add( new Column( annotation ) );
		}
	}

	public static AnnotationInstance extractExplicitJoinTable(MemberDescriptor backingMember, EntityBindingContext context) {
		final Map<DotName,AnnotationInstance> memberAnnotationMap = context.getMemberAnnotationInstances( backingMember );

		final AnnotationInstance collectionTableAnnotation = memberAnnotationMap.get( JpaDotNames.COLLECTION_TABLE );
		final AnnotationInstance joinTableAnnotation = memberAnnotationMap.get( JpaDotNames.JOIN_TABLE );

		if ( collectionTableAnnotation != null && joinTableAnnotation != null ) {
			throw context.makeMappingException(
					"@CollectionTable and @JoinTable used together : " + backingMember.toString()
//					log.collectionTableAndJoinTableUsedTogether(
//							context.getOrigin().getName(),
//							backingMember.getName()
//					)
			);
		}

		if ( collectionTableAnnotation != null ) {
			if ( memberAnnotationMap.containsKey( JpaDotNames.ELEMENT_COLLECTION ) ) {
				throw context.makeMappingException(
						"@CollectionTable used without @ElementCollection : " + backingMember.toString()
//						log.collectionTableWithoutElementCollection(
//								context.getOrigin().getName(),
//								backingMember.getName()
//						)
				);
			}
			return collectionTableAnnotation;
		}

		if ( joinTableAnnotation != null ) {
			if ( !memberAnnotationMap.containsKey( JpaDotNames.ONE_TO_ONE )
				&& !memberAnnotationMap.containsKey( JpaDotNames.MANY_TO_ONE )
				&& !memberAnnotationMap.containsKey( JpaDotNames.ONE_TO_MANY )
				&& !memberAnnotationMap.containsKey( JpaDotNames.MANY_TO_MANY ) ) {
				throw context.makeMappingException(
						"Found @JoinTable for non-association attribute : " + backingMember.toString()
//						log.joinTableForNonAssociationAttribute(
//								context.getOrigin().getName(),
//								backingMember.getName()
//						)
				);
			}
			return joinTableAnnotation;
		}

		return null;
	}

	public static FetchCharacteristicsSingularAssociation determineFetchCharacteristicsSingularAssociation(
			MemberDescriptor backingMember,
			EntityBindingContext context) {
		final FetchCharacteristicsSingularAssociationImpl.Builder builder =
				new FetchCharacteristicsSingularAssociationImpl.Builder( context.getMappingDefaults() );

		builder.setFetchStyle( determineFetchStyle( backingMember, context ) );
		builder.setFetchTiming( extractFetchTiming( backingMember, context ) );
		builder.setUnwrapProxies( determineWhetherToUnwrapProxy( backingMember, context ) );

		return builder.createFetchCharacteristics();
	}

	private static FetchTiming extractFetchTiming(MemberDescriptor backingMember, EntityBindingContext context) {
		// get the @ManyToOne or @OneToOne annotation
		AnnotationInstance fetchSourceAnnotation = context.getMemberAnnotationInstances( backingMember )
				.get( JpaDotNames.MANY_TO_ONE );
		if ( fetchSourceAnnotation == null ) {
			fetchSourceAnnotation = context.getMemberAnnotationInstances( backingMember )
					.get( JpaDotNames.ONE_TO_ONE );
		}

		final FetchType jpaFetchType =
				context.getTypedValueExtractor( FetchType.class ).extract( fetchSourceAnnotation, "fetch" );

		return jpaFetchType == FetchType.EAGER
				? FetchTiming.IMMEDIATE
				: FetchTiming.DELAYED;
	}
}
