/*
 * Hibernate, Relational Persistence for Idiomatic Java
 *
 * License: GNU Lesser General Public License (LGPL), version 2.1 or later.
 * See the lgpl.txt file in the root directory or http://www.gnu.org/licenses/lgpl-2.1.html.
 */
package org.hibernate.boot.annotations.source.spi;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.function.Consumer;

/**
 * Registry of all {@link ManagedClass} references
 *
 * @author Steve Ebersole
 */
public class ManagedClassRegistry {
	private final AnnotationProcessingContext context;

	private final Map<String, ManagedClass> managedClassMap = new ConcurrentHashMap<>();
	private final Map<String, List<ManagedClass>> subTypeManagedClassMap = new ConcurrentHashMap<>();

	public ManagedClassRegistry(AnnotationProcessingContext context) {
		this.context = context;
	}

	public ManagedClass findManagedClass(String name) {
		return managedClassMap.get( name );
	}

	public ManagedClass getManagedClass(String name) {
		final ManagedClass named = managedClassMap.get( name );
		if ( named == null ) {
			throw new UnknownManagedClassException( "Unknown managed class" );
		}
		return named;
	}

	public void forEachManagedClass(Consumer<ManagedClass> consumer) {
		managedClassMap.values().forEach( consumer );
	}

	public List<ManagedClass> getDirectSubTypes(String superTypeName) {
		return subTypeManagedClassMap.get( superTypeName );
	}

	public void forEachDirectSubType(String superTypeName, Consumer<ManagedClass> consumer) {
		final List<ManagedClass> directSubTypes = getDirectSubTypes( superTypeName );
		if ( directSubTypes != null ) {
			directSubTypes.forEach( consumer );
		}
	}

	public void addManagedClass(ManagedClass managedClass) {
		addManagedClass( managedClass.getClassName(), managedClass );
	}

	public void addManagedClass(String name, ManagedClass managedClass) {
		managedClassMap.put( name, managedClass );

		if ( managedClass.getSuperType() != null ) {
			List<ManagedClass> subTypes = subTypeManagedClassMap.get( managedClass.getSuperType().getName() );
			if ( subTypes == null ) {
				subTypes = new ArrayList<>();
				subTypeManagedClassMap.put( managedClass.getSuperType().getName(), subTypes );
			}
			subTypes.add( managedClass );
		}
	}

}
