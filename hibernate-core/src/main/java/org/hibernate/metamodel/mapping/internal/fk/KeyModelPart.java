/*
 * Hibernate, Relational Persistence for Idiomatic Java
 *
 * License: GNU Lesser General Public License (LGPL), version 2.1 or later
 * See the lgpl.txt file in the root directory or http://www.gnu.org/licenses/lgpl-2.1.html
 */
package org.hibernate.metamodel.mapping.internal.fk;

import java.util.function.BiConsumer;
import java.util.function.Consumer;

import org.hibernate.metamodel.mapping.VirtualModelPart;
import org.hibernate.sql.results.graph.Fetchable;
import org.hibernate.type.ForeignKeyDirection;

/**
 * Generalized contract for describing one side of an association foreign-key
 * as a virtual model-part.
 *
 * @author Steve Ebersole
 */
public interface KeyModelPart extends VirtualModelPart, Fetchable {
	/**
	 * Get the descriptor for the associated foreign-key
	 */
	ForeignKey getForeignKeyDescriptor();

	/**
	 * todo (6.0) : use white-board pattern here as well to move the underlying list
	 * 		to MappingModelCreationProcess like we've done for identifier and attribute
	 * 		initialization listeners
	 */
	void registerForeignKeyInitializationListener(Consumer<ForeignKey> listener);

	/**
	 * The direction of the foreign-key that this side represents
	 */
	ForeignKeyDirection getDirection();

	/**
	 * The model-part that represents this side of the foreign-key in the
	 * domain model.
	 */
	ForeignKeySource getMappedModelPart();
}
