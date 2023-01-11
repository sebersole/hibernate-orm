/*
 * Hibernate, Relational Persistence for Idiomatic Java
 *
 * License: GNU Lesser General Public License (LGPL), version 2.1 or later.
 * See the lgpl.txt file in the root directory or http://www.gnu.org/licenses/lgpl-2.1.html.
 */
package org.hibernate.boot.model.source.annotations.internal.hcann;

import org.hibernate.annotations.common.reflection.XProperty;
import org.hibernate.boot.annotations.internal.LazyAnnotationTarget;
import org.hibernate.boot.annotations.spi.AnnotationDescriptorRegistry;
import org.hibernate.boot.model.source.annotations.spi.FieldSource;

/**
 * @author Steve Ebersole
 */
public class FieldSourceImpl extends LazyAnnotationTarget implements FieldSource {
	private final XProperty xProperty;

	public FieldSourceImpl(XProperty xProperty, AnnotationDescriptorRegistry annotationDescriptorRegistry) {
		super( xProperty::getAnnotations, annotationDescriptorRegistry );
		this.xProperty = xProperty;
	}

	@Override
	public String getName() {
		return xProperty.getName();
	}
}
