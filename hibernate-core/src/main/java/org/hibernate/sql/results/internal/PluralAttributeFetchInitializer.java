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
import org.hibernate.sql.results.spi.FetchParentAccess;
import org.hibernate.sql.results.spi.Initializer;
import org.hibernate.sql.results.spi.PluralAttributeFetch;

import org.jboss.logging.Logger;

/**
 * @author Steve Ebersole
 */
public class PluralAttributeFetchInitializer extends AbstractPluralAttributeInitializer {
	private static final Logger log = Logger.getLogger( PluralAttributeFetchInitializer.class );

	private final FetchParentAccess parentAccess;

	public PluralAttributeFetchInitializer(
			FetchParentAccess parentAccess,
			PluralAttributeFetch fetchDescriptor,
			Consumer<Initializer> initializerConsumer,
			AssemblerCreationState creationState,
			AssemblerCreationContext creationContext) {
		super( fetchDescriptor, initializerConsumer, creationState, creationContext );
		this.parentAccess = parentAccess;
	}
}
