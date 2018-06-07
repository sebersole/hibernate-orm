/*
 * Hibernate, Relational Persistence for Idiomatic Java
 *
 * License: GNU Lesser General Public License (LGPL), version 2.1 or later
 * See the lgpl.txt file in the root directory or http://www.gnu.org/licenses/lgpl-2.1.html
 */
package org.hibernate.sql.results.internal;

import org.hibernate.engine.spi.SharedSessionContractImplementor;
import org.hibernate.metamodel.model.domain.spi.EmbeddedTypeDescriptor;
import org.hibernate.sql.results.spi.CompositeSqlSelectionGroup;
import org.hibernate.sql.results.spi.JdbcValuesSourceProcessingOptions;
import org.hibernate.sql.results.spi.QueryResultAssembler;
import org.hibernate.sql.results.spi.RowProcessingState;
import org.hibernate.type.descriptor.java.spi.JavaTypeDescriptor;

/**
 * @author Steve Ebersole
 */
public class CompositeQueryResultAssembler implements QueryResultAssembler {
	private final CompositeQueryResultImpl returnComposite;
	private final CompositeSqlSelectionGroup sqlSelectionGroup;
	private final EmbeddedTypeDescriptor<?> embeddedDescriptor;

	public CompositeQueryResultAssembler(
			CompositeQueryResultImpl returnComposite,
			CompositeSqlSelectionGroup sqlSelectionGroup,
			EmbeddedTypeDescriptor embeddedPersister) {
		this.returnComposite = returnComposite;
		this.sqlSelectionGroup = sqlSelectionGroup;
		this.embeddedDescriptor = embeddedPersister;
	}

	@Override
	public JavaTypeDescriptor getJavaTypeDescriptor() {
		return returnComposite.getJavaTypeDescriptor();
	}

	@Override
	public Object assemble(RowProcessingState rowProcessingState, JdbcValuesSourceProcessingOptions options) {
		final SharedSessionContractImplementor session = rowProcessingState.getJdbcValuesSourceProcessingState()
				.getPersistenceContext();
		final Object[] values = (Object[]) sqlSelectionGroup.hydrateStateArray( rowProcessingState );
//		embeddedDescriptor.visitStateArrayNavigables(
//				contributor -> {
//					final List<SqlSelection> sqlSelections = sqlSelectionGroup.getSqlSelections( contributor );
//					final Object subValue;
//					if ( sqlSelections.isEmpty() ) {
//						subValue = rowProcessingState.getJdbcValue( sqlSelections.get( 0 ) );
//					}
//					else {
//						final Object[] subValues = new Object[ sqlSelections.size() ];
//						for ( int i = 0; i < sqlSelections.size(); i++ ) {
//							subValues[ i ] = rowProcessingState.getJdbcValue( sqlSelections.get( i ) );
//						}
//						subValue = subValues;
//					}
//
//					values[ contributor.getStateArrayPosition() ] = contributor.hydrate( subValue, session );
//				}
//		);
		embeddedDescriptor.visitStateArrayContributors(
				contributor -> {
					final int position = contributor.getStateArrayPosition();
					values[ position ] = contributor.resolveHydratedState(
							values[position],
							rowProcessingState,
							session,
							null
					);
				}
		);

		final Object instance = embeddedDescriptor.instantiate( session );
		embeddedDescriptor.setPropertyValues( instance, values );
		return instance;
	}
}
