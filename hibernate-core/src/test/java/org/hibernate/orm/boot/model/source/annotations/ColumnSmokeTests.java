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
import org.hibernate.boot.model.source.annotations.internal.StandardAnnotationUsage;
import org.hibernate.boot.model.source.annotations.spi.JpaAnnotations;

import org.junit.jupiter.api.Test;

import jakarta.persistence.Column;

import static org.assertj.core.api.AssertionsForInterfaceTypes.assertThat;

/**
 * @author Steve Ebersole
 */
public class ColumnSmokeTests {
	@Test
	void testBasicBuilding() throws NoSuchFieldException {
		final Field nameField = SimpleColumnEntity.class.getDeclaredField( "name" );

		final Comment nameCommentAnn = nameField.getAnnotation( Comment.class );
		assertThat( nameCommentAnn.value() ).isEqualTo( "SimpleColumnEntity#name" );

		final Column nameColumnAnn = nameField.getAnnotation( Column.class );
		final ColumnSourceImpl columnSource = new ColumnSourceImpl( nameColumnAnn );

		verifyNameAttributeMapping( columnSource );
	}

	private void verifyNameAttributeMapping(ColumnSourceImpl columnSource) {
		assertThat( columnSource.getName() ).isEqualTo( "description" );
		assertThat( columnSource.getContainingTableName() ).isNull();
		assertThat( columnSource.isNullable() ).isEqualTo( TruthValue.FALSE );
		assertThat( columnSource.isUnique() ).isTrue();
		assertThat( columnSource.isInsertable() ).isFalse();
		assertThat( columnSource.isUpdateable() ).isTrue();
	}

	@Test
	void testOverrideHandling() throws NoSuchFieldException {
		final Field nameField = SimpleColumnEntity.class.getDeclaredField( "name" );

		final ColumnSourceImpl columnSource = new ColumnSourceImpl( nameField.getAnnotation( Column.class ) );
		columnSource.applyComment( nameField.getAnnotation( Comment.class ) );

		// note: up to this point the assertions are the same from `#testBasicBuilding`.
		// now, apply an "overlay" from another column - this one uses all default values
		final Field name2Field = SimpleColumnEntity.class.getDeclaredField( "name2" );
		final Column name2ColumnAnn = name2Field.getAnnotation( Column.class );
		columnSource.overlay( name2ColumnAnn );

		verifyOverriddenValues( columnSource );
	}

	private void verifyOverriddenValues(ColumnSourceImpl columnSource) {
		assertThat( columnSource.getContainingTableName() ).isEqualTo( "another_table" );
		assertThat( columnSource.isNullable() ).isEqualTo( TruthValue.TRUE );
		assertThat( columnSource.isUnique() ).isFalse();
		assertThat( columnSource.isInsertable() ).isTrue();
		assertThat( columnSource.isUpdateable() ).isTrue();
	}

	@Test
	void testAnnotationUsage() throws NoSuchFieldException {
		final Field nameField = SimpleColumnEntity.class.getDeclaredField( "name" );
		final Column nameColumnAnn = nameField.getAnnotation( Column.class );
		final StandardAnnotationUsage<Column> nameColumnAnnUsage = new StandardAnnotationUsage<>( nameColumnAnn, JpaAnnotations.COLUMN, null );
		final ColumnSourceImpl columnSource = new ColumnSourceImpl( nameColumnAnnUsage );
		verifyNameAttributeMapping( columnSource );
	}

	@Test
	void testAnnotationUsageOverride() throws NoSuchFieldException {
		final Field nameField = SimpleColumnEntity.class.getDeclaredField( "name" );
		final Column nameColumnAnn = nameField.getAnnotation( Column.class );
		final StandardAnnotationUsage<Column> nameColumnAnnUsage = new StandardAnnotationUsage<>( nameColumnAnn, JpaAnnotations.COLUMN, null );
		final ColumnSourceImpl columnSource = new ColumnSourceImpl( nameColumnAnnUsage );

		final Field name2Field = SimpleColumnEntity.class.getDeclaredField( "name2" );
		final Column name2ColumnAnn = name2Field.getAnnotation( Column.class );
		final StandardAnnotationUsage<Column> name2ColumnAnnUsage = new StandardAnnotationUsage<>( name2ColumnAnn, JpaAnnotations.COLUMN, null );
		columnSource.overlay( name2ColumnAnnUsage );
		verifyOverriddenValues( columnSource );
	}

}
