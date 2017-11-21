/*
 * Hibernate, Relational Persistence for Idiomatic Java
 *
 * License: GNU Lesser General Public License (LGPL), version 2.1 or later.
 * See the lgpl.txt file in the root directory or <http://www.gnu.org/licenses/lgpl-2.1.html>.
 */
package org.hibernate.jpa.test;

import javax.persistence.EntityManager;

import org.hibernate.testing.TestForIssue;
import org.junit.Test;

import static org.junit.Assert.fail;

/**
 * @author Gail Badner
 */
public class EntityManagerClosedTest extends BaseEntityManagerFunctionalTestCase {

	@Test
	@TestForIssue( jiraKey = "HHH-12110")
	public void testGetMetamodel() {
		EntityManager em = getOrCreateEntityManager();
		em.close();
		try {
			em.getMetamodel();
			fail( "should have thrown IllegalStateException" );
		}
		catch (IllegalStateException ex) {
			// expected
		}
	}

	@Test
	@TestForIssue( jiraKey = "HHH-12110")
	public void testGetCriteriaBuilder() {
		EntityManager em = getOrCreateEntityManager();
		em.close();
		try {
			em.getCriteriaBuilder();
			fail( "should have thrown IllegalStateException" );
		}
		catch (IllegalStateException ex) {
			// expected
		}
	}

	@Test
	@TestForIssue( jiraKey = "HHH-12110")
	public void testGetEntityManagerFactory() {
		EntityManager em = getOrCreateEntityManager();
		em.close();
		try {
			em.getEntityManagerFactory();
			fail( "should have thrown IllegalStateException" );
		}
		catch (IllegalStateException ex) {
			// expected
		}
	}

	@Test
	@TestForIssue( jiraKey = "HHH-12110")
	public void testCreateNamedQuery() {
		EntityManager em = getOrCreateEntityManager();
		em.close();
		try {
			em.createNamedQuery( "abc" );
			fail( "should have thrown IllegalStateException" );
		}
		catch (IllegalStateException ex) {
			// expected
		}
	}
}
