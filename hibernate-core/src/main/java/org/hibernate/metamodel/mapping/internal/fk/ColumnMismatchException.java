/*
 * Hibernate, Relational Persistence for Idiomatic Java
 *
 * License: GNU Lesser General Public License (LGPL), version 2.1 or later
 * See the lgpl.txt file in the root directory or http://www.gnu.org/licenses/lgpl-2.1.html
 */
package org.hibernate.metamodel.mapping.internal.fk;

import org.hibernate.MappingException;
import org.hibernate.metamodel.mapping.NonTransientException;

/**
 * Indicates a problem matching columns for the FK.  Usually this is in terms of the number of
 * columns on either side.
 *
 * @author Steve Ebersole
 */
public class ColumnMismatchException extends MappingException implements NonTransientException {
	public ColumnMismatchException(String message) {
		super( message );
	}

	public ColumnMismatchException(String message, Throwable cause) {
		super( message, cause );
	}
}
