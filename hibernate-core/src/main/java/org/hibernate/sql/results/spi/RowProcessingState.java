/*
 * Hibernate, Relational Persistence for Idiomatic Java
 *
 * License: GNU Lesser General Public License (LGPL), version 2.1 or later
 * See the lgpl.txt file in the root directory or http://www.gnu.org/licenses/lgpl-2.1.html
 */
package org.hibernate.sql.results.spi;

import org.hibernate.engine.spi.EntityKey;
import org.hibernate.metamodel.model.domain.spi.Readable;

/**
 * State pertaining to the processing of a single row of a JdbcValuesSource
 *
 * @author Steve Ebersole
 */
public interface RowProcessingState extends Readable.ResolutionContext {
	/**
	 * Access to the "parent state" related to the overall processing
	 * of the results.
	 */
	JdbcValuesSourceProcessingState getJdbcValuesSourceProcessingState();

	/**
	 * Retrieve the value corresponding to the given SqlSelection as part
	 * of the "current JDBC row".
	 *
	 * We read all the ResultSet values for the given row one time
	 * and store them into an array internally based on the principle that multiple
	 * accesses to this array will be significantly faster than accessing them
	 * from the ResultSet potentially multiple times.
	 */
	Object getJdbcValue(SqlSelection sqlSelection);

	void registerNonExists(EntityFetch fetch);

	void finishRowProcessing();

	@Override
	default Object resolveEntityInstance(EntityKey entityKey, boolean eager) {
		// First, look for it in the PC as a managed entity
		final Object managedEntity = getJdbcValuesSourceProcessingState()
				.getPersistenceContext()
				.getPersistenceContext()
				.getEntity( entityKey );
		if ( managedEntity != null ) {
			// todo (6.0) : check status?  aka, return deleted entities?
			return managedEntity;
		}

		// Next, check currently loading entities
		final LoadingEntityEntry loadingEntry = getJdbcValuesSourceProcessingState()
				.getPersistenceContext()
				.getPersistenceContext()
				.getLoadContexts()
				.findLoadingEntityEntry( entityKey );
		if ( loadingEntry != null ) {
			return loadingEntry.getEntityInstance();
		}

		// Lastly, try to load from database
		return getJdbcValuesSourceProcessingState().getPersistenceContext().internalLoad(
				entityKey.getEntityName(),
				entityKey.getIdentifier(),
				eager,
				false
		);
	}
}
