/*
 * Hibernate, Relational Persistence for Idiomatic Java
 *
 * License: GNU Lesser General Public License (LGPL), version 2.1 or later.
 * See the lgpl.txt file in the root directory or http://www.gnu.org/licenses/lgpl-2.1.html.
 */
package org.hibernate.boot.model.source.annotations.internal.hcann;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Consumer;

import org.hibernate.annotations.common.reflection.XClass;
import org.hibernate.annotations.common.reflection.XMethod;
import org.hibernate.annotations.common.reflection.XProperty;
import org.hibernate.boot.annotations.internal.LazyAnnotationTarget;
import org.hibernate.boot.annotations.spi.AnnotationDescriptorRegistry;
import org.hibernate.boot.model.source.annotations.spi.FieldSource;
import org.hibernate.boot.model.source.annotations.spi.ManagedClass;
import org.hibernate.boot.model.source.annotations.spi.MethodSource;
import org.hibernate.internal.util.IndexedConsumer;
import org.hibernate.internal.util.collections.CollectionHelper;

/**
 * ManagedClass implementation based on a {@link XClass} reference
 *
 * @author Steve Ebersole
 */
public class ManagedClassImpl extends LazyAnnotationTarget implements ManagedClass {
	private final XClass xClass;
	private final AnnotationDescriptorRegistry annotationDescriptorRegistry;

	private List<FieldSourceImpl> fields;
	private List<MethodSourceImpl> methods;

	public ManagedClassImpl(XClass xClass, AnnotationDescriptorRegistry annotationDescriptorRegistry) {
		super( xClass::getAnnotations, annotationDescriptorRegistry );
		this.xClass = xClass;
		this.annotationDescriptorRegistry = annotationDescriptorRegistry;

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
	public List<FieldSource> getFields() {
		if ( fields == null ) {
			fields = resolveFields( xClass, annotationDescriptorRegistry );
		}
		//noinspection unchecked,rawtypes
		return (List) fields;
	}

	private static List<FieldSourceImpl> resolveFields(
			XClass xClass,
			AnnotationDescriptorRegistry annotationDescriptorRegistry) {
		final List<XProperty> xFields = xClass.getDeclaredProperties( "field" );
		final ArrayList<FieldSourceImpl> fields = CollectionHelper.arrayList( xFields.size() );
		for ( int i = 0; i < xFields.size(); i++ ) {
			final XProperty xField = xFields.get( i );
			fields.add( new FieldSourceImpl( xField, annotationDescriptorRegistry ) );
		}
		return fields;
	}

	@Override
	public void forEachField(IndexedConsumer<FieldSource> consumer) {
		if ( fields == null ) {
			return;
		}
		//noinspection unchecked
		fields.forEach( (Consumer<FieldSource>) consumer );
	}

	@Override
	public List<MethodSource> getMethods() {
		if ( methods == null ) {
			methods = resolveMethods( xClass, annotationDescriptorRegistry );
		}
		//noinspection unchecked,rawtypes
		return (List) methods;
	}

	private static List<MethodSourceImpl> resolveMethods(
			XClass xClass,
			AnnotationDescriptorRegistry annotationDescriptorRegistry) {
		final List<XMethod> xMethods = xClass.getDeclaredMethods();
		final ArrayList<MethodSourceImpl> methods = CollectionHelper.arrayList( xMethods.size() );
		for ( int i = 0; i < xMethods.size(); i++ ) {
			final XMethod xMethod = xMethods.get( i );
			methods.add( new MethodSourceImpl( xMethod, annotationDescriptorRegistry ) );
		}
		return methods;
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
