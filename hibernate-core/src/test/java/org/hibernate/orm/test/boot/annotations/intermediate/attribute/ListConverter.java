/*
 * Hibernate, Relational Persistence for Idiomatic Java
 *
 * License: GNU Lesser General Public License (LGPL), version 2.1 or later.
 * See the lgpl.txt file in the root directory or http://www.gnu.org/licenses/lgpl-2.1.html.
 */
package org.hibernate.orm.test.boot.annotations.intermediate.attribute;

import java.util.List;

import jakarta.persistence.AttributeConverter;

/**
 * @author Steve Ebersole
 */
public class ListConverter implements AttributeConverter<List<String>,String> {
	@Override
	public String convertToDatabaseColumn(List<String> attribute) {
		return null;
	}

	@Override
	public List<String> convertToEntityAttribute(String dbData) {
		return null;
	}
}
