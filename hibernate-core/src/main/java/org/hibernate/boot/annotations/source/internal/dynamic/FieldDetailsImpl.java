/*
 * Hibernate, Relational Persistence for Idiomatic Java
 *
 * License: GNU Lesser General Public License (LGPL), version 2.1 or later.
 * See the lgpl.txt file in the root directory or http://www.gnu.org/licenses/lgpl-2.1.html.
 */
package org.hibernate.boot.annotations.source.internal.dynamic;

import org.hibernate.boot.annotations.source.spi.ClassDetails;
import org.hibernate.boot.annotations.source.spi.FieldDetails;
import org.hibernate.boot.annotations.spi.AnnotationProcessingContext;


/**
 * Models a field
 *
 * @author Steve Ebersole
 */
public class FieldDetailsImpl extends AbstractDynamicAnnotationTarget implements DynamicMemberDetails, FieldDetails {
	private final String name;
	private ClassDetails type;

	public FieldDetailsImpl(String name, AnnotationProcessingContext processingContext) {
		super( processingContext );
		this.name = name;
	}

	@Override
	public String getName() {
		return name;
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
		return true;
	}
}
