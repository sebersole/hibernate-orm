/*
 * Hibernate, Relational Persistence for Idiomatic Java
 *
 * License: GNU Lesser General Public License (LGPL), version 2.1 or later
 * See the lgpl.txt file in the root directory or http://www.gnu.org/licenses/lgpl-2.1.html
 */
package org.hibernate.orm.test.crud.onetoone.bidirectional;

import javax.persistence.Entity;
import javax.persistence.ForeignKey;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.JoinTable;
import javax.persistence.OneToOne;

import org.hibernate.boot.MetadataSources;
import org.hibernate.orm.test.SessionFactoryBasedFunctionalTest;

import org.junit.jupiter.api.Test;

import org.hamcrest.CoreMatchers;

import static org.hamcrest.MatcherAssert.assertThat;

/**
 * @author Andrea Boriero
 */
public class EntityWithOneToOneJoinTableTest extends SessionFactoryBasedFunctionalTest {

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

	// TODO: make a schema of the 5.3 insert and update
	// TODO:  SingleTableEntityDescriptor refactor the insert statement
	// TODO:  SingleTableEntityDescriptor the update is completely hacked, check if update for secondary tables an update is needed and in case not a insert seem needed check all fileds are not null
	@Test
	public void testOneToOne() {
		sessionFactoryScope().inTransaction( session -> {
			Parent parent = new Parent( 1 , "Hibernate" );
			Child child = new Child( 2, parent );
			child.setName( "Acme" );
			session.save( parent );
			session.save( child );
		} );

		sessionFactoryScope().inTransaction(
				session -> {
					final String queryString = "SELECT c FROM Child c JOIN c.parent d WHERE d.id = :id";
					final Child child = session.createQuery( queryString, Child.class )
							.setParameter( "id", 1 )
							.getSingleResult();

					assertThat( child.getParent(), CoreMatchers.notNullValue() );

					String description = child.getParent().getDescription();
					assertThat( description, CoreMatchers.notNullValue() );
				}
		);


		sessionFactoryScope().inTransaction(
				session -> {
					final Parent parent = session.createQuery(
							"SELECT p FROM Parent p JOIN p.child WHERE p.id = :id",
							Parent.class
					)
							.setParameter( "id", 1 )
							.getSingleResult();

					assertThat( parent.getChild(), CoreMatchers.notNullValue() );
					String name = parent.getChild().getName();
					assertThat( name, CoreMatchers.notNullValue() );
				}

		);

		sessionFactoryScope().inTransaction( session -> {
			final Parent parent = session.get( Parent.class, 1 );
			assertThat( parent.getChild(), CoreMatchers.notNullValue() );
			assertThat( parent.getChild().getName(), CoreMatchers.notNullValue() );
		} );

		sessionFactoryScope().inTransaction( session -> {
			final Child child = session.get( Child.class, 2 );
			assertThat( child.getParent(), CoreMatchers.notNullValue() );
			assertThat( child.getParent().getDescription(), CoreMatchers.notNullValue() );
		} );
	}

	@Entity(name = "Parent")
	public static class Parent {
		private Integer id;

		private String description;
		private Child child;

		Parent() {
		}

		public Parent(Integer id, String description) {
			this.id = id;
			this.description = description;
		}

		@Id
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

		@OneToOne
		@JoinTable(name = "PARENT_CHILD", inverseJoinColumns = @JoinColumn(name = "child_id"), joinColumns = @JoinColumn(name = "parent_id"))
		public Child getChild() {
			return child;
		}

		public void setChild(Child other) {
			this.child = other;
		}
	}


	@Entity(name = "Child")
	public static class Child {
		private Integer id;

		private String name;
		private Parent parent;

		Child() {
		}

		Child(Integer id, Parent parent) {
			this.id = id;
			this.parent = parent;
			this.parent.setChild( this );
		}

		@Id
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

		@OneToOne(mappedBy = "child")
		public Parent getParent() {
			return parent;
		}

		public void setParent(Parent parent) {
			this.parent = parent;
		}
	}
}
