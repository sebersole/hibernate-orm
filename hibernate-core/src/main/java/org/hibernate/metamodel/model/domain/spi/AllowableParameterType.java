/*
 * Hibernate, Relational Persistence for Idiomatic Java
 *
 * License: GNU Lesser General Public License (LGPL), version 2.1 or later
 * See the lgpl.txt file in the root directory or http://www.gnu.org/licenses/lgpl-2.1.html
 */
package org.hibernate.metamodel.model.domain.spi;

import java.util.function.Predicate;

import org.hibernate.sql.ast.produce.metamodel.spi.ExpressableType;
import org.hibernate.type.descriptor.spi.ValueBinder;
import org.hibernate.type.spi.TypeConfiguration;

/**
 * @author Steve Ebersole
 */
public interface AllowableParameterType<T> extends ExpressableType<T> {
	Predicate<StateArrayContributor> STANDARD_INSERT_INCLUSION_CHECK = StateArrayContributor::isInsertable;

	Predicate<StateArrayContributor> STANDARD_UPDATE_INCLUSION_CHECK = StateArrayContributor::isUpdatable;

	/**
	 * The number of JDBC parameters needed for this type.  Should
	 * be the same as each {@link ValueBinder#getNumberOfJdbcParametersNeeded()}
	 */
	int getNumberOfJdbcParametersNeeded();

	/**
	 * Get a binder for values of the given type
	 */
	ValueBinder getValueBinder(Predicate<StateArrayContributor> inclusionChecker, TypeConfiguration typeConfiguration);
}
