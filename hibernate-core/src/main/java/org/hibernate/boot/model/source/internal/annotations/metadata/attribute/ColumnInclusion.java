/*
 * Hibernate, Relational Persistence for Idiomatic Java
 *
 * License: GNU Lesser General Public License (LGPL), version 2.1 or later.
 * See the lgpl.txt file in the root directory or <http://www.gnu.org/licenses/lgpl-2.1.html>.
 */
package org.hibernate.boot.model.source.internal.annotations.metadata.attribute;

/**
 * Helper for determining insertability/updateability of an attribute's columns.
 * <p/>
 * By default an attribute's columns are considered writable.  Cases which
 * indicate it is not writable should call {@link #disable}.
 * <p/>
 * Additionally inclusion can be completely disabled up front via the boolean
 * argument to the constructor to force non-inclusion from containing contexts.
 * Makes it easier to process.
 *
 * @author Steve Ebersole
 */
public class ColumnInclusion {
	private final boolean canBeIncluded;
	private boolean included = true;

	/**
	 * Creates the inclusion helper.
	 *
	 * @param canBeIncluded {@code false} here indicates that the inclusion can
	 * never be included
	 */
	public ColumnInclusion(boolean canBeIncluded) {
		this.canBeIncluded = canBeIncluded;
	}

	public void disable() {
		this.included = false;
	}

	public boolean shouldInclude() {
		return canBeIncluded && included;

	}
}
