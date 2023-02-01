/*
 * Hibernate, Relational Persistence for Idiomatic Java
 *
 * License: GNU Lesser General Public License (LGPL), version 2.1 or later.
 * See the lgpl.txt file in the root directory or http://www.gnu.org/licenses/lgpl-2.1.html.
 */
package org.hibernate.orm.test.boot.annotations.intermediate.attribute;

import java.sql.Types;
import java.util.Set;

import org.hibernate.annotations.Any;
import org.hibernate.annotations.AnyDiscriminator;
import org.hibernate.annotations.AnyKeyJavaClass;
import org.hibernate.annotations.CompositeType;
import org.hibernate.annotations.EmbeddableInstantiator;
import org.hibernate.annotations.Generated;
import org.hibernate.annotations.JavaType;
import org.hibernate.annotations.JdbcType;
import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.annotations.ManyToAny;
import org.hibernate.annotations.Nationalized;
import org.hibernate.annotations.TenantId;
import org.hibernate.annotations.TimeZoneColumn;
import org.hibernate.annotations.TimeZoneStorage;
import org.hibernate.annotations.Type;
import org.hibernate.boot.annotations.model.AccessTypePlacementException;
import org.hibernate.boot.annotations.model.spi.EntityHierarchy;
import org.hibernate.boot.annotations.model.spi.EntityTypeMetadata;
import org.hibernate.boot.annotations.source.spi.ClassDetails;
import org.hibernate.boot.annotations.spi.AnnotationProcessingContext;
import org.hibernate.boot.registry.StandardServiceRegistry;
import org.hibernate.orm.test.boot.annotations.intermediate.ModelHelper;
import org.hibernate.orm.test.mapping.embeddable.strategy.instantiator.registered.NameInstantiator;
import org.hibernate.orm.test.mapping.embeddable.strategy.usertype.embedded.NameCompositeUserType;
import org.hibernate.type.descriptor.java.StringJavaType;
import org.hibernate.type.descriptor.jdbc.VarcharJdbcType;
import org.hibernate.usertype.UserTypeSupport;

import org.hibernate.testing.orm.junit.NotImplementedYet;
import org.hibernate.testing.orm.junit.ServiceRegistry;
import org.hibernate.testing.orm.junit.ServiceRegistryScope;
import org.junit.jupiter.api.Test;

import jakarta.persistence.Access;
import jakarta.persistence.AccessType;
import jakarta.persistence.Basic;
import jakarta.persistence.Convert;
import jakarta.persistence.ElementCollection;
import jakarta.persistence.Embedded;
import jakarta.persistence.EmbeddedId;
import jakarta.persistence.Entity;
import jakarta.persistence.Enumerated;
import jakarta.persistence.Id;
import jakarta.persistence.Lob;
import jakarta.persistence.ManyToMany;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.OneToMany;
import jakarta.persistence.OneToOne;
import jakarta.persistence.Table;
import jakarta.persistence.Temporal;
import jakarta.persistence.TemporalType;
import jakarta.persistence.Transient;
import jakarta.persistence.Version;

import static jakarta.persistence.AccessType.PROPERTY;
import static org.assertj.core.api.Assertions.assertThat;
import static org.hibernate.boot.annotations.model.spi.AttributeMetadata.AttributeNature.ANY;
import static org.hibernate.boot.annotations.model.spi.AttributeMetadata.AttributeNature.BASIC;
import static org.hibernate.boot.annotations.model.spi.AttributeMetadata.AttributeNature.EMBEDDED;
import static org.hibernate.boot.annotations.model.spi.AttributeMetadata.AttributeNature.PLURAL;
import static org.hibernate.boot.annotations.model.spi.AttributeMetadata.AttributeNature.TO_ONE;
import static org.junit.jupiter.api.Assertions.fail;

/**
 * @author Steve Ebersole
 */
@ServiceRegistry
public class AttributeMetadataSmokeTests {
	@Test
	void testClassLevelAccessMismatch(ServiceRegistryScope scope) {
		final EntityTypeMetadata mismatch = ModelHelper.buildHierarchies( scope.getRegistry(), ExplicitClassAccessMismatchEntity.class )
				.iterator()
				.next()
				.getRoot();
		// todo (annotation-source) : the spec seems to imply that this is "undefined", but it is very unclear.
		//		an error seems more appropriate
		assertThat( mismatch.getAttributes() ).hasSize( 0 );
	}

	@Test
	@NotImplementedYet
	void testAccessOnSetter(ServiceRegistryScope scope) {
		try {
			ModelHelper.buildHierarchies( scope.getRegistry(), ExplicitAccessOnSetterEntity.class );
			fail( "Expecting error about annotations on setter" );
		}
		catch (AccessTypePlacementException expected) {
		}
	}

	@Test
	void testMixedAccess(ServiceRegistryScope scope) {
		final EntityTypeMetadata mismatch = ModelHelper.buildHierarchies( scope.getRegistry(), MixedAccess.class )
				.iterator()
				.next()
				.getRoot();
		assertThat( mismatch.getAttributes() ).hasSize( 2 );
		mismatch.getAttributes().forEach( (attributeMetadata) -> {
			if ( "id".equals( attributeMetadata.getName() ) ) {
				assertThat( attributeMetadata.getMember().isField() ).isTrue();
			}
			else {
				assertThat( attributeMetadata.getName() ).isEqualTo( "name" );
				assertThat( attributeMetadata.getMember().isField() ).isFalse();
			}
		} );
	}

	@Test
	void testTransiency(ServiceRegistryScope scope) {
		final EntityTypeMetadata mismatch = ModelHelper.buildHierarchies( scope.getRegistry(), Transiency.class )
				.iterator()
				.next()
				.getRoot();
		assertThat( mismatch.getAttributes() ).hasSize( 2 );
	}

