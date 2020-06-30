/*
 * Hibernate, Relational Persistence for Idiomatic Java
 *
 * License: GNU Lesser General Public License (LGPL), version 2.1 or later
 * See the lgpl.txt file in the root directory or http://www.gnu.org/licenses/lgpl-2.1.html
 */
package org.hibernate.metamodel.mapping.internal.fk;

import java.util.Collections;
import java.util.List;

import org.hibernate.metamodel.mapping.ColumnConsumer;
import org.hibernate.metamodel.mapping.JdbcMapping;

/**
 * Specialization of Side for single column FKs
 *
 * @author Steve Ebersole
 */
public interface SideBasic extends Side {
	String getColumn();

	JdbcMapping getJdbcMapping();

	@Override
	ForeignKeyBasic getForeignKey();

	@Override
	default List<String> getColumnNames() {
		return Collections.singletonList( getColumn() );
	}

	@Override
	default void visitColumns(ColumnConsumer columnConsumer) {
		columnConsumer.accept( getTableName(), getColumn(), getJdbcMapping() );
	}

	@Override
	default List<JdbcMapping> getJdbcMappings() {
		return Collections.singletonList( getJdbcMapping() );
	}
}
