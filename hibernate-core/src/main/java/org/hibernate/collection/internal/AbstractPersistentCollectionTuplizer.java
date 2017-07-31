/*
 * Hibernate, Relational Persistence for Idiomatic Java
 *
 * License: GNU Lesser General Public License (LGPL), version 2.1 or later.
 * See the lgpl.txt file in the root directory or <http://www.gnu.org/licenses/lgpl-2.1.html>.
 */
package org.hibernate.collection.internal;

import java.util.Collection;
import java.util.Iterator;

import org.hibernate.cfg.NotYetImplementedException;
import org.hibernate.collection.spi.PersistentCollection;
import org.hibernate.collection.spi.PersistentCollectionTuplizer;
import org.hibernate.mapping.Property;
import org.hibernate.metamodel.model.creation.spi.RuntimeModelCreationContext;
import org.hibernate.metamodel.model.domain.spi.ManagedTypeDescriptor;
import org.hibernate.metamodel.model.domain.spi.PluralPersistentAttribute;
import org.hibernate.proxy.HibernateProxy;
import org.hibernate.proxy.LazyInitializer;

/**
 * @author Andrea Boriero
 */
public abstract class AbstractPersistentCollectionTuplizer<T extends PersistentCollection>
		implements PersistentCollectionTuplizer<T> {

	@Override
	public boolean contains(Object collection, Object childObject) {
		Iterator elems = getElementsIterator( collection );
		while ( elems.hasNext() ) {
			Object element = elems.next();
			// worrying about proxies is perhaps a little bit of overkill here...
			if ( element instanceof HibernateProxy ) {
				LazyInitializer li = ( (HibernateProxy) element ).getHibernateLazyInitializer();
				if ( !li.isUninitialized() ) {
					element = li.getImplementation();
				}
			}
			if ( element == childObject ) {
				return true;
			}
		}
		return false;
	}

	protected Iterator getElementsIterator(Object collection) {
		return ( (Collection) collection ).iterator();
	}

	@Override
	public PluralPersistentAttribute generatePluralPersistentAttribute(
			ManagedTypeDescriptor container, Property property, RuntimeModelCreationContext context) {
		throw new NotYetImplementedException( "SortedSetTypeTuplizer#generatePluralPersistentAttribute" );
	}
}