	@Test
	void testClassifications(ServiceRegistryScope scope) {
		final StandardServiceRegistry registry = scope.getRegistry();
		final AnnotationProcessingContext processingContext = ModelHelper.buildProcessingContext( registry );

		final Set<EntityHierarchy> entityHierarchies = ModelHelper.buildHierarchies( processingContext, Container.class );
		assertThat( entityHierarchies ).hasSize( 1 );

		final ClassDetails stringDescriptor = processingContext
				.getClassDetailsRegistry()
				.getManagedClass( String.class.getName() );

		final EntityHierarchy entityHierarchy = entityHierarchies.iterator().next();
		final EntityTypeMetadata containerDescriptor = entityHierarchy.getRoot();

		containerDescriptor.getAttributes().forEach( (attributeMetadata) -> {
			assertThat( attributeMetadata.getMember().getType() ).isSameAs( stringDescriptor );

			if ( "id".equals( attributeMetadata.getName() )
					|| "basic".equals( attributeMetadata.getName() )
					|| "temporal".equals( attributeMetadata.getName() )
					|| "converted".equals( attributeMetadata.getName() )
					|| "enumerated".equals( attributeMetadata.getName() )
					|| "generated".equals( attributeMetadata.getName() )
					|| "nationalized".equals( attributeMetadata.getName() )
					|| "tzColumn".equals( attributeMetadata.getName() )
					|| "tzStorage".equals( attributeMetadata.getName() )
					|| "userType".equals( attributeMetadata.getName() )
					|| "javaType".equals( attributeMetadata.getName() )
					|| "jdbcType".equals( attributeMetadata.getName() )
					|| "jdbcTypeCode".equals( attributeMetadata.getName() )
					|| "version".equals( attributeMetadata.getName() )
					|| "tenantId".equals( attributeMetadata.getName() )
					|| "lob".equals( attributeMetadata.getName() ) ) {
				assertThat( attributeMetadata.getNature() ).isEqualTo( BASIC );
			}
			else if ( "embeddedId".equals( attributeMetadata.getName() )
					|| "embedded".equals( attributeMetadata.getName() )
					|| "embeddedInstantiator".equals( attributeMetadata.getName() )
					|| "compositeType".equals( attributeMetadata.getName() ) ) {
				assertThat( attributeMetadata.getNature() ).isEqualTo( EMBEDDED );
			}
			else if ( "any".equals( attributeMetadata.getName() )
					|| "anyKeyJavaClass".equals( attributeMetadata.getName() )
					|| "anyDiscriminator".equals( attributeMetadata.getName() ) ) {
				assertThat( attributeMetadata.getNature() ).isEqualTo( ANY );
			}
			else if ( "oneToOne".equals( attributeMetadata.getName() )
					|| "manyToOne".equals( attributeMetadata.getName() ) ) {
				assertThat( attributeMetadata.getNature() ).isEqualTo( TO_ONE );
			}
			else if ( "elementCollection".equals( attributeMetadata.getName() )
					|| "manyToAny".equals( attributeMetadata.getName() )
					|| "manyToMany".equals( attributeMetadata.getName() )
					|| "oneToMany".equals( attributeMetadata.getName() ) ) {
				assertThat( attributeMetadata.getNature() ).isEqualTo( PLURAL );
			}
			else {
				fail();
			}
		} );
	}

	@Entity( name = "Transiency" )
	@Table( name = "Transiency" )
	public static class Transiency {
	    @Id
	    private Integer id;
	    @Basic
		private String name;
		@Transient
		private String hidden;
	}

	@Entity( name = "Transiency" )
	@Table( name = "Transiency" )
	@Access(AccessType.FIELD)
	public static class MixedAccess {
	    @Id
	    private Integer id;

		@Access(PROPERTY)
		@Basic
		private String getName() {
			return null;
		}
	}

	@Entity( name = "Container" )
	@Table( name = "container" )
	public static class Container {
		@Id
		private String id;

		@EmbeddedId
		private String embeddedId;

		@Basic
		private String basic;

		@Lob
		private String lob;

		@Temporal( TemporalType.TIMESTAMP )
		private String temporal;

		@Enumerated
		private String enumerated;

		@Convert
		private String converted;

		@Nationalized
		private String nationalized;

		@Generated
		private String generated;

		@TimeZoneColumn
		private String tzColumn;

		@TimeZoneStorage
		private String tzStorage;

		@Type(UserTypeImpl.class)
		private String userType;

		@JavaType(StringJavaType.class)
		private String javaType;

		@JdbcType(VarcharJdbcType.class)
		private String jdbcType;

		@JdbcTypeCode(Types.VARCHAR)
		private String jdbcTypeCode;

		@Version
		private String version;

		@TenantId
		private String tenantId;

		@Any
		private String any;

		@AnyDiscriminator
		private String anyDiscriminator;

		@AnyKeyJavaClass(String.class)
		private String anyKeyJavaClass;

		@Embedded
		private String embedded;

		@EmbeddableInstantiator(NameInstantiator.class)
		private String embeddedInstantiator;

		@CompositeType(NameCompositeUserType.class)
		private String compositeType;

		@OneToOne
		private String oneToOne;

		@ManyToOne
		private String manyToOne;

		@ElementCollection
		private String elementCollection;

		@ManyToAny
		private String manyToAny;

		@OneToMany
		private String oneToMany;

		@ManyToMany
		private String manyToMany;
	}

	public static class UserTypeImpl extends UserTypeSupport<String> {
		public UserTypeImpl() {
			super( String.class, Types.VARCHAR );
		}
	}
}
