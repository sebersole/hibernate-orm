/*
 * Hibernate, Relational Persistence for Idiomatic Java
 *
 * License: GNU Lesser General Public License (LGPL), version 2.1 or later.
 * See the lgpl.txt file in the root directory or http://www.gnu.org/licenses/lgpl-2.1.html.
 */
package org.hibernate.boot.annotations.source.internal.reflection;

import java.lang.reflect.Field;

import org.hibernate.boot.annotations.source.internal.LazyAnnotationTarget;
import org.hibernate.boot.annotations.source.spi.AnnotationProcessingContext;
import org.hibernate.boot.annotations.source.spi.FieldDetails;

/**
 * @author Steve Ebersole
 */
public class FieldDetailsImpl extends LazyAnnotationTarget implements FieldDetails {
	private final Field field;

	public FieldDetailsImpl(Field field, AnnotationProcessingContext processingContext) {
		super( field::getAnnotations, processingContext );
		this.field = field;
	}

	@Override
	public String getName() {
		return field.getName();
	}
}
