/*
 * Hibernate, Relational Persistence for Idiomatic Java
 *
 * License: GNU Lesser General Public License (LGPL), version 2.1 or later
 * See the lgpl.txt file in the root directory or http://www.gnu.org/licenses/lgpl-2.1.html
 */
package org.hibernate.metamodel.model.domain;

import org.hibernate.type.descriptor.ValueExtractor;

/**
 * Specialization of DomainType for types that can be used as query parameter bind values
 *
 * @author Steve Ebersole
 */
public interface AllowableParameterType<J> extends SimpleDomainType<J> {
	ValueExtractor<J> getValueExtractor();
}
