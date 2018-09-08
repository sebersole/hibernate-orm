/*
 * Hibernate, Relational Persistence for Idiomatic Java
 *
 * License: GNU Lesser General Public License (LGPL), version 2.1 or later
 * See the lgpl.txt file in the root directory or http://www.gnu.org/licenses/lgpl-2.1.html
 */
package org.hibernate.sql.results.internal.domain.collection;

import java.util.function.Consumer;

import org.hibernate.metamodel.model.domain.spi.PluralPersistentAttribute;
import org.hibernate.sql.results.spi.AssemblerCreationContext;
import org.hibernate.sql.results.spi.AssemblerCreationState;
import org.hibernate.sql.results.spi.DomainResult;
import org.hibernate.sql.results.spi.DomainResultAssembler;
import org.hibernate.sql.results.spi.Initializer;
import org.hibernate.sql.results.spi.PluralAttributeResult;

/**
 * @author Steve Ebersole
 */
public class PluralAttributeResultImpl
		extends AbstractPluralAttributeMappingNode
		implements PluralAttributeResult {

	public PluralAttributeResultImpl(
			PluralPersistentAttribute attributeDescriptor,
			String resultVariable,
			DomainResult keyResult,
			DomainResult identifierResult,
			DomainResult indexResult,
			DomainResult elementResult) {
		super(
				attributeDescriptor,
				resultVariable,
				keyResult,
				identifierResult,
				indexResult,
				elementResult
		);
	}

	@Override
	public String getResultVariable() {
		return super.getResultVariable();
	}

	@Override
	public DomainResultAssembler createResultAssembler(
			Consumer<Initializer> initializerCollector,
			AssemblerCreationState creationOptions,
			AssemblerCreationContext creationContext) {
		final PluralAttributeRootInitializer initializer = new PluralAttributeRootInitializer(
				this,
				initializerCollector,
				creationOptions,
				creationContext
		);

		initializerCollector.accept( initializer );

		return new PluralAttributeAssemblerImpl( initializer );
	}
}
