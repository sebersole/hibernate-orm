/*
 * Hibernate, Relational Persistence for Idiomatic Java
 *
 * License: GNU Lesser General Public License (LGPL), version 2.1 or later.
 * See the lgpl.txt file in the root directory or <http://www.gnu.org/licenses/lgpl-2.1.html>.
 */
package org.hibernate.boot.model;

import org.hibernate.type.descriptor.spi.TypeDescriptorRegistryAccess;
import org.hibernate.type.descriptor.spi.sql.SqlTypeDescriptor;
import org.hibernate.type.descriptor.spi.java.JavaTypeDescriptor;
import org.hibernate.type.mapper.spi.basic.BasicType;
import org.hibernate.type.spi.TypeConfiguration;

/**
 * Defines the target of contributing types, whether via dialects or {@link TypeContributor}
 *
 * @author Steve Ebersole
 */
public interface TypeContributions {
	void contributeJavaTypeDescriptor(JavaTypeDescriptor descriptor);

	void contributeSqlTypeDescriptor(SqlTypeDescriptor descriptor);

	void contributeType(BasicType type, String... registrationKeys);

	TypeConfiguration getTypeConfiguration();

	default TypeDescriptorRegistryAccess getTypeDescriptorRegistryAccess() {
		return getTypeConfiguration().getTypeDescriptorRegistryAccess();
	}
}
