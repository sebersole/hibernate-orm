/*
 * Hibernate, Relational Persistence for Idiomatic Java
 *
 * License: GNU Lesser General Public License (LGPL), version 2.1 or later.
 * See the lgpl.txt file in the root directory or http://www.gnu.org/licenses/lgpl-2.1.html.
 */
package org.hibernate.boot.annotations.model.spi;

import java.util.function.Consumer;

import org.hibernate.boot.annotations.source.spi.AnnotationUsage;
import org.hibernate.boot.annotations.source.spi.JpaAnnotations;

import jakarta.persistence.Inheritance;
import jakarta.persistence.InheritanceType;

/**
 * Intermediate representation of an {@linkplain jakarta.persistence.metamodel.IdentifiableType identifiable type}
 *
 * @author Steve Ebersole
 */
public interface IdentifiableTypeMetadata extends ManagedTypeMetadata {
	EntityHierarchy getHierarchy();

	IdentifiableTypeMetadata getSuperType();

	boolean isAbstract();

	default InheritanceType getLocallyDefinedInheritanceType() {
		final AnnotationUsage<Inheritance> localAnnotation = getManagedClass().getAnnotation( JpaAnnotations.INHERITANCE );
		if ( localAnnotation == null ) {
			return null;
		}

		return localAnnotation.getAttributeValue( "strategy" ).getValue();
	}

	boolean hasSubTypes();

	Iterable<IdentifiableTypeMetadata> getSubTypes();

	void forEachSubType(Consumer<IdentifiableTypeMetadata> consumer);

//	// todo (annotation-source) - id, version, etc
//
//	List<CallbacksMetadata> getJpaCallbacks();
//	void forEachJpaCallback(Consumer<CallbacksMetadata> consumer);
}
