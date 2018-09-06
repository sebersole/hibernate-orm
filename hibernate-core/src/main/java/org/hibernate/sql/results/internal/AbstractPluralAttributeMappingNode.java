/*
 * Hibernate, Relational Persistence for Idiomatic Java
 *
 * License: GNU Lesser General Public License (LGPL), version 2.1 or later
 * See the lgpl.txt file in the root directory or http://www.gnu.org/licenses/lgpl-2.1.html
 */
package org.hibernate.sql.results.internal;

import org.hibernate.metamodel.model.domain.spi.PersistentCollectionDescriptor;
import org.hibernate.metamodel.model.domain.spi.PluralPersistentAttribute;
import org.hibernate.sql.results.spi.DomainResult;
import org.hibernate.sql.results.spi.PluralAttributeMappingNode;
import org.hibernate.type.descriptor.java.spi.JavaTypeDescriptor;

/**
 * @author Steve Ebersole
 */
public abstract class AbstractPluralAttributeMappingNode implements PluralAttributeMappingNode {
	private final PluralPersistentAttribute navigable;
	private final String resultVariable;

	private final DomainResult keyResult;
	private final DomainResult identifierResult;
	private final DomainResult indexResult;
	private final DomainResult elementResult;

	protected AbstractPluralAttributeMappingNode(
			PluralPersistentAttribute navigable,
			String resultVariable,
			DomainResult keyResult,
			DomainResult identifierResult,
			DomainResult indexResult,
			DomainResult elementResult) {
		this.navigable = navigable;
		this.resultVariable = resultVariable;
		this.keyResult = keyResult;
		this.identifierResult = identifierResult;
		this.indexResult = indexResult;
		this.elementResult = elementResult;
	}

	public PluralPersistentAttribute getNavigable() {
		return navigable;
	}

	public String getResultVariable() {
		return resultVariable;
	}

	public JavaTypeDescriptor getJavaTypeDescriptor() {
		return getNavigable().getJavaTypeDescriptor();
	}

	@Override
	public PersistentCollectionDescriptor getCollectionDescriptor() {
		return getNavigable().getPersistentCollectionDescriptor();
	}

	@Override
	public DomainResult getKeyResult() {
		return keyResult;
	}

	@Override
	public DomainResult getIdentifierResult() {
		return identifierResult;
	}

	@Override
	public DomainResult getIndexResult() {
		return indexResult;
	}

	@Override
	public DomainResult getElementResult() {
		return elementResult;
	}
}
