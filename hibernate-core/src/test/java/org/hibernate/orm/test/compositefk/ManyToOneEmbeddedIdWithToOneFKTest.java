/*
 * Hibernate, Relational Persistence for Idiomatic Java
 *
 * License: GNU Lesser General Public License (LGPL), version 2.1 or later
 * See the lgpl.txt file in the root directory or http://www.gnu.org/licenses/lgpl-2.1.html
 */
package org.hibernate.orm.test.compositefk;

import org.hibernate.Hibernate;
import org.hibernate.orm.test.models.datacenter.DataCenter;
import org.hibernate.orm.test.models.datacenter.DataCenterUser;
import org.hibernate.orm.test.models.datacenter.DataCenterUserPk;
import org.hibernate.orm.test.models.datacenter.System;

import org.hibernate.testing.jdbc.SQLStatementInspector;
import org.hibernate.testing.orm.junit.DomainModel;
import org.hibernate.testing.orm.junit.FailureExpected;
import org.hibernate.testing.orm.junit.SessionFactory;
import org.hibernate.testing.orm.junit.SessionFactoryScope;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.core.Is.is;
import static org.hamcrest.core.IsNull.notNullValue;
import static org.junit.Assert.assertTrue;

/**
 * @author Andrea Boriero
 */
@DomainModel(
		annotatedClasses = {
				System.class,
				DataCenterUser.class,
				DataCenter.class
		}
)
@SessionFactory(statementInspectorClass = SQLStatementInspector.class)
public class ManyToOneEmbeddedIdWithToOneFKTest {

	@Test
	public void testGet(SessionFactoryScope scope) {
		SQLStatementInspector statementInspector = (SQLStatementInspector) scope.getStatementInspector();
		statementInspector.clear();
		scope.inTransaction(
				session -> {
					final System system = session.get( System.class, 1 );
					assertThat( system, is( notNullValue() ) );
					assertThat( system.getId() , is(1 ) );

					assertThat( system.getDataCenterUser(), notNullValue() );
					assertThat( system.getDataCenterUser().getPk(), notNullValue() );
					assertTrue( Hibernate.isInitialized( system.getDataCenterUser() ) );

					final DataCenterUserPk pk = system.getDataCenterUser().getPk();
					assertTrue( Hibernate.isInitialized( pk.getDataCenter() ) );

					assertThat( pk.getUsername(), is( "Fab" ) );
					assertThat( pk.getDataCenter().getId(), is( 2 ) );
					assertThat( pk.getDataCenter().getDescription(), is( "Raleigh" ) );

					final DataCenterUser user = system.getDataCenterUser();
					assertThat( user, notNullValue() );

					statementInspector.assertExecutedCount( 1 );
					statementInspector.assertNumberOfOccurrenceInQuery( 0, "join", 2 );
				}
		);
	}

	@Test
	public void testHql(SessionFactoryScope scope) {
		SQLStatementInspector statementInspector = (SQLStatementInspector) scope.getStatementInspector();
		statementInspector.clear();
		/*
		select
			s1_0.id,
			s1_0.dataCenterUser_dataCenter_id,
			s1_0.dataCenterUser_username,
			s1_0.name
		from
			System s1_0
		where
			s1_0.id=?

		select
			d1_0.id,
		 	d1_0.description
		from
        	data_center as d1_0
    	where
        	d1_0.id = ?

		select
			d1_0.dataCenter_id,
			d1_0.username,
			d1_0.privilegeMask
		from
			data_center_user as d1_0
		where
			(
				d1_0.dataCenter_id, d1_0.username
			) in (
				(
					?, ?
				)
			)

			NOTE: currently the 3rd query is:

        select
            d2_0.id,
            d2_0.description,
            d1_0.dataCenter_id,
            d1_0.username,
            d1_0.privilegeMask
        from
            data_center_user as d1_0
        inner join
            data_center as d2_0
                on d1_0.dataCenter_id = d2_0.id
        where
            (
                d1_0.dataCenter_id, d1_0.username
            ) in (
                (
                    ?, ?
                )
            )
		 */
		scope.inTransaction(
				session -> {
					// this HQL should load the System with id = 1

					System system = (System) session.createQuery( "from System e where e.id = :id" )
							.setParameter( "id", 1 ).uniqueResult();

					assertThat( system, is( notNullValue() ) );

					statementInspector.assertExecutedCount( 3 );
					statementInspector.assertNumberOfOccurrenceInQuery( 0, "join", 0 );
					statementInspector.assertNumberOfOccurrenceInQuery( 1, "join", 0 );
					statementInspector.assertNumberOfOccurrenceInQuery( 2, "join", 0 );


					assertTrue( Hibernate.isInitialized( system.getDataCenterUser() ) );

					final DataCenterUserPk pk = system.getDataCenterUser().getPk();
					assertTrue( Hibernate.isInitialized( pk.getDataCenter() ) );

					assertThat( pk.getUsername(), is( "Fab" ) );
					assertThat( pk.getDataCenter().getId(), is( 2 ) );
					assertThat( pk.getDataCenter().getDescription(), is( "Raleigh" ) );

					DataCenterUser user = system.getDataCenterUser();
					assertThat( user, is( notNullValue() ) );
					statementInspector.assertExecutedCount( 3 );
				}
		);
	}

