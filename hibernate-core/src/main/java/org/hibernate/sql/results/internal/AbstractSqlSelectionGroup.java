/*
 * Hibernate, Relational Persistence for Idiomatic Java
 *
 * License: GNU Lesser General Public License (LGPL), version 2.1 or later
 * See the lgpl.txt file in the root directory or http://www.gnu.org/licenses/lgpl-2.1.html
 */
package org.hibernate.sql.results.internal;

import java.util.List;
import java.util.Map;
import java.util.function.BiConsumer;

import org.hibernate.metamodel.model.domain.spi.StateArrayContributor;
import org.hibernate.sql.results.spi.SqlSelection;
import org.hibernate.sql.results.spi.SqlSelectionGroup;

/**
 * @author Steve Ebersole
 */
public abstract class AbstractSqlSelectionGroup implements SqlSelectionGroup {
	private final Map<StateArrayContributor<?>,List<SqlSelection>> sqlSelectionsByContributor;

	public AbstractSqlSelectionGroup(Map<StateArrayContributor<?>, List<SqlSelection>> sqlSelectionsByContributor) {
		this.sqlSelectionsByContributor = sqlSelectionsByContributor;
	}

	@Override
	public List<SqlSelection> getSqlSelections(StateArrayContributor contributor) {
		return sqlSelectionsByContributor.get( contributor );
	}

	@Override
	public void visitSelections(BiConsumer<StateArrayContributor, List<SqlSelection>> action) {
		sqlSelectionsByContributor.forEach( action );
	}
}
