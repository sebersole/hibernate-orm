/*
 * Hibernate, Relational Persistence for Idiomatic Java
 *
 * License: GNU Lesser General Public License (LGPL), version 2.1 or later
 * See the lgpl.txt file in the root directory or http://www.gnu.org/licenses/lgpl-2.1.html
 */
package org.hibernate.orm.test.crud;

import java.util.List;

import org.hibernate.boot.MetadataSources;
import org.hibernate.orm.test.SessionFactoryBasedFunctionalTest;
import org.hibernate.orm.test.support.domains.gambit.EntityOfSets;

import org.hibernate.testing.junit5.FailureExpected;
import org.junit.jupiter.api.Test;

/**
 * @author Steve Ebersole
 */
@FailureExpected( "Lots still to implement for collection support" )
public class EntityOfSetsCrudTest extends SessionFactoryBasedFunctionalTest {
	@Override
	protected void applyMetadataSources(MetadataSources metadataSources) {
		super.applyMetadataSources( metadataSources );
		metadataSources.addAnnotatedClass( EntityOfSets.class );
	}

	@Override
	protected boolean exportSchema() {
		return true;
	}


	@Test
	public void testOperations() {
		sessionFactoryScope().inTransaction( session -> session.createQuery( "delete EntityOfSets" ).executeUpdate() );

		final EntityOfSets entity = new EntityOfSets( 1 );

		entity.getSetOfBasics().add( "first string" );
		entity.getSetOfBasics().add( "second string" );

//		entity.getSetOfComponents().add(
//				new Component(
//						5,
//						10L,
//						15,
//						"component string",
//						new Component.Nested(
//								"first nested string",
//								"second nested string"
//						)
//				)
//		);

		sessionFactoryScope().inTransaction( session -> session.save( entity ) );
		sessionFactoryScope().inTransaction(
				session -> {
					final Integer value = session.createQuery( "select e.id from EntityOfSets e", Integer.class ).uniqueResult();
					assert value == 1;
				}
		);
		sessionFactoryScope().inTransaction(
				session -> {
					final EntityOfSets loaded = session.get( EntityOfSets.class, 1 );
					assert loaded != null;
//					assert loaded.getSetOfBasics().size() == 2;
				}
		);
		sessionFactoryScope().inTransaction(
				session -> {
					final List<EntityOfSets> list = session.byMultipleIds( EntityOfSets.class )
							.multiLoad( 1, 2 );
					assert list.size() == 1;
					final EntityOfSets loaded = list.get( 0 );
					assert loaded != null;
//					assert loaded.getSetOfBasics().size() == 2;
				}
		);
	}
}
