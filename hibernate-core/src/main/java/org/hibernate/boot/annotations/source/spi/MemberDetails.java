/*
 * Hibernate, Relational Persistence for Idiomatic Java
 *
 * License: GNU Lesser General Public License (LGPL), version 2.1 or later.
 * See the lgpl.txt file in the root directory or http://www.gnu.org/licenses/lgpl-2.1.html.
 */
package org.hibernate.boot.annotations.source.spi;

import java.lang.reflect.Member;

/**
 * Details about a {@linkplain Member member} while processing annotations.
 *
 * @apiNote This can be a virtual member, meaning there is no physical
 * member in the declaring type (which itself might be virtual)
 *
 * @author Steve Ebersole
 */
public interface MemberDetails extends AnnotationTarget {
	/**
	 * The name of the member.  This would be the name of the method or field.
	 */
	String getName();

	/**
	 * The field type or method return type
	 */
	ClassDetails getType();

	/**
	 * Whether the member is a field.
	 *
	 * @return {@code true} indicates the member is a field; {@code false} indicates it is a method.
	 */
	default boolean isField() {
		return getKind() == Kind.FIELD;
	}

	/**
	 * Can this member be a persistent attribute
	 */
	boolean isPersistable();

	/**
	 * For members representing attributes, determine the attribute name
	 */
	String resolveAttributeName();
}
