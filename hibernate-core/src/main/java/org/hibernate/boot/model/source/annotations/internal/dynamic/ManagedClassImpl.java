/*
 * Hibernate, Relational Persistence for Idiomatic Java
 *
 * License: GNU Lesser General Public License (LGPL), version 2.1 or later.
 * See the lgpl.txt file in the root directory or http://www.gnu.org/licenses/lgpl-2.1.html.
 */
package org.hibernate.boot.model.source.annotations.internal.dynamic;

import java.util.List;

import org.hibernate.boot.annotations.spi.AnnotationDescriptorRegistry;
import org.hibernate.boot.model.source.annotations.internal.DelayedAnnotationTarget;
import org.hibernate.boot.model.source.annotations.spi.FieldSource;
import org.hibernate.boot.model.source.annotations.spi.ManagedClass;
import org.hibernate.boot.model.source.annotations.spi.MethodSource;
import org.hibernate.internal.util.IndexedConsumer;

/**
 * ManagedClass implementation for a {@linkplain org.hibernate.metamodel.RepresentationMode#MAP dynamic model} class
 *
 * @author Steve Ebersole
 */
public class ManagedClassImpl extends DelayedAnnotationTarget implements ManagedClass {
	private final String name;

	public ManagedClassImpl(String name, AnnotationDescriptorRegistry annotationDescriptorRegistry) {
		super( annotationDescriptorRegistry );
		this.name = name;
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
	public List<FieldSource> getFields() {
		throw new IllegalStateException( "Not yet implemented" );
	}

	@Override
	public void forEachField(IndexedConsumer<FieldSource> consumer) {
		throw new IllegalStateException( "Not yet implemented" );
	}

	@Override
	public List<MethodSource> getMethods() {
		throw new IllegalStateException( "Not yet implemented" );
	}

	@Override
	public void forEachMethod(IndexedConsumer<MethodSource> consumer) {
		throw new IllegalStateException( "Not yet implemented" );
	}
}
