/*
 * Hibernate, Relational Persistence for Idiomatic Java
 *
 * License: GNU Lesser General Public License (LGPL), version 2.1 or later.
 * See the lgpl.txt file in the root directory or <http://www.gnu.org/licenses/lgpl-2.1.html>.
 */

/*
 * Hibernate, Relational Persistence for Idiomatic Java
 *
 * Copyright (c) 2011, Red Hat Inc. or third-party contributors as
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
package org.hibernate.boot.model.source.internal.annotations.metadata.attribute;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.EnumSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.SortedMap;
import java.util.SortedSet;
import javax.persistence.AccessType;
import javax.persistence.CascadeType;
import javax.persistence.ConstraintMode;
import javax.persistence.FetchType;

import org.hibernate.annotations.CacheConcurrencyStrategy;
import org.hibernate.annotations.FetchMode;
import org.hibernate.annotations.LazyCollectionOption;
import org.hibernate.annotations.OnDeleteAction;
import org.hibernate.boot.jandex.spi.HibernateDotNames;
import org.hibernate.boot.jandex.spi.JpaDotNames;
import org.hibernate.boot.model.Caching;
import org.hibernate.boot.model.CustomSql;
import org.hibernate.boot.model.MemberDescriptor;
import org.hibernate.boot.model.TruthValue;
import org.hibernate.boot.model.source.internal.annotations.impl.ForeignKeyInformation;
import org.hibernate.boot.model.source.internal.annotations.metadata.type.ManagedTypeMetadata;
import org.hibernate.boot.model.source.internal.annotations.metadata.util.AssociationHelper;
import org.hibernate.boot.model.source.internal.annotations.metadata.util.ConverterAndOverridesHelper;
import org.hibernate.boot.model.source.internal.annotations.util.AnnotationBindingHelper;
import org.hibernate.boot.model.source.internal.hbm.FetchCharacteristicsPluralAttributeImpl;
import org.hibernate.boot.model.source.spi.AttributePath;
import org.hibernate.boot.model.source.spi.AttributeRole;
import org.hibernate.boot.model.source.spi.CollectionNature;
import org.hibernate.boot.model.source.spi.FetchCharacteristicsPluralAttribute;
import org.hibernate.boot.model.source.spi.PluralAttributeElementNature;
import org.hibernate.cfg.NotYetImplementedException;
import org.hibernate.engine.FetchStyle;
import org.hibernate.internal.util.StringHelper;
import org.hibernate.mapping.PropertyGeneration;

import org.jboss.jandex.AnnotationInstance;
import org.jboss.jandex.AnnotationValue;
import org.jboss.jandex.ClassInfo;
import org.jboss.jandex.DotName;
import org.jboss.jandex.Type;

/**
 * Represents a plural persistent attribute.
 *
 * @author Steve Ebersole
 * @author Hardy Ferentschik
 * @author Strong Liu
 */
