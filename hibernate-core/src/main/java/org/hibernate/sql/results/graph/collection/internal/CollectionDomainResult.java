/*
 * Hibernate, Relational Persistence for Idiomatic Java
 *
 * License: GNU Lesser General Public License (LGPL), version 2.1 or later
 * See the lgpl.txt file in the root directory or http://www.gnu.org/licenses/lgpl-2.1.html
 */
package org.hibernate.sql.results.graph.collection.internal;

import java.util.Collections;
import java.util.List;

import org.hibernate.LockMode;
import org.hibernate.collection.spi.CollectionInitializerProducer;
import org.hibernate.collection.spi.CollectionSemantics;
import org.hibernate.metamodel.mapping.CollectionPart;
import org.hibernate.metamodel.mapping.PluralAttributeMapping;
import org.hibernate.query.NavigablePath;
import org.hibernate.sql.ast.tree.from.TableGroup;
import org.hibernate.sql.results.graph.AssemblerCreationState;
import org.hibernate.sql.results.graph.DomainResult;
import org.hibernate.sql.results.graph.DomainResultAssembler;
import org.hibernate.sql.results.graph.DomainResultCreationState;
import org.hibernate.sql.results.graph.Fetch;
import org.hibernate.sql.results.graph.FetchParent;
import org.hibernate.sql.results.graph.FetchableContainer;
import org.hibernate.sql.results.graph.collection.CollectionInitializer;
import org.hibernate.sql.results.graph.collection.CollectionResultGraphNode;
import org.hibernate.type.descriptor.java.JavaTypeDescriptor;

/**
 * @author Steve Ebersole
 */
public class CollectionDomainResult implements DomainResult, CollectionResultGraphNode, FetchParent {
	private final NavigablePath loadingPath;
	private final PluralAttributeMapping loadingAttribute;

	private final String resultVariable;

	private final DomainResult fkResult;

	private final CollectionInitializerProducer initializerProducer;

	public CollectionDomainResult(
			NavigablePath loadingPath,
			PluralAttributeMapping loadingAttribute,
			String resultVariable,
			TableGroup tableGroup,
			DomainResultCreationState creationState) {
		this.loadingPath = loadingPath;
		this.loadingAttribute = loadingAttribute;
		this.resultVariable = resultVariable;

		this.fkResult = loadingAttribute.getForeignKeyDescriptor().getReferringSide().getKeyPart().createDomainResult(
				loadingPath,
				tableGroup,
				null,
				creationState
		);

		final CollectionSemantics collectionSemantics = loadingAttribute.getCollectionDescriptor().getCollectionSemantics();
		initializerProducer = collectionSemantics.createInitializerProducer(
				loadingPath,
				loadingAttribute,
				this,
				true,
				null,
				LockMode.READ,
				creationState
		);
	}

	@Override
	public String getResultVariable() {
		return resultVariable;
	}

	@Override
	public JavaTypeDescriptor getResultJavaTypeDescriptor() {
		return loadingAttribute.getJavaTypeDescriptor();
	}

	@Override
	public DomainResultAssembler createResultAssembler(AssemblerCreationState creationState) {
		final CollectionInitializer initializer = (CollectionInitializer) creationState.resolveInitializer(
				getNavigablePath(),
				() -> {
					final DomainResultAssembler referringAssembler = fkResult.createResultAssembler( creationState );

					return initializerProducer.produceInitializer(
							loadingPath,
							loadingAttribute,
							null,
							LockMode.READ,
							referringAssembler,
							referringAssembler,
							creationState
					);
				}
		);

		return new EagerCollectionAssembler( loadingAttribute, initializer );
	}

	@Override
	public FetchableContainer getReferencedMappingContainer() {
		return loadingAttribute;
	}

	@Override
	public FetchableContainer getReferencedMappingType() {
		return getReferencedMappingContainer();
	}

	@Override
	public NavigablePath getNavigablePath() {
		return loadingPath;
	}

	@Override
	public Fetch getKeyFetch() {
		return initializerProducer.getIndexFetch();
	}

	public Fetch getElementFetch() {
		return initializerProducer.getElementFetch();
	}

	@Override
	public List<Fetch> getFetches() {
		return Collections.singletonList( initializerProducer.getElementFetch() );
	}

	@Override
	public Fetch findFetch(String fetchableName) {
		final CollectionPart.Nature nature = CollectionPart.Nature.fromName( fetchableName );
		if ( nature == null ) {
			return null;
		}

		switch ( nature ) {
			case INDEX: {
				return getKeyFetch();
			}
			case ELEMENT: {
				return getElementFetch();
			}
			default: {
				return null;
			}
		}
	}

}
