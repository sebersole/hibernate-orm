/*
 * Hibernate, Relational Persistence for Idiomatic Java
 *
 * License: GNU Lesser General Public License (LGPL), version 2.1 or later.
 * See the lgpl.txt file in the root directory or <http://www.gnu.org/licenses/lgpl-2.1.html>.
 */

/*
 * Hibernate, Relational Persistence for Idiomatic Java
 *
 * Copyright (c) 2014, Red Hat Inc. or third-party contributors as
 * indicated by the @author tags or express copyright attribution
 * statements applied by the authors.  All third-party contributions are
 * distributed under license by Red Hat Inc.
 *
 * This copyrighted material is made available to anyone wishing to use, modify,
 * copy, or redistribute it subject to the terms and conditions of the GNU
 * Lesser General Public License, as published by the Free Software Foundation.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY
 * or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU Lesser General Public License
 * for more details.
 *
 * You should have received a copy of the GNU Lesser General Public License
 * along with this distribution; if not, write to:
 * Free Software Foundation, Inc.
 * 51 Franklin Street, Fifth Floor
 * Boston, MA  02110-1301  USA
 */
package org.hibernate.boot.model.source.internal;

import org.jboss.jandex.ClassInfo;
import org.jboss.jandex.FieldInfo;
import org.jboss.jandex.MethodInfo;

/**
 * Fills-in non-public aspects of the {@link java.lang.reflect.Modifier} class
 *
 * @author Steve Ebersole
 */
public class ModifierUtils {

	private static final int BRIDGE    = 0x00000040;
	private static final int ENUM      = 0x00004000;
	private static final int SYNTHETIC = 0x00001000;

	/**
	 * Disallow instantiation.  This is a utility class, use statically.
	 */
	private ModifierUtils() {
	}

	/**
	 * Determine if the given method is a bridge.
	 *
	 * @param methodInfo The method descriptor to check
	 *
	 * @return {@code true} if the method is a bridge , {@code false} otherwise.
	 */
	public static boolean isBridge(MethodInfo methodInfo) {
		return (methodInfo.flags() & BRIDGE) != 0;
	}

	/**
	 * Determine if the given Java type is an enum.
	 *
	 * @param classInfo The descriptor of the Java type to check
	 *
	 * @return {@code true} if the Java type is an enum, {@code false} otherwise.
	 */
	public static boolean isEnum(ClassInfo classInfo) {
		return (classInfo.flags() & ENUM) != 0;
	}

	/**
	 * Determine is the given field is synthetic
	 *
	 * @param fieldInfo The field to check
	 *
	 * @return {@code true} if the field is synthetic, {@code false} otherwise.
	 */
	public static boolean isSynthetic(FieldInfo fieldInfo) {
		return (fieldInfo.flags() & SYNTHETIC) != 0;
	}

	/**
	 * Determine is the given method is synthetic
	 *
	 * @param methodInfo The method to check
	 *
	 * @return {@code true} if the method is synthetic, {@code false} otherwise.
	 */
	public static boolean isSynthetic(MethodInfo methodInfo) {
		return (methodInfo.flags() & SYNTHETIC) != 0;
	}
}
