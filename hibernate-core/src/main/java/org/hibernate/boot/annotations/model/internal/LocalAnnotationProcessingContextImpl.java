/*
 * Hibernate, Relational Persistence for Idiomatic Java
 *
 * License: GNU Lesser General Public License (LGPL), version 2.1 or later.
 * See the lgpl.txt file in the root directory or http://www.gnu.org/licenses/lgpl-2.1.html.
 */
package org.hibernate.boot.annotations.model.internal;

import java.lang.annotation.Annotation;
import java.util.List;
import java.util.function.Consumer;

import org.hibernate.boot.annotations.model.spi.LocalAnnotationProcessingContext;
import org.hibernate.boot.annotations.model.spi.ManagedTypeMetadata;
import org.hibernate.boot.annotations.source.spi.AnnotationDescriptor;
import org.hibernate.boot.annotations.source.spi.AnnotationDescriptorRegistry;
import org.hibernate.boot.annotations.source.spi.AnnotationUsage;
import org.hibernate.boot.annotations.source.spi.ClassDetailsRegistry;
import org.hibernate.boot.annotations.spi.AnnotationProcessingContext;
import org.hibernate.boot.model.relational.Database;
import org.hibernate.boot.model.relational.Namespace;
import org.hibernate.boot.spi.MetadataBuildingContext;


/**
 * AnnotationBindingContext relative to processing a specific {@linkplain #getScope() managed-type}
 * 
 * @author Steve Ebersole
 */
public class LocalAnnotationProcessingContextImpl implements LocalAnnotationProcessingContext {
	private final ManagedTypeMetadata scope;
	private final AnnotationProcessingContext parentProcessingContext;

	public LocalAnnotationProcessingContextImpl(
			AbstractManagedTypeMetadata scope,
			AnnotationProcessingContext parentProcessingContext) {
		super();
		this.scope = scope;
		this.parentProcessingContext = parentProcessingContext;
	}

	@Override
	public ManagedTypeMetadata getScope() {
		return scope;
	}

	@Override
	public Database getDatabase() {
		return parentProcessingContext.getMetadataBuildingContext().getMetadataCollector().getDatabase();
	}

	@Override
	public Namespace getDefaultNamespace() {

		return null;
	}

	@Override
	public ClassDetailsRegistry getClassDetailsRegistry() {
		return parentProcessingContext.getClassDetailsRegistry();
	}

	@Override
	public AnnotationDescriptorRegistry getAnnotationDescriptorRegistry() {
		return parentProcessingContext.getAnnotationDescriptorRegistry();
	}

	@Override
	public MetadataBuildingContext getMetadataBuildingContext() {
		return parentProcessingContext.getMetadataBuildingContext();
	}

	@Override
	public void registerUsage(AnnotationUsage<?> usage) {
		parentProcessingContext.registerUsage( usage );
	}

	@Override
	public <A extends Annotation> List<AnnotationUsage<A>> getAllUsages(AnnotationDescriptor<A> annotationDescriptor) {
		return parentProcessingContext.getAllUsages( annotationDescriptor );
	}

	@Override
	public <A extends Annotation> void forEachUsage(AnnotationDescriptor<A> annotationDescriptor, Consumer<AnnotationUsage<A>> consumer) {
		parentProcessingContext.forEachUsage( annotationDescriptor, consumer );
	}
}
