/*
 * Hibernate, Relational Persistence for Idiomatic Java
 *
 * License: GNU Lesser General Public License (LGPL), version 2.1 or later
 * See the lgpl.txt file in the root directory or http://www.gnu.org/licenses/lgpl-2.1.html
 */
package org.hibernate.sql;

import org.hibernate.type.descriptor.java.spi.BasicJavaDescriptor;
import org.hibernate.type.descriptor.sql.spi.SqlTypeDescriptor;

/**
 * Models the type of a thing that can be used in a SQL query
 *
 * todo (6.0) : come up with a (much) better name : SqlExpressableType?
 *
 * @author Steve Ebersole
 */
public interface JdbcValueMapper {
	BasicJavaDescriptor getJavaTypeDescriptor();
	SqlTypeDescriptor getSqlTypeDescriptor();

	JdbcValueExtractor getJdbcValueExtractor();
	JdbcValueBinder getJdbcValueBinder();
}
