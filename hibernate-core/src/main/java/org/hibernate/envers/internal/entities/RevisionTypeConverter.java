/*
 * Hibernate, Relational Persistence for Idiomatic Java
 *
 * License: GNU Lesser General Public License (LGPL), version 2.1 or later.
 * See the lgpl.txt file in the root directory or <http://www.gnu.org/licenses/lgpl-2.1.html>.
 */
package org.hibernate.envers.internal.entities;

import javax.persistence.AttributeConverter;

import org.hibernate.envers.RevisionType;

/**
 * @author Chris Cranford
 */
public class RevisionTypeConverter implements AttributeConverter<RevisionType, Integer> {
	@Override
	public Integer convertToDatabaseColumn(RevisionType attribute) {
		return attribute == null ? null : attribute.getRepresentation().intValue();
	}

	@Override
	public RevisionType convertToEntityAttribute(Integer dbData) {
		return RevisionType.fromRepresentation( dbData == null ? null : dbData.byteValue() );
	}
}
