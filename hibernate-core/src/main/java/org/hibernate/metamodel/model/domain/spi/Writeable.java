/*
 * Hibernate, Relational Persistence for Idiomatic Java
 *
 * License: GNU Lesser General Public License (LGPL), version 2.1 or later
 * See the lgpl.txt file in the root directory or http://www.gnu.org/licenses/lgpl-2.1.html
 */
package org.hibernate.metamodel.model.domain.spi;

import java.util.function.BiConsumer;
import java.util.function.Consumer;
import java.util.function.Predicate;

import org.hibernate.NotYetImplementedFor6Exception;
import org.hibernate.engine.spi.SharedSessionContractImplementor;
import org.hibernate.metamodel.model.relational.spi.Column;
import org.hibernate.sql.SqlExpressableType;
import org.hibernate.sql.ast.Clause;
import org.hibernate.type.spi.TypeConfiguration;

/**
 * Represents a value that can ultimately be written to the database.  The process of
 * getting a value ready to write to the database involves any number of steps:
 *
 * 		* For basic values, this usually only applies any "value converters" (attribute converters, enum conversions, etc)
 *		* For components, this is (1) splits the composite into the individual sub-values array and then (2)
 *			applies any value conversions on these...
 *
 * <D> The domain representation of the writable
 * <I> The "intermediate" or hydrated form of the writeable - this is typically `Object` or `Object[]`
 *
 * @author Steve Ebersole
 */
public interface Writeable {
	Predicate<StateArrayContributor> STANDARD_INSERT_INCLUSION_CHECK = StateArrayContributor::isInsertable;
	Predicate<StateArrayContributor> STANDARD_UPDATE_INCLUSION_CHECK = StateArrayContributor::isUpdatable;

	default void visitJdbcTypes(
			Consumer<SqlExpressableType> action,
			Clause clause,
			TypeConfiguration typeConfiguration) {
		visitColumns(
				(type, column) -> action.accept( type ),
				clause,
				typeConfiguration
		);
	}

	default void visitColumns(
			BiConsumer<SqlExpressableType,Column> action,
			Clause clause,
			TypeConfiguration typeConfiguration) {
		throw new NotYetImplementedFor6Exception( getClass() );
	}

	/**
	 * Produce a multi-dimensional array of extracted simple value
	 */
	default Object unresolve(Object value, SharedSessionContractImplementor session) {
		throw new NotYetImplementedFor6Exception( getClass() );
	}

	/**
	 * Produce a flattened array from dehydrated state
	 */
	default void dehydrate(
			Object value,
			JdbcValueCollector jdbcValueCollector,
			Clause clause,
			SharedSessionContractImplementor session) {
		throw new NotYetImplementedFor6Exception( getClass() );
	}

	@FunctionalInterface
	interface JdbcValueCollector {
		void collect(Object jdbcValue, SqlExpressableType type, Column boundColumn);
	}
}
