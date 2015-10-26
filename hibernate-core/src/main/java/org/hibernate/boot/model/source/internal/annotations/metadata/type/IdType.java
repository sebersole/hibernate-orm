/*
 * Hibernate, Relational Persistence for Idiomatic Java
 *
 * License: GNU Lesser General Public License (LGPL), version 2.1 or later.
 * See the lgpl.txt file in the root directory or <http://www.gnu.org/licenses/lgpl-2.1.html>.
 */
package org.hibernate.boot.model.source.internal.annotations.metadata.type;

/**
 * An enum for the type of id configuration for an entity.
 *
 * @author Hardy Ferentschik
 */
public enum IdType {
	/**
	 * single @Id annotation.  Corresponds to
	 * {@link org.hibernate.id.EntityIdentifierNature#SIMPLE}
	 */
	SIMPLE,

	/**
	 * multiple @Id annotations.  Corresponds to
	 * {@link org.hibernate.id.EntityIdentifierNature#NON_AGGREGATED_COMPOSITE}
	 */
	NON_AGGREGATED,

	/**
	 * Indicates encountered {@code @EmbeddedId} annotation.  Corresponds to
	 * {@link org.hibernate.id.EntityIdentifierNature#AGGREGATED_COMPOSITE}
	 */
	AGGREGATED,

	/**
	 * does not contain any identifier mappings
	 */
	NONE
}
