/*
 * Hibernate, Relational Persistence for Idiomatic Java
 *
 * License: GNU Lesser General Public License (LGPL), version 2.1 or later.
 * See the lgpl.txt file in the root directory or http://www.gnu.org/licenses/lgpl-2.1.html.
 */
package org.hibernate.sql.group;

/**
 * @author Steve Ebersole
 */
public enum MutationType {
	INSERT( TableInsert.class ),
	UPDATE( TableUpdate.class ),
	DELETE( TableDelete.class );

	private final Class<? extends TableMutation> tableMutationClass;
	private final boolean canSkipTables;

	MutationType(Class<? extends TableMutation> tableMutationClass) {
		this.tableMutationClass = tableMutationClass;
		this.canSkipTables = TableInsert.class.equals( tableMutationClass )
				|| TableUpdate.class.isAssignableFrom( tableMutationClass );
	}

	public Class<? extends TableMutation> getTableMutationClass() {
		return tableMutationClass;
	}

	public boolean canSkipTables() {
		return canSkipTables;
	}
}
