/*
 * Hibernate, Relational Persistence for Idiomatic Java
 *
 * License: GNU Lesser General Public License (LGPL), version 2.1 or later.
 * See the lgpl.txt file in the root directory or http://www.gnu.org/licenses/lgpl-2.1.html.
 */
package org.hibernate.boot.annotations.source.internal.dynamic;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.function.Consumer;

import org.hibernate.boot.annotations.source.spi.ClassDetails;
import org.hibernate.boot.annotations.source.spi.FieldDetails;
import org.hibernate.boot.annotations.source.spi.MethodDetails;
import org.hibernate.boot.annotations.spi.AnnotationProcessingContext;
import org.hibernate.internal.util.IndexedConsumer;

/**
 * ClassDetails implementation for a {@linkplain org.hibernate.metamodel.RepresentationMode#MAP dynamic model} class
 *
 * @author Steve Ebersole
 */
public class ClassDetailsImpl extends AbstractDynamicAnnotationTarget implements ClassDetails {
	private final String name;
	private final String className;
	private final ClassDetails superType;

	private final List<FieldDetailsImpl> fields = new ArrayList<>();
	private final List<MethodDetailsImpl> methods = new ArrayList<>();

	public ClassDetailsImpl(
			String name,
			String className,
			ClassDetailsImpl superType,
			AnnotationProcessingContext processingContext) {
		super( processingContext );
		this.name = name;
		this.className = className;
		this.superType = superType;

		processingContext.getClassDetailsRegistry().addManagedClass( name, this );
	}

	@Override
	public String getName() {
		return name;
	}

	@Override
	public String getClassName() {
		return className;
	}

	@Override
	public boolean isAbstract() {
		return false;
	}

	@Override
	public ClassDetails getSuperType() {
		return superType;
	}

	@Override
	public List<ClassDetails> getImplementedInterfaceTypes() {
		return Collections.emptyList();
	}

	@Override
	public List<FieldDetails> getFields() {
		//noinspection rawtypes,unchecked
		return (List) fields;
	}

	@Override
	public void forEachField(IndexedConsumer<FieldDetails> consumer) {
		//noinspection unchecked,rawtypes
		fields.forEach( (Consumer) consumer );
	}

	@Override
	public List<MethodDetails> getMethods() {
		//noinspection rawtypes,unchecked
		return (List) methods;
	}

	@Override
	public void forEachMethod(IndexedConsumer<MethodDetails> consumer) {
		//noinspection unchecked,rawtypes
		methods.forEach( (Consumer) consumer );
	}

	@Override
	public <X> Class<X> toJavaClass() {
		throw new UnsupportedOperationException();
	}

	public void addField(FieldDetailsImpl field) {
		fields.add( field );
	}

	public void addMethod(MethodDetailsImpl method) {
		methods.add( method );
	}
}
