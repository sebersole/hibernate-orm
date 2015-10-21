/*
 * Hibernate, Relational Persistence for Idiomatic Java
 *
 * License: GNU Lesser General Public License (LGPL), version 2.1 or later.
 * See the lgpl.txt file in the root directory or <http://www.gnu.org/licenses/lgpl-2.1.html>.
 */
package org.hibernate.boot.model.source.internal.annotations.impl;

import org.hibernate.EntityMode;
import org.hibernate.annotations.CacheConcurrencyStrategy;
import org.hibernate.annotations.PolymorphismType;
import org.hibernate.boot.jandex.spi.HibernateDotNames;
import org.hibernate.boot.jandex.spi.JpaDotNames;
import org.hibernate.boot.model.Caching;
import org.hibernate.boot.model.TruthValue;
import org.hibernate.boot.model.source.internal.annotations.DiscriminatorSource;
import org.hibernate.boot.model.source.internal.annotations.EntityHierarchySource;
import org.hibernate.boot.model.source.internal.annotations.IdentifierSource;
import org.hibernate.boot.model.source.internal.annotations.MultiTenancySource;
import org.hibernate.boot.model.source.internal.annotations.VersionAttributeSource;
import org.hibernate.boot.model.source.internal.annotations.metadata.type.EntityBindingContext;
import org.hibernate.boot.model.source.internal.annotations.metadata.type.EntityTypeMetadata;
import org.hibernate.boot.model.source.internal.annotations.metadata.type.IdType;
import org.hibernate.boot.model.source.internal.annotations.metadata.type.RootEntityTypeMetadata;
import org.hibernate.boot.model.source.internal.annotations.util.AnnotationBindingHelper;
import org.hibernate.boot.model.source.spi.InheritanceType;
import org.hibernate.engine.OptimisticLockStyle;
import org.hibernate.internal.util.StringHelper;

import org.jboss.jandex.AnnotationInstance;

/**
 * Adapt the built ManagedTypeMetadata hierarchy to the "source" hierarchy
 *
 * @author Hardy Ferentschik
 * @author Steve Ebersole
 */
public class EntityHierarchySourceImpl implements EntityHierarchySource {
	private final InheritanceType inheritanceType;
	private final RootEntitySourceImpl rootEntitySource;

	private final IdentifierSource identifierSource;
	private final OptimisticLockStyle optimisticLockStyle;
	private final VersionAttributeSource versionAttributeSource;
	private final DiscriminatorSourceSupport discriminatorSource;

	private final Caching caching;
	private final Caching naturalIdCaching;

	private final MultiTenancySource multiTenancySource;

	private final String whereClause;
	private final String rowId;
	private final boolean mutable;
	private final boolean useExplicitPolymorphism;


	public EntityHierarchySourceImpl(RootEntityTypeMetadata root, InheritanceType inheritanceType) {
		this.inheritanceType = inheritanceType;

		// this starts the "choreographed" creation of the Entity and MappedSuperclass
		// objects making up the hierarchy.  See the discussion on
		// the RootEntitySourceImpl ctor for details...
		this.rootEntitySource = new RootEntitySourceImpl( root, this );

		this.identifierSource = determineIdentifierSource( root, rootEntitySource );

		this.optimisticLockStyle = determineOptimisticLockStyle( root );
		this.versionAttributeSource = determineVersionAttributeSource( root );
		this.discriminatorSource = determineDiscriminatorSource( root );

		this.caching = determineCachingSettings( root );
		this.naturalIdCaching = determineNaturalIdCachingSettings( root );

		this.multiTenancySource = determineMultiTenancySource( root );

		// (im)mutability
		final AnnotationInstance hibernateImmutableAnnotation = AnnotationBindingHelper.findTypeAnnotation(
				HibernateDotNames.IMMUTABLE,
				root
		);
		this.mutable = ( hibernateImmutableAnnotation == null );

		// implicit/explicit polymorphism (see HHH-6400)
		PolymorphismType polymorphism = PolymorphismType.IMPLICIT;
		final AnnotationInstance polymorphismAnnotation = AnnotationBindingHelper.findTypeAnnotation(
				HibernateDotNames.POLYMORPHISM,
				root
		);
		if ( polymorphismAnnotation != null && polymorphismAnnotation.value( "type" ) != null ) {
			polymorphism = PolymorphismType.valueOf( polymorphismAnnotation.value( "type" ).asEnum() );
		}
		this.useExplicitPolymorphism =  ( polymorphism == PolymorphismType.EXPLICIT );

		// where restriction
		final AnnotationInstance whereAnnotation = AnnotationBindingHelper.findTypeAnnotation(
				HibernateDotNames.WHERE,
				root
		);
		this.whereClause = whereAnnotation != null && whereAnnotation.value( "clause" ) != null
				? whereAnnotation.value( "clause" ).asString()
				: null;

		this.rowId = root.getRowId();
	}

