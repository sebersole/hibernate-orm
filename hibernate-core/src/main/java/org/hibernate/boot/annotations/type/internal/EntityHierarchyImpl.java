/*
 * Hibernate, Relational Persistence for Idiomatic Java
 *
 * License: GNU Lesser General Public License (LGPL), version 2.1 or later.
 * See the lgpl.txt file in the root directory or http://www.gnu.org/licenses/lgpl-2.1.html.
 */
package org.hibernate.boot.annotations.type.internal;

import org.hibernate.annotations.Cache;
import org.hibernate.annotations.NaturalIdCache;
import org.hibernate.boot.annotations.AnnotationSourceLogging;
import org.hibernate.boot.annotations.source.spi.AnnotationUsage;
import org.hibernate.boot.annotations.source.spi.HibernateAnnotations;
import org.hibernate.boot.annotations.source.spi.JpaAnnotations;
import org.hibernate.boot.annotations.source.spi.ManagedClass;
import org.hibernate.boot.annotations.spi.AnnotationBindingContext;
import org.hibernate.boot.annotations.type.spi.EntityHierarchy;
import org.hibernate.boot.annotations.type.spi.EntityTypeMetadata;
import org.hibernate.boot.annotations.type.spi.IdentifiableTypeMetadata;
import org.hibernate.boot.model.source.spi.Caching;
import org.hibernate.cache.spi.access.AccessType;

import jakarta.persistence.Cacheable;
import jakarta.persistence.InheritanceType;
import jakarta.persistence.SharedCacheMode;

/**
 * @author Steve Ebersole
 */
public class EntityHierarchyImpl implements EntityHierarchy {
	private final EntityTypeMetadata entityTypeMetadata;

	private final InheritanceType inheritanceType;
	private final Caching caching;
	private final Caching naturalIdCaching;

	public EntityHierarchyImpl(EntityTypeMetadata entityTypeMetadata, AnnotationBindingContext bindingContext) {
		this.entityTypeMetadata = entityTypeMetadata;
		this.inheritanceType = determineInheritanceType( entityTypeMetadata );

		final ManagedClass entityClass = entityTypeMetadata.getManagedClass();

		final AnnotationUsage<Cacheable> cacheableAnnotation = entityClass.getAnnotation( JpaAnnotations.CACHEABLE );
		final AnnotationUsage<Cache> cacheAnnotation = entityClass.getAnnotation( HibernateAnnotations.CACHE );
		final AnnotationUsage<NaturalIdCache> naturalIdCacheAnnotation = entityClass.getAnnotation( HibernateAnnotations.NATURAL_ID_CACHE );
		final AccessType implicitCacheAccessType = bindingContext.getBuildingOptions().getImplicitCacheAccessType();
		final SharedCacheMode sharedCacheMode = bindingContext.getBuildingOptions().getSharedCacheMode();

		switch ( sharedCacheMode ) {
			case NONE: {
				this.caching = new Caching( false );
				this.naturalIdCaching = new Caching( false );
				break;
			}
			case ALL: {
				this.caching = new Caching( cacheAnnotation, implicitCacheAccessType, entityClass.getName() );
				this.naturalIdCaching = new Caching( naturalIdCacheAnnotation, implicitCacheAccessType, caching.getRegion() + "NaturalId" );
				break;
			}
			case DISABLE_SELECTIVE: {
				// Caching is disabled for `@Cacheable(false)`, enabled otherwise
				final boolean cached;
				if ( cacheableAnnotation == null ) {
					cached = true;
				}
				else {
					cached = cacheableAnnotation.getValueAttributeValue().getValue();
				}
				if ( cached ) {
					this.caching = new Caching( cacheAnnotation, implicitCacheAccessType, entityClass.getName() );
					this.naturalIdCaching = new Caching( naturalIdCacheAnnotation, implicitCacheAccessType, caching.getRegion() + "NaturalId" );
				}
				else {
					this.caching = new Caching( false );
					this.naturalIdCaching = new Caching( false );
				}
				break;
			}
			default: {
				// ENABLE_SELECTIVE
				// UNSPECIFIED

				// Caching is enabled for all entities for <code>Cacheable(true)</code>
				// is specified.  All other entities are not cached.
				final boolean cached = cacheableAnnotation != null && cacheableAnnotation.getValueAttributeValue().getValue( boolean.class );
				if ( cached ) {
					this.caching = new Caching( cacheAnnotation, implicitCacheAccessType, entityClass.getName() );
					this.naturalIdCaching = new Caching( naturalIdCacheAnnotation, implicitCacheAccessType, caching.getRegion() + "NaturalId" );
				}
				else {
					this.caching = new Caching( false );
					this.naturalIdCaching = new Caching( false );
				}
			}
		}
	}

	private InheritanceType determineInheritanceType(EntityTypeMetadata root) {
		// Validate that there is no @Inheritance annotation further down the hierarchy
		if ( AnnotationSourceLogging.ANNOTATION_SOURCE_LOGGER_DEBUG_ENABLED ) {
			ensureNoInheritanceAnnotationsOnSubclasses( root );
		}

		final InheritanceType inheritanceType = root.getLocallyDefinedInheritanceType();
		if ( inheritanceType != null ) {
			return inheritanceType;
		}

		return InheritanceType.SINGLE_TABLE;
	}

	private void ensureNoInheritanceAnnotationsOnSubclasses(IdentifiableTypeMetadata type) {
		type.forEachSubType( (subType) -> {
			if ( subType.getLocallyDefinedInheritanceType() != null ) {
				AnnotationSourceLogging.ANNOTATION_SOURCE_LOGGER.debugf(
						"@javax.persistence.Inheritance was specified on non-root entity [%s]; ignoring...",
						type.getManagedClass().getName()
				);
			}
			ensureNoInheritanceAnnotationsOnSubclasses( subType );
		} );
	}

	@Override
	public EntityTypeMetadata getRoot() {
		return entityTypeMetadata;
	}

	@Override
	public InheritanceType getInheritanceType() {
		return inheritanceType;
	}

	@Override
	public Caching getCaching() {
		return caching;
	}

	@Override
	public Caching getNaturalIdCaching() {
		return naturalIdCaching;
	}
}
