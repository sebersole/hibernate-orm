/*
 * Hibernate, Relational Persistence for Idiomatic Java
 *
 * License: GNU Lesser General Public License (LGPL), version 2.1 or later.
 * See the lgpl.txt file in the root directory or http://www.gnu.org/licenses/lgpl-2.1.html.
 */
package org.hibernate.boot.annotations.bind.xml.internal;

import java.lang.annotation.Annotation;
import java.util.List;
import java.util.function.Consumer;

import org.hibernate.boot.annotations.bind.xml.spi.XmlClassAnnotationProcessingContext;
import org.hibernate.boot.annotations.bind.xml.spi.XmlDocumentAnnotationProcessingContext;
import org.hibernate.boot.annotations.source.spi.AnnotationDescriptor;
import org.hibernate.boot.annotations.source.spi.AnnotationDescriptorRegistry;
import org.hibernate.boot.annotations.source.spi.AnnotationUsage;
import org.hibernate.boot.annotations.source.spi.ClassDetailsRegistry;
import org.hibernate.boot.jaxb.mapping.ManagedType;
import org.hibernate.boot.spi.MetadataBuildingContext;

/**
 * Models details about the JAXB node for an entity, mapped-superclass or embeddable class
 *
 * @author Steve Ebersole
 */
public class ClassBinding implements XmlClassAnnotationProcessingContext {
	private final XmlDocumentAnnotationProcessingContext documentContext;
	private final ManagedType xmlNode;

	public ClassBinding(ManagedType xmlNode, XmlDocumentAnnotationProcessingContext documentContext) {
		this.documentContext = documentContext;
		this.xmlNode = xmlNode;
	}

	@Override
	public XmlDocumentAnnotationProcessingContext getDocumentProcessingContext() {
		return documentContext;
	}

	@Override
	public ManagedType getClassNode() {
		return xmlNode;
	}

	@Override
	public MetadataBuildingContext getMetadataBuildingContext() {
		return documentContext.getMetadataBuildingContext();
	}

	@Override
	public AnnotationDescriptorRegistry getAnnotationDescriptorRegistry() {
		return documentContext.getAnnotationDescriptorRegistry();
	}

	@Override
	public ClassDetailsRegistry getClassDetailsRegistry() {
		return documentContext.getClassDetailsRegistry();
	}

	@Override
	public void registerUsage(AnnotationUsage<?> usage) {
		documentContext.registerUsage( usage );
	}

	@Override
	public <A extends Annotation> List<AnnotationUsage<A>> getAllUsages(AnnotationDescriptor<A> annotationDescriptor) {
		return documentContext.getAllUsages( annotationDescriptor );
	}

	@Override
	public <A extends Annotation> void forEachUsage(AnnotationDescriptor<A> annotationDescriptor, Consumer<AnnotationUsage<A>> consumer) {
		documentContext.forEachUsage( annotationDescriptor, consumer );
	}
}
