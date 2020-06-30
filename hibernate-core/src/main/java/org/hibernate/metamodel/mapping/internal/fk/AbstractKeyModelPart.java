/*
 * Hibernate, Relational Persistence for Idiomatic Java
 *
 * License: GNU Lesser General Public License (LGPL), version 2.1 or later
 * See the lgpl.txt file in the root directory or http://www.gnu.org/licenses/lgpl-2.1.html
 */
package org.hibernate.metamodel.mapping.internal.fk;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Consumer;
import java.util.function.Supplier;

import org.hibernate.metamodel.mapping.ToOneAttributeMapping;
import org.hibernate.metamodel.mapping.internal.MappingModelCreationProcess;
import org.hibernate.metamodel.model.domain.NavigableRole;
import org.hibernate.type.ForeignKeyDirection;

/**
 * Base support for KeyModelPart implementations
 *
 * @author Steve Ebersole
 */
public abstract class AbstractKeyModelPart implements KeyModelPart {
	private final NavigableRole navigableRole;

	private ForeignKey foreignKey;
	private ForeignKeyDirection foreignKeyDirection;

	private List<Consumer<ForeignKey>> listeners;

	public AbstractKeyModelPart(NavigableRole navigableRole) {
		this.navigableRole = navigableRole;
	}

	protected final void postConstruct(
			Supplier<ToOneAttributeMapping> mappedByAttributeAccess,
			Consumer<Consumer<ForeignKey>> foreignKeyGenerator,
			MappingModelCreationProcess creationProcess) {
		final ToOneAttributeMapping mappedByAttribute = mappedByAttributeAccess == null ? null : mappedByAttributeAccess.get();
		if ( mappedByAttribute != null ) {
			// we have mapped-by - use the FK descriptor from the mapped-by owning side
			mappedByAttribute.getKeyModelPart().registerForeignKeyInitializationListener( this::setForeignKey );

			return;
		}

		foreignKeyGenerator.accept( this::setForeignKey );
	}

	@Override
	public NavigableRole getNavigableRole() {
		return navigableRole;
	}

	@Override
	public void registerForeignKeyInitializationListener(Consumer<ForeignKey> listener) {
		if ( foreignKey != null ) {
			listener.accept( foreignKey );
		}
		else {
			if ( listeners == null ) {
				listeners = new ArrayList<>();
				listeners.add( listener );
			}
		}
	}

	@Override
	public ForeignKey getForeignKeyDescriptor() {
		return foreignKey;
	}

	@Override
	public ForeignKeyDirection getDirection() {
		return foreignKeyDirection;
	}

	protected void setForeignKey(ForeignKey foreignKey) {
		assert this.foreignKey == null;

		assert foreignKey != null;
		assert foreignKey.getReferringSide() != null;
		assert foreignKey.getReferringSide().getKeyPart() != null;
		assert foreignKey.getTargetSide() != null;
		assert foreignKey.getTargetSide().getKeyPart() != null;

		this.foreignKey = foreignKey;
		if ( foreignKey.getReferringSide().getKeyPart() == this ) {
			foreignKeyDirection = ForeignKeyDirection.REFERRING;
		}
		else {
			foreignKeyDirection = ForeignKeyDirection.TARGET;
		}

		if ( listeners != null ) {
			//noinspection ForLoopReplaceableByForEach
			for ( int i = 0; i < listeners.size(); i++ ) {
				listeners.get( i ).accept( foreignKey );
			}

			listeners.clear();
			listeners = null;
		}
	}
}
