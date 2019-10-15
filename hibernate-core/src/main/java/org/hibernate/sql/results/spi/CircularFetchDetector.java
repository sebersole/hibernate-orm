/*
 * Hibernate, Relational Persistence for Idiomatic Java
 *
 * License: GNU Lesser General Public License (LGPL), version 2.1 or later
 * See the lgpl.txt file in the root directory or http://www.gnu.org/licenses/lgpl-2.1.html
 */
package org.hibernate.sql.results.spi;


import org.hibernate.query.NavigablePath;
import org.hibernate.sql.results.internal.domain.BiDirectionalFetchImpl;
import org.hibernate.sql.results.internal.domain.RootBiDirectionalFetchImpl;

/**
 * Maintains state while processing a Fetch graph to be able to detect
 * and handle circular bi-directional references
 *
 * @author Steve Ebersole
 */
public class CircularFetchDetector {

	public Fetch findBiDirectionalFetch(FetchParent fetchParent, Fetchable fetchable) {
		if ( ! fetchable.isCircular( fetchParent ) ) {
			return null;
		}

		assert fetchParent instanceof Fetch;
		final Fetch fetchParentAsFetch = (Fetch) fetchParent;

		final String parentParentPath = fetchParentAsFetch.getFetchParent().getNavigablePath();
		assert parentParentPath != null;

		assert fetchParentAsFetch.getFetchParent().getNavigablePath().equals( parentParentPath );

		if ( fetchParentAsFetch.getFetchParent() instanceof Fetch ) {
			return new BiDirectionalFetchImpl(
					NavigablePath.append( fetchParent.getNavigablePath(), fetchable.getFetchableName() ),
					fetchParent,
					fetchParentAsFetch
			);
		}
		else {
			assert fetchParentAsFetch instanceof EntityResult;

			// note : the "`fetchParentAsFetch` is `RootBiDirectionalFetchImpl`" case would
			// 		be handled in the `Fetch` block since `RootBiDirectionalFetchImpl` is a Fetch

			return new RootBiDirectionalFetchImpl(
					NavigablePath.append( fetchParent.getNavigablePath(), fetchable.getFetchableName() ),
					fetchParent,
					(EntityResult) fetchParentAsFetch
			);
		}
	}
}
