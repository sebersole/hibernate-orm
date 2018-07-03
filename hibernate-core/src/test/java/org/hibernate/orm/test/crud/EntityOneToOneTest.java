/*
 * Hibernate, Relational Persistence for Idiomatic Java
 *
 * License: GNU Lesser General Public License (LGPL), version 2.1 or later.
 * See the lgpl.txt file in the root directory or <http://www.gnu.org/licenses/lgpl-2.1.html>.
 */
package org.hibernate.orm.test.crud;

import java.util.Objects;

import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.OneToOne;

import org.hamcrest.CoreMatchers;
import org.hibernate.boot.MetadataSources;
import org.hibernate.orm.test.SessionFactoryBasedFunctionalTest;
import org.junit.jupiter.api.Test;

import static org.hamcrest.MatcherAssert.assertThat;

/**
 * @author Chris Cranford
 */
public class EntityOneToOneTest extends SessionFactoryBasedFunctionalTest {

	@Entity(name = "Parent")
	public static class Parent {
		@Id
		private Integer id;
		private String description;
		@OneToOne(mappedBy = "parent")
		private Child child;

		Parent() {

		}

		Parent(Integer id) {
			this.id = id;
		}

		public Integer getId() {
			return id;
		}

		public void setId(Integer id) {
			this.id = id;
		}

		public String getDescription() {
			return description;
		}

		public void setDescription(String description) {
			this.description = description;
		}

		public Child getChild() {
			return child;
		}

		public void setChild(Child child) {
			this.child = child;
		}

		@Override
		public boolean equals(Object o) {
			if ( this == o ) {
				return true;
			}
			if ( o == null || getClass() != o.getClass() ) {
				return false;
			}
			Parent parent = (Parent) o;
			return Objects.equals( id, parent.id ) &&
					Objects.equals( description, parent.description );
		}

		@Override
		public int hashCode() {
			return Objects.hash( id, description );
		}
	}

	@Entity(name = "Child")
	public static class Child {
		@Id
		private Integer id;
		private String name;
		@OneToOne
		private Parent parent;

		Child() {

		}

		Child(Integer id, Parent parent) {
			this.id = id;
			this.parent = parent;
			this.parent.setChild( this );
		}

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

		public Parent getParent() {
			return parent;
		}

		public void setParent(Parent parent) {
			this.parent = parent;
		}

		@Override
		public boolean equals(Object o) {
			if ( this == o ) {
				return true;
			}
			if ( o == null || getClass() != o.getClass() ) {
				return false;
			}
			Child child = (Child) o;
			return Objects.equals( id, child.id ) &&
					Objects.equals( name, child.name ) &&
					Objects.equals( parent, child.parent );
		}

		@Override
		public int hashCode() {
			return Objects.hash( id, name, parent );
		}
	}

	@Override
	protected void applyMetadataSources(MetadataSources metadataSources) {
		super.applyMetadataSources( metadataSources );

		metadataSources.addAnnotatedClass( Parent.class );
		metadataSources.addAnnotatedClass( Child.class );
	}

	@Override
	protected boolean exportSchema() {
		return true;
	}

	@Test
	public void testOneToOne() {
		sessionFactoryScope().inTransaction( session -> {
			Parent parent = new Parent( 1 );
			Child child = new Child( 1, parent );
			child.setName( "Acme" );
			session.save( parent );
			session.save( child );
		} );

		sessionFactoryScope().inTransaction(
				session -> {
					final Parent parent = session.createQuery(
							"SELECT p FROM Parent p JOIN p.child WHERE p.id = :id",
							Parent.class
					)
					.setParameter( "id", 1 )
					.getSingleResult();

					assertThat( parent.getChild(), CoreMatchers.notNullValue() );
					assertThat( parent.getChild().getName(), CoreMatchers.notNullValue() );
				}
		);

		sessionFactoryScope().inTransaction( session -> {
			final Parent parent = session.get( Parent.class, 1 );
			assertThat( parent.getChild(), CoreMatchers.notNullValue() );
			assertThat( parent.getChild().getName(), CoreMatchers.notNullValue() );
		} );
	}
}
