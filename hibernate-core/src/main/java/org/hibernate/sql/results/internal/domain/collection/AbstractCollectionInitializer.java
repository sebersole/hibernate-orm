/*
 * Hibernate, Relational Persistence for Idiomatic Java
 *
 * License: GNU Lesser General Public License (LGPL), version 2.1 or later
 * See the lgpl.txt file in the root directory or http://www.gnu.org/licenses/lgpl-2.1.html
 */
package org.hibernate.sql.results.internal.domain.collection;

import org.hibernate.HibernateException;
import org.hibernate.engine.spi.CollectionKey;
import org.hibernate.metamodel.model.domain.spi.PersistentCollectionDescriptor;
import org.hibernate.metamodel.model.domain.spi.PluralPersistentAttribute;
import org.hibernate.sql.results.spi.CollectionInitializer;
import org.hibernate.sql.results.spi.DomainResultAssembler;
import org.hibernate.sql.results.spi.FetchParentAccess;
import org.hibernate.sql.results.spi.RowProcessingState;

/**
 * Base support for CollectionInitializer implementations
 *
 * @author Steve Ebersole
 */
public abstract class AbstractCollectionInitializer implements CollectionInitializer {
	private final PersistentCollectionDescriptor collectionDescriptor;
	private final FetchParentAccess parentAccess;
	private final boolean isJoinFetch;

	// todo (6.0) : we may need an assembler for both the key on the owner side and the key on the collection side.
	//		* owner side : mainly needed in case the key for the collection side is
	// 			null (no collection elements for that row).  We still need to create
	//			the appropriate collection instance and register its entry in the PC
	//		* collection side : to determine the collection key
	//
	// 		- possible solution wrt "owner side" would be to expose a method from FetchParentAccess like:
	//			````
	//			Object getResolvedState(Navigable navigable)
	//			````

	// todo (6.0) : need to make sure that the fetch parent's initializer is registered before the collection initializer

	private final DomainResultAssembler keyAssembler;

	// per-row state
	private Object collectionFkValue;
	private CollectionKey collectionKey;


	@SuppressWarnings("WeakerAccess")
	protected AbstractCollectionInitializer(
			PersistentCollectionDescriptor collectionDescriptor,
			FetchParentAccess parentAccess,
			boolean isJoinFetch,
			DomainResultAssembler keyAssembler) {
		this.collectionDescriptor = collectionDescriptor;
		this.parentAccess = parentAccess;
		this.isJoinFetch = isJoinFetch;
		this.keyAssembler = keyAssembler;
	}

	protected PersistentCollectionDescriptor getCollectionDescriptor() {
		return collectionDescriptor;
	}

	// todo (6.0) : poorly named - just means whether the collection data is available
	//		from the JdbcValuesSource (joined or collection-loader) or whether
	//		we need a subsequent select load
	protected boolean isJoinFetch() {
		return isJoinFetch;
	}

	protected CollectionKey getCollectionKey() {
		return collectionKey;
	}

	protected FetchParentAccess getParentAccess() {
		return parentAccess;
	}

	public Object getCollectionFkValue() {
		return collectionFkValue;
	}

	@Override
	public PluralPersistentAttribute getFetchedAttribute() {
		return getCollectionDescriptor().getDescribedAttribute();
	}

	@Override
	public void hydrate(RowProcessingState rowProcessingState) {
		collectionFkValue = keyAssembler.assemble(
				rowProcessingState,
				rowProcessingState.getJdbcValuesSourceProcessingState().getProcessingOptions()
		);

		final Object parentFkValue;

		if ( getParentAccess() == null ) {
			// a collection root (CollectionLoader)..
			// 		- just use the collectionFkValue as the parentFkValue.
			//
			// todo (6.0) : is it legal for `collectionFkValue` to be null here?
			// 		I think not because we are loading a collection by a provided key value
			parentFkValue = collectionFkValue;
		}
		else {
			parentFkValue = parentAccess.getResolvedState( getFetchedAttribute(), rowProcessingState );
		}

		if ( parentFkValue == null ) {
			throw new HibernateException( "Collection parentFkValue cannot be null : " + getFetchedAttribute().getNavigableRole().getFullPath() );
		}

		collectionKey = new CollectionKey( getFetchedAttribute().getPersistentCollectionDescriptor(), parentFkValue );

		afterKeyHydrated( rowProcessingState );
	}

	protected void afterKeyHydrated(RowProcessingState rowProcessingState) {
	}

	@Override
	public void finishUpRow(RowProcessingState rowProcessingState) {
		collectionKey = null;
		collectionFkValue = null;
	}
}
