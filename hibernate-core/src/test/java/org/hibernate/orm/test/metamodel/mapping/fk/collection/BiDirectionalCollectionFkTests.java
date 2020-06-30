/*
 * Hibernate, Relational Persistence for Idiomatic Java
 *
 * License: GNU Lesser General Public License (LGPL), version 2.1 or later
 * See the lgpl.txt file in the root directory or http://www.gnu.org/licenses/lgpl-2.1.html
 */
package org.hibernate.orm.test.metamodel.mapping.fk.collection;

import org.hibernate.NotYetImplementedFor6Exception;

import org.hibernate.testing.orm.junit.FailureExpected;
import org.hibernate.testing.orm.junit.SessionFactory;
import org.junit.jupiter.api.Test;

/**
 * @author Steve Ebersole
 */
@SessionFactory
public class BiDirectionalCollectionFkTests {
	@Test
	@FailureExpected( reason = "Need domain model that includes a collection mapped with an aggregated composite key")
	void testAggregatedCompositeKey() {
		throw new NotYetImplementedFor6Exception( getClass() );
	}

	@Test
	void name() {

	}
}
