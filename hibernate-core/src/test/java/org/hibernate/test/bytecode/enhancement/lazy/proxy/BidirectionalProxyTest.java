/*
 * Hibernate, Relational Persistence for Idiomatic Java
 *
 * License: GNU Lesser General Public License (LGPL), version 2.1 or later.
 * See the lgpl.txt file in the root directory or <http://www.gnu.org/licenses/lgpl-2.1.html>.
 */
package org.hibernate.test.bytecode.enhancement.lazy.proxy;

import java.io.Serializable;
import java.util.LinkedHashSet;
import java.util.Set;
import javax.persistence.Basic;
import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.Id;
import javax.persistence.Inheritance;
import javax.persistence.InheritanceType;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.MappedSuperclass;
import javax.persistence.OneToMany;
import javax.persistence.Table;

import org.hibernate.annotations.LazyGroup;
import org.hibernate.annotations.LazyToOne;
import org.hibernate.annotations.LazyToOneOption;
import org.hibernate.boot.MetadataSources;
import org.hibernate.boot.SessionFactoryBuilder;
import org.hibernate.boot.registry.StandardServiceRegistryBuilder;
import org.hibernate.cfg.AvailableSettings;

import org.hibernate.testing.TestForIssue;
import org.hibernate.testing.bytecode.enhancement.BytecodeEnhancerRunner;
import org.hibernate.testing.bytecode.enhancement.EnhancementOptions;
import org.hibernate.testing.junit4.BaseNonConfigCoreFunctionalTestCase;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;

@TestForIssue( jiraKey = "HHH-11147" )
@RunWith( BytecodeEnhancerRunner.class )
@EnhancementOptions( lazyLoading = true )
public class BidirectionalProxyTest  extends BaseNonConfigCoreFunctionalTestCase {

	@Test
	public void testIt() {
		inTransaction(
				session -> {
					for (BEntity b : session.createQuery("from BEntity b", BEntity.class).getResultList()) {
						AChildEntity a = (AChildEntity) b.getA();
						a.getVersion();
					}
				}
		);
	}

	@Override
	protected void configureStandardServiceRegistryBuilder(StandardServiceRegistryBuilder ssrb) {
		super.configureStandardServiceRegistryBuilder( ssrb );
		ssrb.applySetting( AvailableSettings.ALLOW_ENHANCEMENT_AS_PPROXY, "true" );
		ssrb.applySetting( AvailableSettings.FORMAT_SQL, "false" );
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
		sources.addAnnotatedClass( BEntity.class );
		sources.addAnnotatedClass( AMappedSuperclass.class );
		sources.addAnnotatedClass( AEntity.class );
		sources.addAnnotatedClass( AChildEntity.class );
	}

	@Before
	public void prepareTestData() {
		inTransaction(
				session -> {
					AChildEntity a = new AChildEntity("a");
					BEntity b = new BEntity("b");
					b.setA(a);
					session.persist(a);
					session.persist(b);
				}
		);
	}

	@Entity(name="BEntity")
	@Table(name="B")
	public static class BEntity implements Serializable {
		@Id
		private String id;

		public BEntity(String id) {
			this();
			setId(id);
		}

		protected BEntity() {
		}

		public String getId() {
			return id;
		}

		protected void setId(String id) {
			this.id = id;
		}

		public void setA(AChildEntity a) {
			aChildEntity = a;
			a.getEntries().add(this);
		}

		public AChildEntity getA() {
			return aChildEntity;
		}

		@ManyToOne(fetch= FetchType.LAZY)
		@LazyToOne(LazyToOneOption.NO_PROXY)
		@LazyGroup("aEntity")
		@JoinColumn(name="aEntity")
		protected AChildEntity aChildEntity = null;
	}

	@MappedSuperclass
	public static class AMappedSuperclass implements Serializable {

		@Id
		private String id;

		@Basic
		private short version;

		public AMappedSuperclass(String id) {
			setId(id);
		}

		protected AMappedSuperclass() {
		}

		public String getId() {
			return id;
		}

		protected void setId(String id) {
			this.id = id;
		}

		public short getVersion() {
			return version;
		}

		public void setVersion(short version) {
			this.version = version;
		}
	}

	@Entity(name="AEntity")
	@Inheritance(strategy = InheritanceType.TABLE_PER_CLASS)
	@Table(name="A")
	public static class AEntity extends AMappedSuperclass {

		public AEntity(String id) {
			super(id);
		}

		protected AEntity() {
		}
	}

	@Entity(name="AChildEntity")
	@Table(name="ACChild")
	public static class AChildEntity extends AEntity {

		public AChildEntity(String id) {
			super(id);
		}

		protected AChildEntity() {
		}

		public Set getEntries() {
			return Entries;
		}

		@OneToMany(targetEntity=BEntity.class, mappedBy="aChildEntity", fetch=FetchType.LAZY)
		protected Set Entries = new LinkedHashSet();
	}

}