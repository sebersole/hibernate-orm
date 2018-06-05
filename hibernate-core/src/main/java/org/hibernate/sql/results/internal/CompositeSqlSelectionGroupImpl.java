/*
 * Hibernate, Relational Persistence for Idiomatic Java
 *
 * License: GNU Lesser General Public License (LGPL), version 2.1 or later
 * See the lgpl.txt file in the root directory or http://www.gnu.org/licenses/lgpl-2.1.html
 */
package org.hibernate.sql.results.internal;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.hibernate.metamodel.model.domain.spi.EmbeddedTypeDescriptor;
import org.hibernate.metamodel.model.domain.spi.StateArrayContributor;
import org.hibernate.sql.ast.produce.spi.ColumnReferenceQualifier;
import org.hibernate.sql.results.spi.CompositeSqlSelectionGroup;
import org.hibernate.sql.results.spi.QueryResultCreationContext;
import org.hibernate.sql.results.spi.SqlSelection;

/**
 * @author Steve Ebersole
 */
public class CompositeSqlSelectionGroupImpl extends AbstractSqlSelectionGroup implements CompositeSqlSelectionGroup {

	public static CompositeSqlSelectionGroup buildSqlSelectionGroup(
			EmbeddedTypeDescriptor<?> embeddedDescriptor,
			ColumnReferenceQualifier qualifier,
			QueryResultCreationContext creationContext) {
		final Map<StateArrayContributor<?>,List<SqlSelection>> sqlSelectionsByContributor = new HashMap<>();

		for ( StateArrayContributor<?> stateArrayContributor : embeddedDescriptor.getStateArrayContributors() ) {
			sqlSelectionsByContributor.put(
					stateArrayContributor,
					stateArrayContributor.resolveSqlSelections( qualifier, creationContext )
			);
		}

		return new CompositeSqlSelectionGroupImpl( sqlSelectionsByContributor );
	}

	public CompositeSqlSelectionGroupImpl(Map<StateArrayContributor<?>, List<SqlSelection>> sqlSelectionsByContributor) {
		super( sqlSelectionsByContributor );
	}
}
