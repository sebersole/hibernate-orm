/*
 * Hibernate, Relational Persistence for Idiomatic Java
 *
 * License: GNU Lesser General Public License (LGPL), version 2.1 or later.
 * See the lgpl.txt file in the root directory or http://www.gnu.org/licenses/lgpl-2.1.html.
 */
package org.hibernate.boot.model.source.annotations.internal;

import java.lang.annotation.Annotation;

import org.hibernate.annotations.common.reflection.XProperty;
import org.hibernate.boot.model.source.annotations.spi.AnnotationBindingContext;
import org.hibernate.boot.model.source.internal.hbm.XmlElementMetadata;
import org.hibernate.boot.model.source.spi.AttributePath;
import org.hibernate.boot.model.source.spi.AttributeRole;
import org.hibernate.boot.model.source.spi.AttributeSource;
import org.hibernate.boot.model.source.spi.HibernateTypeSource;
import org.hibernate.boot.model.source.spi.ToolingHintContext;

/**
 * @author Steve Ebersole
 */
public class XClassAttributeSource extends AbstractAnnotationTarget implements AttributeSource {
	private final XProperty xProperty;

	public XClassAttributeSource(XProperty xProperty, AnnotationBindingContext bindingContext) {
		super( xProperty.getAnnotations(), bindingContext );
		this.xProperty = xProperty;

		final Annotation[] annotations = xProperty.getAnnotations();
	}

	@Override
	public String getName() {
		return xProperty.getName();
	}

	@Override
	public XmlElementMetadata getSourceType() {
		throw new UnsupportedOperationException();
	}

	@Override
	public boolean isSingular() {
		throw new UnsupportedOperationException();
	}

	@Override
	public String getXmlNodeName() {
		throw new UnsupportedOperationException();
	}

	@Override
	public AttributePath getAttributePath() {
		throw new UnsupportedOperationException();
	}

	@Override
	public AttributeRole getAttributeRole() {
		throw new UnsupportedOperationException();
	}

	@Override
	public HibernateTypeSource getTypeInformation() {
		throw new UnsupportedOperationException();
	}

	@Override
	public String getPropertyAccessorName() {
		return null;
	}

	@Override
	public boolean isIncludedInOptimisticLocking() {
		return false;
	}

	@Override
	public ToolingHintContext getToolingHintContext() {
		return null;
	}
}
