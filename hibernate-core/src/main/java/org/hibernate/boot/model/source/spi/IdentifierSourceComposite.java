/*
 * Hibernate, Relational Persistence for Idiomatic Java
 *
 * License: GNU Lesser General Public License (LGPL), version 2.1 or later.
 * See the lgpl.txt file in the root directory or <http://www.gnu.org/licenses/lgpl-2.1.html>.
 */
package org.hibernate.boot.model.source.spi;

/**
 * Common contract for composite identifiers.  Specific sub-types include aggregated
 * (think {@link javax.persistence.EmbeddedId}) and non-aggregated (think
 * {@link javax.persistence.IdClass}).
 *
 * @author Steve Ebersole
 */
public interface IdentifierSourceComposite extends IdentifierSource {
	/**
	 * Handle silly SpecJ reading of the JPA spec.  They believe composite identifiers should have "partial generation"
	 * capabilities.
	 *
	 * @param identifierAttributeName The name of the individual attribute within the composite identifier.
	 *
	 * @return The generator for the named attribute (within the composite).
	 */
	IdentifierGenerationInformation getIndividualAttributeIdentifierGenerationInformation(String identifierAttributeName);
}
