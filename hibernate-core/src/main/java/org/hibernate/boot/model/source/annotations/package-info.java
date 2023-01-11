/*
 * Hibernate, Relational Persistence for Idiomatic Java
 *
 * License: GNU Lesser General Public License (LGPL), version 2.1 or later.
 * See the lgpl.txt file in the root directory or http://www.gnu.org/licenses/lgpl-2.1.html.
 */

/**
 * Abstractions of reflection style references like
 * Reflection-style {@link java.lang.Class}, {@link java.lang.reflect.Member}
 * abstraction used as targets for
 * Support for modeling the application domain model per mapping annotations.
 * <p/>
 * Uses {@link org.hibernate.boot.annotations} to abstract between 2 different sources<ul>
 *     <li>
 *         actual {@link java.lang.annotation.Annotation} instances
 *     </li>
 *     <li>
 *         {@code orm.xml} {@linkplain org.hibernate.boot.jaxb.mapping.JaxbEntityMappings JAXB bindings}
 *     </li>
 * </ul>
 *
 * actual
 * {@link java.lang.annotation.Annotation} instances and
 * {@linkplain org.hibernate.boot.jaxb.mapping.JaxbEntityMappings JAXB bindings}
 * @author Steve Ebersole
 */
package org.hibernate.boot.model.source.annotations;
