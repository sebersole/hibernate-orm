/*
 * Hibernate, Relational Persistence for Idiomatic Java
 *
 * License: GNU Lesser General Public License (LGPL), version 2.1 or later.
 * See the lgpl.txt file in the root directory or <http://www.gnu.org/licenses/lgpl-2.1.html>.
 */
package org.hibernate.envers.internal.entities;

import org.hibernate.envers.RevisionType;
import org.hibernate.metamodel.model.convert.internal.OrdinalEnumValueConverter;
import org.hibernate.type.descriptor.java.internal.IntegerJavaDescriptor;
import org.hibernate.type.descriptor.sql.spi.IntegerSqlDescriptor;
import org.hibernate.type.internal.StandardBasicTypeImpl;

/**
 * A hibernate type for the {@link RevisionType} enum.
 *
 * @author Adam Warski (adam at warski dot org)
 * @author Chris Cranford
 */
public class RevisionTypeType extends StandardBasicTypeImpl<RevisionType> {
	public static final RevisionTypeType INSTANCE = new RevisionTypeType();

	public RevisionTypeType() {
		super(
				RevisionTypeJavaDescriptor.INSTANCE,
				IntegerJavaDescriptor.INSTANCE,
				IntegerSqlDescriptor.INSTANCE,
				new OrdinalEnumValueConverter(
						RevisionTypeJavaDescriptor.INSTANCE,
						IntegerJavaDescriptor.INSTANCE
				)
		);
	}
}
