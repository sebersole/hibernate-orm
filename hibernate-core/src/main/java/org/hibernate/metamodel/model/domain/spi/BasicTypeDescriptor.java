/*
 * Hibernate, Relational Persistence for Idiomatic Java
 *
 * License: GNU Lesser General Public License (LGPL), version 2.1 or later
 * See the lgpl.txt file in the root directory or http://www.gnu.org/licenses/lgpl-2.1.html
 */
package org.hibernate.metamodel.model.domain.spi;

import org.hibernate.metamodel.model.domain.BasicDomainType;

/**
 * SPI extension to {@link BasicDomainType}
 *
 * @author Steve Ebersole
 */
public interface BasicTypeDescriptor<J>
		extends BasicDomainType<J>, SimpleTypeDescriptor<J>, AllowableParameterType<J>, AllowableFunctionReturnType<J> {
}
