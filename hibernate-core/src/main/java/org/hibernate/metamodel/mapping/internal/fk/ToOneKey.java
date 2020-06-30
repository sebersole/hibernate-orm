/*
 * Hibernate, Relational Persistence for Idiomatic Java
 *
 * License: GNU Lesser General Public License (LGPL), version 2.1 or later
 * See the lgpl.txt file in the root directory or http://www.gnu.org/licenses/lgpl-2.1.html
 */
package org.hibernate.metamodel.mapping.internal.fk;

import org.hibernate.LockMode;
import org.hibernate.engine.FetchTiming;
import org.hibernate.metamodel.mapping.ToOneAttributeMapping;
import org.hibernate.query.NavigablePath;
import org.hibernate.sql.results.graph.DomainResultCreationState;
import org.hibernate.sql.results.graph.FetchParent;
import org.hibernate.sql.results.graph.entity.EntityFetch;

/**
 * Defines a side of a foreign-key of a to-one as a ModelPart
 */
public interface ToOneKey extends KeyModelPart {
	String PART_NAME = "{key}";

	@Override
	default String getPartName() {
		return PART_NAME;
	}

	@Override
	default String getFetchableName() {
		return PART_NAME;
	}

	@Override
	ToOneAttributeMapping getMappedModelPart();

	EntityFetch generateEntityFetch(
			FetchParent fetchParent,
			NavigablePath fetchablePath,
			FetchTiming fetchTiming,
			boolean selected,
			LockMode lockMode,
			String resultVariable,
			DomainResultCreationState creationState);
}
