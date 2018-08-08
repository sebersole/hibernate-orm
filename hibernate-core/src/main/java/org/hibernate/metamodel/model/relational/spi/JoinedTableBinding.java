/*
 * Hibernate, Relational Persistence for Idiomatic Java
 *
 * License: GNU Lesser General Public License (LGPL), version 2.1 or later
 * See the lgpl.txt file in the root directory or http://www.gnu.org/licenses/lgpl-2.1.html
 */
package org.hibernate.metamodel.model.relational.spi;

/**
 * Used to represent both secondary tables and table joins used in joined inheritance.
 *
 * @author Steve Ebersole
 */
public class JoinedTableBinding {
	private final Table referringTable;
	private final Table targetTable;
	private final ForeignKey joinForeignKey;

	private final boolean optional;
	private final boolean inverse;

	public JoinedTableBinding(
			Table referringTable,
			Table targetTable,
			ForeignKey joinForeignKey,
			boolean optional,
			boolean inverse) {
		this.referringTable = referringTable;
		this.targetTable = targetTable;
		this.joinForeignKey = joinForeignKey;
		this.optional = optional;
		this.inverse = inverse;
	}

	public Table getReferringTable() {
		return referringTable;
	}

	public Table getTargetTable() {
		return targetTable;
	}

	public ForeignKey getJoinForeignKey() {
		return joinForeignKey;
	}

	public boolean isOptional() {
		return optional;
	}

	public boolean isInverse() {
		return inverse;
	}
}
