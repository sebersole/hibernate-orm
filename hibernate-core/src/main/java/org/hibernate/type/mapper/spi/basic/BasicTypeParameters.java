/*
 * Hibernate, Relational Persistence for Idiomatic Java
 *
 * License: GNU Lesser General Public License (LGPL), version 2.1 or later.
 * See the lgpl.txt file in the root directory or <http://www.gnu.org/licenses/lgpl-2.1.html>.
 */
package org.hibernate.type.mapper.spi.basic;

import java.util.Comparator;

import org.hibernate.type.descriptor.spi.MutabilityPlan;
import org.hibernate.type.descriptor.spi.java.JavaTypeDescriptor;
import org.hibernate.type.descriptor.spi.sql.SqlTypeDescriptor;

/**
 * Essentially acts as a parameter-object to {@link BasicTypeRegistry#resolveBasicType}.  Defines
 * the
 *
 * @author Steve Ebersole
 */
public interface BasicTypeParameters<T> {
	JavaTypeDescriptor<T> getJavaTypeDescriptor();
	SqlTypeDescriptor getSqlTypeDescriptor();
	AttributeConverterDefinition getAttributeConverterDefinition();
	MutabilityPlan<T> getMutabilityPlan();
	Comparator<T> getComparator();
	javax.persistence.TemporalType getTemporalPrecision();
}
