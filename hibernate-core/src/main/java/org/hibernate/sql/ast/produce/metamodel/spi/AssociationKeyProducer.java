/*
 * Hibernate, Relational Persistence for Idiomatic Java
 *
 * License: GNU Lesser General Public License (LGPL), version 2.1 or later
 * See the lgpl.txt file in the root directory or http://www.gnu.org/licenses/lgpl-2.1.html
 */
package org.hibernate.sql.ast.produce.metamodel.spi;

/**
 * A producer for AssociationKey instances, used to identify
 * circularities while walking a domain model's Navigable metamodel.
 *
 * @author Steve Ebersole
 * @author Andrea Boriero
 */
public interface AssociationKeyProducer {
	AssociationKey getAssociationKey();
}
