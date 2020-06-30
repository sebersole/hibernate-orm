/*
 * Hibernate, Relational Persistence for Idiomatic Java
 *
 * License: GNU Lesser General Public License (LGPL), version 2.1 or later
 * See the lgpl.txt file in the root directory or http://www.gnu.org/licenses/lgpl-2.1.html
 */
package org.hibernate.orm.test.metamodel.mapping.collections;

import java.util.List;
import java.util.Set;
import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.OneToMany;
import javax.persistence.Table;

import org.hibernate.testing.orm.junit.DomainModel;
import org.hibernate.testing.orm.junit.DomainModelScope;
import org.hibernate.testing.orm.junit.ServiceRegistry;
import org.hibernate.testing.orm.junit.SessionFactory;
import org.hibernate.testing.orm.junit.SessionFactoryScope;
import org.junit.jupiter.api.Test;

/**
 * @author Steve Ebersole
 */
@DomainModel(
		annotatedClasses = {
				BootstrapSmokeTests.ListContainer.class,
				BootstrapSmokeTests.BiDirectionalListEntity.class,
				BootstrapSmokeTests.BiDirectionalSetEntity.class,
				BootstrapSmokeTests.SetContainer.class
		}
)
@ServiceRegistry
@SessionFactory
public class BootstrapSmokeTests {
	@Test
	public void testMappingModelCreation(SessionFactoryScope scope) {
	}

	@Entity( name = "ListContainer" )
	@Table( name = "lists" )
	public static class ListContainer {
		@Id
		private Integer id;
		private String name;
		@OneToMany
		@JoinColumn( name = "this_id" )
		private List<BiDirectionalListEntity> these;
		@OneToMany( mappedBy = "container" )
		private List<BiDirectionalListEntity> those;
	}

	@Entity( name = "BiDirectionalListEntity" )
	@Table( name = "bidir_list_entity" )
	public static class BiDirectionalListEntity {
		@Id
		private Integer id;
		private String name;
		@ManyToOne
		@JoinColumn( name = "container_id" )
		ListContainer container;
	}

	@Entity( name = "SetContainer" )
	@Table( name = "sets" )
	public static class SetContainer {
		@Id
		private Integer id;
		private String name;
		@OneToMany
		@JoinColumn( name = "this_id" )
		private Set<BiDirectionalSetEntity> these;
		@OneToMany( mappedBy = "container" )
		private Set<BiDirectionalSetEntity> those;
	}

	@Entity( name = "BiDirectionalSetEntity" )
	@Table( name = "bidir_set_entity" )
	public static class BiDirectionalSetEntity {
		@Id
		private Integer id;
		private String name;
		@ManyToOne
		@JoinColumn( name = "container_id" )
		SetContainer container;
	}

}
