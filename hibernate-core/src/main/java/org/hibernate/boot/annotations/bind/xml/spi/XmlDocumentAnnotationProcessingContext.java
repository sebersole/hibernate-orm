/*
 * Hibernate, Relational Persistence for Idiomatic Java
 *
 * License: GNU Lesser General Public License (LGPL), version 2.1 or later.
 * See the lgpl.txt file in the root directory or http://www.gnu.org/licenses/lgpl-2.1.html.
 */
package org.hibernate.boot.annotations.bind.xml.spi;

import org.hibernate.boot.annotations.spi.AnnotationProcessingContext;
import org.hibernate.boot.jaxb.Origin;
import org.hibernate.boot.jaxb.mapping.JaxbEntityMappings;
import org.hibernate.boot.model.relational.Database;
import org.hibernate.boot.model.relational.Namespace;

/**
 * Specialization of AnnotationProcessingContext for each {@code mapping.xml} we process
 *
 * @author Steve Ebersole
 */
public interface XmlDocumentAnnotationProcessingContext extends AnnotationProcessingContext {
	JaxbEntityMappings getXmlMapping();

	Origin getXmlOrigin();

	default boolean isComplete() {
		return getXmlMapping().getPersistenceUnitMetadata() != null
				&& getXmlMapping().getPersistenceUnitMetadata().getXmlMappingMetadataComplete() != null;
	}

	default Database getDatabase() {
		return getMetadataBuildingContext().getMetadataCollector().getDatabase();
	}

	default Namespace getDefaultNamespace() {
		return getDatabase().getDefaultNamespace();
	}
}
