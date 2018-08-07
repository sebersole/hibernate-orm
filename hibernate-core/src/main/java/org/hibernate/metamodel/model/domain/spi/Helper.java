/*
 * Hibernate, Relational Persistence for Idiomatic Java
 *
 * License: GNU Lesser General Public License (LGPL), version 2.1 or later
 * See the lgpl.txt file in the root directory or http://www.gnu.org/licenses/lgpl-2.1.html
 */
package org.hibernate.metamodel.model.domain.spi;

import org.hibernate.FetchMode;
import org.hibernate.boot.model.domain.PersistentAttributeMapping;
import org.hibernate.engine.FetchStrategy;
import org.hibernate.engine.FetchStyle;
import org.hibernate.engine.FetchTiming;
import org.hibernate.mapping.Collection;

import org.jboss.logging.Logger;

/**
 * @author Steve Ebersole
 */
public class Helper {
	public static FetchStrategy determineFetchStrategy(
			PersistentAttributeMapping bootModelAttribute,
			ManagedTypeDescriptor runtimeModelContainer,
			EntityDescriptor entityDescriptor) {

		// todo (6.0) : implement this
		// 		for now, always assume LAZY
		return new FetchStrategy( FetchTiming.DELAYED, FetchStyle.SELECT );

//		final FetchTiming fetchTiming = determineTiming( bootModelAttribute, entityDescriptor );
//		return new FetchStrategy(
//				fetchTiming,
//				determineStyle( bootModelAttribute, entityDescriptor, fetchTiming )
//		);
	}

	private static FetchTiming determineTiming(
			PersistentAttributeMapping bootModelAttribute,
			EntityDescriptor entityDescriptor) {
		if ( bootModelAttribute.isLazy() ) {
			return FetchTiming.DELAYED;
		}

		// todo (6.0) : account for bytecode lazy loading (from the attribute's container)

		return FetchTiming.IMMEDIATE;
	}

	private static final Logger log = Logger.getLogger( Helper.class );

	private static FetchStyle determineStyle(
			PersistentAttributeMapping bootModelAttribute,
			EntityDescriptor entityDescriptor,
			FetchTiming fetchTiming) {
		// todo (6.0) : allow subselect fetching for entity refs

		final FetchMode fetchMode = bootModelAttribute.getValueMapping().getFetchMode();

		switch ( fetchMode ) {
			case JOIN: {
				if ( bootModelAttribute.isLazy() ) {
					log.debugf(
							"%s.%s defined join fetch and lazy",
							bootModelAttribute.getEntity().getEntityName(),
							bootModelAttribute.getName()
					);
				}
				return FetchStyle.JOIN;
			}
			default: {
				return FetchStyle.SELECT;
			}
		}
	}

	public static FetchStrategy determineFetchStrategy(Collection bootCollectionDescriptor) {
		return new FetchStrategy(
					determineTiming( bootCollectionDescriptor ),
					determineStyle( bootCollectionDescriptor )
			);
	}

	private static FetchTiming determineTiming(Collection bootCollectionDescriptor) {
		if ( bootCollectionDescriptor.isLazy() || bootCollectionDescriptor.isExtraLazy() ) {
			return FetchTiming.DELAYED;
		}
		else {
			return FetchTiming.IMMEDIATE;
		}
	}

	private static FetchStyle determineStyle(Collection bootCollectionDescriptor) {
		if ( bootCollectionDescriptor.getBatchSize() > 1 ) {
			return FetchStyle.BATCH;
		}

		if ( bootCollectionDescriptor.isSubselectLoadable() ) {
			return FetchStyle.SUBSELECT;
		}

		final FetchMode fetchMode = bootCollectionDescriptor.getFetchMode();

		if ( fetchMode == FetchMode.JOIN ) {
			return FetchStyle.JOIN;
		}

		return FetchStyle.SELECT;
	}
}
