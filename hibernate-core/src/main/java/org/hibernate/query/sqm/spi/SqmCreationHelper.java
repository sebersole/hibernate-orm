/*
 * Hibernate, Relational Persistence for Idiomatic Java
 *
 * License: GNU Lesser General Public License (LGPL), version 2.1 or later
 * See the lgpl.txt file in the root directory or http://www.gnu.org/licenses/lgpl-2.1.html
 */
package org.hibernate.query.sqm.spi;

import org.hibernate.query.NavigablePath;
import org.hibernate.query.sqm.tree.domain.SqmPath;

/**
 * @author Steve Ebersole
 */
public class SqmCreationHelper {
	public static String buildRootNavigablePath(String base, String alias) {
		return alias == null
				? base
				: base + "(" + alias + ")";
	}

	public static String buildSubNavigablePath(SqmPath<?> lhs, String subNavigableName, String alias) {
		if ( lhs == null ) {
			throw new IllegalArgumentException(
					"`lhs` cannot be null for a sub-navigable reference - " + subNavigableName
			);
		}

		return NavigablePath.append(
				lhs.getNavigablePath(),
				alias == null
						? subNavigableName
						: subNavigableName + "(" + alias + ")"
		);
	}

	private SqmCreationHelper() {
	}

}
