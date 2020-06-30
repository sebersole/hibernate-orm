/*
 * Hibernate, Relational Persistence for Idiomatic Java
 *
 * License: GNU Lesser General Public License (LGPL), version 2.1 or later
 * See the lgpl.txt file in the root directory or http://www.gnu.org/licenses/lgpl-2.1.html
 */
package org.hibernate.sql.results.graph;

import org.hibernate.metamodel.mapping.ModelPart;
import org.hibernate.query.NavigablePath;
import org.hibernate.sql.exec.spi.ExecutionContext;
import org.hibernate.sql.results.jdbc.spi.RowProcessingState;

/**
 * Defines a multi-step process for initializing entity, collection and composite
 * state.  Each step is performed on every initializer before starting the next
 * step.
 *
 * @author Steve Ebersole
 */
public interface Initializer {

	// todo (6.0) : define `ResultInitializer` and `FetchInitializer` specializations?
	//		- would allow us to do things like `FetchInitializer#getFetchOptions`
	//
	// todo (6.0) : relatedly (^^) consider replacing `LoadType`, etc with info on the DomainResult?
	//		- I.e. `DomainResult#allowProxyCreation`, `DomainResult#unwrapProxies`, etc
	//		- see `org.hibernate.event.spi.LoadEventListener.LoadType`


	/**
	 * The path being initialized
	 */
	NavigablePath getNavigablePath();

	/**
	 * The model-part being initialized
	 */
	ModelPart getInitializedPart();

	/**
	 * The instance being initialized as part of the current row.
	 *
	 * Note, this is available after {@link #resolveInstance} is processed.
	 * It *may* be available prior to that, though that is dependant on the
	 * specific Initializer type and should not be depended on generically.
	 */
	Object getInitializedInstance();

	/**
	 * Step 1 - Resolve the key value for this initializer for the current
	 * row.
	 *
	 * After this point, the initializer knows the entity/collection/component
	 * key for the current row
	 */
	void resolveKey(RowProcessingState rowProcessingState);

	/**
	 * Step 2 - Using the key resolved in {@link #resolveKey}, resolve the
	 * instance (of the thing initialized) to use for the current row.
	 *
	 * After this point, the initializer knows the entity/collection/component
	 * instance for the current row based on the resolved key
	 *
	 * todo (6.0) : much of the various implementations of this are similar enough to handle in a common base implementation (templating?)
	 * 		things like resolving as managed (Session cache), from second-level cache, from LoadContext, etc..
	 */
	void resolveInstance(RowProcessingState rowProcessingState);

	/**
	 * Step 3 - Initialize the state of the instance resolved in
	 * {@link #resolveInstance} from the current row values.
	 *
	 * All resolved state for the current row is injected into the resolved
	 * instance
	 */
	void initializeInstance(RowProcessingState rowProcessingState);

	/**
	 * Lifecycle method called at the end of the current row processing.
	 * Provides ability to complete processing from the current row and
	 * prepare for the next row.
	 */
	void finishUpRow(RowProcessingState rowProcessingState);

	/**
	 * Lifecycle method called at the very end of the result values processing
	 */
	default void endLoading(ExecutionContext context) {
		// by default - nothing to do
	}
}
