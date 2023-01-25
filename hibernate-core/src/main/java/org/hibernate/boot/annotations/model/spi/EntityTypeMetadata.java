/*
 * Hibernate, Relational Persistence for Idiomatic Java
 *
 * License: GNU Lesser General Public License (LGPL), version 2.1 or later.
 * See the lgpl.txt file in the root directory or http://www.gnu.org/licenses/lgpl-2.1.html.
 */
package org.hibernate.boot.annotations.model.spi;

import org.hibernate.boot.model.naming.ImplicitEntityNameSource;

/**
 * Intermediate representation of an {@linkplain jakarta.persistence.metamodel.EntityType entity type}
 *
 * @author Steve Ebersole
 */
public interface EntityTypeMetadata extends IdentifiableTypeMetadata, ImplicitEntityNameSource {
	String getEntityName();
	String getJpaEntityName();
}
