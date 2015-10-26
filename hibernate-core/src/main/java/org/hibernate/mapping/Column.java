/*
 * Hibernate, Relational Persistence for Idiomatic Java
 *
 * License: GNU Lesser General Public License (LGPL), version 2.1 or later.
 * See the lgpl.txt file in the root directory or <http://www.gnu.org/licenses/lgpl-2.1.html>.
 */
package org.hibernate.mapping;

import java.io.Serializable;
import java.util.Locale;

import org.hibernate.MappingException;
import org.hibernate.boot.model.naming.Identifier;
import org.hibernate.dialect.Dialect;
import org.hibernate.dialect.function.SQLFunctionRegistry;
import org.hibernate.engine.jdbc.Size;
import org.hibernate.internal.util.StringHelper;
import org.hibernate.sql.Template;

/**
 * A column of a relational database table
 *
 * @author Gavin King
 * @author Steve Ebersole
 */
public class Column implements Selectable, Serializable, Cloneable {

	public static final int DEFAULT_LENGTH = 255;
	public static final int DEFAULT_PRECISION = 19;
	public static final int DEFAULT_SCALE = 2;

	private Identifier logicalName;
	private Identifier physicalName;

	private boolean isIdentity;

	private String sqlType;
	private Integer jdbcTypeCode;
	private final Size size = new Size();

	private boolean nullable = true;
	private boolean unique;
	private String checkConstraint;
	private String comment;
	private String defaultValue;
	private String customWrite;
	private String customRead;

	int uniqueInteger;


	public Column() {
	}

	/**
	 * Get the logical name of this column.  This is the name (implicitly or explicitly)
	 * from the mappings before any physical naming strategy transformations are applied.
	 *
	 * @return The logical name.
	 *
	 * @see #getPhysicalName()
	 */
	public Identifier getLogicalName() {
		return logicalName;
	}

	public void setLogicalName(Identifier logicalName) {
		this.logicalName = logicalName;
	}

	/**
	 * Get the physical name of this column.  This is the name of the column as it is
	 * in the underlying database table.
	 *
	 * @return The physical column name
	 *
	 * @see #getLogicalName()
	 */
	public Identifier getPhysicalName() {
		return physicalName;
	}

	public void setPhysicalName(Identifier physicalName) {
		this.physicalName = physicalName;
	}

	/**
	 * Is this column an IDENTITY value?
	 *
	 * @return {@code true} indicates it is an IDENTITY value; {@code false} indicates
	 * it is not
	 */
	public boolean isIdentity() {
		return isIdentity;
	}

	public void markAsIdentity(boolean isIdentity) {
		this.isIdentity = isIdentity;
	}

	/**
	 * The database-specific SQL type of the column as used to create it.
	 *
	 * @return The database-specific SQL type
	 */
	public String getSqlType() {
		return sqlType;
	}

	public void setSqlType(String sqlType) {
		this.sqlType = sqlType;
	}

	/**
	 * Returns the JDBC type code of the column.  If {@code null}, the type code
	 * is not (yet) known.
	 *
	 * @return The JDBC type code.
	 *
	 * @see java.sql.Types
	 */
	public Integer getJdbcTypeCode() {
		return jdbcTypeCode;
	}

	public void setJdbcTypeCode(Integer typeCode) {
		jdbcTypeCode = typeCode;
	}

	/**
	 * The sizing arguments for the JDBC type code.
	 *
	 * @return The column type size arguments
	 */
	public Size getSize() {
		return size;
	}

	/**
	 * Does the column allow {@code NULL} values?
	 *
	 * @return {@code true} indicates it does allow {@code NULL}; {@code false} indicates it does not
	 */
	public boolean isNullable() {
		return nullable;
	}

	public void setNullable(boolean nullable) {
		this.nullable = nullable;
	}

	/**
	 * Is the column (by itself) unique?
	 *
	 * @return {@code true} indicates it is unique; {@code false} indicates it is not.
	 */
	public boolean isUnique() {
		return unique;
	}

	public void setUnique(boolean unique) {
		this.unique = unique;
	}

	public String getCheckConstraint() {
		return checkConstraint;
	}

	public void setCheckConstraint(String checkConstraint) {
		this.checkConstraint = checkConstraint;
	}

	public boolean hasCheckConstraint() {
		return checkConstraint != null;
	}

