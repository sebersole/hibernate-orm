/*
 * Hibernate, Relational Persistence for Idiomatic Java
 *
 * License: GNU Lesser General Public License (LGPL), version 2.1 or later.
 * See the lgpl.txt file in the root directory or http://www.gnu.org/licenses/lgpl-2.1.html.
 */
package org.hibernate.boot.annotations.source.spi;

import java.util.List;

import org.hibernate.internal.util.IndexedConsumer;

import jakarta.persistence.spi.PersistenceUnitInfo;

/**
 * A descriptor for "special classes" that Hibernate knows about through various means -
 * entities, embeddables, mapped-superclasses, attribute-converters, listeners, etc
 *
 * Models a {@linkplain PersistenceUnitInfo#getManagedClassNames() "managed class"},
 * but in the larger sense for all the "special classes" we know about -
 * entities, embeddables, mapped-superclasses, attribute-converters, listeners, etc
 * <p/>
 *
 * @author Steve Ebersole
 */
public interface ManagedClass extends AnnotationTarget {
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

	@Override
	default Kind getKind() {
		return Kind.CLASS;
	}

	boolean isAbstract();

	ManagedClass getSuperType();

	List<ManagedClass> getImplementedInterfaceTypes();

	default boolean implementsInterface(Class<?> interfaceJavaType) {
		return implementsInterface( interfaceJavaType.getName() );
	}

	default boolean implementsInterface(ManagedClass interfaceType) {
		return implementsInterface( interfaceType.getClassName() );
	}

	default boolean implementsInterface(String interfaceTypeName) {
		final List<ManagedClass> implementedInterfaceTypes = getImplementedInterfaceTypes();
		for ( int i = 0; i < implementedInterfaceTypes.size(); i++ ) {
			final ManagedClass implementedInterfaceType = implementedInterfaceTypes.get( i );
			if ( implementedInterfaceType.getClassName().equals( interfaceTypeName ) ) {
				return true;
			}
		}
		return false;
	}

	/**
	 * Get the fields for this class
	 */
	List<FieldDetails> getFields();

	/**
	 * Visit each field
	 */
	void forEachField(IndexedConsumer<FieldDetails> consumer);

	/**
	 * Get the methods for this class
	 */
	List<MethodDetails> getMethods();

	/**
	 * Visit each method
	 */
	void forEachMethod(IndexedConsumer<MethodDetails> consumer);
}
