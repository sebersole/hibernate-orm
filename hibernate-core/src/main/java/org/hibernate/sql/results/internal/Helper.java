/*
 * Hibernate, Relational Persistence for Idiomatic Java
 *
 * License: GNU Lesser General Public License (LGPL), version 2.1 or later
 * See the lgpl.txt file in the root directory or http://www.gnu.org/licenses/lgpl-2.1.html
 */
package org.hibernate.sql.results.internal;

import java.util.Collection;
import java.util.List;

import org.hibernate.metamodel.model.domain.spi.StateArrayContributor;
import org.hibernate.sql.results.spi.RowProcessingState;
import org.hibernate.sql.results.spi.SqlSelection;
import org.hibernate.sql.results.spi.SqlSelectionGroup;

/**
 * @author Steve Ebersole
 */
public class Helper {
	/**
	 * Singleton access
	 */
	public static final Helper INSTANCE = new Helper();

	private Helper() {
	}

	public Object[] structureState(
			Collection<StateArrayContributor<?>> contributors,
			SqlSelectionGroup selectionGroup,
			RowProcessingState jdbcValues) {
		final Object[] results = new Object[ contributors.size() ];
		for ( StateArrayContributor<?> contributor : contributors ) {
			results[ contributor.getStateArrayPosition() ] = shapeContributorState(
					selectionGroup,
					contributor,
					jdbcValues
			);
		}
		return results;

	}

	private Object shapeContributorState(
			SqlSelectionGroup selectionGroup,
			StateArrayContributor<?> contributor,
			RowProcessingState jdbcValues) {
		final List<SqlSelection> sqlSelections = selectionGroup.getSqlSelections( contributor );
		if ( sqlSelections.size() == 1 ) {
			return jdbcValues.getJdbcValue( sqlSelections.get( 0 ) );
		}
		else {
			final Object[] values = new Object[ sqlSelections.size() ];
			for ( int i = 0; i < sqlSelections.size(); i++ ) {
				values[ i ] = jdbcValues.getJdbcValue( sqlSelections.get( i ) );
				// todo (6.0) : should really apply this recursively
				//		StateArrayContributorContainer?  As in:
				//
				//			if ( contributor instanceof StateArrayContributorContainer ) {
				//				structureState(
				//						( (StateArrayContributorContainer) contributor ).getStateArrayContributors(),
				//
				//				);
				//			}
				//
				// But how to know the "sub" SqlSelectionGroup?
			}
			return values;
		}
	}
}
