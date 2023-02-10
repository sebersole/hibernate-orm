/*
 * Hibernate, Relational Persistence for Idiomatic Java
 *
 * License: GNU Lesser General Public License (LGPL), version 2.1 or later.
 * See the lgpl.txt file in the root directory or http://www.gnu.org/licenses/lgpl-2.1.html.
 */
package org.hibernate.boot.annotations.source.internal;

import java.lang.annotation.Annotation;
import java.util.List;
import java.util.function.Consumer;

import org.hibernate.boot.annotations.source.spi.AnnotationDescriptor;
import org.hibernate.boot.annotations.source.spi.AnnotationUsage;
import org.hibernate.boot.annotations.source.spi.PackageDetails;

/**
 * @author Steve Ebersole
 */
public class NoPackageDetailsImpl implements PackageDetails {
	private final String packageName;

	public NoPackageDetailsImpl(String packageName) {
		this.packageName = packageName;
	}

	@Override
	public String getName() {
		return packageName;
	}

	@Override
	public void forEachAnnotation(Consumer<AnnotationUsage<? extends Annotation>> consumer) {
	}

	@Override
	public <A extends Annotation> AnnotationUsage<A> getAnnotation(AnnotationDescriptor<A> type) {
		return null;
	}

	@Override
	public <A extends Annotation> List<AnnotationUsage<A>> getAnnotations(AnnotationDescriptor<A> type) {
		return null;
	}

	@Override
	public <A extends Annotation> void forEachAnnotation(AnnotationDescriptor<A> type, Consumer<AnnotationUsage<A>> consumer) {
	}

	@Override
	public <A extends Annotation> AnnotationUsage<A> getNamedAnnotation(AnnotationDescriptor<A> type, String name, String attributeName) {
		return null;
	}
}
