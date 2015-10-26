/*
 * Hibernate, Relational Persistence for Idiomatic Java
 *
 * License: GNU Lesser General Public License (LGPL), version 2.1 or later.
 * See the lgpl.txt file in the root directory or <http://www.gnu.org/licenses/lgpl-2.1.html>.
 */
package org.hibernate.boot.model.source.internal.annotations.metadata.attribute;

import java.util.Set;
import javax.persistence.CascadeType;

/**
 * @author Steve Ebersole
 */
public interface AssociationAttribute {
	String getMappedByAttributeName();
	@Deprecated
	boolean isInverse();

	Set<CascadeType> getJpaCascadeTypes();
	Set<org.hibernate.annotations.CascadeType> getHibernateCascadeTypes();
	boolean isOrphanRemoval();

	boolean isOptional();
	boolean isIgnoreNotFound();
}
