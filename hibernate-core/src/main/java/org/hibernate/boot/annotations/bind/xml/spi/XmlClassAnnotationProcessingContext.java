/*
 * Hibernate, Relational Persistence for Idiomatic Java
 *
 * License: GNU Lesser General Public License (LGPL), version 2.1 or later.
 * See the lgpl.txt file in the root directory or http://www.gnu.org/licenses/lgpl-2.1.html.
 */
package org.hibernate.boot.annotations.bind.xml.spi;

import org.hibernate.boot.annotations.spi.AnnotationProcessingContext;
import org.hibernate.boot.jaxb.mapping.ManagedType;

/**
 * Specialized AnnotationProcessingContext specific to a particular
 * entity, mapped-superclass or embeddable node
 *
 * @author Steve Ebersole
 */
public interface XmlClassAnnotationProcessingContext extends AnnotationProcessingContext {
	/**
	 * The entity, mapped-superclass or embeddable class node
	 */
	ManagedType getClassNode();

	/**
	 * The parent AnnotationProcessingContext for the XML document as a whole
	 */
	XmlDocumentAnnotationProcessingContext getDocumentProcessingContext();
}
