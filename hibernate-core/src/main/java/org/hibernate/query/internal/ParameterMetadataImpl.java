/*
 * Hibernate, Relational Persistence for Idiomatic Java
 *
 * License: GNU Lesser General Public License (LGPL), version 2.1 or later.
 * See the lgpl.txt file in the root directory or <http://www.gnu.org/licenses/lgpl-2.1.html>.
 */
package org.hibernate.query.internal;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.function.Supplier;

import org.hibernate.query.spi.QueryParameterImplementor;

/**
 * Encapsulates metadata about parameters encountered within a query.
 *
 * @author Steve Ebersole
 */
public class ParameterMetadataImpl extends AbstractParameterMetadata {
	private final List<QueryParameterImplementor<?>> queryParameterList;

	private ParameterMetadataImpl(
			Map<Integer, QueryParameterImplementor<?>> ordinalDescriptorMap,
			Map<String, QueryParameterImplementor<?>> namedDescriptorMap,
			List<QueryParameterImplementor<?>> queryParameterList) {
		super( ordinalDescriptorMap, namedDescriptorMap );
		this.queryParameterList = queryParameterList;
	}

	public static class Builder extends AbstractParameterMetadata.Builder {
		private List<QueryParameterImplementor<?>> queryParameterList;

		public QueryParameterImplementor<?> addNamed(String name, Supplier<QueryParameterImplementor<?>> creator) {
			return addParameter( findOrCreate( name, creator ) );
		}

		private QueryParameterImplementor<?> addParameter(QueryParameterImplementor<?> queryParameter) {
			if ( queryParameterList == null ) {
				queryParameterList = new ArrayList<>();
			}
			queryParameterList.add( queryParameter );
			return queryParameter;

		}

		public QueryParameterImplementor<?> addPositional(Integer position, Supplier<QueryParameterImplementor<?>> creator) {
			return addParameter( findOrCreate( position, creator ) );
		}

		public ParameterMetadataImpl build() {
			return new ParameterMetadataImpl( finalizedOrdinalDescriptorMap(), finalizedNamedDescriptorMap(), queryParameterList );
		}
	}
}
