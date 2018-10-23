/*
 * Hibernate, Relational Persistence for Idiomatic Java
 *
 * License: GNU Lesser General Public License (LGPL), version 2.1 or later
 * See the lgpl.txt file in the root directory or http://www.gnu.org/licenses/lgpl-2.1.html
 */
package org.jboss.perf;

import org.hibernate.engine.spi.SessionFactoryImplementor;
import org.hibernate.query.sqm.produce.internal.hql.HqlParseTreeBuilder;
import org.hibernate.query.sqm.produce.internal.hql.SemanticQueryBuilder;
import org.hibernate.query.sqm.produce.internal.hql.grammar.HqlParser;

/**
 * @author Andrea Boriero
 */
public class ORM6HQLSemanticModelInterpreter extends AbstractBenchmarkHQLSemanticModelInterpreter {

	@Override
	public Object getSemanticModel(String hqlString) {
		HqlParser.StatementContext statementContext = HqlParseTreeBuilder.INSTANCE.parseHql( hqlString ).statement();
		return SemanticQueryBuilder.buildSemanticModel( statementContext,
														(SessionFactoryImplementor) entityManagerFactory
		);
	}
}
