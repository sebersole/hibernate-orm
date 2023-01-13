/*
 * Hibernate, Relational Persistence for Idiomatic Java
 *
 * License: GNU Lesser General Public License (LGPL), version 2.1 or later.
 * See the lgpl.txt file in the root directory or http://www.gnu.org/licenses/lgpl-2.1.html.
 */
package org.hibernate.boot.annotations.source.internal.dynamic;

import java.util.Collections;
import java.util.List;

import org.hibernate.boot.annotations.source.internal.DelayedAnnotationTarget;
import org.hibernate.boot.annotations.source.spi.AnnotationProcessingContext;
import org.hibernate.boot.annotations.source.spi.FieldDetails;
import org.hibernate.boot.annotations.source.spi.ManagedClass;
import org.hibernate.boot.annotations.source.spi.MethodDetails;
import org.hibernate.internal.util.IndexedConsumer;

/**
 * ManagedClass implementation for a {@linkplain org.hibernate.metamodel.RepresentationMode#MAP dynamic model} class
 *
 * @author Steve Ebersole
 */
public class ManagedClassImpl extends DelayedAnnotationTarget implements ManagedClass {
	private final String name;
	private final ManagedClass superType;

	public ManagedClassImpl(
			String name,
			ManagedClassImpl superType,
			AnnotationProcessingContext processingContext) {
		super( processingContext );
		this.name = name;
		this.superType = superType;

		processingContext.getManagedClassRegistry().addManagedClass( name, this );
	}

	@Override
	public String getName() {
		return name;
	}

	@Override
	public String getClassName() {
		return null;
	}

	@Override
	public boolean isAbstract() {
		return false;
	}

	@Override
	public ManagedClass getSuperType() {
		return superType;
	}

	@Override
	public List<ManagedClass> getImplementedInterfaceTypes() {
		return Collections.emptyList();
	}

	@Override
	public List<FieldDetails> getFields() {
		throw new IllegalStateException( "Not yet implemented" );
	}

	@Override
	public void forEachField(IndexedConsumer<FieldDetails> consumer) {
		throw new IllegalStateException( "Not yet implemented" );
	}

	@Override
	public List<MethodDetails> getMethods() {
		throw new IllegalStateException( "Not yet implemented" );
	}

	@Override
	public void forEachMethod(IndexedConsumer<MethodDetails> consumer) {
		throw new IllegalStateException( "Not yet implemented" );
	}
}
