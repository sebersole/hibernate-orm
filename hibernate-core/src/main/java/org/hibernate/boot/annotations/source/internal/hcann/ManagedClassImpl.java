/*
 * Hibernate, Relational Persistence for Idiomatic Java
 *
 * License: GNU Lesser General Public License (LGPL), version 2.1 or later.
 * See the lgpl.txt file in the root directory or http://www.gnu.org/licenses/lgpl-2.1.html.
 */
package org.hibernate.boot.annotations.source.internal.hcann;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Consumer;

import org.hibernate.annotations.common.reflection.XClass;
import org.hibernate.annotations.common.reflection.XMethod;
import org.hibernate.annotations.common.reflection.XProperty;
import org.hibernate.boot.annotations.source.internal.LazyAnnotationTarget;
import org.hibernate.boot.annotations.source.spi.AnnotationProcessingContext;
import org.hibernate.boot.annotations.source.spi.FieldDetails;
import org.hibernate.boot.annotations.source.spi.ManagedClass;
import org.hibernate.boot.annotations.source.spi.ManagedClassRegistry;
import org.hibernate.boot.annotations.source.spi.MethodDetails;
import org.hibernate.internal.util.IndexedConsumer;
import org.hibernate.internal.util.collections.CollectionHelper;

/**
 * ManagedClass implementation based on a {@link XClass} reference
 *
 * @author Steve Ebersole
 */
public class ManagedClassImpl extends LazyAnnotationTarget implements ManagedClass {
	private final XClass xClass;

	private final ManagedClass superType;
	private List<ManagedClass> implementedInterfaces;

	private List<FieldDetailsImpl> fields;
	private List<MethodDetailsImpl> methods;

	public ManagedClassImpl(XClass xClass, AnnotationProcessingContext processingContext) {
		super( xClass::getAnnotations, processingContext );
		this.xClass = xClass;

		this.superType = determineSuperType( xClass, processingContext );

		processingContext.getManagedClassRegistry().addManagedClass( this );
	}

	private ManagedClass determineSuperType(XClass xClass, AnnotationProcessingContext processingContext) {
		final XClass superclass = xClass.getSuperclass();
		if ( superclass == null ) {
			return null;
		}
		if ( Object.class.getName().equals( superclass.getName() ) ) {
			return null;
		}

		final ManagedClassRegistry managedClassRegistry = processingContext.getManagedClassRegistry();
		final ManagedClass existing = managedClassRegistry.findManagedClass( superclass.getName() );
		if ( existing != null ) {
			return existing;
		}

		final ManagedClassImpl managedClass = new ManagedClassImpl( superclass, processingContext );
		managedClassRegistry.addManagedClass( managedClass );
		return managedClass;
	}

	@Override
	public String getName() {
		return getClassName();
	}

	@Override
	public String getClassName() {
		return xClass.getName();
	}

	@Override
	public boolean isAbstract() {
		return xClass.isAbstract();
	}

	@Override
	public ManagedClass getSuperType() {
		return superType;
	}

	@Override
	public List<ManagedClass> getImplementedInterfaceTypes() {
		if ( implementedInterfaces == null ) {
			implementedInterfaces = buildImplementedInterfaces();
		}

		return implementedInterfaces;
	}

	private List<ManagedClass> buildImplementedInterfaces() {
		throw new UnsupportedOperationException( "Not yet implemented" );
	}

	@Override
	public List<FieldDetails> getFields() {
		if ( fields == null ) {
			fields = resolveFields( xClass, getProcessingContext() );
		}
		//noinspection unchecked,rawtypes
		return (List) fields;
	}

	private static List<FieldDetailsImpl> resolveFields(XClass xClass, AnnotationProcessingContext processingContext) {
		final List<XProperty> xFields = xClass.getDeclaredProperties( "field" );
		final ArrayList<FieldDetailsImpl> fields = CollectionHelper.arrayList( xFields.size() );
		for ( int i = 0; i < xFields.size(); i++ ) {
			final XProperty xField = xFields.get( i );
			fields.add( new FieldDetailsImpl( xField, processingContext ) );
		}
		return fields;
	}

	@Override
	public void forEachField(IndexedConsumer<FieldDetails> consumer) {
		if ( fields == null ) {
			return;
		}
		//noinspection unchecked
		fields.forEach( (Consumer<FieldDetails>) consumer );
	}

	@Override
	public List<MethodDetails> getMethods() {
		if ( methods == null ) {
			methods = resolveMethods( xClass, getProcessingContext() );
		}
		//noinspection unchecked,rawtypes
		return (List) methods;
	}

	private static List<MethodDetailsImpl> resolveMethods(
			XClass xClass,
			AnnotationProcessingContext processingContext) {
		final List<XMethod> xMethods = xClass.getDeclaredMethods();
		final ArrayList<MethodDetailsImpl> methods = CollectionHelper.arrayList( xMethods.size() );
		for ( int i = 0; i < xMethods.size(); i++ ) {
			final XMethod xMethod = xMethods.get( i );
			methods.add( new MethodDetailsImpl( xMethod, processingContext ) );
		}
		return methods;
	}

	@Override
	public void forEachMethod(IndexedConsumer<MethodDetails> consumer) {
		if ( methods == null ) {
			return;
		}
		//noinspection unchecked,rawtypes
		methods.forEach( (Consumer) consumer );
	}
}
