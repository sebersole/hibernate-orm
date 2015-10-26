/*
 * Hibernate, Relational Persistence for Idiomatic Java
 *
 * License: GNU Lesser General Public License (LGPL), version 2.1 or later.
 * See the lgpl.txt file in the root directory or <http://www.gnu.org/licenses/lgpl-2.1.html>.
 */
package org.hibernate.boot.model.source.spi;

import javax.persistence.GenerationType;

/**
 * Models the information about a {@link javax.persistence.GeneratedValue} annotation attached
 * to an attribute.
 *
 * @author Steve Ebersole
 */
public class IdentifierGenerationInformation {
	private final GenerationType generationType;
	private final String localName;

	public IdentifierGenerationInformation(GenerationType generationType, String localName) {
		this.generationType = generationType;
		this.localName = localName;
	}

	public GenerationType getGenerationType() {
		return generationType;
	}

	public String getLocalName() {
		return localName;
	}
}
