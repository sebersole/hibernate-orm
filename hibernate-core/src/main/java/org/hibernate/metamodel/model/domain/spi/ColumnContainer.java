/*
 * Hibernate, Relational Persistence for Idiomatic Java
 *
 * License: GNU Lesser General Public License (LGPL), version 2.1 or later
 * See the lgpl.txt file in the root directory or http://www.gnu.org/licenses/lgpl-2.1.html
 */
package org.hibernate.metamodel.model.domain.spi;

import java.util.function.BiConsumer;
import java.util.function.Consumer;

import org.hibernate.NotYetImplementedFor6Exception;
import org.hibernate.metamodel.model.relational.spi.Column;
import org.hibernate.sql.ast.tree.spi.expression.ParameterSpec;
import org.hibernate.sql.ast.tree.spi.expression.StandardJdbcParameter;

/**
 * @author Steve Ebersole
 */
public interface ColumnContainer {
	default void visitColumns(Consumer<Column> consumer) {
		throw new NotYetImplementedFor6Exception( getClass() );
	}

	default void toJdbcParameters(BiConsumer<Column, ParameterSpec<?>> collector) {
		visitColumns(
				column -> {
					collector.accept( column, new StandardJdbcParameter( column.getJdbcValueMapper() ) );
				}
		);
	}
}
