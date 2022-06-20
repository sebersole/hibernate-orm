/*
 * Hibernate, Relational Persistence for Idiomatic Java
 *
 * License: GNU Lesser General Public License (LGPL), version 2.1 or later.
 * See the lgpl.txt file in the root directory or http://www.gnu.org/licenses/lgpl-2.1.html.
 */
package org.hibernate.engine.jdbc.mutation.internal;

import java.util.LinkedHashSet;
import java.util.Set;
import java.util.function.Consumer;

import org.hibernate.type.descriptor.ValueBinder;

/**
 * @author Steve Ebersole
 */
class BindingGroup {
	private final String tableName;
	private final Set<Binding> bindings;

	public BindingGroup(String tableName) {
		this.tableName = tableName;
		this.bindings = new LinkedHashSet<>();
	}

	public String getTableName() {
		return tableName;
	}

	public Set<Binding> getBindings() {
		return bindings;
	}

	public void forEachBinding(Consumer<Binding> action) {
		bindings.forEach( action );
	}

	public void bindParameter(
			Object value,
			ValueBinder<Object> valueBinder,
			int position) {
		bindings.add( new Binding( this, value, valueBinder, position ) );
	}

	public void clear() {
		bindings.clear();
	}
}
