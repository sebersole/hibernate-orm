/*
 * Hibernate, Relational Persistence for Idiomatic Java
 *
 * License: GNU Lesser General Public License (LGPL), version 2.1 or later.
 * See the lgpl.txt file in the root directory or <http://www.gnu.org/licenses/lgpl-2.1.html>.
 */
package org.hibernate.persister.common.spi;

import org.hibernate.sqm.domain.ManagedType;
import org.hibernate.type.spi.Type_2;

/**
 * @author Steve Ebersole
 */
public abstract class AbstractSingularAttribute<O extends Type_2>
		extends AbstractAttributeImpl
		implements SingularAttributeImplementor {
	private final O ormType;

	public AbstractSingularAttribute(
			ManagedType declaringType,
			String name,
			O ormType) {
		super( declaringType, name );
		this.ormType = ormType;
	}

	@Override
	public O getType() {
		return ormType;
	}

	@Override
	public O getBoundType() {
		return getType();
	}
}
