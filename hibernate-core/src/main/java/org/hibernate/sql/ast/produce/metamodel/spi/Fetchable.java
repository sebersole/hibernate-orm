/*
 * Hibernate, Relational Persistence for Idiomatic Java
 *
 * License: GNU Lesser General Public License (LGPL), version 2.1 or later
 * See the lgpl.txt file in the root directory or http://www.gnu.org/licenses/lgpl-2.1.html
 */
package org.hibernate.sql.ast.produce.metamodel.spi;

import org.hibernate.LockMode;
import org.hibernate.NotYetImplementedFor6Exception;
import org.hibernate.engine.FetchStrategy;
import org.hibernate.engine.spi.SharedSessionContractImplementor;
import org.hibernate.sql.results.spi.DomainResultCreationContext;
import org.hibernate.sql.results.spi.DomainResultCreationState;
import org.hibernate.sql.results.spi.Fetch;
import org.hibernate.sql.results.spi.FetchParent;
import org.hibernate.sql.results.spi.FetchParentAccess;

/**
 * @author Steve Ebersole
 */
public interface Fetchable<T> extends Joinable<T> {
	FetchStrategy getMappedFetchStrategy();

	Fetch generateFetch(
			FetchParent fetchParent,
			FetchStrategy fetchStrategy,
			LockMode lockMode, String resultVariable,
			DomainResultCreationContext creationContext,
			DomainResultCreationState creationState);

	default Object extractDelayedFetchKey(
			FetchParentAccess parentAccess,
			SharedSessionContractImplementor session) {
		// todo (6.0) : need to figure out how to handle FetchParentAccess for embeddables
		//		ultimately need the embeddable's container's key
		throw new NotYetImplementedFor6Exception( getClass() );
	}
}
