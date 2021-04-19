/*
 * Hibernate, Relational Persistence for Idiomatic Java
 *
 * License: GNU Lesser General Public License (LGPL), version 2.1 or later.
 * See the lgpl.txt file in the root directory or <http://www.gnu.org/licenses/lgpl-2.1.html>.
 */
package org.hibernate.boot.model.process.jandex.internal;

import java.lang.reflect.Field;
import java.util.List;
import java.util.Map;

import org.hibernate.HibernateException;
import org.hibernate.internal.util.ReflectHelper;

import org.jboss.jandex.AnnotationInstance;
import org.jboss.jandex.DotName;
import org.jboss.jandex.Index;

/**
 * Mainly a helper for reflecting on aspects of Jandex's {@link org.jboss.jandex.Index}
 * that it does not expose
 */
public class JandexIndexImplHelper {
	/**
	 * Accessto `org.jboss.jandex.Index#annotations`
	 */
	public static Map<DotName, List<AnnotationInstance>> getAnnotationInstanceMap(Index index) {
		// should only ever happen once during bootstrap, so no need to cache this
		try {
			final Field field = ReflectHelper.findField(
					Index.class,
					"annotations",
					() -> new HibernateException( "Unable to locate `org.jboss.jandex.Index#annotations` reflectively`" )
			);
			return (Map<DotName, List<AnnotationInstance>>) field.get( index );
		}
		catch (IllegalAccessException e) {
			throw new HibernateException( "Unable to access `org.jboss.jandex.Index#annotations` reflectively", e );
		}
	}
}
