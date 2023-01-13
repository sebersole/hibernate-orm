/*
 * Hibernate, Relational Persistence for Idiomatic Java
 *
 * License: GNU Lesser General Public License (LGPL), version 2.1 or later.
 * See the lgpl.txt file in the root directory or http://www.gnu.org/licenses/lgpl-2.1.html.
 */
package org.hibernate.boot.annotations.source.internal;

import org.hibernate.boot.annotations.source.internal.reflection.ManagedClassImpl;
import org.hibernate.boot.annotations.source.spi.AnnotationDescriptorRegistry;
import org.hibernate.boot.annotations.source.spi.AnnotationProcessingContext;
import org.hibernate.boot.annotations.source.spi.ManagedClassRegistry;
import org.hibernate.type.descriptor.java.JavaType;
import org.hibernate.type.descriptor.jdbc.JdbcType;

import jakarta.persistence.AttributeConverter;

/**
 * @author Steve Ebersole
 */
public class AnnotationProcessingContextImpl implements AnnotationProcessingContext {
	private final AnnotationDescriptorRegistry descriptorRegistry;
	private final ManagedClassRegistry managedClassRegistry;

	public AnnotationProcessingContextImpl() {
		this.descriptorRegistry = new AnnotationDescriptorRegistry( this );
		this.managedClassRegistry = new ManagedClassRegistry( this );

		managedClassRegistry.addManagedClass( new ManagedClassImpl( AttributeConverter.class, this ) );
		managedClassRegistry.addManagedClass( new ManagedClassImpl( JavaType.class, this ) );
		managedClassRegistry.addManagedClass( new ManagedClassImpl( JdbcType.class, this ) );
	}

	@Override
	public AnnotationDescriptorRegistry getAnnotationDescriptorRegistry() {
		return descriptorRegistry;
	}

	@Override
	public ManagedClassRegistry getManagedClassRegistry() {
		return managedClassRegistry;
	}
}
