/*
 * Hibernate, Relational Persistence for Idiomatic Java
 *
 * License: GNU Lesser General Public License (LGPL), version 2.1 or later.
 * See the lgpl.txt file in the root directory or <http://www.gnu.org/licenses/lgpl-2.1.html>.
 */
package org.hibernate.jpa.internal.util;

import javax.persistence.LockModeType;

import org.hibernate.LockMode;
import org.hibernate.internal.util.LockModeConverter;

/**
 * Helper to deal with {@link LockModeType} <-> {@link LockMode} conversions.
 *
 * @author Steve Ebersole
 */
public final class LockModeTypeHelper {
	private LockModeTypeHelper() {
	}

	public static LockModeType getLockModeType(LockMode lockMode) {
		return LockModeConverter.convertToLockModeType( lockMode );
	}

	public static LockMode getLockMode(LockModeType lockModeType) {
		return LockModeConverter.convertToLockMode( lockModeType );
	}

	public static LockMode interpretLockMode(Object value) {
		if ( value == null ) {
			return LockMode.NONE;
		}

		if ( value instanceof LockMode ) {
			return (LockMode) value;
		}

		if ( value instanceof LockModeType ) {
			return getLockMode( (LockModeType) value );
		}

		if ( value instanceof String ) {
			// first try LockMode name
			final LockMode nativeLockMode = LockMode.fromExternalForm( (String) value );
			if ( nativeLockMode != null ) {
				return nativeLockMode;
			}

			final LockMode jpaLockModeCorollary = getLockMode( LockModeType.valueOf( (String) value ) );
			if ( jpaLockModeCorollary != null ) {
				return jpaLockModeCorollary;
			}
		}

		throw new IllegalArgumentException( "Unknown lock mode source : " + value );
	}

}
