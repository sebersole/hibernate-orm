/*
 * Hibernate, Relational Persistence for Idiomatic Java
 *
 * License: GNU Lesser General Public License (LGPL), version 2.1 or later
 * See the lgpl.txt file in the root directory or http://www.gnu.org/licenses/lgpl-2.1.html
 */
package org.hibernate.metamodel.model.domain.spi;

import java.util.function.Predicate;

import org.hibernate.query.sqm.produce.spi.SqmCreationContext;
import org.hibernate.query.sqm.tree.expression.domain.SqmEntityIdentifierReferenceComposite;
import org.hibernate.query.sqm.tree.expression.domain.SqmEntityTypedReference;
import org.hibernate.query.sqm.tree.expression.domain.SqmNavigableContainerReference;
import org.hibernate.query.sqm.tree.expression.domain.SqmNavigableReference;
import org.hibernate.query.sqm.tree.from.SqmFrom;
import org.hibernate.sql.ast.produce.metamodel.spi.EmbeddedValueExpressableType;
import org.hibernate.type.descriptor.spi.ValueBinder;
import org.hibernate.type.descriptor.spi.ValueExtractor;
import org.hibernate.type.spi.TypeConfiguration;

/**
 * @author Steve Ebersole
 */
public interface EntityIdentifierComposite<O,J>
		extends EntityIdentifier<O,J>, EmbeddedValuedNavigable<J>, EmbeddedValueExpressableType<J> {
	@Override
	default boolean matchesNavigableName(String navigableName) {
		return getNavigableName().equals( navigableName )
				|| LEGACY_NAVIGABLE_ID.equals( navigableName )
				|| NAVIGABLE_ID.equals( navigableName );
	}

	@Override
	default SqmNavigableReference createSqmExpression(
			SqmFrom sourceSqmFrom,
			SqmNavigableContainerReference containerReference,
			SqmCreationContext creationContext) {
		return new SqmEntityIdentifierReferenceComposite(
				(SqmEntityTypedReference) containerReference,
				this
		);
	}

	@Override
	default int getNumberOfJdbcParametersNeeded() {
		return getColumns().size();
	}

	@Override
	default ValueBinder getValueBinder(
			Predicate<StateArrayContributor> inclusionChecker,
			TypeConfiguration typeConfiguration) {
		return getEmbeddedDescriptor().getValueBinder( inclusionChecker, typeConfiguration );
	}

	@Override
	default ValueExtractor getValueExtractor(TypeConfiguration typeConfiguration) {
		return getEmbeddedDescriptor().getValueExtractor( typeConfiguration );
	}
}
