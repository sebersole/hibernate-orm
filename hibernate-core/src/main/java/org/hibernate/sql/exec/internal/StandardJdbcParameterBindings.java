/*
 * Hibernate, Relational Persistence for Idiomatic Java
 *
 * License: GNU Lesser General Public License (LGPL), version 2.1 or later
 * See the lgpl.txt file in the root directory or http://www.gnu.org/licenses/lgpl-2.1.html
 */
package org.hibernate.sql.exec.internal;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.function.BiConsumer;

import org.hibernate.cache.spi.QueryKey;
import org.hibernate.sql.ast.tree.spi.expression.ParameterSpec;
import org.hibernate.sql.exec.spi.JdbcParameterBindings;

/**
 * @author Steve Ebersole
 */
public class StandardJdbcParameterBindings implements JdbcParameterBindings {
	public static final StandardJdbcParameterBindings NO_BINDINGS = new StandardJdbcParameterBindings( Collections.emptyMap() );

	private final Map<ParameterSpec, Object> bindingMap;

	public StandardJdbcParameterBindings(Map<ParameterSpec, Object> bindingMap) {
		this.bindingMap = bindingMap;
	}

	@Override
	@SuppressWarnings("unchecked")
	public <J> J getBindValue(ParameterSpec parameter) {
		return (J) bindingMap.get( parameter );
	}

	@Override
	public void visitBindings(BiConsumer<ParameterSpec, Object> consumer) {
		bindingMap.forEach( consumer );
	}

	@Override
	public QueryKey.ParameterBindingsMemento generateQueryKeyMemento() {
		// todo (6.0) : implement this - as-is returning null simply does not differentiate caching at all
		return null;
	}

	public static class Builder {
		private Map<ParameterSpec,Object> bindingMap;

		public void add(ParameterSpec parameterSpec, Object value) {
			if ( bindingMap == null ) {
				bindingMap = new HashMap<>();
			}
			bindingMap.put( parameterSpec, value );
		}

		public StandardJdbcParameterBindings build() {
			if ( bindingMap == null ) {
				return NO_BINDINGS;
			}
			else {
				return new StandardJdbcParameterBindings( bindingMap );
			}
		}
	}
}
