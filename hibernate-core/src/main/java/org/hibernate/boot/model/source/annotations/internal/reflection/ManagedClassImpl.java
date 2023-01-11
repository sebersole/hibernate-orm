/*
 * Hibernate, Relational Persistence for Idiomatic Java
 *
 * License: GNU Lesser General Public License (LGPL), version 2.1 or later.
 * See the lgpl.txt file in the root directory or http://www.gnu.org/licenses/lgpl-2.1.html.
 */
package org.hibernate.boot.model.source.annotations.internal.reflection;

import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.util.List;
import java.util.function.Consumer;

import org.hibernate.boot.annotations.internal.LazyAnnotationTarget;
import org.hibernate.boot.annotations.spi.AnnotationDescriptorRegistry;
import org.hibernate.boot.model.source.annotations.spi.FieldSource;
import org.hibernate.boot.model.source.annotations.spi.ManagedClass;
import org.hibernate.boot.model.source.annotations.spi.MethodSource;
import org.hibernate.internal.util.IndexedConsumer;
import org.hibernate.internal.util.collections.CollectionHelper;

/**
 * ManagedClass implementation based on a {@link Class} reference
 *
 * @author Steve Ebersole
 */
public class ManagedClassImpl extends LazyAnnotationTarget implements ManagedClass {
	private final String name;
	private final Class<?> managedClass;
	private final AnnotationDescriptorRegistry annotationDescriptorRegistry;

	private List<FieldSourceImpl> fields;
	private List<MethodSourceImpl> methods;

	public ManagedClassImpl(
			Class<?> managedClass,
			AnnotationDescriptorRegistry annotationDescriptorRegistry) {
		this( managedClass.getName(), managedClass, annotationDescriptorRegistry );
	}

	public ManagedClassImpl(
			String name,
			Class<?> managedClass,
			AnnotationDescriptorRegistry annotationDescriptorRegistry) {
		super( managedClass::getDeclaredAnnotations, annotationDescriptorRegistry );
		this.name = name;
		this.managedClass = managedClass;
		this.annotationDescriptorRegistry = annotationDescriptorRegistry;
	}

	@Override
	public String getName() {
		return name;
	}

	@Override
	public String getClassName() {
		return managedClass.getName();
	}

	public Class<?> getManagedClass() {
		return managedClass;
	}

	@Override
	public List<FieldSource> getFields() {
		if ( fields == null ) {
			final Field[] reflectionFields = managedClass.getFields();
			this.fields = CollectionHelper.arrayList( reflectionFields.length );
			for ( int i = 0; i < reflectionFields.length; i++ ) {
				final Field reflectionField = reflectionFields[i];
				fields.add( new FieldSourceImpl( reflectionField, annotationDescriptorRegistry ) );
			}
		}
		//noinspection unchecked,rawtypes
		return (List) fields;
	}

	@Override
	public void forEachField(IndexedConsumer<FieldSource> consumer) {
		if ( fields == null ) {
			return;
		}

		//noinspection unchecked,rawtypes
		fields.forEach( (Consumer) consumer );
	}

	@Override
	public List<MethodSource> getMethods() {
		if ( methods == null ) {
			final Method[] reflectionMethods = managedClass.getMethods();
			this.methods = CollectionHelper.arrayList( reflectionMethods.length );
			for ( int i = 0; i < reflectionMethods.length; i++ ) {
				this.methods.add( new MethodSourceImpl( reflectionMethods[i], annotationDescriptorRegistry ) );
			}
		}
		//noinspection unchecked,rawtypes
		return (List) methods;
	}

	@Override
	public void forEachMethod(IndexedConsumer<MethodSource> consumer) {
		if ( methods == null ) {
			return;
		}

		//noinspection unchecked,rawtypes
		methods.forEach( (Consumer) consumer );
	}
}
