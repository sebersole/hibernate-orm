/*
 * Hibernate, Relational Persistence for Idiomatic Java
 *
 * License: GNU Lesser General Public License (LGPL), version 2.1 or later
 * See the lgpl.txt file in the root directory or http://www.gnu.org/licenses/lgpl-2.1.html
 */
package org.hibernate.sql.results.spi;

import java.util.HashMap;
import java.util.Map;

import org.hibernate.metamodel.model.domain.NavigableRole;
import org.hibernate.metamodel.model.domain.spi.Navigable;
import org.hibernate.query.NavigablePath;
import org.hibernate.sql.ast.produce.metamodel.spi.Fetchable;
import org.hibernate.sql.results.internal.BiDirectionalFetchImpl;
import org.hibernate.type.descriptor.java.spi.JavaTypeDescriptor;

/**
 * Maintains state while processing a Fetch graph to be able to detect
 * and handle circular bi-directional references
 *
 * @author Steve Ebersole
 */
public class CircularFetchDetector {
	private Map<NavigablePath, Fetch> navigablePathFetchMap;

	public Fetch findBiDirectionalFetch(FetchParent fetchParent, Fetchable fetchable) {
		if ( navigablePathFetchMap == null ) {
			return null;
		}

		// bi-directional references are a special case that need special treatment.
		//
		// `p.address.resident.homeAddress
		//
		// what we mean is a fetch path like `a.parent.child.parent`.  here the terminal
		// `parent` name is the same reference as its parent's (`a.parent.child`)
		// parent's (a.parent`) path.
		//
		// In such a case we want to (mostly) reuse the "parent parent" path fetch
		//
		// see if we have such a case...

		final NavigablePath parentParentPath = fetchParent.getNavigablePath().getParent();

		final NavigableRole fetchableNavigableRole = fetchable.getNavigableRole();
		final String fetchableNavName = fetchableNavigableRole.getNavigableName();

		if ( parentParentPath != null ) {
			// NOTE : pointing back to the root is a special special case :)
			//		it requires type checking to detect

			final FetchParent parentFetchParent = ( (Fetch) fetchParent ).getFetchParent();
//			final Fetch parentParentFetch = navigablePathFetchMap.get( parentParentPath );
			final Navigable parentParentNavigable = parentFetchParent.getNavigableContainer();

			if ( parentParentNavigable == fetchable ) {
				// we do...
				//
				// in other words, the `Fetchable`'s `NavigablePath`, relative to its FetchParent here would
				// be:
				// 		a.parent.child.parent
				//
				// it's parentPath is `a.parent.child` so its parentParentPath is `a.parent`.  so this Fetchable's
				// path is really the same reference as its parentParentPath.  This is a special case, handled here...

				// first, this *should* mean we have already "seen" the Fetch generated parentParentPath.  So
				// look up in the `navigablePathFetchMap` to get that Fetch

				// and use it to create and register the "bi directional" form

				final NavigablePath fetchableNavigablePath = fetchParent.getNavigablePath().append( fetchableNavName );

				final Fetch biDirectionalFetch = new BiDirectionalFetchImpl(
						(Fetch) parentFetchParent,
						fetchableNavigablePath
				);

				addFetch( biDirectionalFetch );

				return biDirectionalFetch;
			}
		}

		if ( parentParentPath != null && parentParentPath.getLocalName().equals( fetchableNavName ) ) {
		}

		return null;
	}

	public void addFetch(Fetch fetch) {
		if ( navigablePathFetchMap == null ) {
			navigablePathFetchMap = new HashMap<>();
		}
		navigablePathFetchMap.put( fetch.getNavigablePath(), fetch );
	}
}
