/*
 * Hibernate, Relational Persistence for Idiomatic Java
 *
 * License: GNU Lesser General Public License (LGPL), version 2.1 or later.
 * See the lgpl.txt file in the root directory or <http://www.gnu.org/licenses/lgpl-2.1.html>.
 */
package org.hibernate.type.descriptor.spi.java.basic;

import org.hibernate.type.descriptor.spi.TypeDescriptorRegistryAccess;
import org.hibernate.type.descriptor.spi.java.JavaTypeDescriptor;

/**
 * Specialization of JavaTypeDescriptor for values representing temporal values.
 *
 * @author Steve Ebersole
 */
public interface TemporalJavaTypeDescriptor<T> extends JavaTypeDescriptor<T> {
	javax.persistence.TemporalType getPrecision();

	<X> TemporalJavaTypeDescriptor<X> resolveTypeForPrecision(
			javax.persistence.TemporalType precision,
			TypeDescriptorRegistryAccess scope);
}
