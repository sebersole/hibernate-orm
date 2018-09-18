/*
 * Hibernate, Relational Persistence for Idiomatic Java
 *
 * License: GNU Lesser General Public License (LGPL), version 2.1 or later
 * See the lgpl.txt file in the root directory or http://www.gnu.org/licenses/lgpl-2.1.html
 */
package org.hibernate.orm.test.crud;

import java.sql.PreparedStatement;
import java.util.Collection;
import java.util.List;

import org.hibernate.Hibernate;
import org.hibernate.boot.MetadataSources;
import org.hibernate.orm.test.SessionFactoryBasedFunctionalTest;
import org.hibernate.orm.test.support.domains.gambit.Component;
import org.hibernate.orm.test.support.domains.gambit.EntityOfSets;

import org.hibernate.testing.junit5.FailureExpected;
import org.junit.Assert;
import org.junit.jupiter.api.Test;

/**
 * @author Steve Ebersole
 */
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
	@FailureExpected( "EXTRA_LAZY handling not yet implemented" )
	public void testOperations() {
		sessionFactoryScope().inTransaction( session -> session.createQuery( "delete EntityOfSets" ).executeUpdate() );

		final EntityOfSets entity = new EntityOfSets( 1 );

		entity.getSetOfBasics().add( "first string" );
		entity.getSetOfBasics().add( "second string" );

		entity.getSetOfComponents().add(
				new Component(
						5,
						10L,
						15,
						"component string",
						new Component.Nested(
								"first nested string",
								"second nested string"
						)
				)
		);

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
					checkExpectedSize( loaded.getSetOfBasics(), 2 );
				}
		);
		sessionFactoryScope().inTransaction(
				session -> {
					final List<EntityOfSets> list = session.byMultipleIds( EntityOfSets.class )
							.multiLoad( 1, 2 );
					assert list.size() == 1;
					final EntityOfSets loaded = list.get( 0 );
					assert loaded != null;
					checkExpectedSize( loaded.getSetOfBasics(), 2 );
				}
		);
	}

	private void checkExpectedSize(Collection collection, int expectedSize) {
		// todo (6.0) : loading collections not yet implemented
		//		skip for now
		Hibernate.initialize( collection );
		if ( collection.size() != expectedSize ) {
			Assert.fail(
					"Expecting Collection of size `" + expectedSize +
							"`, but passed Collection has `" + collection.size() + "` entries"
			);
		}

	}


	@Test
	@FailureExpected( "A problem in missing rows from the result set" )
	public void testEagerOperations() {
		sessionFactoryScope().inTransaction( session -> session.createQuery( "delete EntityOfSets" ).executeUpdate() );

		final EntityOfSets entity = new EntityOfSets( 1 );


		// ~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~
		// Cascading is not yet implemented, so for now manually create the
		// collection rows
		sessionFactoryScope().inTransaction( session -> session.save( entity ) );
		sessionFactoryScope().inSession(
				session -> session.doWork(
						connection -> {
							final PreparedStatement statement = connection.prepareStatement(
									"insert into EntityOfSets_setOfBasics (EntityOfSets_id, setOfBasics) values (?,?)"
							);
							statement.setInt( 1, 1 );
							statement.setString( 2, "first string" );
							statement.addBatch();
							statement.setInt( 1, 1 );
							statement.setString( 2, "second string" );
							statement.addBatch();
							statement.executeBatch();
						}
				)
		);
//		entity.getSetOfBasics().add( "first string" );
//		entity.getSetOfBasics().add( "second string" );
//
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
//
//		sessionFactoryScope().inTransaction( session -> session.save( entity ) );
		// ~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~

		sessionFactoryScope().inTransaction(
				session -> {
					final Integer value = session.createQuery( "select e.id from EntityOfSets e", Integer.class ).uniqueResult();
					assert value == 1;
				}
		);

		// the generated queries are fine, however:
		// 		- for the inner join case, the collections are never created and so no result is returned
		//		- for the outer join case, run into a problem with not all of PersistentCollectionDescriptor
		//			used by PersistentCollection are done (specifically reading size)

		sessionFactoryScope().inTransaction(
				session -> {
					final EntityOfSets loaded = session.createQuery( "select e from EntityOfSets e left join fetch e.setOfBasics", EntityOfSets.class ).uniqueResult();
					assert loaded != null;
					checkExpectedSize( loaded.getSetOfBasics(), 2 );
				}
		);

		sessionFactoryScope().inTransaction(
				session -> {
					final EntityOfSets loaded = session.createQuery( "select e from EntityOfSets e inner join fetch e.setOfBasics", EntityOfSets.class ).uniqueResult();
					assert loaded != null;
					checkExpectedSize( loaded.getSetOfBasics(), 2 );
				}
		);

//		sessionFactoryScope().inTransaction(
//				session -> {
//					final EntityOfSets loaded = session.get( EntityOfSets.class, 1 );
//					assert loaded != null;
//					checkExpectedSize( loaded.getSetOfBasics(), 2 );
//				}
//		);
//		sessionFactoryScope().inTransaction(
//				session -> {
//					final List<EntityOfSets> list = session.byMultipleIds( EntityOfSets.class )
//							.multiLoad( 1, 2 );
//					assert list.size() == 1;
//					final EntityOfSets loaded = list.get( 0 );
//					assert loaded != null;
//					checkExpectedSize( loaded.getSetOfBasics(), 2 );
//				}
//		);
	}
}
