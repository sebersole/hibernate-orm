/*
 * Hibernate, Relational Persistence for Idiomatic Java
 *
 * License: GNU Lesser General Public License (LGPL), version 2.1 or later.
 * See the lgpl.txt file in the root directory or <http://www.gnu.org/licenses/lgpl-2.1.html>.
 */
package org.hibernate.test.annotations.persister;

import org.hibernate.HibernateException;
import org.hibernate.cache.spi.access.EntityStorageAccess;
import org.hibernate.cache.spi.access.NaturalIdStorageAccess;
import org.hibernate.mapping.PersistentClass;
import org.hibernate.persister.entity.SingleTableEntityPersister;
import org.hibernate.persister.spi.PersisterCreationContext;

/**
 * @author Shawn Clowater
 */
public class EntityPersister extends SingleTableEntityPersister {
	public EntityPersister(
			PersistentClass persistentClass,
			EntityStorageAccess cache,
			NaturalIdStorageAccess naturalIdRegionAccess,
			PersisterCreationContext creationContext) throws HibernateException {
		super( persistentClass, cache, naturalIdRegionAccess, creationContext );
	}
}
