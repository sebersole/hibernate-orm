/*
 * Hibernate, Relational Persistence for Idiomatic Java
 *
 * License: GNU Lesser General Public License (LGPL), version 2.1 or later
 * See the lgpl.txt file in the root directory or http://www.gnu.org/licenses/lgpl-2.1.html
 */
package org.hibernate.sql.exec.internal;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;
import java.util.function.BiConsumer;

import org.hibernate.sql.ast.tree.spi.expression.ParameterSpec;
import org.hibernate.sql.exec.spi.JdbcParameterBindings;
import org.hibernate.sql.exec.spi.MultiSetJdbcParameterBindings;

/**
 * @author Steve Ebersole
 */
public class StandardMultiSetJdbcParameterBindings implements MultiSetJdbcParameterBindings {
	public static final StandardMultiSetJdbcParameterBindings NO_BINDINGS = new StandardMultiSetJdbcParameterBindings( Collections.emptyList() );

	private Iterator<JdbcParameterBindings> bindingSets;

	private JdbcParameterBindings currentBindings;

	public StandardMultiSetJdbcParameterBindings(List<JdbcParameterBindings> bindingSets) {
		this.bindingSets = bindingSets.iterator();
	}

	@Override
	public boolean next() {
		final boolean hasNext = bindingSets.hasNext();
		if ( !hasNext ) {
			return false;
		}

		currentBindings = bindingSets.next();
		return true;
	}

	@Override
	public <J> J getBindValue(ParameterSpec parameter) {
		return currentBindings.getBindValue( parameter );
	}

	@Override
	public void visitBindings(BiConsumer<ParameterSpec, Object> consumer) {
		currentBindings.visitBindings( consumer );
	}

	public static class Builder {
		private List<JdbcParameterBindings> bindingSets;

		public void addBindingSet(JdbcParameterBindings bindingSet) {
			if ( bindingSets == null ) {
				bindingSets = new ArrayList<>();
			}

			bindingSets.add( bindingSet );
		}

		public StandardMultiSetJdbcParameterBindings build() {
			if( bindingSets == null ) {
				return NO_BINDINGS;
			}

			return new StandardMultiSetJdbcParameterBindings( bindingSets );
		}
	}
}
