/*
 * Hibernate, Relational Persistence for Idiomatic Java
 *
 * License: GNU Lesser General Public License (LGPL), version 2.1 or later.
 * See the lgpl.txt file in the root directory or http://www.gnu.org/licenses/lgpl-2.1.html.
 */
package org.hibernate.boot.annotations.source.spi;

import java.lang.annotation.Annotation;

/**
 * Describes the usage of an annotation.  That is, not the
 * {@linkplain AnnotationDescriptor annotation class} itself, but
 * rather a particular usage of the annotation on one of its
 * allowable {@linkplain AnnotationTarget targets}.
 *
 * @apiNote Abstracts the underlying source of the annotation information,
 * whether that is the {@linkplain Annotation annotation} itself, Jandex,
 * HCANN, etc.
 *
 * @author Steve Ebersole
 */
public interface AnnotationUsage<A extends Annotation> {
	/**
	 * The descriptor for the annotation for which this is a usage
	 */
	AnnotationDescriptor<A> getAnnotationDescriptor();

	/**
	 * The target where this annotation is placed
	 */
	AnnotationTarget getAnnotationTarget();

	/**
	 * The value of the annotation attribute named {@code "value"}
	 */
	default AnnotationAttributeValue getValueAttributeValue() {
		return getAttributeValue( "value" );
	}

	/**
	 * The value of the named annotation attribute
	 */
	AnnotationAttributeValue getAttributeValue(String name);

}
