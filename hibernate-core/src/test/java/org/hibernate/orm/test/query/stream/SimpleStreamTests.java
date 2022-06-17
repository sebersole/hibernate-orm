/*
 * Hibernate, Relational Persistence for Idiomatic Java
 *
 * License: GNU Lesser General Public License (LGPL), version 2.1 or later.
 * See the lgpl.txt file in the root directory or http://www.gnu.org/licenses/lgpl-2.1.html.
 */
package org.hibernate.orm.test.query.stream;

import java.util.List;

import org.hibernate.engine.spi.SharedSessionContractImplementor;
import org.hibernate.orm.test.query.criteria.fluent.Person;
import org.hibernate.orm.test.query.criteria.fluent.Person_;
import org.hibernate.query.sqm.NodeBuilder;
import org.hibernate.query.sqm.tree.from.SqmRoot;
import org.hibernate.query.sqm.tree.select.SqmQuerySpec;
import org.hibernate.query.sqm.tree.select.SqmSelectStatement;

import org.hibernate.testing.orm.junit.DomainModel;
import org.hibernate.testing.orm.junit.SessionFactory;
import org.hibernate.testing.orm.junit.SessionFactoryScope;
import org.junit.jupiter.api.Test;

import jakarta.persistence.metamodel.SingularAttribute;

/**
 * @author Steve Ebersole
 */
@DomainModel( annotatedClasses = Person.class)
@SessionFactory
public class SimpleStreamTests {
//	@Test
//	public void filterTest(SessionFactoryScope scope) {
//		scope.inTransaction( (session) -> {
//			final List<Person> results = session.stream( Person.class )
//					.filter( (p) -> p.getName().equals( "Andrea" ) )
//					.collect( Collectors.toList() );
//		} );
//	}
//	@Test
//	public void filterTest2(SessionFactoryScope scope) {
//		scope.inTransaction( (session) -> {
//			final List<String> results = session.stream( Person.class )
//					.filter( (p) -> p.getId().equals( 1 ) )
////					.equal( Person_.name, "Andrea" )
//					.map( Person::getName )
//					.collect( Collectors.toList() );
//		} );
//	}

	private static class Fluent<T> {
		private final SharedSessionContractImplementor session;
		private final NodeBuilder nodeBuilder;

		private final SqmSelectStatement<?> sqmStmnt;
		private final SqmQuerySpec<?> sqm;
		private final SqmRoot<T> root;

		public Fluent(SharedSessionContractImplementor session, Class<T> entityType) {
			this.session = session;
			nodeBuilder = (NodeBuilder) session.getCriteriaBuilder();
			sqmStmnt = new SqmSelectStatement<>( Object.class, nodeBuilder );
			sqm = sqmStmnt.getQuerySpec();
			root = sqmStmnt.from( entityType );
		}

		public <X> Fluent<T> equal(SingularAttribute<? super T, X> fixture, X value) {
			sqm.applyPredicate( nodeBuilder.equal( root.get( fixture ), value ) );
			return this;
		}

		public <X> Fluent<T> equal(SingularAttribute<? super T, X> fixture, SingularAttribute<?, X> value) {
			sqm.applyPredicate( nodeBuilder.equal( root.get( fixture ), value ) );
			return this;
		}




		public List<?> list() {
			return session.createSelectionQuery( sqmStmnt ).list();
		}
	}

	@Test
	public void simpleTest(SessionFactoryScope scope) {
		scope.inTransaction( (session) -> {
			final List<?> andrea = new Fluent<>( session, Person.class )
					.equal( Person_.name, "Andrea" )
					.list();
		} );

//			// from Person p where p.name = "Andrea"
//			final List<Person> results = session.query( Person.class )
//					.equal( Person_.name, "Andrea" );
//
//			// from Person p join p.mate m where p.name = "Andrea" and m.name = "Fabiana"
//			final List<Person> results = session.query( Person.class )
//					.equal( Person_.name, "Andrea" )
//					.join( (p) -> p.join( Person_.mate ) )
//
//
//			// from Person p join fetch p.mate m where p.name = "Andrea" and m.name = "Fabiana"
//
//			final List<Person> results = session.query( Person.class )
//					.equal( Person_.name, "Andrea" )
//
//
//
//			final List<Person> results = session.query( Person.class )
//					.map(Person_.name)
//					.equal( Person_.name, "Andrea" )
//					.restrict( (p) -> {
//						session.getCriteriaBuilder().equal( p.get(Person_.mate ).get(Person_.name), "Fabiana")
//					})
//					.fetch(Person_.mate)
//					.with( Person_.mate, (p) ->  {
//						applyPredicate( equal( p.get(Person_.name), "Fabiana" ) )
//					} )
//					.join( Person_.mate )
//
//
//			final List<Person> results = session.query( Person.class )
//					.join( Person_.mate )
//					.restrict( (p) -> session.getCriteriaBuilder().equal(  )p.get(Person_.name))
//					.equal( Person_.name, "Andrea" )
//					.map( Person_.name )
//					.stream()
//					.collect( Collectors.toList() );
//		} );
	}

}
