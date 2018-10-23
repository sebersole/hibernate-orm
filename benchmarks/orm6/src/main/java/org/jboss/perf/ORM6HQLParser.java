/*
 * Hibernate, Relational Persistence for Idiomatic Java
 *
 * License: GNU Lesser General Public License (LGPL), version 2.1 or later
 * See the lgpl.txt file in the root directory or http://www.gnu.org/licenses/lgpl-2.1.html
 */
package org.jboss.perf;

import org.hibernate.query.sqm.produce.internal.hql.HqlParseTreeBuilder;

/**
 * @author Andrea Boriero
 */
public class ORM6HQLParser implements BenchmarkHQLParserTree {

	@Override
	public Object getHQLParser(String hqlString) {
		return HqlParseTreeBuilder.INSTANCE.parseHql( hqlString );
	}
}