	private IdentifierSource determineIdentifierSource(RootEntityTypeMetadata root, RootEntitySourceImpl rootSource) {
		final IdType idType = root.getIdType();

		switch ( idType ) {
			case SIMPLE: {
				return new SimpleIdentifierSourceImpl(
						rootSource,
						(SingularAttributeSourceImpl) rootSource.getIdentifierAttributes().get( 0 )
				);
			}
			case AGGREGATED: {
				return new AggregatedCompositeIdentifierSourceImpl(
						rootSource,
						(SingularAttributeSourceEmbeddedImpl) rootSource.getIdentifierAttributes().get( 0 ),
						rootSource.getMapsIdSources()
				);
			}
			case NON_AGGREGATED: {
				return new NonAggregatedCompositeIdentifierSourceImpl( rootSource );
			}
			default: {
				throw root.getLocalBindingContext().makeMappingException(
						"Entity did not define an identifier"
				);
			}
		}
	}

	private Caching determineCachingSettings(EntityTypeMetadata root) {
		// I am not so sure that we should be interpreting SharedCacheMode here.
		// Caching accepts a TruthValue value for this purpose.  Might be better
		// to unify this in Binder or in SessionFactoryImpl

		Caching caching = new Caching( TruthValue.UNKNOWN );

		final AnnotationInstance hibernateCacheAnnotation = root.findTypeAnnotation( HibernateDotNames.CACHE );
		if ( hibernateCacheAnnotation != null ) {
			applyRequestedHibernateCachingValues( caching, hibernateCacheAnnotation );
			return caching;
		}

		applyJpaCachingValues(
				root.getLocalBindingContext(),
				caching,
				root.findTypeAnnotation( JpaDotNames.CACHEABLE )
		);

		return caching;
	}

	private void applyRequestedHibernateCachingValues(Caching caching, AnnotationInstance hibernateCacheAnnotation) {
		caching.setRequested( TruthValue.TRUE );

		if ( hibernateCacheAnnotation.value( "usage" ) != null ) {
			caching.setAccessType(
					CacheConcurrencyStrategy.parse( hibernateCacheAnnotation.value( "usage" ).asEnum() ).toAccessType()
			);
		}

		if ( hibernateCacheAnnotation.value( "region" ) != null ) {
			caching.setRegion( hibernateCacheAnnotation.value( "region" ).asString() );
		}

		caching.setCacheLazyProperties(
				hibernateCacheAnnotation.value( "include" ) != null
						&& "all".equals( hibernateCacheAnnotation.value( "include" ).asString() )
		);
	}

	private void applyJpaCachingValues(
			EntityBindingContext localBindingContext,
			Caching caching,
			AnnotationInstance jpaCacheableAnnotation) {
		// todo : note a fan of applying SharedCacheMode here.
		//		imo this should be handled by Binder
		switch ( localBindingContext.getBuildingOptions().getSharedCacheMode() ) {
			case ALL: {
				caching.setRequested( TruthValue.TRUE );
				break;
			}
			case ENABLE_SELECTIVE: {
				// In the ENABLE_SELECTIVE case, the @Cacheable annotation must be present
				//	and its value must be true
				if ( jpaCacheableAnnotation == null ) {
					// No annotation present, so we do not enable caching
					caching.setRequested( TruthValue.FALSE );
				}
				else {
					final boolean value = localBindingContext.getTypedValueExtractor( boolean.class ).extract(
							jpaCacheableAnnotation,
							"value"
					);
					// we enable caching if the value was true
					caching.setRequested( value ? TruthValue.TRUE : TruthValue.FALSE );
				}
				break;
			}
			case DISABLE_SELECTIVE: {
				// In the DISABLE_SELECTIVE case we enable caching for all entities
				// unless it explicitly says to not too
				if ( jpaCacheableAnnotation == null ) {
					// No annotation present, so the entity did not explicitly opt out
					// of caching
					caching.setRequested( TruthValue.TRUE );
				}
				else {
					final boolean value = localBindingContext.getTypedValueExtractor( boolean.class ).extract(
							jpaCacheableAnnotation,
							"value"
					);
					// we enable caching if the value was true
					caching.setRequested( value ? TruthValue.TRUE : TruthValue.FALSE );
				}
				break;
			}
			default: {
				// treat both NONE and UNSPECIFIED the same
				caching.setRequested( TruthValue.FALSE );
				break;
			}
		}

		if ( caching.getRequested() != TruthValue.FALSE ) {
			caching.setCacheLazyProperties( true );
		}
	}

