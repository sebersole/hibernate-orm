/*
 * Hibernate, Relational Persistence for Idiomatic Java
 *
 * License: GNU Lesser General Public License (LGPL), version 2.1 or later.
 * See the lgpl.txt file in the root directory or http://www.gnu.org/licenses/lgpl-2.1.html.
 */
package org.hibernate.boot.model.source.annotations.spi;

import java.util.List;

import org.hibernate.internal.util.IndexedConsumer;

import jakarta.persistence.spi.PersistenceUnitInfo;

/**
 * Models a {@linkplain PersistenceUnitInfo#getManagedClassNames() "managed class"},
 * but in the larger sense for all the "special classes" we know about -
 * entities, embeddables, mapped-superclasses, attribute-converters, listeners, etc
 * <p/>
 *
 * @author Steve Ebersole
 */
public interface ManagedClass {
	/**
	 * The name of the managed class.
	 * <p/>
	 * Generally this is the same as the {@linkplain #getClassName() class name}.
	 * But in the case of Hibernate's {@code entity-name} feature, this would
	 * be the {@code entity-name}
	 */
	String getName();

	/**
	 * The name of the {@link Class} of this managed-type.
	 *
	 * @apiNote Will be {@code null} for dynamic models
	 */
	String getClassName();

	/**
	 * Get the fields for this class
	 */
	List<FieldSource> getFields();

	/**
	 * Visit each field
	 */
	void forEachField(IndexedConsumer<FieldSource> consumer);

	/**
	 * Get the methods for this class
	 */
	List<MethodSource> getMethods();

	/**
	 * Visit each method
	 */
	void forEachMethod(IndexedConsumer<MethodSource> consumer);
}
