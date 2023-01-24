/*
 * Hibernate, Relational Persistence for Idiomatic Java
 *
 * License: GNU Lesser General Public License (LGPL), version 2.1 or later.
 * See the lgpl.txt file in the root directory or http://www.gnu.org/licenses/lgpl-2.1.html.
 */
package org.hibernate.boot.annotations.model.spi;

import java.util.Collection;
import java.util.function.Consumer;

import org.hibernate.boot.annotations.source.spi.ClassDetails;

/**
 * Intermediate representation of a {@linkplain jakarta.persistence.metamodel.ManagedType managed type}
 *
 * @author Steve Ebersole
 */
public interface ManagedTypeMetadata {
	/**
	 * The underlying managed-class
	 */
	ClassDetails getManagedClass();

	LocalAnnotationProcessingContext getLocalProcessingContext();

	Collection<AttributeMetadata> getAttributes();

	void forEachAttribute(Consumer<AttributeMetadata> consumer);
}
