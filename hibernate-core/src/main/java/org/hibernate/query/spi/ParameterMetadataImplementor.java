/*
 * Hibernate, Relational Persistence for Idiomatic Java
 *
 * License: GNU Lesser General Public License (LGPL), version 2.1 or later
 * See the lgpl.txt file in the root directory or http://www.gnu.org/licenses/lgpl-2.1.html
 */
package org.hibernate.query.spi;

import java.util.function.Consumer;
import java.util.function.Predicate;

import org.hibernate.query.ParameterMetadata;

/**
 * @author Steve Ebersole
 */
public interface ParameterMetadataImplementor<P extends QueryParameterImplementor<?>> extends ParameterMetadata<P> {
	@Override
	boolean containsReference(P parameter);

	/**
	 * Visits each query parameter.
	 *
	 * @apiNote The semantic here is to visit each parameter exactly once
	 * regardless of how many times that parameter appears in the query.
	 */
	void visitParameters(Consumer<P> collector);

	boolean hasAnyMatching(Predicate<P> filter);
}
