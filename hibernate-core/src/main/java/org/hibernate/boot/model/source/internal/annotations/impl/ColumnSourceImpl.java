/*
 * Hibernate, Relational Persistence for Idiomatic Java
 *
 * License: GNU Lesser General Public License (LGPL), version 2.1 or later.
 * See the lgpl.txt file in the root directory or <http://www.gnu.org/licenses/lgpl-2.1.html>.
 */

/*
 * Hibernate, Relational Persistence for Idiomatic Java
 *
 * Copyright (c) 2011, Red Hat Inc. or third-party contributors as
 * indicated by the @author tags or express copyright attribution
 * statements applied by the authors.  All third-party contributions are
 * distributed under license by Red Hat Inc.
 *
 * This copyrighted material is made available to anyone wishing to use, modify,
 * copy, or redistribute it subject to the terms and conditions of the GNU
 * Lesser General Public License, as published by the Free Software Foundation.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY
 * or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU Lesser General Public License
 * for more details.
 *
 * You should have received a copy of the GNU Lesser General Public License
 * along with this distribution; if not, write to:
 * Free Software Foundation, Inc.
 * 51 Franklin Street, Fifth Floor
 * Boston, MA  02110-1301  USA
 */
package org.hibernate.boot.model.source.internal.annotations.impl;

import org.hibernate.boot.model.TruthValue;
import org.hibernate.boot.model.source.internal.annotations.ColumnSource;
import org.hibernate.boot.model.source.internal.annotations.metadata.attribute.AbstractPersistentAttribute;
import org.hibernate.boot.model.source.internal.annotations.metadata.attribute.BasicAttribute;
import org.hibernate.boot.model.source.internal.annotations.metadata.attribute.Column;
import org.hibernate.boot.model.source.internal.annotations.metadata.attribute.PersistentAttribute;
import org.hibernate.boot.model.source.internal.hbm.SizeSourceImpl;
import org.hibernate.boot.model.source.spi.JdbcDataType;
import org.hibernate.boot.model.source.spi.SizeSource;

/**
 * @author Hardy Ferentschik
 */
public class ColumnSourceImpl implements ColumnSource {
	private final Column columnValues;
	private final String defaultTableName;
	private final String readFragment;
	private final String writeFragment;
	private final String checkCondition;

	public ColumnSourceImpl(Column columnValues) {
		this( columnValues, null );
	}

	public ColumnSourceImpl(Column columnValues, String defaultTableName) {
		this( columnValues, defaultTableName, null, null, null );
	}

	public ColumnSourceImpl(
			Column columnValues,
			String defaultTableName,
			String readFragment,
			String writeFragment,
			String checkCondition) {
		this.columnValues = columnValues;
		this.defaultTableName = defaultTableName;
		this.readFragment = readFragment;
		this.writeFragment = writeFragment;
		this.checkCondition = checkCondition;
	}

	public ColumnSourceImpl(AbstractPersistentAttribute attribute, Column columnValues) {
		this( attribute, columnValues, null );
	}

	public ColumnSourceImpl(AbstractPersistentAttribute attribute, Column columnValues, String defaultTableName) {
		boolean isBasicAttribute = attribute != null && attribute.getAttributeNature() == PersistentAttribute.AttributeNature.BASIC;
		this.readFragment = attribute != null && isBasicAttribute ? ( (BasicAttribute) attribute ).getCustomReadFragment() : null;
		this.writeFragment = attribute != null && isBasicAttribute ? ( (BasicAttribute) attribute ).getCustomWriteFragment() : null;
		this.checkCondition = attribute != null ? attribute.getCheckCondition() : null;
		this.columnValues = columnValues;
		this.defaultTableName = defaultTableName;
	}

	@Override
	public Nature getNature() {
		return Nature.COLUMN;
	}

	@Override
	public String getName() {
		return columnValues == null ? null : columnValues.getName();
	}

	@Override
	public TruthValue isNullable() {
		if ( columnValues == null || columnValues.isNullable() == null ) {
			return null;
		}
		return columnValues.isNullable() ? TruthValue.TRUE : TruthValue.FALSE;
	}

	@Override
	public String getDefaultValue() {
		return null;
	}

	@Override
	public String getSqlType() {
		if ( columnValues == null ) {
			return null;
		}
		return columnValues.getColumnDefinition();
	}

	@Override
	public JdbcDataType getDatatype() {
		return null;
	}

	@Override
	public SizeSource getSizeSource() {
		if ( columnValues == null ) {
			return null;
		}
		return new SizeSourceImpl(
				columnValues.getPrecision(), columnValues.getScale(), columnValues.getLength()
		);
	}

	@Override
	public boolean isUnique() {
		return columnValues != null && columnValues.isUnique() != null && columnValues.isUnique();
	}

	@Override
	public String getComment() {
		return null;
	}

	@Override
	public String getContainingTableName() {
		if ( columnValues == null ) {
			return defaultTableName;
		}
		else if ( columnValues.getTable() == null ) {
			return defaultTableName;
		}
		else {
			return columnValues.getTable();
		}
	}

	// these come from attribute ~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~

	@Override
	public String getReadFragment() {
		return readFragment;
	}

	@Override
	public String getWriteFragment() {
		return writeFragment;
	}

	@Override
	public String getCheckCondition() {
		return checkCondition;
	}
}


