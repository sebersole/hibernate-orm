/*
 * Hibernate, Relational Persistence for Idiomatic Java
 *
 * License: GNU Lesser General Public License (LGPL), version 2.1 or later.
 * See the lgpl.txt file in the root directory or http://www.gnu.org/licenses/lgpl-2.1.html.
 */
package org.hibernate.boot.annotations.type.spi;

import org.hibernate.boot.model.source.spi.Caching;

import jakarta.persistence.InheritanceType;

/**
 * Models an entity hierarchy comprised of {@linkplain EntityTypeMetadata entity}
 * and {@linkplain MappedSuperclassTypeMetadata mapped-superclass} types.
 *
 * @author Steve Ebersole
 */
public interface EntityHierarchy {
	/**
	 * The hierarchy's root type.
	 */
	EntityTypeMetadata getRoot();

	/**
	 * The inheritance strategy for the hierarchy.
	 */
	InheritanceType getInheritanceType();

	/**
	 * The caching configuration for entities in this hierarchy.
	 */
	Caching getCaching();

	/**
	 * The caching configuration for this hierarchy's {@linkplain org.hibernate.annotations.NaturalId natural-id}
	 */
	Caching getNaturalIdCaching();
}
