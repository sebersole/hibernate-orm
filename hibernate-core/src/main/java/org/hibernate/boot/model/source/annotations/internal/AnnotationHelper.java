/*
 * Hibernate, Relational Persistence for Idiomatic Java
 *
 * License: GNU Lesser General Public License (LGPL), version 2.1 or later.
 * See the lgpl.txt file in the root directory or http://www.gnu.org/licenses/lgpl-2.1.html.
 */
package org.hibernate.boot.model.source.annotations.internal;

import java.util.function.Consumer;

import org.hibernate.boot.model.source.annotations.spi.AnnotationUsage;

/**
 * Collection of helper functions related to annotation handling
 *
 * @author Steve Ebersole
 */
public class AnnotationHelper {
	private AnnotationHelper() {
		// disallow direct instantiation
	}

	public static <T> void ifSpecified(AnnotationUsage.AttributeValue attributeValue, Consumer<T> consumer) {
		if ( attributeValue == null ) {
			return;
		}

		if ( attributeValue.isDefaultValue() ) {
			return;
		}

		consumer.accept( attributeValue.getValue() );
	}

	public static <T> void ifSpecified(T value, Consumer<T> consumer) {
		if ( value == null ) {
			return;
		}

		consumer.accept( value );
	}
}
