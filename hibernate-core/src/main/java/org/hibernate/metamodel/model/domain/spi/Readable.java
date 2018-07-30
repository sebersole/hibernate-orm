/*
 * Hibernate, Relational Persistence for Idiomatic Java
 *
 * License: GNU Lesser General Public License (LGPL), version 2.1 or later
 * See the lgpl.txt file in the root directory or http://www.gnu.org/licenses/lgpl-2.1.html
 */
package org.hibernate.metamodel.model.domain.spi;

import org.hibernate.NotYetImplementedFor6Exception;
import org.hibernate.engine.spi.EntityKey;
import org.hibernate.engine.spi.SharedSessionContractImplementor;
import org.hibernate.sql.exec.spi.ExecutionContext;
import org.hibernate.sql.results.spi.Initializer;
import org.hibernate.sql.results.spi.QueryResult;
import org.hibernate.sql.results.spi.QueryResultCreationContext;

/**
 *
 * @param <V> The data's JDBC values array form
 * @param <H> The data's
 * @param <D>
 */
public interface Readable<V,H,D> {

	default QueryResult createQueryResult(
			java.util.function.Consumer<Initializer> collector,
			QueryResultCreationContext creationContext) {
		throw new NotYetImplementedFor6Exception( getClass() );
	}

	// todo (6.0) : "hydrate" relies on SqlSelection and jdbc value processing uses those to build the hydrated values itself.
	//		seems like this contract should define a `hydrateState` method, but implementing
	//		such a thing means passing in the SqlSelection(s) need to access the state.
	//
	//		however, such a solution is the only real way to account for collections, e.g., which
	//		are such a contributor but which return a "special" marker value on hydrate
	//
	// something like:

	/**
	 * An array shaping method.
	 *
	 * @apiNote The incoming `jdbcValues` might be a single object or an array of objects
	 * depending on whether this Readable reported one or more SqlSelections.
	 * The return follows the same rules.  For a composite-value, an `Object[]` would be returned
	 * representing the composite's "simple state".  For entity-value, the return would
	 * be its id's "simple state" : again a single `Object` for simple ids, an array for
	 * composite ids.  All others return a single value.
	 *
	 * todo (6.0) : this may not be true for ANY mappings - verify
	 * 		- those may return the (id,discriminator) tuple
	 */
	default H hydrate(V jdbcValues, SharedSessionContractImplementor session) {
		throw new NotYetImplementedFor6Exception( getClass() );
	}

	/**
	 * Given a hydrated representation of this Readable, resolve its
	 * domain representation.
	 * <p>
	 * E.g. for a composite, the hydrated form is an Object[] representing the
	 * "simple state" of the composite's attributes.  Resolution of those values
	 * returns the instance of the component with its resolved values injected.
	 *
	 * @apiNote
	 */
	default D resolveHydratedState(
			H hydratedForm,
			ExecutionContext executionContext,
			SharedSessionContractImplementor session,
			Object containerInstance) {
		throw new NotYetImplementedFor6Exception( getClass() );
	}

	interface ResolutionContext {
		Object resolveEntityInstance(EntityKey entityKey, boolean eager);
	}
}
