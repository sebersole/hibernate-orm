/*
 * Hibernate, Relational Persistence for Idiomatic Java
 *
 * License: GNU Lesser General Public License (LGPL), version 2.1 or later.
 * See the lgpl.txt file in the root directory or <http://www.gnu.org/licenses/lgpl-2.1.html>.
 */
package org.hibernate.boot.model.source.annotations.spi;

import org.hibernate.boot.model.annotations.spi.AnnotationDescriptorRegistry;
import org.hibernate.boot.model.annotations.spi.AnnotationProcessingContext;
import org.hibernate.boot.spi.MetadataBuildingContext;

/**
 * @author Steve Ebersole
 */
public interface AnnotationBindingContext extends MetadataBuildingContext {
	AnnotationProcessingContext getAnnotationProcessingContext();

	default AnnotationDescriptorRegistry getAnnotationDescriptorRegistry() {
		return getAnnotationProcessingContext().getAnnotationDescriptorRegistry();
	}
}
