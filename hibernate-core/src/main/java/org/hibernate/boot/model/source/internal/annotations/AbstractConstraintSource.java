/*
 * Hibernate, Relational Persistence for Idiomatic Java
 *
 * License: GNU Lesser General Public License (LGPL), version 2.1 or later.
 * See the lgpl.txt file in the root directory or <http://www.gnu.org/licenses/lgpl-2.1.html>.
 */
package org.hibernate.boot.model.source.internal.annotations;

import java.util.List;

import org.hibernate.boot.model.source.spi.ConstraintSource;


/**
 * @author Hardy Ferentschik
 */
class AbstractConstraintSource implements ConstraintSource {
	protected final String name;
	protected final String tableName;
	protected final List<String> columnNames;
	protected final List<String> orderings;

	protected AbstractConstraintSource(String name, String tableName, List<String> columnNames, List<String> orderings) {
		this.name = name;
		this.tableName = tableName;
		this.columnNames = columnNames;
		this.orderings = orderings;
	}

	@Override
	public String name() {
		return name;
	}

	@Override
	public String getTableName() {
		return tableName;
	}

	@Override
	public List<String> columnNames() {
		return columnNames;
	}
	
	public List<String> orderings() {
		return orderings;
	}

	@Override
	public boolean equals(Object o) {
		if ( this == o ) {
			return true;
		}
		if ( o == null || getClass() != o.getClass() ) {
			return false;
		}

		AbstractConstraintSource that = (AbstractConstraintSource) o;

		if ( columnNames != null ? !columnNames.equals( that.columnNames ) : that.columnNames != null ) {
			return false;
		}
		if ( orderings != null ? !orderings.equals( that.orderings ) : that.orderings != null ) {
			return false;
		}
		if ( name != null ? !name.equals( that.name ) : that.name != null ) {
			return false;
		}
		if ( tableName != null ? !tableName.equals( that.tableName ) : that.tableName != null ) {
			return false;
		}

		return true;
	}

	@Override
	public int hashCode() {
		int result = name != null ? name.hashCode() : 0;
		result = 31 * result + ( tableName != null ? tableName.hashCode() : 0 );
		result = 31 * result + ( columnNames != null ? columnNames.hashCode() : 0 );
		result = 31 * result + ( orderings != null ? orderings.hashCode() : 0 );
		return result;
	}
}


