/*
 * Hibernate, Relational Persistence for Idiomatic Java
 *
 * License: GNU Lesser General Public License (LGPL), version 2.1 or later.
 * See the lgpl.txt file in the root directory or <http://www.gnu.org/licenses/lgpl-2.1.html>.
 */
package org.hibernate.boot.model.source.internal.annotations.metadata.attribute;

import org.hibernate.boot.model.source.spi.PluralAttributeIndexNature;

import org.jboss.jandex.ClassInfo;

/**
 * Presents metadata about the elements of a plural attribute.
 * <p/>
 * Note that this is actually only valid for persistent lists and maps.  For
 * all other plural attribute natures,
 *
 * @author Steve Ebersole
 */
public interface PluralAttributeIndexDetails {
	/**
	 * Get the descriptor for the Java type of the collection index.
	 *
	 * @return The descriptor for the Java type of the collection index.
	 */
	ClassInfo getJavaType();

	/**
	 * Get the nature of the collection index.  To a large degree, this is
	 * used to know what further, more specific type-casts can be performed.
	 *
	 * @return The nature of the collection index
	 */
	PluralAttributeIndexNature getIndexNature();
}
