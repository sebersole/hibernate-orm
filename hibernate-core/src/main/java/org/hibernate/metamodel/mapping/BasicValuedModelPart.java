/*
 * Hibernate, Relational Persistence for Idiomatic Java
 *
 * License: GNU Lesser General Public License (LGPL), version 2.1 or later
 * See the lgpl.txt file in the root directory or http://www.gnu.org/licenses/lgpl-2.1.html
 */
package org.hibernate.metamodel.mapping;

import java.util.Collections;
import java.util.List;

import org.hibernate.sql.results.graph.Fetchable;
import org.hibernate.type.descriptor.java.JavaTypeDescriptor;
import org.hibernate.type.spi.TypeConfiguration;

/**
 * @author Steve Ebersole
 */
public interface BasicValuedModelPart extends BasicValuedMapping, ModelPart, Fetchable {
	/**
	 * The table expression (table name or subselect) that contains
	 * the {@linkplain #getMappedColumnExpression mapped column}
	 */
	String getContainingTableExpression();

	/**
	 * The column expression (column name or formula) to which this basic value
	 * is mapped
	 */
	String getMappedColumnExpression();

	@Override
	default void visitColumns(ColumnConsumer consumer) {
		consumer.accept( getContainingTableExpression(), getMappedColumnExpression(), getJdbcMapping() );
	}

	@Override
	default List<JdbcMapping> getJdbcMappings(TypeConfiguration typeConfiguration) {
		return Collections.singletonList( getJdbcMapping() );
	}

	@Override
	default JavaTypeDescriptor<?> getJavaTypeDescriptor() {
		return getJdbcMapping().getJavaTypeDescriptor();
	}

	@Override
	JdbcMapping getJdbcMapping();

	@Override
	default MappingType getPartMappingType() {
		return this::getJavaTypeDescriptor;
	}
}
