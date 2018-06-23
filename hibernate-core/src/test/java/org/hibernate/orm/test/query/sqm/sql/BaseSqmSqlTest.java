/*
 * Hibernate, Relational Persistence for Idiomatic Java
 *
 * License: GNU Lesser General Public License (LGPL), version 2.1 or later
 * See the lgpl.txt file in the root directory or http://www.gnu.org/licenses/lgpl-2.1.html
 */
package org.hibernate.orm.test.query.sqm.sql;

import java.util.Collections;

import org.hibernate.engine.spi.SharedSessionContractImplementor;
import org.hibernate.orm.test.query.sqm.BaseSqmUnitTest;
import org.hibernate.query.sqm.tree.SqmSelectStatement;
import org.hibernate.sql.ast.consume.spi.SqlAstSelectToJdbcSelectConverter;
import org.hibernate.sql.ast.produce.spi.SqlAstBuildingContext;
import org.hibernate.sql.ast.produce.spi.SqlAstSelectDescriptor;
import org.hibernate.sql.ast.produce.sqm.spi.SqmSelectToSqlAstConverter;
import org.hibernate.sql.exec.spi.JdbcSelect;

/**
 * @author Steve Ebersole
 */
public abstract class BaseSqmSqlTest extends BaseSqmUnitTest {

	protected JdbcSelect buildJdbcSelect(
			String hql,
			SqlAstBuildingContext sqlAstBuildingContext) {

		final SqmSelectStatement sqm = interpretSelect( hql );

		final SqmSelectToSqlAstConverter sqmConverter = new SqmSelectToSqlAstConverter( sqlAstBuildingContext, Collections.emptyMap() );

		final SqlAstSelectDescriptor interpretation = sqmConverter.interpret( sqm );

		return SqlAstSelectToJdbcSelectConverter.interpret(
				interpretation,
				sqlAstBuildingContext.getSessionFactory()
		);
	}

	private SqlAstBuildingContext createSqlAstCreationContext(SharedSessionContractImplementor session) {
		return null;
	}
}