public class PluralAttribute
		extends AbstractPersistentAttribute
		implements FetchableAttribute, AssociationAttribute {
	private static final EnumSet<CollectionNature> CANNOT_HAVE_COLLECTION_ID = EnumSet.of(
			CollectionNature.SET,
			CollectionNature.MAP,
			CollectionNature.LIST,
			CollectionNature.ARRAY
	);

	private String mappedByAttributeName;
	private final boolean isInverse;
	private final Set<CascadeType> jpaCascadeTypes;
	private final Set<org.hibernate.annotations.CascadeType> hibernateCascadeTypes;
	private final boolean isOrphanRemoval;
	private final boolean ignoreNotFound;
	private final boolean isOptional;
	private final boolean isUnWrapProxy;

	private final FetchStyle fetchStyle;
	private final boolean isLazy;

	// information about the collection
	private final CollectionIdInformation collectionIdInformation;
	private final CollectionNature collectionNature;
	private final String customPersister;
	private final Caching caching;
	private final String comparatorName;
	private final String customLoaderName;
	private final CustomSql customInsert;
	private final CustomSql customUpdate;
	private final CustomSql customDelete;
	private final CustomSql customDeleteAll;
	private final FetchCharacteristicsPluralAttribute fetchCharacteristics;
	private final String whereClause;
	private final String orderBy;
	private final boolean sorted;
	private final boolean mutable;

	// information about the FK
	private final OnDeleteAction onDeleteAction;
	private final ForeignKeyInformation foreignKeyInformation;

	// information about the element
	private final PluralAttributeElementDetails elementDetails;

	// information about the index
	private final PluralAttributeIndexDetails indexDetails;

	private final AnnotationInstance joinTableAnnotation;
	private ArrayList<Column> joinColumnValues = new ArrayList<Column>();
	private ArrayList<Column> inverseJoinColumnValues = new ArrayList<Column>();

	public PluralAttribute(
			ManagedTypeMetadata container,
			String name,
			AttributePath attributePath,
			AttributeRole attributeRole,
			MemberDescriptor backingMember,
			AccessType accessType,
			String accessorStrategy) {
		super(
				container,
				name,
				attributePath,
				attributeRole,
				backingMember,
				AttributeNature.PLURAL,
				accessType,
				accessorStrategy
		);

		this.collectionIdInformation = CollectionIdInformationImpl.make( this );
		this.collectionNature = resolveCollectionNature( collectionIdInformation );

		// we make an assumption here based on JPA spec that the exposed member type must be the
		// Java Collection Framework interfaces and leverage that to extract the value/key types
		List<ClassInfo> parameterTypes = AnnotationBindingHelper.extractTypeParameters(
				backingMember.type(),
				getContext()
		);
		final ClassInfo collectionValueClassInfo = collectionNature == CollectionNature.MAP
				? parameterTypes.get( 1 )
				: parameterTypes.get( 0 );
		final ClassInfo mapKeyClassInfo =  collectionNature == CollectionNature.MAP
				? parameterTypes.get( 0 )
				: null;

		this.elementDetails = resolveElementDetails( backingMember, collectionValueClassInfo );
		this.indexDetails = resolveIndexDetails( backingMember, collectionNature, mapKeyClassInfo );

		final AnnotationInstance associationAnnotation = memberAnnotationMap().get(
				determineAnnotationName( elementDetails.getElementNature() )
		);
		final AnnotationInstance lazyCollectionAnnotation = memberAnnotationMap().get( HibernateDotNames.LAZY_COLLECTION );

		this.mappedByAttributeName = AssociationHelper.determineMappedByAttributeName( associationAnnotation );

		this.fetchStyle = AssociationHelper.determineFetchStyle( backingMember, getContext() );
		this.isLazy = AssociationHelper.determineWhetherIsLazy(
				associationAnnotation,
				lazyCollectionAnnotation,
				backingMember,
				fetchStyle,
				true,
				getContext()
		);
		this.fetchCharacteristics = FetchCharacteristicsPluralAttributeImpl.interpret(
				container.getLocalBindingContext(),
				extractAnnotationValue( memberAnnotationMap().get( HibernateDotNames.FETCH ), "value", FetchMode.class ),
				extractAnnotationValue( lazyCollectionAnnotation, "value", LazyCollectionOption.class ),
				extractAnnotationValue( associationAnnotation, "value", FetchType.class ),
				extractAnnotationValue( memberAnnotationMap().get( HibernateDotNames.BATCH_SIZE ), "value", Integer.class )
		);
		this.isOptional = AssociationHelper.determineOptionality( associationAnnotation );
		this.isUnWrapProxy = AssociationHelper.determineWhetherToUnwrapProxy( backingMember, getContext() );

		this.jpaCascadeTypes = AssociationHelper.determineCascadeTypes( associationAnnotation );
		this.hibernateCascadeTypes = AssociationHelper.determineHibernateCascadeTypes( backingMember, getContext() );
		this.isOrphanRemoval = AssociationHelper.determineOrphanRemoval( associationAnnotation );
		this.ignoreNotFound = AssociationHelper.determineWhetherToIgnoreNotFound( backingMember, getContext() );

		this.mutable = !memberAnnotationMap().containsKey( HibernateDotNames.IMMUTABLE );

		this.whereClause = determineWereClause( backingMember );
		this.orderBy = determineOrderBy( backingMember );

		this.foreignKeyInformation = extractForeignKeyInformation();

		this.caching = determineCachingSettings( backingMember );

		this.customPersister = determineCustomPersister( backingMember );
		this.customLoaderName = determineCustomLoaderName( backingMember );
		this.customInsert = AnnotationBindingHelper.extractCustomSql(
				memberAnnotationMap().get( HibernateDotNames.SQL_INSERT )
		);
		this.customUpdate = AnnotationBindingHelper.extractCustomSql(
				memberAnnotationMap().get( HibernateDotNames.SQL_UPDATE )
		);
		this.customDelete = AnnotationBindingHelper.extractCustomSql(
				memberAnnotationMap().get( HibernateDotNames.SQL_DELETE )
		);
		this.customDeleteAll = AnnotationBindingHelper.extractCustomSql(
				memberAnnotationMap().get( HibernateDotNames.SQL_DELETE_ALL )
		);

		this.onDeleteAction = determineOnDeleteAction( backingMember );

		final AnnotationInstance sortNaturalAnnotation = memberAnnotationMap().get( HibernateDotNames.SORT_NATURAL );
		final AnnotationInstance sortComparatorAnnotation = memberAnnotationMap().get( HibernateDotNames.SORT_COMPARATOR );
		if ( sortNaturalAnnotation != null ) {
			this.sorted = true;
			this.comparatorName = "natural";
		}
		else if ( sortComparatorAnnotation != null ) {
			this.sorted = true;
			this.comparatorName = sortComparatorAnnotation.value().asString();
		}
		else {
			this.sorted = false;
			this.comparatorName = null;
		}

		if ( this.mappedByAttributeName == null ) {
			// todo : not at all a fan of this mess...
			AssociationHelper.processJoinColumnAnnotations(
					backingMember,
					joinColumnValues,
					getContext()
			);
			AssociationHelper.processJoinTableAnnotations(
					backingMember,
					joinColumnValues,
					inverseJoinColumnValues,
					getContext()
			);
			this.joinTableAnnotation = AssociationHelper.extractExplicitJoinTable(
					backingMember,
					getContext()
			);
			
			isInverse = false;
		}
		else {
			this.joinTableAnnotation = null;
			isInverse = true;
		}
		joinColumnValues.trimToSize();
		inverseJoinColumnValues.trimToSize();

		ConverterAndOverridesHelper.processConverters( this );
		ConverterAndOverridesHelper.processAttributeOverrides( this );
		ConverterAndOverridesHelper.processAssociationOverrides( this );

		validateMapping();
	}

	private <T> T extractAnnotationValue(
			AnnotationInstance annotationInstance,
			String attributeName,
			Class<T> valueClass) {
		if ( annotationInstance == null ) {
			return null;
		}

		return getContext().getTypedValueExtractor( valueClass ).extract( annotationInstance, attributeName );
	}

	private DotName determineAnnotationName(PluralAttributeElementNature elementNature) {
		switch ( elementNature ) {
			case BASIC:
			case AGGREGATE: {
				return JpaDotNames.ELEMENT_COLLECTION;
			}
			case ONE_TO_MANY: {
				return JpaDotNames.ONE_TO_MANY;
			}
			case MANY_TO_MANY: {
				return JpaDotNames.MANY_TO_MANY;
			}
			case MANY_TO_ANY: {
				return HibernateDotNames.MANY_TO_ANY;
			}
			default: {
				throw getContext().makeMappingException(
						"Unexpected PluralAttributeElementNature [" + elementNature + "] for attribute [" + getBackingMember().toString() + "]"
				);
			}
		}
	}

	private CollectionNature resolveCollectionNature(CollectionIdInformation collectionIdInformation) {
		if ( getBackingMember().type().kind() == Type.Kind.ARRAY ) {
			return CollectionNature.ARRAY;
		}

		// todo expose this as a building option
		final boolean preferListOverBag = false;
		final boolean hasOrderColumn = memberAnnotationMap().containsKey( JpaDotNames.ORDER_COLUMN );

		final CollectionNature fromExposedInterfaceType = colectionNatureBasedOnExposedInterfaceType();
		switch ( fromExposedInterfaceType ) {
			case LIST:
			case BAG: {
				if ( collectionIdInformation != null ) {
					return CollectionNature.ID_BAG;
				}
				//noinspection PointlessBooleanExpression
				if ( hasOrderColumn || preferListOverBag ) {
					return CollectionNature.LIST;
				}
				return CollectionNature.BAG;
			}
			default: {
				return fromExposedInterfaceType;
			}
		}
	}

	private CollectionNature colectionNatureBasedOnExposedInterfaceType() {
		final ClassInfo memberClassInfo = getContext().getJandexIndex().getClassByName( getBackingMember().type().name() );

		final ClassInfo mapClassInfo = getContext().getJandexIndex().getClassByName( DotName.createSimple( Map.class.getName() ) );
		if ( AnnotationBindingHelper.isAssignableFrom( mapClassInfo, memberClassInfo, getContext() ) ) {
			return CollectionNature.MAP;
		}

		final ClassInfo listClassInfo = getContext().getJandexIndex().getClassByName( DotName.createSimple( List.class.getName() ) );
		if ( AnnotationBindingHelper.isAssignableFrom( listClassInfo, memberClassInfo, getContext() ) ) {
			return CollectionNature.LIST;
		}

		final ClassInfo setClassInfo = getContext().getJandexIndex().getClassByName( DotName.createSimple( Set.class.getName() ) );
		if ( AnnotationBindingHelper.isAssignableFrom( setClassInfo, memberClassInfo, getContext() ) ) {
			return CollectionNature.SET;
		}

		final ClassInfo collectionClassInfo = getContext().getJandexIndex().getClassByName( DotName.createSimple( Collection.class.getName() ) );
		if ( AnnotationBindingHelper.isAssignableFrom( collectionClassInfo, memberClassInfo, getContext() ) ) {
			return CollectionNature.BAG;
		}

		throw getContext().makeMappingException(
				"Unable to determine CollectionNature for attribute [" + getBackingMember() + "]; not any of expected natures : " +
						StringHelper.join(
								CollectionNature.values(), new StringHelper.Renderer<CollectionNature>() {
									@Override
									public String render(CollectionNature value) {
										return value.name();
									}
								}
						)
		);
	}

	private ForeignKeyInformation extractForeignKeyInformation() {
		return ForeignKeyInformation.from(
				AnnotationBindingHelper.findFirstNonNull(
						memberAnnotationMap().get( JpaDotNames.JOIN_COLUMN ),
						memberAnnotationMap().get( JpaDotNames.JOIN_TABLE ),
						memberAnnotationMap().get( JpaDotNames.COLLECTION_TABLE )
				),
				getContext()
		);
	}

	private PluralAttributeElementDetails resolveElementDetails(
			MemberDescriptor backingMember,
			ClassInfo elementType) {
		if ( memberAnnotationMap().containsKey( JpaDotNames.ELEMENT_COLLECTION ) ) {
			if ( getContext().getTypeAnnotationInstances( elementType.name() ).containsKey( JpaDotNames.EMBEDDED ) ) {
				return new PluralAttributeElementDetailsEmbedded( this, elementType );
			}
			else {
				return new PluralAttributeElementDetailsBasic( this, elementType );
			}
		}

		if ( memberAnnotationMap().containsKey( JpaDotNames.ONE_TO_MANY )
				|| memberAnnotationMap().containsKey( JpaDotNames.MANY_TO_MANY ) ) {
			return new PluralAttributeElementDetailsEntity( this, elementType );
		}

		if ( memberAnnotationMap().containsKey( HibernateDotNames.MANY_TO_ANY ) ) {
			throw new NotYetImplementedException( "@ManyToAny element support still baking" );
		}

		throw getContext().makeMappingException(
				"Unable to resolve mapping details collection element : " + backingMember.toString()
		);
	}

	private PluralAttributeIndexDetails resolveIndexDetails(
			MemberDescriptor backingMember,
			CollectionNature collectionNature,
			ClassInfo mapKeyClassInfo) {
		// could be an array/list
		if ( collectionNature == CollectionNature.ARRAY
				|| collectionNature == CollectionNature.LIST ) {
			return new PluralAttributeIndexDetailsSequential( this, backingMember );
		}

		// or a map
		if ( collectionNature != CollectionNature.MAP ) {
			return null;
		}

		final AnnotationInstance mapKeyAnnotation = memberAnnotationMap().get( JpaDotNames.MAP_KEY );
		final AnnotationInstance mapKeyClassAnnotation = memberAnnotationMap().get( JpaDotNames.MAP_KEY_CLASS );
		final AnnotationInstance mapKeyColumnAnnotation = memberAnnotationMap().get( JpaDotNames.MAP_KEY_COLUMN );
		final AnnotationInstance mapKeyEnumeratedAnnotation = memberAnnotationMap().get( JpaDotNames.MAP_KEY_ENUMERATED );
		final AnnotationInstance mapKeyTemporalAnnotation = memberAnnotationMap().get( JpaDotNames.MAP_KEY_TEMPORAL );
		final AnnotationInstance mapKeyTypeAnnotation = memberAnnotationMap().get( HibernateDotNames.MAP_KEY_TYPE );

		final List<AnnotationInstance> mapKeyJoinColumnAnnotations = collectMapKeyJoinColumnAnnotations( backingMember );

		if ( mapKeyAnnotation != null && mapKeyClassAnnotation != null ) {
			// this is an error according to the spec...
			throw getContext().makeMappingException(
					"Map attribute defined both @MapKey and @MapKeyClass; only one should be used : " +
							backingMember.attributeName()
			);
		}


		// ~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~
		// @MapKey

		if ( mapKeyAnnotation != null ) {
			final AnnotationValue value = mapKeyAnnotation.value( "name" );
			String mapKeyAttributeName = null;
			if ( value != null ) {
				mapKeyAttributeName = StringHelper.nullIfEmpty( value.asString() );
			}
			return new PluralAttributeIndexDetailsMapKeyEntityAttribute( this, backingMember, mapKeyClassInfo, mapKeyAttributeName );
		}



		// ~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~
		// @MapKeyEnumerated / @MapKeyTemporal imply basic key

		if ( mapKeyEnumeratedAnnotation != null || mapKeyTemporalAnnotation != null ) {
			return new PluralAttributeIndexDetailsMapKeyBasic( this, backingMember, mapKeyClassInfo, mapKeyColumnAnnotation );
		}


		// ~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~
		// if we could not decode a specific key type, we assume basic

		ClassInfo mapKeyType = mapKeyClassInfo;
		if ( mapKeyClassAnnotation != null ) {
			final DotName name = mapKeyClassAnnotation.value().asClass().name();
			mapKeyType = getContext().getJandexIndex().getClassByName( name );
		}
		if (mapKeyType == null && mapKeyTypeAnnotation != null) {
			final AnnotationInstance typeAnnotation = getContext().getTypedValueExtractor( AnnotationInstance.class ).extract(
					mapKeyTypeAnnotation,
					"value"
			);
			final DotName name = DotName.createSimple( typeAnnotation.value( "type" ).asString() );
			mapKeyType = getContext().getJandexIndex().getClassByName( name );
		}
		if ( mapKeyType == null ) {
			if ( !mapKeyJoinColumnAnnotations.isEmpty() ) {
				throw getContext().makeMappingException(
						"Map key type could not be resolved (to determine entity name to use as key), " +
								"but @MapKeyJoinColumn(s) was present.  Map should either use generics or " +
								"use @MapKeyClass/@MapKeyType to specify entity class"
				);
			}
			return new PluralAttributeIndexDetailsMapKeyBasic( this, backingMember, mapKeyClassInfo, mapKeyColumnAnnotation );
		}


		// ~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~
		// Level 4 : if @MapKeyJoinColumn(s) were specified, we have an entity

		if ( !mapKeyJoinColumnAnnotations.isEmpty() ) {
			throw new NotYetImplementedException( "Entities as map keys not yet implemented" );
		}


		// ~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~
		// Level 5 : if decode the nature of the map key type

		if ( getContext().getTypeAnnotationInstances( mapKeyType.name() ).containsKey( JpaDotNames.EMBEDDABLE ) ) {
			return new PluralAttributeIndexDetailsMapKeyEmbedded( this, backingMember, mapKeyClassInfo );
		}

		if ( getContext().getTypeAnnotationInstances( mapKeyType.name() ).containsKey( JpaDotNames.ENTITY ) ) {
			throw new NotYetImplementedException( "Entities as map keys not yet implemented" );
		}

		return new PluralAttributeIndexDetailsMapKeyBasic( this, backingMember, mapKeyType, mapKeyColumnAnnotation );
	}

	private List<AnnotationInstance> collectMapKeyJoinColumnAnnotations(MemberDescriptor backingMember) {
		Map<DotName,AnnotationInstance> memberAnnotationMap = getContext().getMemberAnnotationInstances( backingMember );
		final AnnotationInstance singular = memberAnnotationMap.get( JpaDotNames.MAP_KEY_JOIN_COLUMN );
		final AnnotationInstance plural = memberAnnotationMap.get( JpaDotNames.MAP_KEY_JOIN_COLUMNS );

		if ( singular != null && plural != null ) {
			throw getContext().makeMappingException(
					"Attribute [" + backingMember.toString() +
							"] declared both @MapKeyJoinColumn and " +
							"@MapKeyJoinColumns; should only use one or the other"
			);
		}

		if ( singular == null && plural == null ) {
			return Collections.emptyList();
		}

		if ( singular != null ) {
			return Collections.singletonList( singular );
		}

		final AnnotationInstance[] annotations = getContext().getTypedValueExtractor( AnnotationInstance[].class ).extract(
				plural,
				"value"
		);
		if ( annotations == null || annotations.length == 0 ) {
			return null;
		}

		return Arrays.asList( annotations );
	}

	private Caching determineCachingSettings(MemberDescriptor backingMember) {
		Caching caching = new Caching( TruthValue.UNKNOWN );

		final AnnotationInstance hbmCacheAnnotation = memberAnnotationMap().get( HibernateDotNames.CACHE );
		if ( hbmCacheAnnotation != null ) {
			caching.setRequested( TruthValue.TRUE );

			final AnnotationValue usageValue = hbmCacheAnnotation.value( "usage" );
			if ( usageValue != null ) {
				caching.setAccessType( CacheConcurrencyStrategy.parse( usageValue.asEnum() ).toAccessType() );
			}

			final AnnotationValue regionValue = hbmCacheAnnotation.value( "region" );
			if ( regionValue != null ) {
				caching.setRegion( regionValue.asString() );
			}

			// NOTE "include" is irrelevant for collections
		}

		return caching;
	}

	private String determineCustomLoaderName(MemberDescriptor backingMember) {
		final AnnotationInstance loaderAnnotation = memberAnnotationMap().get( HibernateDotNames.LOADER );
		return loaderAnnotation == null
				? null
				: StringHelper.nullIfEmpty( loaderAnnotation.value( "namedQuery" ).asString() );
	}

	private String determineCustomPersister(MemberDescriptor backingMember) {
		final AnnotationInstance persisterAnnotation = memberAnnotationMap().get( HibernateDotNames.PERSISTER );
		return persisterAnnotation == null
				? null
				: StringHelper.nullIfEmpty( persisterAnnotation.value( "impl" ).asString() );
	}

	private OnDeleteAction determineOnDeleteAction(MemberDescriptor backingMember) {
		final AnnotationInstance onDeleteAnnotation = memberAnnotationMap().get(
				HibernateDotNames.ON_DELETE
		);
		return onDeleteAnnotation == null
				? null
				: OnDeleteAction.valueOf( onDeleteAnnotation.value( "action" ).asString() );
	}

	private String determineWereClause(MemberDescriptor backingMember) {
		final AnnotationInstance whereAnnotation = memberAnnotationMap().get( HibernateDotNames.WHERE );
		return whereAnnotation == null
				? null
				: getContext().getTypedValueExtractor( String.class ).extract( whereAnnotation, "clause" );
	}

	private String determineOrderBy(MemberDescriptor backingMember) {
		final AnnotationInstance hbmOrderBy = memberAnnotationMap().get( HibernateDotNames.ORDER_BY );
		final AnnotationInstance jpaOrderBy = memberAnnotationMap().get( JpaDotNames.ORDER_BY );

		if ( hbmOrderBy != null && jpaOrderBy != null ) {
			throw getContext().makeMappingException(
					"Cannot use sql order by clause (@org.hibernate.annotations.OrderBy) " +
							"in conjunction with JPA order by clause (@java.persistence.OrderBy) on  " +
							backingMember.toString()
			);
		}


		if ( hbmOrderBy != null ) {
			return StringHelper.nullIfEmpty( hbmOrderBy.value( "clause" ).asString() );
		}

		if ( jpaOrderBy != null ) {
			// this could be an empty string according to JPA spec 11.1.38 -
			// If the ordering element is not specified for an entity association, ordering by the primary key of the
			// associated entity is assumed
			// The binder will need to take this into account and generate the right property names
			final AnnotationValue orderByValue = jpaOrderBy.value();
			final String value = orderByValue == null ? null : StringHelper.nullIfEmpty( orderByValue.asString() );
			if ( value == null || value.equalsIgnoreCase( "asc" ) ) {
				return isBasicCollection() ?  "$element$ asc" : "id asc" ;
			}
			else if ( value.equalsIgnoreCase( "desc" ) ) {
				return isBasicCollection() ? "$element$ desc" : "id desc";
			}
			else {
				return value;
			}
		}

		return null;
	}

	private void validateMapping() {
		checkSortedTypeIsSortable();
		checkIfCollectionIdIsWronglyPlaced();
	}

	private void checkIfCollectionIdIsWronglyPlaced() {
		if ( collectionIdInformation != null && CANNOT_HAVE_COLLECTION_ID.contains( collectionNature ) ) {
			throw getContext().makeMappingException(
					"The Collection type doesn't support @CollectionId annotation: " + getRole()
			);
		}
	}

	private void checkSortedTypeIsSortable() {
		if ( collectionNature != CollectionNature.MAP
				&& collectionNature != CollectionNature.SET ) {
			return;
		}

		final ClassInfo sortedMapClassInfo = getContext().getJandexIndex().getClassByName(
				DotName.createSimple( SortedMap.class.getName() )
		);
		final ClassInfo sortedSetClassInfo = getContext().getJandexIndex().getClassByName(
				DotName.createSimple( SortedSet.class.getName() )
		);
		final ClassInfo memberClassInfo = getContext().getJandexIndex().getClassByName(
				getBackingMember().type().name()
		);
		if ( AnnotationBindingHelper.isAssignableFrom( sortedMapClassInfo, memberClassInfo, getContext() )
				|| AnnotationBindingHelper.isAssignableFrom( sortedSetClassInfo, memberClassInfo, getContext() ) ) {
			if ( !isSorted() ) {
				throw getContext().makeMappingException(
						"A SortedSet/SortedMap attribute has to define @SortNatural or @SortComparator : " +
								getBackingMember().toString()
				);
			}
		}
	}

	public AnnotationInstance getJoinTableAnnotation() {
		return joinTableAnnotation;
	}

	public ArrayList<Column> getJoinColumnValues() {
		return joinColumnValues;
	}

	public ArrayList<Column> getInverseJoinColumnValues() {
		return inverseJoinColumnValues;
	}

	@Override
	public boolean isOptional() {
		return isOptional;
	}

	@Override
	public boolean isInsertable() {
		// irrelevant
		return true;
	}

	@Override
	public boolean isUpdatable() {
		// irrelevant
		return true;
	}

	@Override
	public PropertyGeneration getPropertyGeneration() {
		return PropertyGeneration.NEVER;
	}

	@Override
	public String getMappedByAttributeName() {
		return mappedByAttributeName;
	}

	public void setMappedByAttributeName(String mappedByAttributeName) {
		this.mappedByAttributeName = mappedByAttributeName;
	}

	@Override
	public boolean isInverse() {
		return isInverse;
	}

	@Override
	public Set<CascadeType> getJpaCascadeTypes() {
		return jpaCascadeTypes;
	}

	@Override
	public Set<org.hibernate.annotations.CascadeType> getHibernateCascadeTypes() {
		return hibernateCascadeTypes;
	}

	@Override
	public boolean isOrphanRemoval() {
		return isOrphanRemoval;
	}

	@Override
	public boolean isIgnoreNotFound() {
		return ignoreNotFound;
	}

	@Override
	public FetchCharacteristicsPluralAttribute getFetchCharacteristics() {
		return fetchCharacteristics;
	}

	@Override
	public boolean isLazy() {
		return isLazy;
	}

	public CollectionIdInformation getCollectionIdInformation() {
		return collectionIdInformation;
	}

	public PluralAttributeElementDetails getElementDetails() {
		return elementDetails;
	}

	public PluralAttributeIndexDetails getIndexDetails() {
		return indexDetails;
	}

	public CollectionNature getCollectionNature() {
		return collectionNature;
	}

	public String getWhereClause() {
		return whereClause;
	}

	public String getOrderBy() {
		return orderBy;
	}

	public String getInverseForeignKeyName() {
		return foreignKeyInformation.getInverseForeignKeyName();
	}

	public String getExplicitForeignKeyName(){
		return foreignKeyInformation.getExplicitForeignKeyName();
	}

	public boolean createForeignKeyConstraint(){
		return foreignKeyInformation.getConstraintMode() != ConstraintMode.NO_CONSTRAINT;
 	}

	public Caching getCaching() {
		return caching;
	}

	public String getCustomPersister() {
		return customPersister;
	}

	public String getCustomLoaderName() {
		return customLoaderName;
	}

	public CustomSql getCustomInsert() {
		return customInsert;
	}

	public CustomSql getCustomUpdate() {
		return customUpdate;
	}

	public CustomSql getCustomDelete() {
		return customDelete;
	}

	public CustomSql getCustomDeleteAll() {
		return customDeleteAll;
	}

	@Override
	public String toString() {
		return "PluralAttribute{name='" + getRole().getFullPath() + '\'' + '}';
	}
	public OnDeleteAction getOnDeleteAction() {
		return onDeleteAction;
	}

	public String getComparatorName() {
		return comparatorName;
	}

	public boolean isSorted() {
		return sorted;
	}

	@Override
	public boolean isIncludeInOptimisticLocking() {
		return hasOptimisticLockAnnotation()
				? super.isIncludeInOptimisticLocking()
				: !isInverse;
	}

	private boolean isBasicCollection() {
		return getElementDetails().getElementNature() == PluralAttributeElementNature.BASIC
				|| getElementDetails().getElementNature() == PluralAttributeElementNature.AGGREGATE;
	}


	public boolean isMutable() {
		return mutable;
	}
}


