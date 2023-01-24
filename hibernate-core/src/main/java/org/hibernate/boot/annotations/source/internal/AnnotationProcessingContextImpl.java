/*
 * Hibernate, Relational Persistence for Idiomatic Java
 *
 * License: GNU Lesser General Public License (LGPL), version 2.1 or later.
 * See the lgpl.txt file in the root directory or http://www.gnu.org/licenses/lgpl-2.1.html.
 */
package org.hibernate.boot.annotations.source.internal;

import org.hibernate.boot.annotations.source.internal.reflection.ClassDetailsImpl;
import org.hibernate.boot.annotations.source.spi.AnnotationDescriptorRegistry;
import org.hibernate.boot.annotations.source.spi.ClassDetailsRegistry;
import org.hibernate.boot.annotations.spi.AnnotationProcessingContext;
import org.hibernate.boot.spi.MetadataBuildingContext;
import org.hibernate.type.descriptor.java.JavaType;
import org.hibernate.type.descriptor.jdbc.JdbcType;

import jakarta.persistence.AttributeConverter;

/**
 * @author Steve Ebersole
 */
public class AnnotationProcessingContextImpl implements AnnotationProcessingContext {
	private final AnnotationDescriptorRegistry descriptorRegistry;
	private final ClassDetailsRegistry classDetailsRegistry;
	private final MetadataBuildingContext buildingContext;

	public AnnotationProcessingContextImpl(MetadataBuildingContext buildingContext) {
		this.buildingContext = buildingContext;
		this.descriptorRegistry = new AnnotationDescriptorRegistry( this );
		this.classDetailsRegistry = new ClassDetailsRegistry( this );

		classDetailsRegistry.addManagedClass( new ClassDetailsImpl( AttributeConverter.class, this ) );
		classDetailsRegistry.addManagedClass( new ClassDetailsImpl( JavaType.class, this ) );
		classDetailsRegistry.addManagedClass( new ClassDetailsImpl( JdbcType.class, this ) );
	}

	@Override
	public AnnotationDescriptorRegistry getAnnotationDescriptorRegistry() {
		return descriptorRegistry;
	}

	@Override
	public ClassDetailsRegistry getClassDetailsRegistry() {
		return classDetailsRegistry;
	}

	@Override
	public MetadataBuildingContext getMetadataBuildingContext() {
		return buildingContext;
	}
}
