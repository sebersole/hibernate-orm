/*
 * Hibernate, Relational Persistence for Idiomatic Java
 *
 * License: GNU Lesser General Public License (LGPL), version 2.1 or later
 * See the lgpl.txt file in the root directory or http://www.gnu.org/licenses/lgpl-2.1.html
 */
package org.hibernate.metamodel.model.domain.spi;

import javax.persistence.metamodel.Type;

import org.hibernate.NotYetImplementedFor6Exception;
import org.hibernate.engine.spi.EntityUniqueKey;
import org.hibernate.engine.spi.SharedSessionContractImplementor;
import org.hibernate.sql.ast.produce.metamodel.spi.EntityValuedExpressableType;
import org.hibernate.sql.ast.produce.spi.ColumnReferenceQualifier;
import org.hibernate.sql.ast.produce.spi.SqlAstCreationContext;
import org.hibernate.sql.ast.produce.spi.TableReferenceContributor;
import org.hibernate.sql.ast.tree.spi.expression.domain.NavigableReference;
import org.hibernate.sql.results.spi.DomainResult;
import org.hibernate.sql.results.spi.DomainResultCreationContext;
import org.hibernate.sql.results.spi.DomainResultCreationState;
import org.hibernate.sql.results.spi.SqlSelectionGroupNode;
import org.hibernate.type.descriptor.java.spi.EntityJavaDescriptor;

/**
 * Specialization of Navigable(Container) for any entity-valued Navigable
 *
 * @author Steve Ebersole
 */
public interface EntityValuedNavigable<J>
		extends EntityValuedExpressableType<J>, NavigableContainer<J>, TableReferenceContributor {
	@Override
	default Type.PersistenceType getPersistenceType() {
		return Type.PersistenceType.ENTITY;
	}

	EntityJavaDescriptor<J> getJavaTypeDescriptor();

	@Override
	default DomainResult createDomainResult(
			NavigableReference navigableReference,
			String resultVariable,
			DomainResultCreationContext creationContext,
			DomainResultCreationState creationState) {
		return getEntityDescriptor().createDomainResult(
				navigableReference,
				resultVariable,
				creationContext,
				creationState
		);
	}

	boolean isNullable();

	@Override
	default SqlSelectionGroupNode resolveSqlSelections(
			ColumnReferenceQualifier qualifier,
			SqlAstCreationContext resolutionContext) {
		throw new NotYetImplementedFor6Exception(  );
	}

	default EntityUniqueKey createEntityUniqueKey(SharedSessionContractImplementor session) {
		throw new UnsupportedOperationException( getClass().getName() + "#createEntityUniqueKey" );
	}
}
