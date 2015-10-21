/*
 * Hibernate, Relational Persistence for Idiomatic Java
 *
 * License: GNU Lesser General Public License (LGPL), version 2.1 or later.
 * See the lgpl.txt file in the root directory or <http://www.gnu.org/licenses/lgpl-2.1.html>.
 */
package org.hibernate.boot.model;

import java.util.List;

import org.jboss.jandex.AnnotationInstance;
import org.jboss.jandex.ClassInfo;
import org.jboss.jandex.FieldInfo;
import org.jboss.jandex.MethodInfo;
import org.jboss.jandex.Type;

/**
 * Wrapper around a Jandex MethodInfo or FieldInfo.
 *
 * @see PersistentAttributeMemberResolver
 */
public interface MemberDescriptor {
	/**
	 * Categorizes the wrapped types
	 */
	enum Kind { METHOD, FIELD }

	/**
	 * Obtan an indication of the kind of Jandex object wrapped this descriptor.
	 *
	 * @return Indication of the kind of Jandex object
	 */
	Kind kind();

	/**
	 * The corresponding persistent attribute name
	 *
	 * @return The persistent attribute nae
	 */
	String attributeName();

	/**
	 * The underlying method/field name
	 *
	 * @return The underlying method/field name
	 */
	String sourceName();

	ClassInfo declaringClass();

	Type type();

	List<AnnotationInstance> annotations();

	/**
	 * Obtain the Jandex object as a method.  Will result in exception if
	 * {@link #kind()} does not return {@link Kind#METHOD}
	 */
	MethodInfo asMethod();

	/**
	 * Obtain the Jandex object as a field.  Will result in exception if
	 * {@link #kind()} does not return {@link Kind#FIELD}
	 */
	FieldInfo asField();
}
