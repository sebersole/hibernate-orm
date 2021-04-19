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
import org.jboss.jandex.FieldInfo;

/**
 * @author Steve Ebersole
 */
public class FieldInfoBuilder {
	private final FieldInfo fieldInfo;
	private final Map<DotName, List<AnnotationInstance>> fieldAnnotations;
	private final Consumer<AnnotationInstance> annotationInstanceConsumer;

	public FieldInfoBuilder(FieldInfo incomingFieldInfo, Consumer<AnnotationInstance> annotationInstanceConsumer) {
		this.annotationInstanceConsumer = annotationInstanceConsumer;
	}
}
