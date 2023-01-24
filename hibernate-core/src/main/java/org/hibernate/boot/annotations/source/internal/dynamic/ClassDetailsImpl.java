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
public class ClassDetailsImpl extends DelayedAnnotationTarget implements ClassDetails {
	private final String name;
	private final ClassDetails superType;

	public ClassDetailsImpl(
			String name,
			ClassDetailsImpl superType,
			AnnotationProcessingContext processingContext) {
		super( processingContext );
		this.name = name;
		this.superType = superType;

		processingContext.getClassDetailsRegistry().addManagedClass( name, this );
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
	public ClassDetails getSuperType() {
		return superType;
	}

	@Override
	public List<ClassDetails> getImplementedInterfaceTypes() {
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
