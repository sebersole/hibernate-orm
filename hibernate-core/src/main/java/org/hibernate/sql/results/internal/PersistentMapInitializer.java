/*
 * Hibernate, Relational Persistence for Idiomatic Java
 *
 * License: GNU Lesser General Public License (LGPL), version 2.1 or later
 * See the lgpl.txt file in the root directory or http://www.gnu.org/licenses/lgpl-2.1.html
 */
package org.hibernate.sql.results.internal;

import java.util.function.Consumer;

import org.hibernate.sql.results.spi.AssemblerCreationContext;
import org.hibernate.sql.results.spi.AssemblerCreationState;
import org.hibernate.sql.results.spi.Initializer;
import org.hibernate.sql.results.spi.PluralAttributeMappingNode;

/**
 * @author Steve Ebersole
 */
public class PersistentMapInitializer extends AbstractPluralAttributeInitializer {
	public PersistentMapInitializer(
			PluralAttributeMappingNode resultDescriptor,
			Consumer<Initializer> initializerConsumer,
			AssemblerCreationState creationState,
			AssemblerCreationContext creationContext) {
		super( resultDescriptor, initializerConsumer, creationState, creationContext );
	}
}
