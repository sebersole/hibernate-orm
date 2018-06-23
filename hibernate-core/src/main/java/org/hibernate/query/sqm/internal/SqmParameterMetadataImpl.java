/*
 * Hibernate, Relational Persistence for Idiomatic Java
 *
 * License: GNU Lesser General Public License (LGPL), version 2.1 or later
 * See the lgpl.txt file in the root directory or http://www.gnu.org/licenses/lgpl-2.1.html
 */
package org.hibernate.query.sqm.internal;

import java.util.LinkedHashMap;
import java.util.Map;

import org.hibernate.query.internal.AbstractParameterMetadata;
import org.hibernate.query.internal.QueryParameterNamedImpl;
import org.hibernate.query.spi.QueryParameterImplementor;
import org.hibernate.query.sqm.tree.expression.SqmParameter;

/**
 * @author Steve Ebersole
 */
public class SqmParameterMetadataImpl extends AbstractParameterMetadata {
	private final LinkedHashMap<SqmParameter,QueryParameterImplementor<?>> paramXref;

	public SqmParameterMetadataImpl(
			Map<Integer, QueryParameterImplementor<?>> ordinalDescriptorMap,
			Map<String, QueryParameterImplementor<?>> namedDescriptorMap,
			LinkedHashMap<SqmParameter, QueryParameterImplementor<?>> paramXref) {
		super( ordinalDescriptorMap, namedDescriptorMap );
		this.paramXref = paramXref;
	}

	public LinkedHashMap<SqmParameter, QueryParameterImplementor<?>> getSqmParamToQueryParamXref() {
		return paramXref;
	}

	public static class Builder extends AbstractParameterMetadata.Builder {
		private LinkedHashMap<SqmParameter,QueryParameterImplementor<?>> paramXref;

		public void addParameter(SqmParameter sqmParameter) {
			final QueryParameterImplementor<?> queryParam = findOrCreate( sqmParameter );

			if ( paramXref == null ) {
				paramXref = new LinkedHashMap<>();
			}
			paramXref.put( sqmParameter, queryParam );
		}

		private QueryParameterImplementor<?> findOrCreate(SqmParameter sqmParameter) {
			final String name = sqmParameter.getName();
			if ( name != null ) {
				return findOrCreate( name, () ->  QueryParameterNamedImpl.fromSqm( sqmParameter ) );
			}

			final Integer position = sqmParameter.getPosition();
			if ( position != null ) {
				return findOrCreate( position, () ->  QueryParameterNamedImpl.fromSqm( sqmParameter ) );
			}

			throw new UnsupportedOperationException( "Unrecognized query parameter type : " + sqmParameter );
		}

		public SqmParameterMetadataImpl build() {
			return new SqmParameterMetadataImpl( finalizedOrdinalDescriptorMap(), finalizedNamedDescriptorMap(), paramXref );
		}
	}
}