	private Caching determineNaturalIdCachingSettings(EntityTypeMetadata root) {
		Caching naturalIdCaching = new Caching( TruthValue.FALSE );

		final AnnotationInstance naturalIdCacheAnnotation = AnnotationBindingHelper.findTypeAnnotation(
				HibernateDotNames.NATURAL_ID_CACHE,
				root
		);
		if ( naturalIdCacheAnnotation != null ) {
			if ( naturalIdCacheAnnotation.value( "region" ) != null ) {
				String region = naturalIdCacheAnnotation.value( "region" ).asString();
				if ( StringHelper.isNotEmpty( region ) ) {
					naturalIdCaching.setRegion( region );
				}
			}
			naturalIdCaching.setRequested( TruthValue.TRUE );
		}

		return naturalIdCaching;
	}

	private OptimisticLockStyle determineOptimisticLockStyle(EntityTypeMetadata root) {
		OptimisticLockStyle style = OptimisticLockStyle.VERSION;
		final AnnotationInstance optimisticLocking = AnnotationBindingHelper.findTypeAnnotation(
				HibernateDotNames.OPTIMISTIC_LOCKING,
				root
		);
		if ( optimisticLocking != null && optimisticLocking.value( "type" ) != null ) {
			style = OptimisticLockStyle.valueOf( optimisticLocking.value( "type" ).asEnum() );
		}
		return style;
	}

	private VersionAttributeSource determineVersionAttributeSource(RootEntityTypeMetadata root) {
		if ( root.getVersionAttribute() == null ) {
			return null;
		}
		return new VersionAttributeSourceImpl( root.getVersionAttribute(), root );
	}

	private DiscriminatorSourceSupport determineDiscriminatorSource(EntityTypeMetadata root) {
		switch ( inheritanceType ) {
			case JOINED: {
				if ( root.containsDiscriminator() ) {
					return root.getLocalBindingContext().getBuildingOptions().ignoreExplicitDiscriminatorsForJoinedInheritance()
							? null
							: new DiscriminatorSourceImpl( root );
				}
				else {
					return root.getLocalBindingContext().getBuildingOptions().createImplicitDiscriminatorsForJoinedInheritance()
							? new ImplicitDiscriminatorSourceImpl( root )
							: null;
				}
			}
			case DISCRIMINATED: {
				return root.containsDiscriminator()
						? new DiscriminatorSourceImpl( root )
						: new ImplicitDiscriminatorSourceImpl( root );
			}
			case UNION: {
				return null;
			}
			case NO_INHERITANCE: {
				return null;
			}
			default: {
				return null;
			}
		}
	}

	private MultiTenancySource determineMultiTenancySource(EntityTypeMetadata root) {
		return root.hasMultiTenancySourceInformation()
				? new MutliTenancySourceImpl( root )
				: null;
	}

	public RootEntitySourceImpl getRoot() {
		return rootEntitySource;
	}

	public InheritanceType getHierarchyInheritanceType() {
		return inheritanceType;
	}

	public IdentifierSource getIdentifierSource() {
		return identifierSource;
	}

	public VersionAttributeSource getVersionAttributeSource() {
		return versionAttributeSource;
	}

	public DiscriminatorSource getDiscriminatorSource() {
		return discriminatorSource;
	}

	public MultiTenancySource getMultiTenancySource() {
		return multiTenancySource;
	}

	public EntityMode getEntityMode() {
		return EntityMode.POJO;
	}

	public boolean isMutable() {
		return mutable;
	}

	public boolean isExplicitPolymorphism() {
		return useExplicitPolymorphism;
	}

	public String getWhere() {
		return whereClause;
	}

	public String getRowId() {
		return rowId;
	}

	public OptimisticLockStyle getOptimisticLockStyle() {
		return optimisticLockStyle;
	}

	public Caching getCaching() {
		return caching;
	}

	public Caching getNaturalIdCaching() {
		return naturalIdCaching;
	}

	@Override
	public String toString() {
		return "EntityHierarchySourceImpl{rootEntitySource=" + rootEntitySource.getEntityName()
				+ ", inheritanceType=" + inheritanceType + '}';
	}
}


