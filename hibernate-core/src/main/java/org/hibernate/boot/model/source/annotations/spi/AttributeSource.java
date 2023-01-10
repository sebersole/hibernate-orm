/*
 * Hibernate, Relational Persistence for Idiomatic Java
 *
 * License: GNU Lesser General Public License (LGPL), version 2.1 or later.
 * See the lgpl.txt file in the root directory or http://www.gnu.org/licenses/lgpl-2.1.html.
 */
package org.hibernate.boot.model.source.annotations.spi;

import org.hibernate.boot.annotations.spi.AnnotationTarget;

/**
 * Source for an attribute while processing annotations.
 *
 * @author Steve Ebersole
 */
public interface AttributeSource extends AnnotationTarget {
	/**
	 * Obtain the attribute name.
	 *
	 * @return The attribute name. {@code null} is NOT allowed!
	 */
	String getName();
}
