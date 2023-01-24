/*
 * Hibernate, Relational Persistence for Idiomatic Java
 *
 * License: GNU Lesser General Public License (LGPL), version 2.1 or later.
 * See the lgpl.txt file in the root directory or http://www.gnu.org/licenses/lgpl-2.1.html.
 */
package org.hibernate.boot.annotations.bind.spi;

import org.hibernate.boot.annotations.source.spi.AnnotationDescriptorRegistry;
import org.hibernate.boot.annotations.source.spi.ClassDetailsRegistry;
import org.hibernate.boot.annotations.spi.AnnotationProcessingContext;
import org.hibernate.boot.spi.MetadataBuildingContext;

/**
 * @author Steve Ebersole
 */
public interface AnnotationBindingContext extends MetadataBuildingContext {
	AnnotationProcessingContext getAnnotationProcessingContext();

	default AnnotationDescriptorRegistry getAnnotationDescriptorRegistry() {
		return getAnnotationProcessingContext().getAnnotationDescriptorRegistry();
	}

	default ClassDetailsRegistry getClassDetailsRegistry() {
		return getAnnotationProcessingContext().getClassDetailsRegistry();
	}
}
