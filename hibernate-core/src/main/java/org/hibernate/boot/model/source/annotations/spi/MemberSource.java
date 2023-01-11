/*
 * Hibernate, Relational Persistence for Idiomatic Java
 *
 * License: GNU Lesser General Public License (LGPL), version 2.1 or later.
 * See the lgpl.txt file in the root directory or http://www.gnu.org/licenses/lgpl-2.1.html.
 */
package org.hibernate.boot.model.source.annotations.spi;

import java.lang.reflect.Member;

import org.hibernate.boot.annotations.spi.AnnotationTarget;

/**
 * Details about a {@linkplain Member member} while processing annotations.
 *
 * @apiNote This can be a virtual member, meaning there is no physical
 * member in the declaring type
 *
 * @author Steve Ebersole
 */
public interface MemberSource extends AnnotationTarget {
	/**
	 * The name of the member.  This would be the name of the method or field.
	 */
	String getName();
}