	public String getComment() {
		return comment;
	}

	public void setComment(String comment) {
		this.comment = comment;
	}

	public String getDefaultValue() {
		return defaultValue;
	}

	public void setDefaultValue(String defaultValue) {
		this.defaultValue = defaultValue;
	}

	public String getCustomWrite() {
		return customWrite;
	}

	public void setCustomWrite(String customWrite) {
		this.customWrite = customWrite;
	}

	public String getCustomRead() {
		return customRead;
	}

	public void setCustomRead(String customRead) {
		this.customRead = customRead;
	}

	@Override
	public boolean isFormula() {
		return false;
	}

	@Override
	public String getText(Dialect d) {
		return getPhysicalName().render( d );
	}

	@Override
	public String getText() {
		return getPhysicalName().render();
	}

	@Override
	public String getAlias(Dialect dialect) {
		final String name = physicalName.render( dialect );
		final int lastLetter = StringHelper.lastIndexOfLetter( name );
		final String suffix = Integer.toString( uniqueInteger ) + '_';

		String alias = name;
		if ( lastLetter == -1 ) {
			alias = "column";
		}
		else if ( name.length() > lastLetter + 1 ) {
			alias = name.substring( 0, lastLetter + 1 );
		}

		boolean useRawName = name.length() + suffix.length() <= dialect.getMaxAliasLength()
				&& !physicalName.isQuoted() && !name.toLowerCase( Locale.ROOT ).equals( "rowid" );
		if ( !useRawName ) {
			if ( suffix.length() >= dialect.getMaxAliasLength() ) {
				throw new MappingException(
						String.format(
								"Unique suffix [%s] length must be less than maximum [%d]",
								suffix, dialect.getMaxAliasLength()
						)
				);
			}
			if ( alias.length() + suffix.length() > dialect.getMaxAliasLength() ) {
				alias = alias.substring( 0, dialect.getMaxAliasLength() - suffix.length() );
			}
		}
		return alias + suffix;
	}

	@Override
	public String getAlias(Dialect dialect, Table table) {
		return getAlias( dialect ) + table.getUniqueInteger() + '_';
	}

	@Override
	public String getTemplate(Dialect dialect, SQLFunctionRegistry functionRegistry) {
		return hasCustomRead()
				? Template.renderWhereStringTemplate( customRead, dialect, functionRegistry )
				: Template.TEMPLATE + '.' + getPhysicalName().render( dialect );
	}

	public boolean hasCustomRead() {
		return ( customRead != null && customRead.length() > 0 );
	}

	public String getReadExpr(Dialect dialect) {
		return hasCustomRead() ? customRead : getPhysicalName().render( dialect );
	}

	public String getWriteExpr() {
		return ( customWrite != null && customWrite.length() > 0 ) ? customWrite : "?";
	}

	@Override
	public int hashCode() {
		return physicalName.render().hashCode();
	}

	@Override
	public boolean equals(Object object) {
		return object instanceof Column && equals( (Column) object );
	}

	@SuppressWarnings("SimplifiableIfStatement")
	public boolean equals(Column column) {
		if ( null == column ) {
			return false;
		}
		if ( this == column ) {
			return true;
		}

		return physicalName.equals( column.physicalName );
	}

	@Override
	public String toString() {
		return "Column(" + physicalName.render() + ')';
	}

	public Column makeCopy() {
		Column copy = new Column();
		copy.setLogicalName( getLogicalName() );
		copy.setPhysicalName( getPhysicalName() );
		copy.markAsIdentity( isIdentity() );
		copy.setJdbcTypeCode( getJdbcTypeCode() );
		copy.setSqlType( getSqlType() );
		copy.getSize().setLength( getSize().getLength() );
		copy.getSize().setPrecision( getSize().getPrecision() );
		copy.getSize().setScale( getSize().getScale() );
		copy.getSize().setLobMultiplier( getSize().getLobMultiplier() );
		copy.setNullable( isNullable() );
		copy.setUnique( isUnique() );
		copy.setCheckConstraint( getCheckConstraint() );
		copy.setComment( getComment() );
		copy.setDefaultValue( getDefaultValue() );
		copy.setCustomRead( getCustomRead() );
		copy.setCustomWrite( getCustomWrite() );

		//usually useless
		copy.uniqueInteger = uniqueInteger;

		return copy;
	}

}
