/*
 * Hibernate, Relational Persistence for Idiomatic Java
 *
 * License: GNU Lesser General Public License (LGPL), version 2.1 or later.
 * See the lgpl.txt file in the root directory or http://www.gnu.org/licenses/lgpl-2.1.html.
 */
package org.hibernate.boot.model.source.annotations.internal;

import java.util.Set;

import org.hibernate.annotations.Check;
import org.hibernate.annotations.ColumnDefault;
import org.hibernate.annotations.ColumnTransformer;
import org.hibernate.annotations.Comment;
import org.hibernate.boot.jaxb.mapping.JaxbColumn;
import org.hibernate.boot.model.TruthValue;
import org.hibernate.boot.model.source.annotations.spi.AnnotationUsage;
import org.hibernate.boot.model.source.annotations.spi.JdbcTypeSource;
import org.hibernate.boot.model.source.spi.ColumnSource;
import org.hibernate.boot.model.source.spi.JdbcDataType;
import org.hibernate.boot.model.source.spi.SizeSource;
import org.hibernate.internal.util.NullnessHelper;
import org.hibernate.internal.util.StringHelper;

import jakarta.persistence.Column;

import static org.hibernate.boot.model.source.annotations.internal.AnnotationHelper.ifNotDefault;
import static org.hibernate.boot.model.source.annotations.internal.AnnotationHelper.ifSpecified;

/**
 * ColumnSource for annotation and {@code orm.xml} processing
 *
 * @author Steve Ebersole
 */
public class ColumnSourceImpl implements ColumnSource, SizeSource {
	private String name;
	private String tableName;

	private TruthValue nullable;
	private boolean unique;

	private final JdbcTypeSource jdbcTypeSource = new JdbcTypeSource();
	private Integer length;
	private Integer precision;
	private Integer scale;

	private String sqlTypeDefinition;
	private String defaultValue;
	private String comment;
	private String checkCondition;

	private boolean insertable = true;
	private boolean updateable = true;
	private String customReadFragment;
	private String customWriteFragment;

	public ColumnSourceImpl() {
	}

	public ColumnSourceImpl(Column annotation) {
		this( annotation, null );
	}

	public ColumnSourceImpl(Column annotation, String defaultName) {
		this.name = NullnessHelper.coalesce( annotation.name(), defaultName );
		this.tableName = StringHelper.nullIfEmpty( annotation.table() );
		this.sqlTypeDefinition = StringHelper.nullIfEmpty( annotation.columnDefinition() );

		applyPrimitives( annotation );
	}

	public ColumnSourceImpl(AnnotationUsage<Column> annotation) {
		setName( AnnotationHelper.nullIfUnspecified( annotation.getAttributeValue( "name" ) ) );
		setTableName( AnnotationHelper.nullIfUnspecified( annotation.getAttributeValue( "table" ) ) );
		setSqlTypeDefinition( AnnotationHelper.nullIfUnspecified( annotation.getAttributeValue( "columnDefinition" ) ) );

		overlay( annotation );
	}

	public ColumnSourceImpl(JaxbColumn jaxbColumn) {
		overlay( jaxbColumn );
	}

	public JdbcTypeSource getJdbcTypeSource() {
		return jdbcTypeSource;
	}

	public String getSqlTypeDefinition() {
		return sqlTypeDefinition;
	}

	public void setSqlTypeDefinition(String sqlTypeDefinition) {
		this.sqlTypeDefinition = sqlTypeDefinition;
	}

	public boolean isInsertable() {
		return insertable;
	}

	public void setInsertable(boolean insertable) {
		this.insertable = insertable;
	}

	public boolean isUpdateable() {
		return updateable;
	}

	public void setUpdateable(boolean updateable) {
		this.updateable = updateable;
	}


	// ~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~
	// ColumnSource

	@Override
	public Nature getNature() {
		return Nature.COLUMN;
	}

	@Override
	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	@Override
	public String getContainingTableName() {
		return tableName;
	}

	public void setTableName(String tableName) {
		this.tableName = tableName;
	}

	@Override
	public TruthValue isNullable() {
		return nullable;
	}

	public void setNullable(boolean nullable) {
		this.nullable = nullable ? TruthValue.TRUE : TruthValue.FALSE;
	}

	@Override
	public String getDefaultValue() {
		return defaultValue;
	}

	public void setDefaultValue(String defaultValue) {
		this.defaultValue = defaultValue;
	}

	@Override
	public String getSqlType() {
		return sqlTypeDefinition;
	}

	@Override
	public JdbcDataType getDatatype() {
		return null;
	}

	@Override
	public SizeSource getSizeSource() {
		return this;
	}

	@Override
	public boolean isUnique() {
		return unique;
	}

	public void setUnique(boolean unique) {
		this.unique = unique;
	}

	@Override
	public String getCheckCondition() {
		return checkCondition;
	}

	public void setCheckCondition(String checkCondition) {
		this.checkCondition = checkCondition;
	}

