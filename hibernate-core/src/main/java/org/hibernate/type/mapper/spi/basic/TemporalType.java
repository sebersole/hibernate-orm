/*
 * Hibernate, Relational Persistence for Idiomatic Java
 *
 * License: GNU Lesser General Public License (LGPL), version 2.1 or later.
 * See the lgpl.txt file in the root directory or <http://www.gnu.org/licenses/lgpl-2.1.html>.
 */
package org.hibernate.type.mapper.spi.basic;

import org.hibernate.type.descriptor.spi.java.basic.TemporalJavaTypeDescriptor;
import org.hibernate.type.spi.TypeConfiguration;

/**
 * Further classification of a BasicType that models a temporal (date, time, instant, etc) value type.
 *
 * @author Steve Ebersole
 */
public interface TemporalType<T> extends BasicType<T> {
	@Override
	TemporalJavaTypeDescriptor<T> getJavaTypeDescriptor();

	javax.persistence.TemporalType getPrecision();

	<X> TemporalType<X> resolveTypeForPrecision(
			javax.persistence.TemporalType precision,
			TypeConfiguration typeConfiguration);
}
