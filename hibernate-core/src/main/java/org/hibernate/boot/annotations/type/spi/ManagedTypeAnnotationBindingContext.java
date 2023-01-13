/*
 * Hibernate, Relational Persistence for Idiomatic Java
 *
 * License: GNU Lesser General Public License (LGPL), version 2.1 or later.
 * See the lgpl.txt file in the root directory or http://www.gnu.org/licenses/lgpl-2.1.html.
 */
package org.hibernate.boot.annotations.type.spi;

import org.hibernate.boot.MappingException;
import org.hibernate.boot.annotations.source.spi.AnnotationDescriptorRegistry;
import org.hibernate.boot.annotations.source.spi.AnnotationProcessingContext;
import org.hibernate.boot.annotations.source.spi.ManagedClassRegistry;
import org.hibernate.boot.annotations.spi.AnnotationBindingContext;
import org.hibernate.boot.annotations.type.internal.AbstractManagedTypeMetadata;
import org.hibernate.boot.internal.DelegatingMetadataBuildingContext;
import org.hibernate.boot.jaxb.Origin;
import org.hibernate.boot.jaxb.SourceType;
import org.hibernate.boot.model.source.spi.LocalMetadataBuildingContext;


/**
 * AnnotationBindingContext relative to processing a specific
 * {@linkplain #getManagedTypeMetadata() managed-type}
 * 
 * @author Steve Ebersole
 */
public class ManagedTypeAnnotationBindingContext
		extends DelegatingMetadataBuildingContext
		implements LocalMetadataBuildingContext, AnnotationBindingContext, AnnotationProcessingContext {
	private final ManagedTypeMetadata managedTypeMetadata;
	private final Origin origin;

	public ManagedTypeAnnotationBindingContext(
			AbstractManagedTypeMetadata managedTypeMetadata,
			AnnotationBindingContext annotationBindingContext) {
		super( annotationBindingContext );
		this.managedTypeMetadata = managedTypeMetadata;
		this.origin = new Origin( SourceType.ANNOTATION, managedTypeMetadata.getName() );
	}

	public ManagedTypeMetadata getManagedTypeMetadata() {
		return managedTypeMetadata;
	}

	@Override
	public Origin getOrigin() {
		return origin;
	}

	public MappingException makeMappingException(String message) {
		return new MappingException( message, getOrigin() );
	}

	public MappingException makeMappingException(String message, Exception cause) {
		return new MappingException( message, cause, getOrigin() );
	}

	@Override
	protected AnnotationBindingContext getDelegate() {
		return (AnnotationBindingContext) super.getDelegate();
	}

	@Override
	public AnnotationProcessingContext getAnnotationProcessingContext() {
		return getDelegate().getAnnotationProcessingContext();
	}

	@Override
	public ManagedClassRegistry getManagedClassRegistry() {
		return getDelegate().getManagedClassRegistry();
	}

	@Override
	public AnnotationDescriptorRegistry getAnnotationDescriptorRegistry() {
		return getDelegate().getAnnotationDescriptorRegistry();
	}
}
