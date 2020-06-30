/*
 * Hibernate, Relational Persistence for Idiomatic Java
 *
 * License: GNU Lesser General Public License (LGPL), version 2.1 or later
 * See the lgpl.txt file in the root directory or http://www.gnu.org/licenses/lgpl-2.1.html
 */
package org.hibernate.orm.test.metamodel.mapping.fk.toone;

import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.Table;

import org.hibernate.Hibernate;
import org.hibernate.metamodel.mapping.BasicEntityIdentifierMapping;
import org.hibernate.metamodel.mapping.EntityMappingType;
import org.hibernate.metamodel.mapping.ToOneAttributeMapping;
import org.hibernate.metamodel.mapping.internal.fk.KeyModelPartBasic;
import org.hibernate.metamodel.mapping.internal.fk.SideBasic;
import org.hibernate.stat.spi.StatisticsImplementor;
import org.hibernate.type.ForeignKeyDirection;

import org.hibernate.testing.orm.junit.DomainModel;
import org.hibernate.testing.orm.junit.SessionFactory;
import org.hibernate.testing.orm.junit.SessionFactoryScope;
import org.junit.jupiter.api.Test;

import static org.hamcrest.CoreMatchers.instanceOf;
import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.CoreMatchers.notNullValue;
import static org.hamcrest.CoreMatchers.sameInstance;
import static org.hamcrest.MatcherAssert.assertThat;

/**
 * Tests for unidirectional to-one mappings
 *
 * @author Steve Ebersole
 */
@DomainModel( annotatedClasses = { UnidirectionalToOneTests.Cat.class, UnidirectionalToOneTests.Hat.class} )
@SessionFactory( generateStatistics = true )
public class UnidirectionalToOneTests {
	@Test
	void testManyToOneBasicKeyMapping(SessionFactoryScope scope) {
		final EntityMappingType hatDescriptor = scope.getSessionFactory()
				.getRuntimeMetamodels()
				.getEntityMappingType( Hat.class );
		final EntityMappingType catDescriptor = scope.getSessionFactory()
				.getRuntimeMetamodels()
				.getEntityMappingType( Cat.class );

		final BasicEntityIdentifierMapping catIdentifier = (BasicEntityIdentifierMapping) catDescriptor.getIdentifierMapping();

		final ToOneAttributeMapping theCat = (ToOneAttributeMapping) hatDescriptor.findAttributeMapping( "theCat" );
		assertThat( theCat.getKeyModelPart(), instanceOf( KeyModelPartBasic.class ) );

		final KeyModelPartBasic theCatKeyPart = (KeyModelPartBasic) theCat.getKeyModelPart();

		assertThat( theCatKeyPart.getDirection(), is( ForeignKeyDirection.REFERRING ) );
		assertThat(
				theCatKeyPart.getForeignKeyDescriptor().getReferringSide().getTableName(),
				is( "hats" )
		);
		assertThat(
				theCatKeyPart.getForeignKeyDescriptor().getReferringSide().getColumn(),
				is( "cat_id" )
		);

		final SideBasic targetSide = theCatKeyPart.getForeignKeyDescriptor().getTargetSide();
		assertThat( targetSide.getKeyPart(), is( catIdentifier ) );
		assertThat(
				theCatKeyPart.getForeignKeyDescriptor(),
				sameInstance( targetSide.getForeignKey() )
		);
	}

	@Test
	void testManyToOneBasicKeyUsage(SessionFactoryScope scope) {
		final StatisticsImplementor statistics = scope.getSessionFactory().getStatistics();

		scope.inTransaction(
				session -> {
					final Cat whiskers = new Cat( 1, "Whiskers" );
					final Hat myFedora = new Hat( 1, "Red Hat", whiskers );
					session.save( whiskers );
					session.save( myFedora );
				}
		);

		// query for Hat - Cat should be loaded via subsequent (N+1) select
		statistics.clear();
		scope.inTransaction(
				session -> {
					final Hat hat = session.createQuery( "from Hat", Hat.class ).uniqueResult();
					assertThat( hat, notNullValue() );
					assertThat( hat.getTheCat(), notNullValue() );
					assertThat( Hibernate.isInitialized( hat.getTheCat() ), is( true ) );

					assertThat( statistics.getPrepareStatementCount(), is( 2L ) );
				}
		);

		// query for Hat with Cat join-fetched
		statistics.clear();
		scope.inTransaction(
				session -> {
					final Hat hat = session.createQuery( "from Hat h join fetch h.theCat", Hat.class ).uniqueResult();
					assertThat( hat, notNullValue() );
					assertThat( hat.getTheCat(), notNullValue() );
					assertThat( Hibernate.isInitialized( hat.getTheCat() ), is( true ) );

					assertThat( statistics.getPrepareStatementCount(), is( 1L ) );
				}
		);
	}

	@Entity( name = "Hat" )
	@Table( name = "hats" )
	public static class Hat {
		@Id
		private Integer id;
		private String style;
		@ManyToOne
		@JoinColumn( name = "cat_id" )
		private Cat theCat;

		public Hat() {
		}

		public Hat(Integer id, String style, Cat theCat) {
			this.id = id;
			this.style = style;
			this.theCat = theCat;
		}

		public Integer getId() {
			return id;
		}

		public void setId(Integer id) {
			this.id = id;
		}

		public String getStyle() {
			return style;
		}

		public void setStyle(String style) {
			this.style = style;
		}

		public Cat getTheCat() {
			return theCat;
		}

		public void setTheCat(Cat theCat) {
			this.theCat = theCat;
		}
	}

	@Entity( name = "Cat" )
	@Table( name = "cats" )
	public static class Cat {
		@Id
		private Integer id;
		private String name;

		public Cat() {
		}

		public Cat(Integer id, String name) {
			this.id = id;
			this.name = name;
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
	}
}
