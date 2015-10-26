/*
 * Hibernate, Relational Persistence for Idiomatic Java
 *
 * License: GNU Lesser General Public License (LGPL), version 2.1 or later.
 * See the lgpl.txt file in the root directory or <http://www.gnu.org/licenses/lgpl-2.1.html>.
 */
package org.hibernate.annotations;

import java.lang.annotation.Retention;
import java.lang.annotation.Target;

import static java.lang.annotation.ElementType.PACKAGE;
import static java.lang.annotation.ElementType.TYPE;
import static java.lang.annotation.RetentionPolicy.RUNTIME;

/**
 * An explicit query import.  Grouped by {@link org.hibernate.annotations.QueryImports}
 *
 * @author Steve Ebersole
 */
@Target({TYPE,PACKAGE})
@Retention(RUNTIME)
public @interface QueryImport {
	/**
	 * The name imported into the HQL/JPQL namespace.  In other words, the name as it would appear
	 * in HQL/JPQL query.
	 */
	String importedName();

	/**
	 * The proper name that the imported name resolves to.
	 * <p/>
	 * If attached to a Class, the FQN of that Class is used by default.
	 */
	String name() default "";
}
