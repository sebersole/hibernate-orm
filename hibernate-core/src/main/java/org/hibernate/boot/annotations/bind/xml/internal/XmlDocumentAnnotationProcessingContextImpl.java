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

import org.hibernate.boot.annotations.bind.xml.spi.XmlDocumentAnnotationProcessingContext;
import org.hibernate.boot.annotations.source.spi.AnnotationDescriptor;
import org.hibernate.boot.annotations.source.spi.AnnotationDescriptorRegistry;
import org.hibernate.boot.annotations.source.spi.AnnotationUsage;
import org.hibernate.boot.annotations.source.spi.ClassDetailsRegistry;
import org.hibernate.boot.annotations.spi.AnnotationProcessingContext;
import org.hibernate.boot.jaxb.Origin;
import org.hibernate.boot.jaxb.mapping.JaxbEntityMappings;
import org.hibernate.boot.spi.MetadataBuildingContext;

/**
 * @author Steve Ebersole
 */
public class XmlDocumentAnnotationProcessingContextImpl implements XmlDocumentAnnotationProcessingContext {
	private final JaxbEntityMappings xmlMapping;
	private final Origin xmlOrigin;
	private final AnnotationProcessingContext parentProcessingContext;

	public XmlDocumentAnnotationProcessingContextImpl(
			JaxbEntityMappings xmlMapping,
			Origin xmlOrigin,
			AnnotationProcessingContext parentProcessingContext) {
		this.xmlMapping = xmlMapping;
		this.xmlOrigin = xmlOrigin;
		this.parentProcessingContext = parentProcessingContext;
	}

	@Override
	public JaxbEntityMappings getXmlMapping() {
		return xmlMapping;
	}

	@Override
	public Origin getXmlOrigin() {
		return xmlOrigin;
	}

	@Override
	public MetadataBuildingContext getMetadataBuildingContext() {
		return parentProcessingContext.getMetadataBuildingContext();
	}

	@Override
	public AnnotationDescriptorRegistry getAnnotationDescriptorRegistry() {
		return parentProcessingContext.getAnnotationDescriptorRegistry();
	}

	@Override
	public ClassDetailsRegistry getClassDetailsRegistry() {
		return parentProcessingContext.getClassDetailsRegistry();
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
