/*
 * Hibernate, Relational Persistence for Idiomatic Java
 *
 * License: GNU Lesser General Public License (LGPL), version 2.1 or later.
 * See the lgpl.txt file in the root directory or <http://www.gnu.org/licenses/lgpl-2.1.html>.
 */
package org.hibernate.type.mapper.spi.any;

import org.hibernate.type.mapper.spi.basic.BasicType;
import org.hibernate.type.mapper.spi.Type;

/**
 * @author Steve Ebersole
 */
public interface AnyType extends Type, org.hibernate.sqm.domain.AnyType {
	@Override
	BasicType getDiscriminatorType();

	@Override
	Type getIdentifierType();

	DiscriminatorMappings getDiscriminatorMappings();
}
