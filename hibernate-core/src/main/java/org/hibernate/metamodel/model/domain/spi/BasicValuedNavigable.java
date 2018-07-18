/*
 * Hibernate, Relational Persistence for Idiomatic Java
 *
 * License: GNU Lesser General Public License (LGPL), version 2.1 or later
 * See the lgpl.txt file in the root directory or http://www.gnu.org/licenses/lgpl-2.1.html
 */
package org.hibernate.metamodel.model.domain.spi;

import org.hibernate.metamodel.model.relational.spi.Column;
import org.hibernate.sql.ast.produce.metamodel.spi.BasicValuedExpressableType;
import org.hibernate.sql.ast.produce.spi.ColumnReferenceQualifier;
import org.hibernate.sql.results.spi.SqlSelectionGroupNode;
import org.hibernate.sql.results.spi.SqlSelectionResolutionContext;
import org.hibernate.type.descriptor.java.spi.BasicJavaDescriptor;
import org.hibernate.type.spi.BasicType;

/**
 * @author Steve Ebersole
 */
public interface BasicValuedNavigable<J> extends BasicValuedExpressableType<J>, Navigable<J> {
	Column getBoundColumn();

	BasicType<J> getBasicType();

	@Override
	default BasicJavaDescriptor<J> getJavaTypeDescriptor() {
		return getBasicType().getJavaTypeDescriptor();
	}

	@Override
	default SqlSelectionGroupNode resolveSqlSelections(
			ColumnReferenceQualifier qualifier,
			SqlSelectionResolutionContext resolutionContext) {
		return resolutionContext.getSqlSelectionResolver().resolveSqlSelection(
				resolutionContext.getSqlSelectionResolver().resolveSqlExpression(
						qualifier,
						getBoundColumn()
				),
				getBasicType().getJavaTypeDescriptor()
				,
				resolutionContext.getSessionFactory().getTypeConfiguration()
		);
	}
}
