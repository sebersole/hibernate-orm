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
import java.util.function.Supplier;

import org.hibernate.boot.annotations.source.UnknownManagedClassException;
import org.hibernate.boot.annotations.source.internal.hcann.ClassDetailsBuilderImpl;
import org.hibernate.boot.annotations.spi.AnnotationProcessingContext;

/**
 * Registry of all {@link ClassDetails} references
 *
 * @author Steve Ebersole
 */
public class ClassDetailsRegistry {
	private final AnnotationProcessingContext context;

	private final ClassDetailsBuilder fallbackClassDetailsBuilder;
	private final Map<String, ClassDetails> managedClassMap = new ConcurrentHashMap<>();
	private final Map<String, List<ClassDetails>> subTypeManagedClassMap = new ConcurrentHashMap<>();

	public ClassDetailsRegistry(AnnotationProcessingContext context) {
		this.context = context;
		this.fallbackClassDetailsBuilder = new ClassDetailsBuilderImpl( context );
	}

	public ClassDetails findManagedClass(String name) {
		return managedClassMap.get( name );
	}

	public ClassDetails getManagedClass(String name) {
		final ClassDetails named = managedClassMap.get( name );
		if ( named == null ) {
			throw new UnknownManagedClassException( "Unknown managed class" );
		}
		return named;
	}

	public void forEachManagedClass(Consumer<ClassDetails> consumer) {
		managedClassMap.values().forEach( consumer );
	}

	public List<ClassDetails> getDirectSubTypes(String superTypeName) {
		return subTypeManagedClassMap.get( superTypeName );
	}

	public void forEachDirectSubType(String superTypeName, Consumer<ClassDetails> consumer) {
		final List<ClassDetails> directSubTypes = getDirectSubTypes( superTypeName );
		if ( directSubTypes != null ) {
			directSubTypes.forEach( consumer );
		}
	}

	public void addManagedClass(ClassDetails classDetails) {
		addManagedClass( classDetails.getClassName(), classDetails );
	}

	public void addManagedClass(String name, ClassDetails classDetails) {
		managedClassMap.put( name, classDetails );

		if ( classDetails.getSuperType() != null ) {
			List<ClassDetails> subTypes = subTypeManagedClassMap.get( classDetails.getSuperType().getName() );
			if ( subTypes == null ) {
				subTypes = new ArrayList<>();
				subTypeManagedClassMap.put( classDetails.getSuperType().getName(), subTypes );
			}
			subTypes.add( classDetails );
		}
	}

	public ClassDetails resolveManagedClass(String name) {
		return resolveManagedClass( name, fallbackClassDetailsBuilder );
	}

	public ClassDetails resolveManagedClass(
			String name,
			ClassDetailsBuilder creator) {
		final ClassDetails existing = managedClassMap.get( name );
		if ( existing != null ) {
			return existing;
		}

		final ClassDetails created = creator.buildClassDetails( name, context );
		addManagedClass( name, created );
		return created;
	}

	public ClassDetails resolveManagedClass(
			String name,
			Supplier<ClassDetails> creator) {
		final ClassDetails existing = managedClassMap.get( name );
		if ( existing != null ) {
			return existing;
		}

		final ClassDetails created = creator.get();
		addManagedClass( name, created );
		return created;
	}
}
