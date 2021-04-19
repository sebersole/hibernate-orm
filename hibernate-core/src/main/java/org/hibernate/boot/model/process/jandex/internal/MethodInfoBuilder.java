/*
 * Hibernate, Relational Persistence for Idiomatic Java
 *
 * License: GNU Lesser General Public License (LGPL), version 2.1 or later.
 * See the lgpl.txt file in the root directory or <http://www.gnu.org/licenses/lgpl-2.1.html>.
 */
package org.hibernate.boot.model.process.jandex.internal;

import java.util.List;
import java.util.Map;
import java.util.function.Consumer;

import org.jboss.jandex.AnnotationInstance;
import org.jboss.jandex.DotName;
import org.jboss.jandex.MethodInfo;

/**
 * @author Steve Ebersole
 */
public class MethodInfoBuilder {
	private final MethodInfo methodInfo;
	private final Consumer<AnnotationInstance> annotationInstanceConsumer;

	private final Map<DotName, List<AnnotationInstance>> methodAnnotations;

	public MethodInfoBuilder(MethodInfo incomingMethodInfo, Consumer<AnnotationInstance> annotationInstanceConsumer) {
		this.annotationInstanceConsumer = annotationInstanceConsumer;
	}
}
