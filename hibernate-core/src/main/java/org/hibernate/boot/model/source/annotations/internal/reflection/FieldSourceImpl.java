/*
 * Hibernate, Relational Persistence for Idiomatic Java
 *
 * License: GNU Lesser General Public License (LGPL), version 2.1 or later.
 * See the lgpl.txt file in the root directory or http://www.gnu.org/licenses/lgpl-2.1.html.
 */
package org.hibernate.boot.model.source.annotations.internal.reflection;

import java.lang.reflect.Field;

import org.hibernate.boot.annotations.internal.LazyAnnotationTarget;
import org.hibernate.boot.annotations.spi.AnnotationDescriptorRegistry;
import org.hibernate.boot.model.source.annotations.spi.FieldSource;

/**
 * @author Steve Ebersole
 */
public class FieldSourceImpl extends LazyAnnotationTarget implements FieldSource {
	private final Field field;

	public FieldSourceImpl(Field field, AnnotationDescriptorRegistry annotationDescriptorRegistry) {
		super( field::getAnnotations, annotationDescriptorRegistry );
		this.field = field;
	}

	@Override
	public String getName() {
		return field.getName();
	}
}
