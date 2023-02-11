/*
 * Hibernate, Relational Persistence for Idiomatic Java
 *
 * License: GNU Lesser General Public License (LGPL), version 2.1 or later.
 * See the lgpl.txt file in the root directory or http://www.gnu.org/licenses/lgpl-2.1.html.
 */
package org.hibernate.boot.annotations.source.spi;

import java.lang.annotation.Annotation;
import java.util.List;
import java.util.function.BiConsumer;

import org.hibernate.internal.util.IndexedConsumer;

import jakarta.persistence.spi.PersistenceUnitInfo;

/**
 * A descriptor for "special classes" that Hibernate knows about through various means -
 * entities, embeddables, mapped-superclasses, attribute-converters, listeners, etc
 *
 * Models a {@linkplain PersistenceUnitInfo#getManagedClassNames() "managed class"},
 * but in the larger sense for all the "special classes" we know about -
 * entities, embeddables, mapped-superclasses, attribute-converters, listeners, etc
 * <p/>
 *
 * @author Steve Ebersole
 */
public interface ClassDetails extends AnnotationTarget {
	/**
	 * The name of the managed class.
	 * <p/>
	 * Generally this is the same as the {@linkplain #getClassName() class name}.
	 * But in the case of Hibernate's {@code entity-name} feature, this would
	 * be the {@code entity-name}
	 */
	String getName();

	/**
	 * The name of the {@link Class} of this managed-type.
	 *
	 * @apiNote Will be {@code null} for dynamic models
	 */
	String getClassName();

	@Override
	default Kind getKind() {
		return Kind.CLASS;
	}

	boolean isAbstract();

	ClassDetails getSuperType();

	List<ClassDetails> getImplementedInterfaceTypes();

	default boolean implementsInterface(Class<?> interfaceJavaType) {
		return implementsInterface( interfaceJavaType.getName() );
	}

	default boolean implementsInterface(ClassDetails interfaceType) {
		return implementsInterface( interfaceType.getClassName() );
	}

	default boolean implementsInterface(String interfaceTypeName) {
		final List<ClassDetails> implementedInterfaceTypes = getImplementedInterfaceTypes();
		for ( int i = 0; i < implementedInterfaceTypes.size(); i++ ) {
			final ClassDetails implementedInterfaceType = implementedInterfaceTypes.get( i );
			if ( implementedInterfaceType.getClassName().equals( interfaceTypeName ) ) {
				return true;
			}
		}
		return false;
	}

	/**
	 * Get the fields for this class
	 */
	List<FieldDetails> getFields();

	/**
	 * Visit each field
	 */
	void forEachField(IndexedConsumer<FieldDetails> consumer);

	/**
	 * Get the methods for this class
	 */
	List<MethodDetails> getMethods();

	/**
	 * Visit each method
	 */
	void forEachMethod(IndexedConsumer<MethodDetails> consumer);

	default <A extends Annotation> void forEachAnnotatedField(
			AnnotationDescriptor<A> annotation,
			BiConsumer<FieldDetails, AnnotationUsage<A>> consumer) {
		forEachField( (index, fieldDetails) -> {
			final AnnotationUsage<A> usage = fieldDetails.getAnnotation( annotation );
			if ( usage != null ) {
				consumer.accept( fieldDetails, usage );
			}
		} );
	}

	default <A extends Annotation> void forEachAnnotatedMethod(
			AnnotationDescriptor<A> annotation,
			BiConsumer<MethodDetails, AnnotationUsage<A>> consumer) {
		forEachMethod( (index, methodDetails) -> {
			final AnnotationUsage<A> usage = methodDetails.getAnnotation( annotation );
			if ( usage != null ) {
				consumer.accept( methodDetails, usage );
			}
		} );
	}

	<X> Class<X> toJavaClass();
}
