/*
 * Hibernate, Relational Persistence for Idiomatic Java
 *
 * License: GNU Lesser General Public License (LGPL), version 2.1 or later.
 * See the lgpl.txt file in the root directory or http://www.gnu.org/licenses/lgpl-2.1.html.
 */
package org.hibernate.boot.jandex.spi;

import java.lang.annotation.Annotation;

import org.jboss.jandex.DotName;

/**
 * Details about an annotation focused on bootstrap needs
 *
 * @author Steve Ebersole
 */
public class AnnotationDetails {
	private final DotName jandexName;
	private final Class<? extends Annotation> javaType;
	private final AnnotationDetails groupingAnnotationDetails;

	public AnnotationDetails(
			DotName jandexName,
			Class<? extends Annotation> javaType,
			AnnotationDetails groupingAnnotationDetails) {
		this.jandexName = jandexName;
		this.javaType = javaType;
		this.groupingAnnotationDetails = groupingAnnotationDetails;
	}

	public static AnnotationDetails createDetails(Class<? extends Annotation> javaType) {
		return createDetails( javaType, null );
	}

	public static AnnotationDetails createDetails(Class<? extends Annotation> javaType, AnnotationDetails groupingAnnotationDetails) {
		return new AnnotationDetails(
				DotName.createSimple( javaType ),
				javaType,
				groupingAnnotationDetails
		);
	}

	/**
	 * The {@linkplain DotName Jandex name} of the annotation class
	 */
	public DotName getJandexName() {
		return jandexName;
	}

	/**
	 * The annotation Java type
	 */
	public Class<? extends Annotation> getJavaType() {
		return javaType;
	}

	/**
	 * If the annotation is {@linkplain java.lang.annotation.Repeatable repeatable},
	 * returns the details for the containing annotation.
	 *
	 * @return If repeatable, the details of the containing annotation; {@code null}
	 * otherwise
	 */
	public AnnotationDetails getGroupingAnnotationDetails() {
		return groupingAnnotationDetails;
	}
}
