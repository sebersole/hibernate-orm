/*
 * Hibernate, Relational Persistence for Idiomatic Java
 *
 * License: GNU Lesser General Public License (LGPL), version 2.1 or later.
 * See the lgpl.txt file in the root directory or http://www.gnu.org/licenses/lgpl-2.1.html.
 */

/**
 * Package for the first phase in processing {@linkplain org.hibernate.boot.annotations.source annotation sources}
 * which is to categorize the {@linkplain org.hibernate.boot.annotations.source.spi.ManagedClass managed classes}
 * as {@linkplain org.hibernate.boot.annotations.type.spi.EntityTypeMetadata entities},
 * {@linkplain org.hibernate.boot.annotations.type.spi.MappedSuperclassTypeMetadata mapped-superclasses},
 * etc.
 *
 * @author Steve Ebersole
 */
package org.hibernate.boot.annotations.type;
