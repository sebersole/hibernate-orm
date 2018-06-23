/*
 * Hibernate, Relational Persistence for Idiomatic Java
 *
 * License: GNU Lesser General Public License (LGPL), version 2.1 or later
 * See the lgpl.txt file in the root directory or http://www.gnu.org/licenses/lgpl-2.1.html
 */
package org.hibernate.sql.ast.produce.spi;

import org.hibernate.annotations.Remove;
import org.hibernate.sql.JdbcValueBinder;
import org.hibernate.sql.JdbcValueExtractor;
import org.hibernate.sql.JdbcValueMapper;
import org.hibernate.type.descriptor.java.spi.BasicJavaDescriptor;
import org.hibernate.type.descriptor.sql.spi.SqlTypeDescriptor;

/**
 * Unifying contract for things that are capable of being an expression at
 * the SQL level.
 *
 * Such an expressable can also be part of the SQL select-clause
 *
 * @author Steve Ebersole
 */
public interface SqlExpressable<J> {
	JdbcValueMapper getJdbcValueMapper();

	@Deprecated
	@Remove
	default BasicJavaDescriptor<J> getJavaTypeDescriptor() {
		return getJdbcValueMapper().getJavaTypeDescriptor();
	}

	@Deprecated
	@Remove
	default SqlTypeDescriptor getSqlTypeDescriptor() {
		return getJdbcValueMapper().getSqlTypeDescriptor();
	}

	@Deprecated
	@Remove
	default JdbcValueBinder<J> getJdbcValueBinder() {
		return getJdbcValueMapper().getJdbcValueBinder();
	}

	@Deprecated
	@Remove
	default JdbcValueExtractor<J> getJdbcValueExtractor() {
		return getJdbcValueMapper().getJdbcValueExtractor();
	}
}
