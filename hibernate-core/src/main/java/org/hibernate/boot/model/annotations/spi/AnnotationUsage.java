/*
 * Hibernate, Relational Persistence for Idiomatic Java
 *
 * License: GNU Lesser General Public License (LGPL), version 2.1 or later.
 * See the lgpl.txt file in the root directory or http://www.gnu.org/licenses/lgpl-2.1.html.
 */
package org.hibernate.boot.model.annotations.spi;

import java.lang.annotation.Annotation;

import org.hibernate.boot.model.annotations.internal.OrmAnnotationDescriptorImpl;

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
	default AttributeValue getValueAttributeValue() {
		return getAttributeValue( "value" );
	}

	/**
	 * The value of the named annotation attribute
	 */
	AttributeValue getAttributeValue(String name);

	/**
	 * The descriptor for the value of a particular attribute for the annotation usage
	 */
	interface AttributeValue {
		/**
		 * Descriptor for the attribute for which this is a value
		 */
		<T> OrmAnnotationDescriptorImpl.AttributeDescriptor<T> getAttributeDescriptor();

		/**
		 * The value
		 */
		<T> T getValue();

		default String getStringValue() {
			return getValue().toString();
		}

		default boolean getBooleanValue() {
			return getValue();
		}

		default boolean getIntValue() {
			return getValue();
		}

		/**
		 * Whether the value is a default.
		 *
		 * @implNote Best guess at the moment since HCANN is unable to make
		 * this distinction.  This will be better handled once we can migrate
		 * to using Jandex for annotations.  See
		 * <a href="https://hibernate.atlassian.net/browse/HHH-9489">HHH-9489</a> (Migrate from commons-annotations to Jandex).
		 */
		boolean isDefaultValue();
	}
}
