/*
 * Hibernate, Relational Persistence for Idiomatic Java
 *
 * License: GNU Lesser General Public License (LGPL), version 2.1 or later.
 * See the lgpl.txt file in the root directory or http://www.gnu.org/licenses/lgpl-2.1.html.
 */
package org.hibernate.boot.model.source.annotations.internal.hcann;

import org.hibernate.annotations.common.reflection.XMethod;
import org.hibernate.boot.annotations.internal.LazyAnnotationTarget;
import org.hibernate.boot.annotations.spi.AnnotationDescriptorRegistry;
import org.hibernate.boot.model.source.annotations.spi.MethodSource;

/**
 * @author Steve Ebersole
 */
public class MethodSourceImpl extends LazyAnnotationTarget implements MethodSource {
	private final XMethod xMethod;

	public MethodSourceImpl(XMethod xMethod, AnnotationDescriptorRegistry annotationDescriptorRegistry) {
		super( xMethod::getAnnotations, annotationDescriptorRegistry );
		this.xMethod = xMethod;
	}

	@Override
	public String getName() {
		return xMethod.getName();
	}
}