	@Override
	public String getComment() {
		return comment;
	}

	public void setComment(String comment) {
		this.comment = comment;
	}

	@Override
	public String getReadFragment() {
		return customReadFragment;
	}

	public void setReadFragment(String fragment) {
		this.customReadFragment = fragment;
	}

	@Override
	public String getWriteFragment() {
		return customWriteFragment;
	}

	public void setWriteFragment(String fragment) {
		this.customWriteFragment = fragment;
	}

	@Override
	public Set<String> getIndexConstraintNames() {
		return null;
	}

	@Override
	public Set<String> getUniqueKeyConstraintNames() {
		return null;
	}


	// ~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~
	// SizeSource

	@Override
	public Integer getLength() {
		return length;
	}

	public void setLength(Integer length) {
		this.length = length;
	}

	@Override
	public Integer getPrecision() {
		return precision;
	}

	public void setPrecision(Integer precision) {
		this.precision = precision;
	}

	@Override
	public Integer getScale() {
		return scale;
	}

	public void setScale(Integer scale) {
		this.scale = scale;
	}


	// ~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~
	// Overlaying

	public void applyColumnDefault(AnnotationUsage<ColumnDefault> columnDefault) {
		if ( columnDefault == null ) {
			return;
		}
		setDefaultValue( columnDefault.getValueAttributeValue().getValue() );
	}

	public void applyColumnDefault(ColumnDefault columnDefault) {
		if ( columnDefault == null ) {
			return;
		}
		setDefaultValue( columnDefault.value() );
	}

	public void applyCheck(Check checkConstraint) {
		if ( checkConstraint == null ) {
			return;
		}
		setCheckCondition( checkConstraint.constraints() );
	}

	public void applyComment(Comment comment) {
		if ( comment == null ) {
			return;
		}
		setComment( comment.value() );
	}

	public void applyComment(AnnotationUsage<Comment> comment) {
		if ( comment == null ) {
			return;
		}
		setComment( comment.getValueAttributeValue().getValue() );
	}

	public void applyColumnTransformer(ColumnTransformer customReadWrite) {
		if ( customReadWrite == null ) {
			return;
		}

		this.customReadFragment = StringHelper.nullIfEmpty( customReadWrite.read() );
		this.customWriteFragment = StringHelper.nullIfEmpty( customReadWrite.write() );
	}

	public void overlay(JaxbColumn jaxbColumn) {
		ifSpecified( jaxbColumn.getName(), this::setName );
		ifSpecified( jaxbColumn.getTable(), this::setTableName );
		ifSpecified( jaxbColumn.getColumnDefinition(), this::setSqlTypeDefinition );
		ifSpecified( jaxbColumn.isNullable(), this::setNullable );
		ifSpecified( jaxbColumn.isUnique(), this::setUnique );
		ifSpecified( jaxbColumn.getLength(), this::setLength );
		ifSpecified( jaxbColumn.getPrecision(), this::setPrecision );
		ifSpecified( jaxbColumn.getScale(), this::setScale );
		ifSpecified( jaxbColumn.isInsertable(), this::setInsertable );
		ifSpecified( jaxbColumn.isUpdatable(), this::setUpdateable );
	}

	public void overlay(AnnotationUsage<Column> override) {
		ifNotDefault( override.getAttributeValue( "name" ), this::setName );
		ifNotDefault( override.getAttributeValue( "table" ), this::setTableName );
		ifNotDefault( override.getAttributeValue( "columnDefinition" ), this::setSqlTypeDefinition );
		ifSpecified( override.getAttributeValue( "nullable" ), this::setNullable );
		ifSpecified( override.getAttributeValue( "unique" ), this::setUnique );
		ifSpecified( override.getAttributeValue( "length" ), this::setLength );
		ifSpecified( override.getAttributeValue( "insertable" ), this::setInsertable );
		ifSpecified( override.getAttributeValue( "updatable" ), this::setUpdateable );
	}

	public void overlay(Column annotation) {
		if ( StringHelper.isNotEmpty( annotation.table() ) ) {
			this.tableName = annotation.table();
		}

		if ( StringHelper.isNotEmpty( annotation.columnDefinition() ) ) {
			this.sqlTypeDefinition = annotation.columnDefinition();
		}

		applyPrimitives( annotation );
	}

	/**
	 * Applies all values for primitive attributes.  We cannot tell
	 * whether the attribute was explicitly specified or not when the
	 * type is primitive.
	 *
	 * @implNote This can change when we migrate to Jandex which we can use to
	 * detect this distinction
	 */
	private void applyPrimitives(Column annotation) {
		this.name = annotation.name();
		this.nullable = annotation.nullable() ? TruthValue.TRUE : TruthValue.FALSE;
		this.unique = annotation.unique();
		this.length = annotation.length();
		this.precision = annotation.precision();
		this.scale = annotation.scale();
		this.insertable = annotation.insertable();
		this.updateable = annotation.updatable();
	}
}
