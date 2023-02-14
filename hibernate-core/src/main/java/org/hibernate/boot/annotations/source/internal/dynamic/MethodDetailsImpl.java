/*
 * Hibernate, Relational Persistence for Idiomatic Java
 *
 * License: GNU Lesser General Public License (LGPL), version 2.1 or later.
 * See the lgpl.txt file in the root directory or http://www.gnu.org/licenses/lgpl-2.1.html.
 */
package org.hibernate.boot.annotations.source.internal.dynamic;

import org.hibernate.boot.annotations.source.spi.ClassDetails;
import org.hibernate.boot.annotations.source.spi.MethodDetails;
import org.hibernate.boot.annotations.spi.AnnotationProcessingContext;

/**
 *
 * @author Steve Ebersole
 */
public class MethodDetailsImpl extends AbstractDynamicAnnotationTarget implements DynamicMemberDetails, MethodDetails {
	private final String name;
	private final String attributeName;
	private ClassDetails type;

	public MethodDetailsImpl(
			String name,
			String attributeName,
			AnnotationProcessingContext processingContext) {
		super( processingContext );
		this.name = name;
		this.attributeName = attributeName;
	}

	public MethodDetailsImpl(
			String name,
			AnnotationProcessingContext processingContext) {
		this( name, null, processingContext );
	}

	@Override
	public String getName() {
		return name;
	}

	@Override
	public String resolveAttributeName() {
		return attributeName;
	}

	@Override
	public ClassDetails getType() {
		return type;
	}

	public void setType(ClassDetails type) {
		this.type = type;
	}

	@Override
	public boolean isPersistable() {
		return attributeName != null;
	}
}
