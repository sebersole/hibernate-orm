package org.hibernate.orm.test.locking;

import jakarta.persistence.Entity;
import jakarta.persistence.EntityManager;
import jakarta.persistence.ExcludedFromVersioning;
import jakarta.persistence.Id;
import jakarta.persistence.OptimisticLockException;
import jakarta.persistence.Version;

import org.hibernate.StaleObjectStateException;
import org.hibernate.dialect.H2Dialect;
import org.hibernate.engine.spi.SessionFactoryImplementor;
import org.hibernate.id.insert.InsertReturningDelegate;

import org.hibernate.testing.orm.junit.EntityManagerFactoryScope;
import org.hibernate.testing.orm.junit.Jpa;
import org.hibernate.testing.orm.junit.RequiresDialect;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertInstanceOf;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;

/// A version predicate matching no rows must report an optimistic failure even when
/// excluded attributes cause the update to return generated values.
///
/// @author Steve Ebersole
@Jpa(annotatedClasses = ExcludedAttributeStaleUpdateTests.Record.class)
@RequiresDialect(H2Dialect.class)
class ExcludedAttributeStaleUpdateTests {
	@BeforeEach
	void setup(EntityManagerFactoryScope scope) {
		var factory = scope.getEntityManagerFactory().unwrap( SessionFactoryImplementor.class );
		assertInstanceOf( InsertReturningDelegate.class,
				factory.getMappingMetamodel().getEntityDescriptor( Record.class ).getUpdateDelegate() );
		scope.inTransaction( entityManager -> entityManager.persist( new Record( 1L, "Original" ) ) );
	}

	@AfterEach
	void cleanup(EntityManagerFactoryScope scope) {
		scope.dropData();
	}

	@ParameterizedTest
	@CsvSource({ "false,false", "false,true", "true,false", "true,true" })
	void staleUpdateReportsOptimisticFailure(boolean deleted, boolean excludedOnly, EntityManagerFactoryScope scope) {
		try (EntityManager entityManager = scope.getEntityManagerFactory().createEntityManager()) {
			entityManager.getTransaction().begin();
			try {
				Record stale = entityManager.find( Record.class, 1L );
				scope.inTransaction( concurrent -> {
					Record current = concurrent.find( Record.class, 1L );
					if ( deleted ) {
						concurrent.remove( current );
					}
					else {
						current.title = "Committed";
					}
				} );
				if ( excludedOnly ) {
					stale.readCount++;
				}
				else {
					stale.title = "Stale";
				}
				OptimisticLockException failure = assertThrows( OptimisticLockException.class, entityManager::flush );
				StaleObjectStateException cause = assertInstanceOf( StaleObjectStateException.class, failure.getCause() );
				assertEquals( Record.class.getName(), cause.getEntityName() );
				assertEquals( 1L, cause.getIdentifier() );
			}
			finally {
				entityManager.getTransaction().rollback();
			}
		}
		scope.inTransaction( entityManager -> {
			Record current = entityManager.find( Record.class, 1L );
			if ( deleted ) {
				assertNull( current );
			}
			else {
				assertEquals( "Committed", current.title );
				assertEquals( 1, current.version );
				assertEquals( 0, current.readCount );
			}
		} );
	}

	@Entity(name = "ExcludedStaleRecord")
	public static class Record {
		@Id
		private Long id;
		private String title;
		@Version
		private long version;
		@ExcludedFromVersioning
		private long readCount;

		public Record() {
		}

		Record(Long id, String title) {
			this.id = id;
			this.title = title;
		}
	}
}
