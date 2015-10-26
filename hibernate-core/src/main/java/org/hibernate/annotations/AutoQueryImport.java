/*
 * Hibernate, Relational Persistence for Idiomatic Java
 *
 * License: GNU Lesser General Public License (LGPL), version 2.1 or later.
 * See the lgpl.txt file in the root directory or <http://www.gnu.org/licenses/lgpl-2.1.html>.
 */
package org.hibernate.annotations;

import java.lang.annotation.Retention;
import java.lang.annotation.Target;

import static java.lang.annotation.ElementType.TYPE;
import static java.lang.annotation.RetentionPolicy.RUNTIME;

/**
 * Attached to an entity class and controls whether the simple entity name should be automatically
 * imported into the HQL/JPQL namespace.  In other words, given an entity class named
 * {@code foo.bar.Baz} importing would make the entity referenceable in HQL/JPQL by the
 * simple name "Baz".
 * <p/>
 * Values specified for {@link javax.persistence.Entity#name()} are also automatically imported.
 * <p/>
 * The default is to import simple names.  This may need to be disabled when there is a collision; e.g.
 * if you have entity classes {@code foo.bar.Baz} and {@code com.acme.Baz}
 *
 * @author Steve Ebersole
 */
@Target(TYPE)
@Retention(RUNTIME)
public @interface AutoQueryImport {
	/**
	 * Should the entity class's simple name be imported into the HQL/JPQL namespace?
	 */
	boolean importName() default true;
}
