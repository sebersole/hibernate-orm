/*
 * Hibernate, Relational Persistence for Idiomatic Java
 *
 * License: GNU Lesser General Public License (LGPL), version 2.1 or later.
 * See the lgpl.txt file in the root directory or <http://www.gnu.org/licenses/lgpl-2.1.html>.
 */
package org.hibernate.orm.test.bytecode.enhancement.basic;

import static org.junit.Assert.assertEquals;

import jakarta.persistence.Access;
import jakarta.persistence.AccessType;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.Id;
import jakarta.persistence.MappedSuperclass;

import org.hibernate.annotations.Cache;
import org.hibernate.annotations.CacheConcurrencyStrategy;
import org.hibernate.testing.TestForIssue;
import org.hibernate.testing.bytecode.enhancement.BytecodeEnhancerRunner;
import org.junit.Test;
import org.junit.runner.RunWith;

@RunWith(BytecodeEnhancerRunner.class)
public class GenericReturnValueMappedSuperclassEnhancementTest {

	@Test
	@TestForIssue(jiraKey = "HHH-12579")
	public void enhanceClassWithGenericReturnValueOnMappedSuperclass() {
		SimpleEntity implementation = new SimpleEntity();

		implementation.setEntity( SimpleEntity.Type.ONE );

		assertEquals( SimpleEntity.Type.ONE, implementation.getEntity() );
	}

	@MappedSuperclass
	@Cache(usage = CacheConcurrencyStrategy.NONE)
	public static class AbstractMappedSuperclassWithGenericReturnValue<T extends Marker> {

		@Id
		@GeneratedValue
		public int id;

		@Access(AccessType.FIELD)
		private T entity;

		public T getEntity() {
			return entity;
		}

		public void setEntity(T entity) {
			this.entity = entity;
		}
	}

	public interface Marker {
	}

	@Entity
	@Cache(usage = CacheConcurrencyStrategy.NONE)
	public static class SimpleEntity extends AbstractMappedSuperclassWithGenericReturnValue<SimpleEntity.Type> {

		public enum Type implements Marker {
			ONE
		}
	}
}
