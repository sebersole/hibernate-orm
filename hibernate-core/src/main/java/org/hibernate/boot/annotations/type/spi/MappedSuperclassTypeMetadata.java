/*
 * Hibernate, Relational Persistence for Idiomatic Java
 *
 * License: GNU Lesser General Public License (LGPL), version 2.1 or later.
 * See the lgpl.txt file in the root directory or http://www.gnu.org/licenses/lgpl-2.1.html.
 */
package org.hibernate.boot.annotations.type.spi;

/**
 * Categorization of a {@linkplain #getManagedClass() ManagedClass} as a
 * {@link jakarta.persistence.metamodel.MappedSuperclassType}
 *
 * @author Steve Ebersole
 */
public interface MappedSuperclassTypeMetadata extends IdentifiableTypeMetadata {
}
