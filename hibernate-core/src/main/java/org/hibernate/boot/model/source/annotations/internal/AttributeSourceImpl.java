/*
 * Hibernate, Relational Persistence for Idiomatic Java
 *
 * License: GNU Lesser General Public License (LGPL), version 2.1 or later.
 * See the lgpl.txt file in the root directory or http://www.gnu.org/licenses/lgpl-2.1.html.
 */
package org.hibernate.boot.model.source.annotations.internal;

import java.util.List;

import org.hibernate.boot.model.annotations.spi.AnnotationUsage;
import org.hibernate.boot.model.source.annotations.spi.AnnotationBindingContext;
import org.hibernate.boot.model.source.annotations.spi.AttributeSource;

/**
 * @author Steve Ebersole
 */
public class AttributeSourceImpl extends AbstractAnnotationTarget implements AttributeSource {
	private final String name;

	public AttributeSourceImpl(String name, AnnotationBindingContext bindingContext) {
		super( bindingContext.getAnnotationProcessingContext() );
		this.name = name;
	}

	public AttributeSourceImpl(String name, List<AnnotationUsage<?>> annotationUsages, AnnotationBindingContext bindingContext) {
		super( annotationUsages, bindingContext.getAnnotationProcessingContext() );
		this.name = name;
	}

	@Override
	public String getName() {
		return name;
	}
}
