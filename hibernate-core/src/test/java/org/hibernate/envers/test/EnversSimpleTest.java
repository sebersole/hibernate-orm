/*
 * Hibernate, Relational Persistence for Idiomatic Java
 *
 * License: GNU Lesser General Public License (LGPL), version 2.1 or later.
 * See the lgpl.txt file in the root directory or <http://www.gnu.org/licenses/lgpl-2.1.html>.
 */
package org.hibernate.envers.test;

import javax.persistence.Entity;
import javax.persistence.Id;

import org.hibernate.envers.Audited;

import org.hibernate.testing.junit5.envers.EnversTest;

import static org.hibernate.testing.transaction.TransactionUtil.doInHibernate;

/**
 * @author Chris Cranford
 */
public class EnversSimpleTest extends EnversSessionFactoryBasedFunctionalTest {

	@Override
	protected Class<?>[] getAnnotatedClasses() {
		return new Class<?>[] { Simple.class };
	}

	@Entity(name = "Simple")
	@Audited
	public static class Simple {
		@Id
		private Integer id;
		private String name;

		public Integer getId() {
			return id;
		}

		public void setId(Integer id) {
			this.id = id;
		}

		public String getName() {
			return name;
		}

		public void setName(String name) {
			this.name = name;
		}
	}

	@EnversTest
	public void initData() throws Exception {
		Integer id = doInHibernate( this::sessionFactory, session -> {
			final Simple simple = new Simple();
			simple.setId( 1 );
			simple.setName( "simple" );
			session.save( simple );
			return simple.getId();
		} );
//
//		doInHibernate( this::sessionFactory, session -> {
//			final Simple simple = session.find( Simple.class, id );
//			simple.setName( "simple2" );
//			session.update( simple );
//		} );
	}
}
