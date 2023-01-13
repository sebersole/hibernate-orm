/*
 * Hibernate, Relational Persistence for Idiomatic Java
 *
 * License: GNU Lesser General Public License (LGPL), version 2.1 or later.
 * See the lgpl.txt file in the root directory or http://www.gnu.org/licenses/lgpl-2.1.html.
 */
package org.hibernate.boot.annotations.source.spi;

/**
 * Models a "{@linkplain java.lang.reflect.Method method}" in a {@link ManagedClass}
 *
 * @author Steve Ebersole
 */
public interface MethodDetails extends MemberDetails {
	@Override
	default Kind getKind() {
		return Kind.METHOD;
	}
}
