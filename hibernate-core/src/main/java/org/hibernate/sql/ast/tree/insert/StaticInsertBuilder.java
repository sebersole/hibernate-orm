/*
 * Hibernate, Relational Persistence for Idiomatic Java
 *
 * License: GNU Lesser General Public License (LGPL), version 2.1 or later.
 * See the lgpl.txt file in the root directory or http://www.gnu.org/licenses/lgpl-2.1.html.
 */
package org.hibernate.sql.ast.tree.insert;

import java.util.ArrayList;
import java.util.List;

/**
 * Used as a "static template" for building insert-statements when creating persisters.
 * This is used for entities mapped as non-dynamic (the default).
 *
 * @apiNote In the context of how other statements are defined, this really should be
 * `InsertStatement.  It is a mistake to keep values on {@link InsertStatement}.  These
 * statement objects should really just model the SQL *string* as a static thing.  Unfortunately
 * the addition of {@link InsertStatement#getValuesList()} violates that paradigm.
 *
 * @author Steve Ebersole
 * @author Andrea Boriero
 */
public class StaticInsertBuilder {
	private final String targetTableName;
	private final List<String> targetColumns = new ArrayList<>();

	public StaticInsertBuilder(String targetTableName) {
		this.targetTableName = targetTableName;
	}

	public String getTargetTableName() {
		return targetTableName;
	}

	public List<String> getTargetColumns() {
		return targetColumns;
	}

	public void addTargetColumn(String column) {
		targetColumns.add( column );
	}
}
