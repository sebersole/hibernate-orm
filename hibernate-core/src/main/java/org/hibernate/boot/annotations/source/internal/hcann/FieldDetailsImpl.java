/*
 * Hibernate, Relational Persistence for Idiomatic Java
 *
 * License: GNU Lesser General Public License (LGPL), version 2.1 or later.
 * See the lgpl.txt file in the root directory or http://www.gnu.org/licenses/lgpl-2.1.html.
 */
package org.hibernate.boot.annotations.source.internal.hcann;

import org.hibernate.annotations.common.reflection.XProperty;
import org.hibernate.boot.annotations.source.internal.LazyAnnotationTarget;
import org.hibernate.boot.annotations.source.spi.ClassDetails;
import org.hibernate.boot.annotations.source.spi.FieldDetails;
import org.hibernate.boot.annotations.spi.AnnotationProcessingContext;

import static org.hibernate.boot.annotations.source.internal.ModifierUtils.isPersistableField;


/**
 * @author Steve Ebersole
 */
public class FieldDetailsImpl extends LazyAnnotationTarget implements FieldDetails {
	private final XProperty xProperty;
	private final ClassDetails type;

	public FieldDetailsImpl(XProperty xProperty, AnnotationProcessingContext processingContext) {
		super( xProperty::getAnnotations, processingContext );
		this.xProperty = xProperty;
		this.type = processingContext.getClassDetailsRegistry().resolveManagedClass(
				xProperty.getType().getName(),
				() -> new ClassDetailsImpl( xProperty.getType(), processingContext )
		);
	}

	@Override
	public String getName() {
		return xProperty.getName();
	}

	@Override
	public ClassDetails getType() {
		return type;
	}

	@Override
	public boolean isPersistable() {
		return isPersistableField( xProperty.getModifiers() );
	}
}
