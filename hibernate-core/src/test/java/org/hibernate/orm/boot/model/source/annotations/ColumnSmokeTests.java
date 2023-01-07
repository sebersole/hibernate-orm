/*
 * Hibernate, Relational Persistence for Idiomatic Java
 *
 * License: GNU Lesser General Public License (LGPL), version 2.1 or later.
 * See the lgpl.txt file in the root directory or http://www.gnu.org/licenses/lgpl-2.1.html.
 */
package org.hibernate.orm.boot.model.source.annotations;

import java.lang.reflect.Field;

import org.hibernate.annotations.Comment;
import org.hibernate.boot.model.TruthValue;
import org.hibernate.boot.model.source.annotations.internal.ColumnSourceImpl;

import org.junit.jupiter.api.Test;

import jakarta.persistence.Basic;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

import static org.assertj.core.api.AssertionsForInterfaceTypes.assertThat;

/**
 * @author Steve Ebersole
 */
public class ColumnSmokeTests {
	@Test
	void testBasicBuilding() throws NoSuchFieldException {
		final Field nameField = SimpleEntity.class.getDeclaredField( "name" );

		final Comment nameCommentAnn = nameField.getAnnotation( Comment.class );
		assertThat( nameCommentAnn.value() ).isEqualTo( "SimpleEntity#name" );

		final Column nameColumnAnn = nameField.getAnnotation( Column.class );
		final ColumnSourceImpl columnSource = new ColumnSourceImpl( nameColumnAnn );
		assertThat( columnSource.getName() ).isEqualTo( "description" );
		assertThat( columnSource.getContainingTableName() ).isNull();
		assertThat( columnSource.isNullable() ).isEqualTo( TruthValue.FALSE );
		assertThat( columnSource.isUnique() ).isTrue();
		assertThat( columnSource.isInsertable() ).isFalse();
		assertThat( columnSource.isUpdateable() ).isTrue();
	}

	@Test
	void testOverrideHandling() throws NoSuchFieldException {
		final Field nameField = SimpleEntity.class.getDeclaredField( "name" );

		final ColumnSourceImpl columnSource = new ColumnSourceImpl( nameField.getAnnotation( Column.class ) );
		columnSource.applyComment( nameField.getAnnotation( Comment.class ) );

		// note: up to this point the assertions are the same from `#testBasicBuilding`.
		// now, apply an "overlay" from another column - this one uses all default values
		final Field name2Field = SimpleEntity.class.getDeclaredField( "name2" );
		final Column name2ColumnAnn = name2Field.getAnnotation( Column.class );
		columnSource.overlay( name2ColumnAnn );

		assertThat( columnSource.getContainingTableName() ).isEqualTo( "another_table" );
		assertThat( columnSource.isNullable() ).isEqualTo( TruthValue.TRUE );
		assertThat( columnSource.isUnique() ).isFalse();
		assertThat( columnSource.isInsertable() ).isTrue();
		assertThat( columnSource.isUpdateable() ).isTrue();
	}

	@Entity( name = "SimpleEntity" )
	@Table( name = "simple_entities" )
	public static class SimpleEntity {
	    @Id
		@Column( name = "id" )
		@Comment( "SimpleEntity PK column" )
	    private Integer id;

	    @Basic
		@Column( name = "description", nullable = false, unique = true, insertable = false )
		@Comment( "SimpleEntity#name" )
		private String name;

	    @Basic
		@Column( table = "another_table", columnDefinition = "special_type" )
		private String name2;

		private SimpleEntity() {
			// for use by Hibernate
		}

		public SimpleEntity(Integer id, String name) {
			this.id = id;
			this.name = name;
		}

		public Integer getId() {
			return id;
		}

		public String getName() {
			return name;
		}

		public void setName(String name) {
			this.name = name;
		}
	}
}
