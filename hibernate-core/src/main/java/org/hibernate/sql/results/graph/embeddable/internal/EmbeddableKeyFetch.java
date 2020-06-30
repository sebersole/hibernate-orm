/*
 * Hibernate, Relational Persistence for Idiomatic Java
 *
 * License: GNU Lesser General Public License (LGPL), version 2.1 or later
 * See the lgpl.txt file in the root directory or http://www.gnu.org/licenses/lgpl-2.1.html
 */
package org.hibernate.sql.results.graph.embeddable.internal;

import java.util.ArrayList;
import java.util.List;

import org.hibernate.LockMode;
import org.hibernate.engine.FetchTiming;
import org.hibernate.metamodel.mapping.EmbeddableMappingType;
import org.hibernate.metamodel.mapping.EmbeddableValuedModelPart;
import org.hibernate.metamodel.mapping.internal.fk.KeyModelPartComposite;
import org.hibernate.metamodel.mapping.internal.fk.KeyTableGroupComposite;
import org.hibernate.query.NavigablePath;
import org.hibernate.sql.ast.spi.FromClauseAccess;
import org.hibernate.sql.ast.spi.SqlAstCreationState;
import org.hibernate.sql.ast.tree.from.TableGroup;
import org.hibernate.sql.results.graph.AssemblerCreationState;
import org.hibernate.sql.results.graph.DomainResultAssembler;
import org.hibernate.sql.results.graph.DomainResultCreationState;
import org.hibernate.sql.results.graph.Fetch;
import org.hibernate.sql.results.graph.FetchParent;
import org.hibernate.sql.results.graph.FetchParentAccess;
import org.hibernate.sql.results.graph.Fetchable;
import org.hibernate.sql.results.graph.embeddable.EmbeddableInitializer;
import org.hibernate.sql.results.graph.embeddable.EmbeddableResultGraphNode;
import org.hibernate.type.descriptor.java.JavaTypeDescriptor;

/**
 * @author Steve Ebersole
 */
public class EmbeddableKeyFetch implements Fetch, EmbeddableResultGraphNode {
	private final NavigablePath navigablePath;
	private final KeyModelPartComposite fetchable;
	private final FetchTiming fetchTiming;
	private final FetchParent fetchParent;

	private final List<Fetch> fetches;

	public EmbeddableKeyFetch(
			NavigablePath navigablePath,
			KeyModelPartComposite fetchable,
			FetchTiming fetchTiming,
			FetchParent fetchParent,
			DomainResultCreationState creationState) {
		this.navigablePath = navigablePath;
		this.fetchable = fetchable;
		this.fetchTiming = fetchTiming;
		this.fetchParent = fetchParent;

		this.fetches = new ArrayList<>( fetchable.getNumberOfFetchables() );

		final SqlAstCreationState sqlAstCreationState = creationState.getSqlAstCreationState();
		final FromClauseAccess fromClauseAccess = sqlAstCreationState.getFromClauseAccess();

		fromClauseAccess.resolveTableGroup(
				navigablePath,
				p -> {
					final TableGroup parentTableGroup = fromClauseAccess.getTableGroup( fetchParent.getNavigablePath() );

					return new KeyTableGroupComposite(
							navigablePath,
							fetchable,
							parentTableGroup,
							creationState
					);
				}
		);

		fetchable.visitFetchables(
				subFetchable -> {
					fetches.add(
							subFetchable.generateFetch(
									this,
									navigablePath.append( subFetchable.getFetchableName() ),
									FetchTiming.IMMEDIATE,
									false,
									LockMode.READ,
									null,
									creationState
							)
					);
				},
				null
		);
	}

	@Override
	public NavigablePath getNavigablePath() {
		return navigablePath;
	}

	@Override
	public Fetch getKeyFetch() {
		return null;
	}

	@Override
	public List<Fetch> getFetches() {
		return fetches;
	}

	@Override
	public Fetch findFetch(String fetchableName) {
		for ( int i = 0; i < fetches.size(); i++ ) {
			final Fetch fetch = fetches.get( i );
			if ( fetchableName.equals( fetch.getFetchedMapping().getFetchableName() ) ) {
				return fetch;
			}
		}

		return null;
	}

	@Override
	public EmbeddableValuedModelPart getReferencedMappingContainer() {
		return fetchable;
	}

	@Override
	public EmbeddableMappingType getReferencedMappingType() {
		return fetchable.getEmbeddableTypeDescriptor();
	}

	@Override
	public FetchParent getFetchParent() {
		return fetchParent;
	}

	@Override
	public Fetchable getFetchedMapping() {
		return fetchable;
	}

	@Override
	public JavaTypeDescriptor getResultJavaTypeDescriptor() {
		return fetchable.getJavaTypeDescriptor();
	}

	@Override
	public FetchTiming getTiming() {
		return fetchTiming;
	}

	@Override
	public boolean hasTableGroup() {
		// if we get here, there was no TableGroup created
		return false;
	}

	@Override
	public DomainResultAssembler createAssembler(FetchParentAccess parentAccess, AssemblerCreationState creationState) {
		final EmbeddableInitializer initializer = (EmbeddableInitializer) creationState.resolveInitializer(
				getNavigablePath(),
				() -> new EmbeddableResultInitializer( this, creationState )
		);

		return new EmbeddableAssembler( initializer );
	}
}
