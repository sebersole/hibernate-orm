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
	private static final Logger log = Logger.getLogger( Helper.class );

	public static FetchStrategy determineFetchStrategy(
			PersistentAttributeMapping bootModelAttribute,
			ManagedTypeDescriptor runtimeModelContainer,
			EntityDescriptor entityDescriptor) {

		FetchStyle style = determineStyle(
				bootModelAttribute,
				entityDescriptor
		);

		return new FetchStrategy(
				determineTiming(
						style,
						bootModelAttribute,
						runtimeModelContainer,
						entityDescriptor
				),
				style
		);
	}

	private static FetchTiming determineTiming(
			FetchStyle style,
			PersistentAttributeMapping bootModelAttribute,
			ManagedTypeDescriptor runtimeModelContainer,
			EntityDescriptor entityDescriptor) {
		switch ( style ) {
			case JOIN: {
				return FetchTiming.IMMEDIATE;
			}
			case BATCH:
			case SUBSELECT: {
				return FetchTiming.DELAYED;
			}
			default: {
				// SELECT case, can be either
				if ( runtimeModelContainer instanceof EntityDescriptor ) {
					final EntityDescriptor container = (EntityDescriptor) runtimeModelContainer;
					if ( !container.hasProxy() && !container.getBytecodeEnhancementMetadata()
							.isEnhancedForLazyLoading() ) {
						return FetchTiming.IMMEDIATE;
					}
					else {
						return FetchTiming.DELAYED;
					}
				}
				else {
					return FetchTiming.DELAYED;
				}
			}
		}
	}

	private static FetchStyle determineStyle(
			PersistentAttributeMapping bootModelAttribute,
			EntityDescriptor entityDescriptor) {
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