	@Test
	public void testHqlJoin(SessionFactoryScope scope) {
		SQLStatementInspector statementInspector = (SQLStatementInspector) scope.getStatementInspector();
		statementInspector.clear();
		scope.inTransaction(
				session -> {
					System system = session.createQuery( "from System e join e.dataCenterUser where e.id = :id", System.class )
							.setParameter( "id", 1 ).uniqueResult();
					statementInspector.assertExecutedCount( 3 );
					statementInspector.assertNumberOfOccurrenceInQuery( 0, "join", 1 );
					statementInspector.assertNumberOfOccurrenceInQuery( 1, "join", 0 );
					statementInspector.assertNumberOfOccurrenceInQuery( 2, "join", 0 );
					assertThat( system, is( notNullValue() ) );
					DataCenterUser user = system.getDataCenterUser();
					assertThat( user, is( notNullValue() ) );
				}
		);
	}

	@Test
	public void testHqlJoinFetch(SessionFactoryScope scope) {
		SQLStatementInspector statementInspector = (SQLStatementInspector) scope.getStatementInspector();
		statementInspector.clear();
		scope.inTransaction(
				session -> {
					System system = session.createQuery(
							"from System e join fetch e.dataCenterUser where e.id = :id",
							System.class
					)
							.setParameter( "id", 1 ).uniqueResult();
					statementInspector.assertExecutedCount( 2 );
					statementInspector.assertNumberOfOccurrenceInQuery( 0, "join", 1 );
					statementInspector.assertNumberOfOccurrenceInQuery( 1, "join", 0 );
					assertThat( system, is( notNullValue() ) );
					DataCenterUser user = system.getDataCenterUser();
					assertThat( user, is( notNullValue() ) );
				}
		);
	}

	@Test
	@FailureExpected(reason = "Embedded parameters has not yet been implemented ")
	public void testEmbeddedIdParameter(SessionFactoryScope scope) {
		scope.inTransaction(
				session -> {
					DataCenter dataCenter = new DataCenter( 2, "sub1" );

					DataCenterUserPk superUserKey = new DataCenterUserPk( dataCenter, "Fab" );

					System system = session.createQuery(
							"from System e join fetch e.user u where u.id = :id",
							System.class
					).setParameter( "id", superUserKey ).uniqueResult();

					assertThat( system, is( notNullValue() ) );
				}
		);
	}

	@Test
	public void testHql2(SessionFactoryScope scope) {
		SQLStatementInspector statementInspector = (SQLStatementInspector) scope.getStatementInspector();
		statementInspector.clear();
		/*
		  select
			s1_0.subsystem_id,
			s1_0.username,
			s1_0.name
		from
			SystemUser as s1_0

        select
			s1_0.id,
			s1_0.description
		from
			Subsystem s1_0
		where
			s1_0.id=?
		 */
		scope.inTransaction(
				session -> {
					DataCenterUser system = (DataCenterUser) session.createQuery( "from DataCenterUser " )
							.uniqueResult();
					assertThat( system, is( notNullValue() ) );

					statementInspector.assertExecutedCount( 2 );
					statementInspector.assertNumberOfOccurrenceInQuery( 0, "join", 0 );
					statementInspector.assertNumberOfOccurrenceInQuery( 1, "join", 0 );

					assertTrue( Hibernate.isInitialized( system.getPk().getDataCenter() ) );

				}
		);
	}


	@BeforeEach
	public void setUp(SessionFactoryScope scope) {
		scope.inTransaction(
				session -> {
					DataCenter dataCenter = new DataCenter( 2, "Raleigh" );
					DataCenterUserPk userKey = new DataCenterUserPk( dataCenter, "Fab" );
					DataCenterUser user = new DataCenterUser( userKey, (byte) 1 );

					System system = new System( 1, "QA" );
					system.setDataCenterUser( user );

					session.save( dataCenter );
					session.save( user );
					session.save( system );
				}
		);
	}

	@AfterEach
	public void tearDown(SessionFactoryScope scope) {
		scope.inTransaction(
				session -> {
					session.createQuery( "delete from System" ).executeUpdate();
					session.createQuery( "delete from DataCenterUser" ).executeUpdate();
					session.createQuery( "delete from DataCenter" ).executeUpdate();
				}
		);
	}

	// data_center( id, ... )
	// data_center_user( username, dataCenter_id, ... )
	// systems( id, dataCenterUser_dataCenter_id, dataCenterUser_username, .. )

	// data_center_user.dataCenter_id -> data_center.id
	// systems.(dataCenterUser_dataCenter_id, dataCenterUser_username) -> data_center_user.(dataCenter_id, username)

}
