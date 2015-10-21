/*
 * Hibernate, Relational Persistence for Idiomatic Java
 *
 * License: GNU Lesser General Public License (LGPL), version 2.1 or later.
 * See the lgpl.txt file in the root directory or <http://www.gnu.org/licenses/lgpl-2.1.html>.
 */
package org.hibernate.boot.model.source.internal.annotations.impl;

import java.util.Collections;
import java.util.List;

import org.hibernate.boot.model.source.internal.annotations.UniqueConstraintSource;

/**
 * @author Hardy Ferentschik
 */
class UniqueConstraintSourceImpl extends AbstractConstraintSource implements UniqueConstraintSource {
	
	public UniqueConstraintSourceImpl(String name, String tableName, List<String> columnNames, List<String> orderings) {
		super( name, tableName, columnNames, orderings );
	}
	
	public UniqueConstraintSourceImpl(String name, String tableName, List<String> columnNames) {
		super( name, tableName, columnNames, Collections.EMPTY_LIST );
	}

	@Override
	public String toString() {
		final StringBuilder sb = new StringBuilder();
		sb.append( "UniqueConstraintSourceImpl" );
		sb.append( "{name='" ).append( name ).append( '\'' );
		sb.append( ", tableName='" ).append( tableName ).append( '\'' );
		sb.append( ", columnNames=" ).append( columnNames );
		sb.append( ", orderings=" ).append( orderings );
		sb.append( '}' );
		return sb.toString();
	}
}


