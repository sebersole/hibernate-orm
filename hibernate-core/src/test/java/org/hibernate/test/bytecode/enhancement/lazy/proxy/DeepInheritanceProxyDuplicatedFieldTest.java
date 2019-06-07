/*
 * Hibernate, Relational Persistence for Idiomatic Java
 *
 * License: GNU Lesser General Public License (LGPL), version 2.1 or later.
 * See the lgpl.txt file in the root directory or <http://www.gnu.org/licenses/lgpl-2.1.html>.
 */
package org.hibernate.test.bytecode.enhancement.lazy.proxy;

import java.io.Serializable;
import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.Inheritance;
import javax.persistence.InheritanceType;
import javax.persistence.MappedSuperclass;

import org.hibernate.Hibernate;
import org.hibernate.boot.MetadataSources;
import org.hibernate.boot.SessionFactoryBuilder;
import org.hibernate.boot.registry.StandardServiceRegistryBuilder;
import org.hibernate.cfg.AvailableSettings;
import org.hibernate.stat.Statistics;

import org.hibernate.testing.TestForIssue;
import org.hibernate.testing.bytecode.enhancement.BytecodeEnhancerRunner;
import org.hibernate.testing.bytecode.enhancement.EnhancementOptions;
import org.hibernate.testing.junit4.BaseNonConfigCoreFunctionalTestCase;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

/**
 * @author Gail Badner
 */

@TestForIssue( jiraKey = "HHH-11147" )
@RunWith( BytecodeEnhancerRunner.class )
@EnhancementOptions( lazyLoading = true )
public class DeepInheritanceProxyDuplicatedFieldTest extends BaseNonConfigCoreFunctionalTestCase {

	@Test
	public void testRootGetValueToInitialize() {
		inTransaction(
				session -> {
					final Statistics stats = sessionFactory().getStatistics();
					stats.clear();

					AEntity aEntity = session.getReference( AEntity.class, "AEntity" );

					assertFalse( Hibernate.isInitialized( aEntity) );
					aEntity.getDuplicatedField();
					assertTrue( Hibernate.isInitialized( aEntity ) );

					assertEquals( 1, stats.getPrepareStatementCount() );
					assertEquals( "dup", aEntity.getDuplicatedField() );
					assertEquals( 1, stats.getPrepareStatementCount() );
				}
		);
	}

	@Test
	public void testRootSetValueToInitialize() {
		inTransaction(
				session -> {
					final Statistics stats = sessionFactory().getStatistics();
					stats.clear();

					AEntity aEntity = session.getReference( AEntity.class, "AEntity" );

					assertFalse( Hibernate.isInitialized( aEntity) );
					aEntity.setDuplicatedField("updated dup");
					assertTrue( Hibernate.isInitialized( aEntity ) );

					assertEquals( 1, stats.getPrepareStatementCount() );
					assertEquals( "updated dup", aEntity.getDuplicatedField() );
					assertEquals( 1, stats.getPrepareStatementCount() );
				}
		);

		inTransaction(
				session -> {
					final Statistics stats = sessionFactory().getStatistics();
					stats.clear();

					AEntity aEntity = session.get( AEntity.class, "AEntity" );
					assertTrue( Hibernate.isInitialized( aEntity) );
					assertEquals( 1, stats.getPrepareStatementCount() );

					assertEquals( "updated dup", aEntity.getDuplicatedField() );
					assertEquals( 1, stats.getPrepareStatementCount() );
				}
		);
	}

	@Test
	public void testMiddleGetValueToInitialize() {
		inTransaction(
				session -> {
					final Statistics stats = sessionFactory().getStatistics();
					stats.clear();

					AAEntity aaEntity = session.getReference( AAEntity.class, "AAEntity" );

					assertFalse( Hibernate.isInitialized( aaEntity) );
					aaEntity.getDuplicatedField();
					assertTrue( Hibernate.isInitialized( aaEntity ) );

					assertEquals( 1, stats.getPrepareStatementCount() );
					assertEquals( "dup", aaEntity.getDuplicatedField() );
					assertEquals( 1, stats.getPrepareStatementCount() );
				}
		);
	}

	@Test
	public void testMiddleSetValueToInitialize() {
		inTransaction(
				session -> {
					final Statistics stats = sessionFactory().getStatistics();
					stats.clear();

					AAEntity aaEntity = session.getReference( AAEntity.class, "AAEntity" );

					assertFalse( Hibernate.isInitialized( aaEntity) );
					aaEntity.setDuplicatedField("updated dup");
					assertTrue( Hibernate.isInitialized( aaEntity ) );

					assertEquals( 1, stats.getPrepareStatementCount() );
					assertEquals( "updated dup", aaEntity.getDuplicatedField() );
					assertEquals( 1, stats.getPrepareStatementCount() );
				}
		);

		inTransaction(
				session -> {
					final Statistics stats = sessionFactory().getStatistics();
					stats.clear();

					AAEntity aaEntity = session.get( AAEntity.class, "AAEntity" );
					assertTrue( Hibernate.isInitialized( aaEntity) );
					assertEquals( 1, stats.getPrepareStatementCount() );

					assertEquals( "updated dup", aaEntity.getDuplicatedField() );
					assertEquals( 1, stats.getPrepareStatementCount() );
				}
		);
	}

