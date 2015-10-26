/*
 * Hibernate, Relational Persistence for Idiomatic Java
 *
 * License: GNU Lesser General Public License (LGPL), version 2.1 or later.
 * See the lgpl.txt file in the root directory or <http://www.gnu.org/licenses/lgpl-2.1.html>.
 */
package org.hibernate.boot.model.source.internal.annotations;

import java.sql.Types;

import org.hibernate.boot.model.TruthValue;
import org.hibernate.boot.model.source.internal.annotations.metadata.type.EntityTypeMetadata;
import org.hibernate.boot.model.source.spi.ColumnSource;
import org.hibernate.engine.jdbc.JdbcDataType;
import org.hibernate.boot.model.source.spi.SizeSource;

/**
 * @author Steve Ebersole
 */
public class ImplicitDiscriminatorColumnSource implements ColumnSource {
	// The implicit column name per JPA spec
	private static final String IMPLICIT_COLUMN_NAME = "DTYPE";

	// The implicit column type per JPA spec
	private final JdbcDataType IMPLICIT_DATA_TYPE = new JdbcDataType(
			Types.VARCHAR,
			"varchar",
			String.class
	);

	private final String columnName;
	private final String comment;

	public ImplicitDiscriminatorColumnSource(EntityTypeMetadata entityTypeMetadata) {
		this.columnName = entityTypeMetadata.getLocalBindingContext().getMappingDefaults().getImplicitDiscriminatorColumnName();
		this.comment = "Discriminator value for " + entityTypeMetadata.getName();
	}

	@Override
	public String getContainingTableName() {
		// null indicates primary table
		return null;
	}

	@Override
	public Nature getNature() {
		return Nature.COLUMN;
	}

	@Override
	public String getName() {
		return columnName;
	}

	@Override
	public String getReadFragment() {
		return null;
	}

	@Override
	public String getWriteFragment() {
		return null;
	}

	@Override
	public TruthValue isNullable() {
		// discriminators should not be nullable
		return TruthValue.FALSE;
	}

	@Override
	public String getDefaultValue() {
		return null;
	}

	@Override
	public String getSqlType() {
		return null;
	}

	@Override
	public JdbcDataType getDatatype() {
		return IMPLICIT_DATA_TYPE;
	}

	@Override
	public SizeSource getSizeSource() {
		return null;
	}

	@Override
	public boolean isUnique() {
		return false;
	}

	@Override
	public String getCheckCondition() {
		return null;
	}

	@Override
	public String getComment() {
		return comment;
	}
}
