/*
 * Hibernate, Relational Persistence for Idiomatic Java
 *
 * License: GNU Lesser General Public License (LGPL), version 2.1 or later
 * See the lgpl.txt file in the root directory or http://www.gnu.org/licenses/lgpl-2.1.html
 */
package org.jboss.perf;

import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.JoinTable;
import javax.persistence.OneToOne;
import javax.persistence.Table;

import org.openjdk.jmh.annotations.Benchmark;
import org.openjdk.jmh.annotations.Scope;
import org.openjdk.jmh.annotations.State;

/**
 * @author Andrea Boriero
 */
public class EntityWithBidirectionalOneToOneJoinTableTest {

	@Benchmark
	public Parent getParent(LocalBenchmarkState state) {
		return state.inTransaction( em -> {
			return em.find( Parent.class, 1 );
		} );
	}

	@State(Scope.Benchmark)
	public static class LocalBenchmarkState extends BaseBenchmarkState {

		@Override
		protected void populateDatabase() {
			inTransaction( entityManager -> {
				Parent parent = new Parent( 1, "Hibernate" );
				Child child = new Child( 2, "Acme", parent );
				Child2 child2 = new Child2( 3, "Fab", parent );
				entityManager.persist( parent );
				entityManager.persist( child );
				entityManager.persist( child2 );
			} );
		}

		@Override
		protected void cleanUpDatabase() {
			inTransaction( entityManager -> {
				entityManager.createQuery( "delete from Parent" ).executeUpdate();
				entityManager.createQuery( "delete from Child" ).executeUpdate();
				entityManager.createQuery( "delete from Child2" ).executeUpdate();
			} );
		}

		@Override
		protected boolean createSchema() {
			return true;
		}

		@Override
		protected Class[] getAnnotatedClasses() {
			return new Class[] { Parent.class, Child.class, Child2.class };
		}

		protected String[] getHbmFiles() {
			return new String[0];
		}
	}

	@Entity(name = "Parent")
	@Table(name = "PARENT")
	public static class Parent {
		@Id
		private Integer id;

		private String description;
		@OneToOne
		@JoinTable(name = "PARENT_CHILD", inverseJoinColumns = @JoinColumn(name = "child_id"), joinColumns = @JoinColumn(name = "parent_id"))
		private Child child;
		@OneToOne
		@JoinTable(name = "PARENT_CHILD2", inverseJoinColumns = @JoinColumn(name = "child_id"), joinColumns = @JoinColumn(name = "parent_id"))
		private Child2 child2;

		Parent() {
		}

		public Parent(Integer id, String description) {
			this.id = id;
			this.description = description;
		}

		public void setChild(Child other) {
			this.child = other;
		}

		public void setChild2(Child2 child2) {
			this.child2 = child2;
		}
	}

	@Entity(name = "Child")
	@Table(name = "CHILD")
	public static class Child {
		@Id
		private Integer id;

		private String name;
		@OneToOne(mappedBy = "child")
		private Parent parent;

		Child() {
		}

		Child(Integer id, String name, Parent parent) {
			this.id = id;
			this.name = name;
			this.parent = parent;
			this.parent.setChild( this );
		}
	}

	@Entity(name = "Child2")
	@Table(name = "CHILD2")
	public static class Child2 {
		@Id
		private Integer id;

		private String name;
		@OneToOne(mappedBy = "child2")
		private Parent parent;

		Child2() {
		}

		Child2(Integer id, String name, Parent child) {
			this.id = id;
			this.name = name;
			this.parent = child;
			this.parent.setChild2( this );
		}
	}
}