	@Test
	public void testLeafGetValueToInitialize() {
		inTransaction(
				session -> {
					final Statistics stats = sessionFactory().getStatistics();
					stats.clear();

					AAAEntity aaaEntity = session.getReference( AAAEntity.class, "AAAEntity" );

					assertFalse( Hibernate.isInitialized( aaaEntity) );
					aaaEntity.getDuplicatedField();
					assertTrue( Hibernate.isInitialized( aaaEntity ) );

					assertEquals( 1, stats.getPrepareStatementCount() );
					assertEquals( "dup", aaaEntity.getDuplicatedField() );
					assertEquals( 1, stats.getPrepareStatementCount() );
				}
		);
	}

	@Test
	public void testLeafSetValueToInitialize() {
		inTransaction(
				session -> {
					final Statistics stats = sessionFactory().getStatistics();
					stats.clear();

					AAAEntity aaaEntity = session.getReference( AAAEntity.class, "AAAEntity" );

					assertFalse( Hibernate.isInitialized( aaaEntity) );
					aaaEntity.setDuplicatedField("updated dup");
					assertTrue( Hibernate.isInitialized( aaaEntity ) );

					assertEquals( 1, stats.getPrepareStatementCount() );
					assertEquals( "updated dup", aaaEntity.getDuplicatedField() );
					assertEquals( 1, stats.getPrepareStatementCount() );
				}
		);

		inTransaction(
				session -> {
					final Statistics stats = sessionFactory().getStatistics();
					stats.clear();

					AAAEntity aaaEntity = session.get( AAAEntity.class, "AAAEntity" );
					assertTrue( Hibernate.isInitialized( aaaEntity) );
					assertEquals( 1, stats.getPrepareStatementCount() );

					assertEquals( "updated dup", aaaEntity.getDuplicatedField() );
					assertEquals( 1, stats.getPrepareStatementCount() );
				}
		);
	}

	@Override
	protected void configureStandardServiceRegistryBuilder(StandardServiceRegistryBuilder ssrb) {
		super.configureStandardServiceRegistryBuilder( ssrb );
		ssrb.applySetting( AvailableSettings.ALLOW_ENHANCEMENT_AS_PPROXY, "true" );
		ssrb.applySetting( AvailableSettings.FORMAT_SQL, "false" );
		ssrb.applySetting( AvailableSettings.GENERATE_STATISTICS, "true" );
	}

	@Override
	protected void configureSessionFactoryBuilder(SessionFactoryBuilder sfb) {
		super.configureSessionFactoryBuilder( sfb );
		sfb.applyStatisticsSupport( true );
		sfb.applySecondLevelCacheSupport( false );
		sfb.applyQueryCacheSupport( false );
	}

	@Override
	protected void applyMetadataSources(MetadataSources sources) {
		super.applyMetadataSources( sources );
		sources.addAnnotatedClass( AMappedSuperclass.class );
		sources.addAnnotatedClass( AEntity.class );
		sources.addAnnotatedClass( AAEntity.class );
		sources.addAnnotatedClass( AAAEntity.class );
	}

	@Before
	public void prepareTestData() {
		inTransaction(
				session -> {
					AEntity aEntity = new AEntity( "AEntity" );
					aEntity.setDuplicatedField( "dup" );
					session.persist( aEntity );

					AAEntity aaEntity = new AAAEntity( "AAEntity" );
					aaEntity.setDuplicatedField( "dup" );
					session.persist( aaEntity );

					AAAEntity aaaEntity = new AAAEntity( "AAAEntity" );
					aaaEntity.setDuplicatedField( "dup" );
					session.persist( aaaEntity );
				}
		);
	}

	@After
	public void clearTestData(){
		inTransaction(
				session -> {
					session.createQuery( "delete from AEntity" ).executeUpdate();
				}
		);
	}

	@MappedSuperclass
	public static class AMappedSuperclass implements Serializable {

		@Id
		private String id;

		private String duplicatedField;

		public String getDuplicatedField() {
			return duplicatedField;
		}

		public void setDuplicatedField(String duplicatedField) {
			this.duplicatedField = duplicatedField;
		}

		public AMappedSuperclass(String id) {
			this.id = id;
		}

		protected AMappedSuperclass() {
		}
	}

	@Entity(name="AEntity")
	@Inheritance(strategy = InheritanceType.SINGLE_TABLE)
	public static class AEntity extends AMappedSuperclass {

		private String duplicatedField;


		public AEntity(String id) {
			super(id);
		}

		protected AEntity() {
		}

		@Override
		public String getDuplicatedField() {
			return duplicatedField;
		}

		@Override
		public void setDuplicatedField(String duplicatedField) {
			this.duplicatedField = duplicatedField;
		}
	}

	@Entity(name="AAEntity")
	public static class AAEntity extends AEntity {

		private String duplicatedField;

		public AAEntity(String id) {
			super(id);
		}

		protected AAEntity() {
		}

		@Override
		public String getDuplicatedField() {
			return duplicatedField;
		}

		@Override
		public void setDuplicatedField(String duplicatedField) {
			this.duplicatedField = duplicatedField;
		}
	}

	@Entity(name="AAAEntity")
	public static class AAAEntity extends AAEntity {

		private String duplicatedField;

		public AAAEntity(String id) {
			super(id);
		}

		protected AAAEntity() {
		}

		@Override
		public String getDuplicatedField() {
			return duplicatedField;
		}

		@Override
		public void setDuplicatedField(String duplicatedField) {
			this.duplicatedField = duplicatedField;
		}
	}
}
