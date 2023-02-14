/*
 * Hibernate, Relational Persistence for Idiomatic Java
 *
 * License: GNU Lesser General Public License (LGPL), version 2.1 or later.
 * See the lgpl.txt file in the root directory or http://www.gnu.org/licenses/lgpl-2.1.html.
 */
package org.hibernate.boot.annotations.source.internal.reflection;

import java.lang.reflect.Method;

import org.hibernate.boot.annotations.source.internal.LazyAnnotationTarget;
import org.hibernate.boot.annotations.source.spi.ClassDetails;
import org.hibernate.boot.annotations.source.spi.MethodDetails;
import org.hibernate.boot.annotations.spi.AnnotationProcessingContext;

import static org.hibernate.boot.annotations.source.internal.ModifierUtils.isPersistableMethod;

/**
 * @author Steve Ebersole
 */
public class MethodDetailsImpl extends LazyAnnotationTarget implements MethodDetails {
	private final Method method;
	private final ClassDetails type;

	public MethodDetailsImpl(Method method, AnnotationProcessingContext processingContext) {
		super( method::getAnnotations, processingContext );
		this.method = method;
		this.type = processingContext.getClassDetailsRegistry().resolveManagedClass(
				method.getReturnType().getName(),
				() -> ClassDetailsBuilderImpl.INSTANCE.buildClassDetails( method.getReturnType(), getProcessingContext() )
		);
	}

	@Override
	public String getName() {
		return method.getName();
	}

	@Override
	public ClassDetails getType() {
		return type;
	}

	@Override
	public boolean isPersistable() {
		if ( method.getParameterCount() > 0 ) {
			// should be the getter
			return false;
		}

		if ( "void".equals( type.getName() ) || "Void".equals( type.getName() ) ) {
			// again, should be the getter
			return false;
		}

		if ( !isPersistableMethod( method.getModifiers() ) ) {
			return false;
		}

		return true;
	}
}
