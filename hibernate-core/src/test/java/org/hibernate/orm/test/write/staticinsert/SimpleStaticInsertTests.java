/*
 * Hibernate, Relational Persistence for Idiomatic Java
 *
 * License: GNU Lesser General Public License (LGPL), version 2.1 or later.
 * See the lgpl.txt file in the root directory or http://www.gnu.org/licenses/lgpl-2.1.html.
 */
package org.hibernate.orm.test.write.staticinsert;

import org.hibernate.testing.orm.domain.StandardDomainModel;
import org.hibernate.testing.orm.domain.retail.CardPayment;
import org.hibernate.testing.orm.domain.retail.DomesticVendor;
import org.hibernate.testing.orm.domain.retail.ForeignVendor;
import org.hibernate.testing.orm.junit.DomainModel;
import org.hibernate.testing.orm.junit.SessionFactory;
import org.hibernate.testing.orm.junit.SessionFactoryScope;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;

/**
 * Simple tests for new "write path" approach in cases of static (non-dynamic) inserts
 *
 * @author Steve Ebersole
 * @author Andrea Boriero
 */
@DomainModel( standardModels = StandardDomainModel.RETAIL )
@SessionFactory
public class SimpleStaticInsertTests {
	@Test
	public void simpleSingleTableWithSecondaryTableTest(SessionFactoryScope scope) {
		scope.inTransaction( (session) -> {
			session.persist( new DomesticVendor( 1, "Acme Anvil Inc.", "Acme Worldwide") );
			session.persist( new ForeignVendor( 2, "Acme Train Inc.", "Acme Worldwide") );
		} );
	}

	@Test
	public void simpleJoinedTest(SessionFactoryScope scope) {
		scope.inTransaction( (session) -> {
			session.persist( new CardPayment( 1, 123456, 1L, "USD" ) );
		} );
	}

	@AfterEach
	public void dropTestData(SessionFactoryScope scope) {
		scope.inTransaction( (session) -> {
			session.createMutationQuery( "delete Vendor" ).executeUpdate();
		} );
	}
}
