/*
 * Hibernate, Relational Persistence for Idiomatic Java
 *
 * License: GNU Lesser General Public License (LGPL), version 2.1 or later.
 * See the lgpl.txt file in the root directory or http://www.gnu.org/licenses/lgpl-2.1.html.
 */
package org.hibernate.engine.jdbc.mutation.internal;

import org.hibernate.type.descriptor.ValueBinder;

/**
 * @author Steve Ebersole
 */
class Binding {
	private final BindingGroup bindingGroup;
	private final Object value;
	private final ValueBinder<Object> valueBinder;
	private final int position;

	public Binding(BindingGroup bindingGroup, Object value, ValueBinder<Object> valueBinder, int position) {
		this.bindingGroup = bindingGroup;
		this.value = value;
		this.valueBinder = valueBinder;
		this.position = position;
	}

	public BindingGroup getBindingGroup() {
		return bindingGroup;
	}

	public Object getValue() {
		return value;
	}

	public ValueBinder<Object> getValueBinder() {
		return valueBinder;
	}

	public int getPosition() {
		return position;
	}

	@Override
	public int hashCode() {
		return bindingGroup.hashCode() + position;
	}

	@Override
	public boolean equals(Object o) {
		if ( this == o ) {
			return true;
		}
		if ( o == null || getClass() != o.getClass() ) {
			return false;
		}
		final Binding other = (Binding) o;
		return position == ( other ).position;
	}
}
