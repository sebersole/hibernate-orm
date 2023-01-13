/*
 * Hibernate, Relational Persistence for Idiomatic Java
 *
 * License: GNU Lesser General Public License (LGPL), version 2.1 or later.
 * See the lgpl.txt file in the root directory or http://www.gnu.org/licenses/lgpl-2.1.html.
 */
package org.hibernate.boot.annotations.type.spi;

import java.util.function.Consumer;

import org.hibernate.boot.annotations.source.spi.AnnotationUsage;
import org.hibernate.boot.annotations.source.spi.JpaAnnotations;

import jakarta.persistence.Inheritance;
import jakarta.persistence.InheritanceType;

/**
 * Categorization of a {@linkplain #getManagedClass() ManagedClass} as a
 * {@link jakarta.persistence.metamodel.IdentifiableType}
 *
 * @author Steve Ebersole
 */
public interface IdentifiableTypeMetadata extends ManagedTypeMetadata {
	IdentifiableTypeMetadata getSuperType();

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
//	List<JpaCallbackInformation> getJpaCallbacks();
//	void forEachJpaCallback(Consumer<JpaCallbackInformation> consumer);
}
