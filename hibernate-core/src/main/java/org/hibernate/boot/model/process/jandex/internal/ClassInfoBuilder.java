/*
 * Hibernate, Relational Persistence for Idiomatic Java
 *
 * License: GNU Lesser General Public License (LGPL), version 2.1 or later.
 * See the lgpl.txt file in the root directory or <http://www.gnu.org/licenses/lgpl-2.1.html>.
 */
package org.hibernate.boot.model.process.jandex.internal;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.function.Consumer;

import org.hibernate.boot.spi.MetadataBuildingContext;

import org.jboss.jandex.AnnotationInstance;
import org.jboss.jandex.ClassInfo;
import org.jboss.jandex.DotName;
import org.jboss.jandex.FieldInfo;
import org.jboss.jandex.MethodInfo;

/**
 * @author Steve Ebersole
 */
public class ClassInfoBuilder {
	private final ClassInfo classInfo;
	private final Consumer<AnnotationInstance> annotationInstanceConsumer;

	private final Map<DotName, List<AnnotationInstance>> classAnnotations;

	private final List<FieldInfoBuilder> fieldInfoBuilders;
	private final List<MethodInfoBuilder> methodInfoBuilders;

	public ClassInfoBuilder(
			ClassInfo incoming,
			Consumer<AnnotationInstance> annotationInstanceConsumer) {
		this.annotationInstanceConsumer = annotationInstanceConsumer;

		classAnnotations = new HashMap<>();
		fieldInfoBuilders = new ArrayList<>();
		methodInfoBuilders = new ArrayList<>();

		this.classInfo = ClassInfo.create(
				incoming.name(),
				incoming.superName(),
				incoming.flags(),
				incoming.interfaces(),
				classAnnotations,
				incoming.hasNoArgsConstructor()
		);

		final Map<DotName, List<AnnotationInstance>> annotationsMap = incoming.annotations();
		annotationsMap.forEach(
				(dotName, annotationInstances) -> {
					annotationInstances.forEach(
							annotationInstance -> {
								final AnnotationInstance copy = AnnotationInstance.create(
										dotName,
										classInfo,
										annotationInstance.values()
								);
								annotationInstanceConsumer.accept( copy );

								final List<AnnotationInstance> list = classAnnotations.computeIfAbsent(
										dotName,
										(d) -> new ArrayList<>()
								);

								list.add( annotationInstance );
							}
					);
				}
		);

		final List<FieldInfo> incomingFields = incoming.fields();
		incomingFields.forEach(
				(incomingFieldInfo) -> {
					final FieldInfoBuilder fieldInfoBuilder = new FieldInfoBuilder( incomingFieldInfo, annotationInstanceConsumer );
					fieldInfoBuilders.add( fieldInfoBuilder );
				}
		);

		final List<MethodInfo> incomingMethodInfos = incoming.methods();
		incomingMethodInfos.forEach(
				(incomingMethodInfo) -> {
					final MethodInfoBuilder builder = new MethodInfoBuilder( incomingMethodInfo, annotationInstanceConsumer );
					methodInfoBuilders.add( builder );
				}
		);
	}

	public ClassInfo getClassInfo() {
		return classInfo;
	}

	public Map<DotName, List<AnnotationInstance>> getClassAnnotations() {
		return classAnnotations;
	}

	public List<FieldInfoBuilder> getFieldInfoBuilders() {
		return fieldInfoBuilders;
	}

	public List<MethodInfoBuilder> getMethodInfoBuilders() {
		return methodInfoBuilders;
	}
}
