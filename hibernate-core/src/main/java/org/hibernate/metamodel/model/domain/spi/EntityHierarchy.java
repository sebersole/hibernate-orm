/*
 * Hibernate, Relational Persistence for Idiomatic Java
 *
 * License: GNU Lesser General Public License (LGPL), version 2.1 or later
 * See the lgpl.txt file in the root directory or http://www.gnu.org/licenses/lgpl-2.1.html
 */
package org.hibernate.metamodel.model.domain.spi;

import org.hibernate.cache.spi.access.EntityDataAccess;
import org.hibernate.engine.OptimisticLockStyle;
import org.hibernate.mapping.RootClass;
import org.hibernate.metamodel.model.creation.spi.RuntimeModelCreationContext;
import org.hibernate.metamodel.model.domain.RepresentationMode;

/**
 * Defines access to information across the entire entity hierarchy
 *
 * @author Steve Ebersole
 */
public interface EntityHierarchy {
	/**
	 * What "entity mode" is in effect for this hierarchy?
	 */
	RepresentationMode getRepresentation();

	/**
	 * What style of inheritance, if any, is defined for this hierarchy?
	 */
	InheritanceStrategy getInheritanceStrategy();

	/**
	 * Access to the root entity for this hierarchy.
	 *
	 * @return The root entity for this hierarchy.
	 */
	<J> EntityDescriptor<J> getRootEntityType();

	/**
	 * Retrieve the descriptor for the hierarchy's identifier.
	 */
	<O,J> EntityIdentifier<O,J> getIdentifierDescriptor();

	/**
	 * Assuming that the hierarchy defines a natural-id, return its descriptor
	 */
	NaturalIdDescriptor getNaturalIdDescriptor();

	/**
	 * Retrieve the descriptor for the hierarchy's discriminator, if one.  May
	 * return {@code null}.
	 */
	<J> DiscriminatorDescriptor<J>  getDiscriminatorDescriptor();

	/**
	 * Retrieve the descriptor for the hierarchy's version (optimistic locking),
	 * if one.  May return {@code null}.
	 */
	<O,J> VersionDescriptor<O,J>  getVersionDescriptor();

	/**
	 * For entities which are optimistically locked
	 * ({@link #getVersionDescriptor()} returns a non-{@code null} value)
	 * retrieves the style of optimistic locking to apply.
	 */
	OptimisticLockStyle getOptimisticLockStyle();

	/**
	 * Retrieve the descriptor for the hierarchy's ROW_ID, if defined.  May
	 * return {@code null}.
	 */
	<J> RowIdDescriptor<J>  getRowIdDescriptor();

	/**
	 * If the entity is defined as multi-tenant, retrieve the descriptor
	 * for the entity's tenancy value.  May return {@code null}.
	 */
	TenantDiscrimination getTenantDiscrimination();

	/**
	 * Retrieve the second-level cache access strategy for this entity hierarchy, assuming
	 * the hierarchy is cached.  May return {@code null} if the hierarchy is not configured
	 * for second-level caching.
	 */
	EntityDataAccess getEntityCacheAccess();

	String getWhere();

	boolean isMutable();
	boolean isImplicitPolymorphismEnabled();

	void finishInitialization(RuntimeModelCreationContext creationContext, RootClass mappingType);
}
