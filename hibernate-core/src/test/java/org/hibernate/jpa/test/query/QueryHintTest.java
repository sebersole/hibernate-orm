/*
 * Hibernate, Relational Persistence for Idiomatic Java
 *
 * License: GNU Lesser General Public License (LGPL), version 2.1 or later.
 * See the lgpl.txt file in the root directory or <http://www.gnu.org/licenses/lgpl-2.1.html>.
 */
package org.hibernate.jpa.test.query;

import org.hibernate.LockMode;
import org.hibernate.jpa.QueryHints;
import org.hibernate.query.Query;

import org.hibernate.testing.junit4.BaseNonConfigCoreFunctionalTestCase;
import org.junit.Test;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * @author Steve Ebersole
 */
public class QueryHintTest extends BaseNonConfigCoreFunctionalTestCase {

	@Override
	protected Class<?>[] getAnnotatedClasses() {
		return new Class<?>[] { Employee.class, Contractor.class };
	}

	@Test
	public void testNativeQueryLockModeHint() {
		inTransaction( (session) -> {
			final Query query = session.createNativeQuery( "select * from Employee" )
					.setHint( QueryHints.HINT_NATIVE_LOCKMODE, "none" );
			assertThat( query.getHints().get( QueryHints.HINT_NATIVE_LOCKMODE ) ).isEqualTo( LockMode.NONE );
			query.list();
		} );
	}
}
