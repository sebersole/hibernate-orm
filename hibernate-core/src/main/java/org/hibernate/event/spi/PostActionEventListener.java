package org.hibernate.event.spi;

import java.io.Serializable;

import org.hibernate.metamodel.model.domain.spi.EntityDescriptor;

/**
 * @author Andrea Boriero
 */
public interface PostActionEventListener extends Serializable {
	/**
	 * Does this listener require that after transaction hooks be registered?
	 *
	 * @param persister The persister for the entity in question.
	 *
	 * @return {@code true} if after transaction callbacks should be added.
	 */
	boolean requiresPostCommitHandling(EntityDescriptor persister);
}

