/*
 * Hibernate, Relational Persistence for Idiomatic Java
 *
 * License: GNU Lesser General Public License (LGPL), version 2.1 or later
 * See the lgpl.txt file in the root directory or http://www.gnu.org/licenses/lgpl-2.1.html
 */
package org.hibernate.sql.results.graph.embeddable.internal;

import java.util.ArrayList;
import java.util.List;

import org.hibernate.internal.util.MutableInteger;
import org.hibernate.metamodel.mapping.EmbeddableMappingType;
import org.hibernate.metamodel.mapping.EmbeddableValuedModelPart;
import org.hibernate.query.NavigablePath;
import org.hibernate.sql.ast.spi.SqlSelection;
import org.hibernate.sql.results.graph.AbstractFetchParent;
import org.hibernate.sql.results.graph.AssemblerCreationState;
import org.hibernate.sql.results.graph.DomainResult;
import org.hibernate.sql.results.graph.DomainResultAssembler;
import org.hibernate.sql.results.graph.DomainResultCreationState;
import org.hibernate.sql.results.graph.Fetch;
import org.hibernate.sql.results.graph.embeddable.EmbeddableInitializer;
import org.hibernate.sql.results.graph.embeddable.EmbeddableResultGraphNode;

/**
 * @author Andrea Boriero
 */
public class EmbeddableForeignKeyResultImpl<T>
		extends AbstractFetchParent
		implements EmbeddableResultGraphNode, DomainResult<T> {

	private final String resultVariable;

	public EmbeddableForeignKeyResultImpl(
			List<SqlSelection> sqlSelections,
			NavigablePath navigablePath,
			EmbeddableValuedModelPart embeddableValuedModelPart,
			String resultVariable,
			DomainResultCreationState creationState) {
		super( embeddableValuedModelPart.getEmbeddableTypeDescriptor(), navigablePath );
		this.resultVariable = resultVariable;
		fetches = new ArrayList<>();
		MutableInteger index = new MutableInteger( 0 );

		afterInitialize( creationState );
	}

	@Override
	public String getResultVariable() {
		return resultVariable;
	}

	@Override
	public DomainResultAssembler<T> createResultAssembler(AssemblerCreationState creationState) {
		final EmbeddableInitializer initializer = (EmbeddableInitializer) creationState.resolveInitializer(
				getNavigablePath(),
				() -> new EmbeddableResultInitializer(this, creationState )
		);

		//noinspection unchecked
		return new EmbeddableAssembler( initializer );
	}

	@Override
	public EmbeddableMappingType getReferencedMappingType() {
		return (EmbeddableMappingType) getFetchContainer().getPartMappingType();
	}

	@Override
	public Fetch findFetch(String fetchableName) {
		return super.findFetch( fetchableName );
	}

	@Override
	public EmbeddableMappingType getFetchContainer() {
		return (EmbeddableMappingType) super.getFetchContainer();
	}

	@Override
	public EmbeddableValuedModelPart getReferencedMappingContainer() {
		return getFetchContainer().getEmbeddedValueMapping();
	}
}
