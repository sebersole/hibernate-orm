/*
 * Hibernate, Relational Persistence for Idiomatic Java
 *
 * License: GNU Lesser General Public License (LGPL), version 2.1 or later.
 * See the lgpl.txt file in the root directory or http://www.gnu.org/licenses/lgpl-2.1.html.
 */
package org.hibernate.boot.annotations.model;

import java.util.Locale;

import org.hibernate.MappingException;
import org.hibernate.boot.annotations.source.spi.ClassDetails;
import org.hibernate.boot.annotations.source.spi.MemberDetails;

import jakarta.persistence.Access;

/**
 * Indicates a problem with the placement of the {@link Access} annotation; either
 * {@linkplain jakarta.persistence.AccessType#FIELD FIELD} on a getter or
 * {@linkplain jakarta.persistence.AccessType#PROPERTY PROPERTY} on a field
 *
 * @author Steve Ebersole
 */
public class AccessTypePlacementException extends MappingException {
	public AccessTypePlacementException(ClassDetails classDetails, MemberDetails memberDetails) {
		super( craftMessage( classDetails, memberDetails ) );
	}

	private static String craftMessage(ClassDetails classDetails, MemberDetails memberDetails) {
		if ( memberDetails.isField() ) {
			return String.format(
					Locale.ROOT,
					"Field `%s.%s` defined `@Access(PROPERTY) - see section 2.3.2 of the specification",
					classDetails.getName(),
					memberDetails.getName()
			);
		}
		else {
			return String.format(
					Locale.ROOT,
					"Method `%s.%s` defined `@Access(FIELD) - see section 2.3.2 of the specification",
					classDetails.getName(),
					memberDetails.getName()
			);
		}

	}
}
