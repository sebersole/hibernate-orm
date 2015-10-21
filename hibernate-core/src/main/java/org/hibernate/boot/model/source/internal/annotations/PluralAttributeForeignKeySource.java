/*
 * Hibernate, Relational Persistence for Idiomatic Java
 *
 * License: GNU Lesser General Public License (LGPL), version 2.1 or later.
 * See the lgpl.txt file in the root directory or <http://www.gnu.org/licenses/lgpl-2.1.html>.
 */
package org.hibernate.boot.model.source.internal.annotations;

/**
 * Describes the source mapping of plural-attribute (collection) foreign-key information.  This is
 * the foreign-key back to the owner (not the map-key for a map!).
 *
 * @author Steve Ebersole
 */
public interface PluralAttributeForeignKeySource
		extends ForeignKeyContributingSource, RelationalValueSourceContainer {
}
