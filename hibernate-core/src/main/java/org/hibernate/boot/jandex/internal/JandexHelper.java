/*
 * Hibernate, Relational Persistence for Idiomatic Java
 *
 * License: GNU Lesser General Public License (LGPL), version 2.1 or later
 * See the lgpl.txt file in the root directory or http://www.gnu.org/licenses/lgpl-2.1.html
 */
package org.hibernate.boot.jandex.internal;

import java.lang.annotation.Annotation;

import org.jboss.jandex.DotName;

/**
 * @author Steve Ebersole
 */
public class JandexHelper {
	/**
	 * Pseudonym for {@link DotName#createSimple(Class)}
	 */
	public static DotName toDotName(Class<?> javaClass) {
		return DotName.createSimple( javaClass );
	}

	/**
	 * Pseudonym for {@link DotName#createSimple(String)}
	 */
	public static DotName toDotName(String name) {
		return DotName.createSimple( name );
	}

	public static <T extends Annotation> Class<T> resolveAnnotationClass(DotName annotationClassName) {
		try {
			//noinspection unchecked
			return (Class<T>) JandexHelper.class.getClassLoader().loadClass( annotationClassName.toString() );
		}
		catch (ClassNotFoundException e) {
			throw new RuntimeException( e );
		}
	}

	private JandexHelper() {
	}
}
