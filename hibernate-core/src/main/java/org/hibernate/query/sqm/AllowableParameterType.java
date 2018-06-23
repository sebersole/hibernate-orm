/*
 * Hibernate, Relational Persistence for Idiomatic Java
 *
 * License: GNU Lesser General Public License (LGPL), version 2.1 or later
 * See the lgpl.txt file in the root directory or http://www.gnu.org/licenses/lgpl-2.1.html
 */
package org.hibernate.query.sqm;

import java.util.function.BiConsumer;
import javax.persistence.TemporalType;

import org.hibernate.annotations.Remove;
import org.hibernate.engine.spi.SharedSessionContractImplementor;
import org.hibernate.metamodel.model.relational.spi.Column;
import org.hibernate.sql.JdbcValueCollector;
import org.hibernate.sql.ast.produce.metamodel.spi.ExpressableType;
import org.hibernate.sql.ast.tree.spi.expression.Expression;
import org.hibernate.sql.ast.tree.spi.expression.ParameterSpec;
import org.hibernate.type.spi.TypeConfiguration;

/**
 * @author Steve Ebersole
 */
public interface AllowableParameterType<T> extends ExpressableType<T> {
	/**
	 * Should use {@link #toJdbcParameters} instead
	 */
	@Remove
	int getNumberOfJdbcParametersToBind();

	AllowableParameterType resolveTemporalPrecision(TemporalType temporalType, TypeConfiguration typeConfiguration);

	void toJdbcParameters(BiConsumer<Column,ParameterSpec<?>> collector);

	void toJdbcValues(
			Object parameterBindValue,
			JdbcValueCollector jdbcValueCollector,
			SharedSessionContractImplementor session);

	/**
	 * todo (6.0) : what to pass?
	 */
	Expression<?> toJdbcParameters();
}
