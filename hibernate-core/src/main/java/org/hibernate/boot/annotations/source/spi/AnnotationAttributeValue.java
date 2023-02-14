/*
 * Hibernate, Relational Persistence for Idiomatic Java
 *
 * License: GNU Lesser General Public License (LGPL), version 2.1 or later.
 * See the lgpl.txt file in the root directory or http://www.gnu.org/licenses/lgpl-2.1.html.
 */
package org.hibernate.boot.annotations.source.spi;

/**
 * The descriptor for the value of a particular attribute for the annotation usage
 */
public interface AnnotationAttributeValue {
	/**
	 * Descriptor for the attribute for which this is a value
	 */
	<T> AnnotationAttributeDescriptor<T> getAttributeDescriptor();

	/**
	 * The value
	 */
	<T> T getValue();

	<T> T getValue(Class<T> type);

	default String asString() {
		return getValue().toString();
	}

	default boolean asBoolean() {
		return getValue();
	}

	default int asInt() {
		return getValue();
	}

	/**
	 * Whether the value is a default.
	 *
	 * @implNote Best guess at the moment since HCANN is unable to make
	 * 		this distinction.  This will be better handled once we can migrate
	 * 		to using Jandex for annotations.  See
	 * 		<a href="https://hibernate.atlassian.net/browse/HHH-9489">HHH-9489</a> (Migrate from commons-annotations to Jandex).
	 */
	boolean isDefaultValue();
}
