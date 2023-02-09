/*
 * Hibernate, Relational Persistence for Idiomatic Java
 *
 * License: GNU Lesser General Public License (LGPL), version 2.1 or later.
 * See the lgpl.txt file in the root directory or http://www.gnu.org/licenses/lgpl-2.1.html.
 */
package org.hibernate.boot.annotations.source.internal.reflection;

import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.function.Consumer;

import org.hibernate.boot.annotations.source.internal.LazyAnnotationTarget;
import org.hibernate.boot.annotations.source.spi.ClassDetails;
import org.hibernate.boot.annotations.source.spi.ClassDetailsRegistry;
import org.hibernate.boot.annotations.source.spi.FieldDetails;
import org.hibernate.boot.annotations.source.spi.MethodDetails;
import org.hibernate.boot.annotations.spi.AnnotationProcessingContext;
import org.hibernate.internal.util.IndexedConsumer;
import org.hibernate.internal.util.collections.ArrayHelper;
import org.hibernate.internal.util.collections.CollectionHelper;

/**
 * ClassDetails implementation based on a {@link Class} reference
 *
 * @author Steve Ebersole
 */
public class ClassDetailsImpl extends LazyAnnotationTarget implements ClassDetails {
	private final String name;
	private final Class<?> managedClass;

	private final ClassDetails superType;
	private List<ClassDetails> interfaces;

	private List<FieldDetailsImpl> fields;
	private List<MethodDetailsImpl> methods;

	public ClassDetailsImpl(
			Class<?> managedClass,
			AnnotationProcessingContext processingContext) {
		this( managedClass.getName(), managedClass, processingContext );
	}

	public ClassDetailsImpl(
			String name,
			Class<?> managedClass,
			AnnotationProcessingContext processingContext) {
		super( managedClass::getDeclaredAnnotations, processingContext );
		this.name = name;
		this.managedClass = managedClass;

		final ClassDetailsRegistry classDetailsRegistry = processingContext.getClassDetailsRegistry();

		final Class<?> superclass = managedClass.getSuperclass();
		if ( superclass == null ) {
			superType = null;
		}
		else {
			superType = classDetailsRegistry.resolveManagedClass(
					superclass.getName(),
					() -> ClassDetailsBuilderImpl.INSTANCE.buildClassDetails( superclass, getProcessingContext() )
			);
		}

		classDetailsRegistry.addManagedClass( this );
	}

	@Override
	public String getName() {
		return name;
	}

	@Override
	public String getClassName() {
		return managedClass.getName();
	}

	@Override
	public Class<?> toJavaClass() {
		return managedClass;
	}

	@Override
	public boolean isAbstract() {
		return Modifier.isAbstract( managedClass.getModifiers() );
	}

	@Override
	public ClassDetails getSuperType() {
		return superType;
	}

	@Override
	public List<ClassDetails> getImplementedInterfaceTypes() {
		if ( interfaces == null ) {
			interfaces = collectInterfaces();
		}
		return interfaces;
	}

	private List<ClassDetails> collectInterfaces() {
		final Class<?>[] interfaceClasses = managedClass.getInterfaces();
		if ( ArrayHelper.isEmpty( interfaceClasses ) ) {
			return Collections.emptyList();
		}

		final ArrayList<ClassDetails> result = CollectionHelper.arrayList( interfaceClasses.length );
		for ( int i = 0; i < interfaceClasses.length; i++ ) {
			final Class<?> interfaceClass = interfaceClasses[ i ];
			final ClassDetails interfaceDetails = getProcessingContext().getClassDetailsRegistry().resolveManagedClass(
					interfaceClass.getName(),
					() -> ClassDetailsBuilderImpl.INSTANCE.buildClassDetails( interfaceClass, getProcessingContext() )
			);
			result.add( interfaceDetails );
		}
		return result;
	}

	@Override
	public List<FieldDetails> getFields() {
		if ( fields == null ) {
			final Field[] reflectionFields = managedClass.getFields();
			this.fields = CollectionHelper.arrayList( reflectionFields.length );
			for ( int i = 0; i < reflectionFields.length; i++ ) {
				final Field reflectionField = reflectionFields[i];
				fields.add( new FieldDetailsImpl( reflectionField, getProcessingContext() ) );
			}
		}
		//noinspection unchecked,rawtypes
		return (List) fields;
	}

	@Override
	public void forEachField(IndexedConsumer<FieldDetails> consumer) {
		if ( fields == null ) {
			return;
		}

		//noinspection unchecked,rawtypes
		fields.forEach( (Consumer) consumer );
	}

	@Override
	public List<MethodDetails> getMethods() {
		if ( methods == null ) {
			final Method[] reflectionMethods = managedClass.getMethods();
			this.methods = CollectionHelper.arrayList( reflectionMethods.length );
			for ( int i = 0; i < reflectionMethods.length; i++ ) {
				this.methods.add( new MethodDetailsImpl( reflectionMethods[i], getProcessingContext() ) );
			}
		}
		//noinspection unchecked,rawtypes
		return (List) methods;
	}

	@Override
	public void forEachMethod(IndexedConsumer<MethodDetails> consumer) {
		if ( methods == null ) {
			return;
		}

		//noinspection unchecked,rawtypes
		methods.forEach( (Consumer) consumer );
	}

	@Override
	public String toString() {
		return "ClassDetails(reflection) {" +
				"    name='" + name + "'," +
				"    managedClass='" + managedClass.getName() + "'," +
				"	 sys-hash-code=" + System.identityHashCode( this ) +
				"}";
	}
}
