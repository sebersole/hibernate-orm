/*
 * Hibernate, Relational Persistence for Idiomatic Java
 *
 * License: GNU Lesser General Public License (LGPL), version 2.1 or later
 * See the lgpl.txt file in the root directory or http://www.gnu.org/licenses/lgpl-2.1.html
 */
package org.hibernate.query;

import org.hibernate.internal.util.StringHelper;
import org.hibernate.metamodel.model.domain.NavigableRole;

/**
 * A representation of the path to a particular Navigable
 * as part of a query relative to a "navigable root".
 *
 * @see NavigableRole
 *
 * @author Steve Ebersole
 */
public class NavigablePath {
	public static final String IDENTIFIER_MAPPER_PROPERTY = "_identifierMapper";

	public static String append(String base, String localName) {
		// the _identifierMapper is a "hidden property" on entities with composite keys.
		// concatenating it will prevent the path from correctly being used to look up
		// various things such as criteria paths and fetch profile association paths
		if ( IDENTIFIER_MAPPER_PROPERTY.equals( localName ) ) {
			return base != null ? base : "";
		}
		else {
			if ( base != null ) {
				return base.isEmpty() ? localName : base + "." + localName;
			}
			else {
				return localName;
			}
		}
	}

	public static String extractTerminalName(String navigablePath) {
		return StringHelper.unqualify( navigablePath );
	}

	public static String extractParentPath(String navigablePath) {
		return StringHelper.qualifier( navigablePath );
	}
}
