/*
 * Hibernate, Relational Persistence for Idiomatic Java
 *
 * License: GNU Lesser General Public License (LGPL), version 2.1 or later.
 * See the lgpl.txt file in the root directory or <http://www.gnu.org/licenses/lgpl-2.1.html>.
 */
package org.hibernate.boot.model.source.internal.annotations.metadata.attribute;

import org.hibernate.boot.model.source.spi.PluralAttributeElementNature;

import org.jboss.jandex.ClassInfo;

/**
 * Presents metadata about the elements of a plural attribute
 *
 * @author Steve Ebersole
 */
public interface PluralAttributeElementDetails {
	/**
	 * Get the descriptor for the Java type of the collection elements.
	 *
	 * @return The descriptor for the Java type of the collection elements.
	 */
	ClassInfo getJavaType();

	/**
	 * Get the nature of the element values.
	 *
	 * @return The element nature
	 */
	PluralAttributeElementNature getElementNature();
}
