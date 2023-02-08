/*
 * Hibernate, Relational Persistence for Idiomatic Java
 *
 * License: GNU Lesser General Public License (LGPL), version 2.1 or later.
 * See the lgpl.txt file in the root directory or http://www.gnu.org/licenses/lgpl-2.1.html.
 */
package org.hibernate.boot.annotations.source.internal;

import java.math.BigDecimal;
import java.math.BigInteger;
import java.sql.Blob;
import java.sql.Clob;
import java.sql.NClob;
import java.util.Collection;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.SortedMap;
import java.util.SortedSet;

import org.hibernate.boot.annotations.source.internal.reflection.ClassDetailsImpl;
import org.hibernate.boot.annotations.source.spi.AnnotationDescriptorRegistry;
import org.hibernate.boot.annotations.source.spi.ClassDetailsRegistry;
import org.hibernate.boot.annotations.spi.AnnotationProcessingContext;
import org.hibernate.boot.spi.MetadataBuildingContext;
import org.hibernate.type.descriptor.java.JavaType;
import org.hibernate.type.descriptor.jdbc.JdbcType;

import jakarta.persistence.AttributeConverter;

/**
 * Standard implementation of AnnotationProcessingContext
 *
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

		// todo (annotation-source) : add all the Hibernate types we might encounter
		//  	in the the domain model - `MutabilityPlan`, `IdentifierGenerator`, etc.

		classDetailsRegistry.addManagedClass( new ClassDetailsImpl( AttributeConverter.class, this ) );
		classDetailsRegistry.addManagedClass( new ClassDetailsImpl( JavaType.class, this ) );
		classDetailsRegistry.addManagedClass( new ClassDetailsImpl( JdbcType.class, this ) );

		// todo (annotation-source) : add any standard Java types here up front
		//  	- anything we know we will never have to enhance really.
		//		- possibly leverage `buildingContext.getBootstrapContext().getTypeConfiguration().getBasicTypeRegistry()`

		classDetailsRegistry.addManagedClass( new ClassDetailsImpl( String.class, this ) );
		classDetailsRegistry.addManagedClass( new ClassDetailsImpl( Boolean.class, this ) );
		classDetailsRegistry.addManagedClass( new ClassDetailsImpl( Enum.class, this ) );
		classDetailsRegistry.addManagedClass( new ClassDetailsImpl( Byte.class, this ) );
		classDetailsRegistry.addManagedClass( new ClassDetailsImpl( Short.class, this ) );
		classDetailsRegistry.addManagedClass( new ClassDetailsImpl( Integer.class, this ) );
		classDetailsRegistry.addManagedClass( new ClassDetailsImpl( Long.class, this ) );
		classDetailsRegistry.addManagedClass( new ClassDetailsImpl( Double.class, this ) );
		classDetailsRegistry.addManagedClass( new ClassDetailsImpl( Float.class, this ) );
		classDetailsRegistry.addManagedClass( new ClassDetailsImpl( BigInteger.class, this ) );
		classDetailsRegistry.addManagedClass( new ClassDetailsImpl( BigDecimal.class, this ) );
		classDetailsRegistry.addManagedClass( new ClassDetailsImpl( Blob.class, this ) );
		classDetailsRegistry.addManagedClass( new ClassDetailsImpl( Clob.class, this ) );
		classDetailsRegistry.addManagedClass( new ClassDetailsImpl( NClob.class, this ) );
		classDetailsRegistry.addManagedClass( new ClassDetailsImpl( Collection.class, this ) );
		classDetailsRegistry.addManagedClass( new ClassDetailsImpl( Set.class, this ) );
		classDetailsRegistry.addManagedClass( new ClassDetailsImpl( List.class, this ) );
		classDetailsRegistry.addManagedClass( new ClassDetailsImpl( Map.class, this ) );
		classDetailsRegistry.addManagedClass( new ClassDetailsImpl( Comparator.class, this ) );
		classDetailsRegistry.addManagedClass( new ClassDetailsImpl( Comparable.class, this ) );
		classDetailsRegistry.addManagedClass( new ClassDetailsImpl( SortedSet.class, this ) );
		classDetailsRegistry.addManagedClass( new ClassDetailsImpl( SortedMap.class, this ) );
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
