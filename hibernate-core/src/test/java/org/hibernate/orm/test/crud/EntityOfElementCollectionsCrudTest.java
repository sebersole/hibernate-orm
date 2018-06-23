/*
 * Hibernate, Relational Persistence for Idiomatic Java
 *
 * License: GNU Lesser General Public License (LGPL), version 2.1 or later
 * See the lgpl.txt file in the root directory or http://www.gnu.org/licenses/lgpl-2.1.html
 */
package org.hibernate.orm.test.crud;

import org.hibernate.boot.MetadataSources;
import org.hibernate.orm.test.SessionFactoryBasedFunctionalTest;
import org.hibernate.orm.test.support.domains.gambit.Component;
import org.hibernate.orm.test.support.domains.gambit.EntityOfElementCollections;

import org.junit.jupiter.api.Test;

/**
 * @author Steve Ebersole
 */
public class EntityOfElementCollectionsCrudTest extends SessionFactoryBasedFunctionalTest {
	@Override
	protected boolean exportSchema() {
		return true;
	}

	@Override
	protected void applyMetadataSources(MetadataSources metadataSources) {
		super.applyMetadataSources( metadataSources );

		metadataSources.addAnnotatedClass( EntityOfElementCollections.class );
	}

	@Test
	public void testIt() {
		final EntityOfElementCollections entity = new EntityOfElementCollections( 1, "it" );
		entity.getStringSet().add( "another" );

		entity.getComponentSet().add(
				new Component(
					"name",
					5,
					10L,
					15,
					new Component.Nested(
							"nested-1",
							"nested-2"
					)
				)
		);

		sessionFactoryScope().inTransaction( session -> session.save( entity ) );
	}
}
