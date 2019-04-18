/*
 * Hibernate, Relational Persistence for Idiomatic Java
 *
 * License: GNU Lesser General Public License (LGPL), version 2.1 or later
 * See the lgpl.txt file in the root directory or http://www.gnu.org/licenses/lgpl-2.1.html
 */
package org.hibernate.bytecode.enhance.spi.interceptor;

import java.util.Collections;
import java.util.Set;

import org.hibernate.LockMode;
import org.hibernate.engine.spi.EntityKey;
import org.hibernate.engine.spi.SharedSessionContractImplementor;
import org.hibernate.engine.spi.Status;
import org.hibernate.persister.entity.EntityPersister;

/**
 * @author Steve Ebersole
 */
public class EnhancementAsProxyLazinessInterceptor extends AbstractLazyLoadInterceptor {
	private final EntityKey entityKey;

	private boolean initialized;

	public EnhancementAsProxyLazinessInterceptor(
			String entityName,
			EntityKey entityKey,
			SharedSessionContractImplementor session) {
		super( entityName, session );
		this.entityKey = entityKey;
	}

	public EntityKey getEntityKey() {
		return entityKey;
	}

	@Override
	protected Object handleRead(Object target, String attributeName, Object value) {
		if ( initialized ) {
			throw new IllegalStateException( "EnhancementAsProxyLazinessInterceptor interception on an initialized instance" );
		}

		try {
			return forceInitialize( target, attributeName );
		}
		finally {
			initialized = true;
		}
	}

	protected Object forceInitialize(Object target, String attributeName) {
		return new Helper( this ).performWork(
				(session, isTemporarySession) -> {
					final EntityPersister persister = session.getFactory()
							.getMetamodel()
							.entityPersister( getEntityName() );

					if ( isTemporarySession ) {
						// Add an entry for this entity in the PC of the temp Session
						// NOTE : a few arguments that would be nice to pass along here...
						//		1) loadedState if we know any
						final Object[] loadedState = null;
						//		2) does a row exist in the db for this entity?
						final boolean existsInDb = true;
						session.getPersistenceContext().addEntity(
								target,
								Status.READ_ONLY,
								loadedState,
								entityKey,
								persister.getVersion( target ),
								LockMode.NONE,
								existsInDb,
								persister,
								true
						);
					}

					return persister.initializeEnhancedEntityUsedAsProxy(
							target,
							attributeName,
							session
					);
				},
				getEntityName(),
				attributeName
		);
	}

	@Override
	protected Object handleWrite(Object target, String attributeName, Object oldValue, Object newValue) {
		if ( initialized ) {
			throw new IllegalStateException( "EnhancementAsProxyLazinessInterceptor interception on an initialized instance" );
		}

		try {
			forceInitialize( target, attributeName );
		}
		finally {
			initialized = true;
		}

		return newValue;
	}

	@Override
	public Set<String> getInitializedLazyAttributeNames() {
		return Collections.emptySet();
	}

	@Override
	public void attributeInitialized(String name) {
		if ( initialized ) {
			throw new UnsupportedOperationException( "Expected call to EnhancementAsProxyLazinessInterceptor#attributeInitialized" );
		}
	}
}
