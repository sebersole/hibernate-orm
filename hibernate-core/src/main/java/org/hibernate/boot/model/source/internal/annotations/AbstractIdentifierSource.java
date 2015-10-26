/*
 * Hibernate, Relational Persistence for Idiomatic Java
 *
 * License: GNU Lesser General Public License (LGPL), version 2.1 or later.
 * See the lgpl.txt file in the root directory or <http://www.gnu.org/licenses/lgpl-2.1.html>.
 */
package org.hibernate.boot.model.source.internal.annotations;

import org.hibernate.boot.model.source.spi.IdentifierSource;

/**
 * @author Steve Ebersole
 */
public abstract class AbstractIdentifierSource implements IdentifierSource {
	private final RootEntitySourceImpl rootEntitySource;

	protected AbstractIdentifierSource(RootEntitySourceImpl rootEntitySource) {
		this.rootEntitySource = rootEntitySource;
	}

	protected RootEntitySourceImpl rootEntitySource() {
		return rootEntitySource;
	}
}
