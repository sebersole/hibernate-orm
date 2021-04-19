package org.hibernate.boot.model.process.jandex.internal;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import org.jboss.jandex.AnnotationInstance;
import org.jboss.jandex.AnnotationValue;
import org.jboss.jandex.ClassInfo;
import org.jboss.jandex.DotName;
import org.jboss.jandex.IndexView;
import org.jboss.jandex.Type;

/**
 * Jandex IndexView containing the augmented mappings
 */
public class AugmentedIndexView implements IndexView {

	static final DotName REPEATABLE = DotName.createSimple( "java.lang.annotation.Repeatable" );

	private final Map<DotName, ClassInfo> classInfoMap = new HashMap<>();

	public AugmentedIndexView(IndexView baseline) {
		final Collection<ClassInfo> knownClasses = baseline.getKnownClasses();
		for ( ClassInfo knownClass : knownClasses ) {
			classInfoMap.put( knownClass.name(), knownClass );
		}
	}

	@Override
	public Collection<ClassInfo> getKnownClasses() {
		return classInfoMap.values();
	}

	@Override
	public ClassInfo getClassByName(DotName className) {
		return classInfoMap.get( className );
	}

	@Override
	public Collection<ClassInfo> getKnownDirectSubclasses(DotName className) {
		final List<ClassInfo> list = subclasses.get(className);
		return list == null ? Collections.emptyList() : Collections.unmodifiableList(list);
	}

	@Override
	public Collection<ClassInfo> getAllKnownSubclasses(DotName className) {
		return null;
	}

	@Override
	public Collection<ClassInfo> getKnownDirectImplementors(DotName className) {
		return null;
	}

	@Override
	public Collection<ClassInfo> getAllKnownImplementors(DotName interfaceName) {
		return null;
	}

	@Override
	public Collection<AnnotationInstance> getAnnotations(DotName annotationName) {
		final List<AnnotationInstance> list = annotations.get( annotationName );
		return list == null ? Collections.emptyList() : Collections.unmodifiableList( list );
	}

	@Override
	public Collection<AnnotationInstance> getAnnotationsWithRepeatable(DotName annotationName, IndexView index) {
		final ClassInfo annotationClass = index.getClassByName( annotationName );
		if ( annotationClass == null ) {
			throw new IllegalArgumentException( "Index does not contain the annotation definition: " + annotationName );
		}

		if ( !annotationClass.isAnnotation() ) {
			throw new IllegalArgumentException( "Not an annotation type: " + annotationClass );
		}

		final AnnotationInstance repeatable = annotationClass.classAnnotation( REPEATABLE );
		if ( repeatable == null ) {
			// Not a repeatable annotation
			return getAnnotations( annotationName );
		}

		final Type containing = repeatable.value().asClass();
		return getRepeatableAnnotations( annotationName, containing.name() );
	}

	private Collection<AnnotationInstance> getRepeatableAnnotations(DotName annotationName, DotName containingAnnotationName) {
		final List<AnnotationInstance> instances = new ArrayList<>( getAnnotations( annotationName ) );

		for ( AnnotationInstance containingInstance : getAnnotations( containingAnnotationName ) ) {
			for ( AnnotationInstance nestedInstance : containingInstance.value().asNestedArray() ) {
				// We need to set the target of the containing instance
				instances.add(
						AnnotationInstance.create(
								nestedInstance.name(),
								containingInstance.target(),
								nestedInstance.values()
						)
				);
			}
		}

		return instances;
	}
}
