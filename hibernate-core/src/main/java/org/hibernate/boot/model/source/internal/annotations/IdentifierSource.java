/*
 * Hibernate, Relational Persistence for Idiomatic Java
 *
 * License: GNU Lesser General Public License (LGPL), version 2.1 or later.
 * See the lgpl.txt file in the root directory or <http://www.gnu.org/licenses/lgpl-2.1.html>.
 */
package org.hibernate.boot.model.source.internal.annotations;

import org.hibernate.boot.model.source.internal.annotations.metadata.attribute.IdentifierGenerationInformation;
import org.hibernate.id.EntityIdentifierNature;

/**
 * Contract describing source of identifier information for the entity.
 *
 * @author Steve Ebersole
 */
public interface IdentifierSource {
	/**
	 * Obtain the nature of this identifier source.
	 *
	 * @return The identifier source's nature.
	 */
	public EntityIdentifierNature getNature();

	/**
	 * Obtain the identifier generator source.
	 *
	 * @return The generator source.
	 */
	IdentifierGenerationInformation getIdentifierGenerationInformation();
}
