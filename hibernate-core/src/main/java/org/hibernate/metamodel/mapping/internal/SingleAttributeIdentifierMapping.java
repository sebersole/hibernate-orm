/*
 * Hibernate, Relational Persistence for Idiomatic Java
 *
 * License: GNU Lesser General Public License (LGPL), version 2.1 or later
 * See the lgpl.txt file in the root directory or http://www.gnu.org/licenses/lgpl-2.1.html
 */
package org.hibernate.metamodel.mapping.internal;

import org.hibernate.metamodel.mapping.EntityIdentifierMapping;
import org.hibernate.property.access.spi.PropertyAccess;

/**
 * Specialization of EntityIdentifierMapping for identifiers that
 * are defined as a single attribute on the entity.  Applies to
 * both basic and aggregated-composite identifiers
 *
 * @author Steve Ebersole
 */
public interface SingleAttributeIdentifierMapping extends EntityIdentifierMapping {
	/**
	 * Access to the identifier attribute's PropertyAccess
	 */
	PropertyAccess getPropertyAccess();
}
