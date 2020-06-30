/*
 * Hibernate, Relational Persistence for Idiomatic Java
 *
 * License: GNU Lesser General Public License (LGPL), version 2.1 or later
 * See the lgpl.txt file in the root directory or http://www.gnu.org/licenses/lgpl-2.1.html
 */
package org.hibernate.sql.results.graph;

import java.util.List;

import org.hibernate.NotYetImplementedFor6Exception;
import org.hibernate.metamodel.mapping.ModelPart;
import org.hibernate.query.NavigablePath;
import org.hibernate.sql.ast.spi.SqlAliasBaseManager;
import org.hibernate.sql.ast.spi.SqlAstCreationState;

/**
 * @author Steve Ebersole
 */
public interface DomainResultCreationState {

	default boolean forceIdentifierSelection(){
		return true;
	}

	SqlAstCreationState getSqlAstCreationState();

	default SqlAliasBaseManager getSqlAliasBaseManager() {
		return (SqlAliasBaseManager) getSqlAstCreationState().getSqlAliasBaseGenerator();
	}

	/**
	 * Resolve the ModelPart associated with a given NavigablePath.  More specific ModelParts should be preferred - e.g.
	 * the SingularAssociationAttributeMapping rather than just the EntityTypeMapping for the associated type
	 */
	default ModelPart resolveModelPart(NavigablePath navigablePath) {
		throw new NotYetImplementedFor6Exception( getClass() );
	}

	/**
	 * Visit fetches for the given parent.
	 *
	 * We walk fetches via the SqlAstCreationContext because each "context"
	 * will define differently what should be fetched (HQL versus load)
	 */
	List<Fetch> buildFetches(FetchParent fetchParent);

	/**
	 * Build the fetch list related to the key of the FetchParent.
	 *
	 * Distinct from {@link #buildFetches} to account for the different
	 * walking required wrt EntityGraph handling as well as make it easier to
	 * trigger walking the key-sub-graphs rather than the normal sub-graphs
	 */
	Fetch buildKeyFetch(FetchParent fetchParent);
}
