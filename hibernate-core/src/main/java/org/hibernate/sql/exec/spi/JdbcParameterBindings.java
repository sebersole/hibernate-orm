/*
 * Hibernate, Relational Persistence for Idiomatic Java
 *
 * License: GNU Lesser General Public License (LGPL), version 2.1 or later
 * See the lgpl.txt file in the root directory or http://www.gnu.org/licenses/lgpl-2.1.html
 */
package org.hibernate.sql.exec.spi;

import java.util.function.BiConsumer;

import org.hibernate.cache.spi.QueryKey;
import org.hibernate.sql.ast.tree.spi.expression.ParameterSpec;

/**
 * Parameter bindings in the JDBC sense.
 *
 * @author Steve Ebersole
 */
public interface JdbcParameterBindings {
	/**
	 * Get the bind value associated with the given parameter
	 */
	<J> J getBindValue(ParameterSpec parameter);

	/**
	 * Visit each JDBC parameter binding
	 */
	void visitBindings(BiConsumer<ParameterSpec,Object> consumer);

	/**
	 * Generate a "memento" for these parameter bindings that can be used
	 * in creating a {@link org.hibernate.cache.spi.QueryKey}
	 */
	QueryKey.ParameterBindingsMemento generateQueryKeyMemento();
}
