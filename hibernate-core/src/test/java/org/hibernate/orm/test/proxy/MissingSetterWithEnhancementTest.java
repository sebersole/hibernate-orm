/*
 * Hibernate, Relational Persistence for Idiomatic Java
 *
 * License: GNU Lesser General Public License (LGPL), version 2.1 or later.
 * See the lgpl.txt file in the root directory or <http://www.gnu.org/licenses/lgpl-2.1.html>.
 */
package org.hibernate.orm.test.proxy;

import jakarta.persistence.Access;
import jakarta.persistence.AccessType;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;

import org.hibernate.MappingException;
import org.hibernate.SessionFactory;
import org.hibernate.boot.registry.BootstrapServiceRegistryBuilder;
import org.hibernate.cfg.Configuration;
import org.hibernate.cfg.Environment;
import org.hibernate.service.ServiceRegistry;

import org.hibernate.testing.ServiceRegistryBuilder;
import org.hibernate.testing.TestForIssue;
import org.hibernate.testing.bytecode.enhancement.BytecodeEnhancerRunner;
import org.hibernate.testing.util.ServiceRegistryUtil;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.fail;

/**
 *
 * @author Christian Beikov
 */
@TestForIssue(jiraKey = "HHH-14460")
@RunWith( BytecodeEnhancerRunner.class )
public class MissingSetterWithEnhancementTest {
    private ServiceRegistry serviceRegistry;

    @Before
    public void setUp() {
		final BootstrapServiceRegistryBuilder builder = new BootstrapServiceRegistryBuilder();
		builder.applyClassLoader( getClass().getClassLoader() );
		serviceRegistry = ServiceRegistryUtil.serviceRegistryBuilder( builder.build() )
				.applySettings( Environment.getProperties() )
				.build();
    }

    @After
    public void tearDown() {
        if ( serviceRegistry != null ) {
            ServiceRegistryBuilder.destroy( serviceRegistry );
        }
    }

    @Test
	public void testEnhancedClassMissesSetterForProperty() {
		Configuration cfg = new Configuration();
		cfg.addAnnotatedClass( EntityWithMissingSetter.class );
		try (SessionFactory sf = cfg.buildSessionFactory( serviceRegistry )) {
			fail( "Setter is missing for `name`. SessionFactory creation should fail." );
		}
		catch (MappingException e) {
			assertEquals(
					"Could not locate setter method for property 'name' of class '"
							+ EntityWithMissingSetter.class.getName() + "'",
					e.getMessage()
			);
		}
	}

	@Entity
	@Access(AccessType.PROPERTY)
	public static class EntityWithMissingSetter {
    	private Long id;
    	@Column
		@Access(AccessType.FIELD)
		private int someInt;

    	@Id
		public Long getId() {
			return id;
		}

		public void setId(Long id) {
			this.id = id;
		}

		public String getName() {
			return null;
		}

	}
}
