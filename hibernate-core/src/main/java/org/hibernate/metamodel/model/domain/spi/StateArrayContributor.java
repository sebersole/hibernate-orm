/*
 * Hibernate, Relational Persistence for Idiomatic Java
 *
 * License: GNU Lesser General Public License (LGPL), version 2.1 or later
 * See the lgpl.txt file in the root directory or http://www.gnu.org/licenses/lgpl-2.1.html
 */
package org.hibernate.metamodel.model.domain.spi;

import java.util.List;
import java.util.Map;

import org.hibernate.NotYetImplementedFor6Exception;
import org.hibernate.annotations.OptimisticLockType;
import org.hibernate.engine.internal.ForeignKeys;
import org.hibernate.engine.internal.NonNullableTransientDependencies;
import org.hibernate.engine.spi.SessionImplementor;
import org.hibernate.engine.spi.SharedSessionContractImplementor;
import org.hibernate.metamodel.model.relational.spi.Column;
import org.hibernate.property.access.spi.PropertyAccess;
import org.hibernate.sql.ast.produce.metamodel.spi.ExpressableType;
import org.hibernate.type.ForeignKeyDirection;
import org.hibernate.type.descriptor.java.MutabilityPlan;

/**
 * Optional contract for a Navigable that can be part of it's container's
 * state array.
 *
 * @author Steve Ebersole
 */
public interface StateArrayContributor<J> extends Navigable<J>, ExpressableType<J>, Readable, Writeable {
	/**
	 * Defines this contributor's position within the state array.
	 *
	 * @apiNote Much like {@link PersistentAttribute#getAttributePosition()}, this
	 * position follows a pre-defined algorithm based on alphabetical order, super
	 * types first.  Note however, that this ordering is only important internally
	 * as Hibernate builds the container's "state array" as part of EntityEntry,
	 * actions, cache entries, etc
	 */
	int getStateArrayPosition();

	void setStateArrayPosition(int position);

	default List<Column> getColumns() {
		throw new NotYetImplementedFor6Exception( getClass().getName() );
	}

	PropertyAccess getPropertyAccess();

	/**
	 * Is this value nullable?
	 */
	boolean isNullable();

	/**
	 * Is this value used in SQL INSERT statements?
	 */
	boolean isInsertable();

	/**
	 * Is this value used in SQL UPDATE statements?
	 */
	boolean isUpdatable();

	/**
	 * Should this Navigable be dirty checked to determine whether its
	 * container is dirty?
	 */
	boolean isIncludedInDirtyChecking();

	/**
	 * For {@link OptimisticLockType#DIRTY} or {@link OptimisticLockType#ALL} style
	 * optimistic locking, should this Navigable be part of its container's optimistic
	 * locking?
	 */
	boolean isIncludedInOptimisticLocking();

	MutabilityPlan<J> getMutabilityPlan();

	@Override
	default Class<J> getJavaType() {
		return getJavaTypeDescriptor().getJavaType();
	}

	default Object replace(
			J originalValue,
			J targetValue,
			Object owner,
			Map copyCache,
			SessionImplementor session) {
		throw new NotYetImplementedFor6Exception();
	}

	default Object replace(
			Object originalValue,
			Object targetValue,
			Object owner,
			Map copyCache,
			ForeignKeyDirection foreignKeyDirection,
			SessionImplementor session) {
		throw new NotYetImplementedFor6Exception();
	}

	default boolean isDirty(
			Object originalValue,
			Object currentValue,
			SharedSessionContractImplementor session) {
		throw new NotYetImplementedFor6Exception( "Not yet implemented for " + this.getClass().getName() );
	}

	void collectNonNullableTransientEntities(
			Object value,
			ForeignKeys.Nullifier nullifier,
			NonNullableTransientDependencies nonNullableTransientEntities,
			SharedSessionContractImplementor session);

}
