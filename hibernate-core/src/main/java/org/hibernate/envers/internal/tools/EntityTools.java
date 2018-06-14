/*
 * Hibernate, Relational Persistence for Idiomatic Java
 *
 * License: GNU Lesser General Public License (LGPL), version 2.1 or later.
 * See the lgpl.txt file in the root directory or <http://www.gnu.org/licenses/lgpl-2.1.html>.
 */
package org.hibernate.envers.internal.tools;

import java.util.Set;
import java.util.stream.Collectors;

import javax.persistence.metamodel.Attribute;

import org.hibernate.Session;
import org.hibernate.engine.spi.SessionFactoryImplementor;
import org.hibernate.engine.spi.SessionImplementor;
import org.hibernate.engine.spi.SharedSessionContractImplementor;
import org.hibernate.internal.util.compare.EqualsHelper;
import org.hibernate.metamodel.model.domain.spi.EntityDescriptor;
import org.hibernate.proxy.HibernateProxy;

/**
 * @author Lukasz Antoniak (lukasz dot antoniak at gmail dot com)
 */
public abstract class EntityTools {
	public static boolean entitiesEqual(SessionImplementor session, String entityName, Object obj1, Object obj2) {
		final Object id1 = getIdentifier( session, entityName, obj1 );
		final Object id2 = getIdentifier( session, entityName, obj2 );

		return EqualsHelper.areEqual( id1, id2 );
	}

	public static Object getIdentifier(SessionImplementor session, String entityName, Object obj) {
		if ( obj == null ) {
			return null;
		}

		if ( obj instanceof HibernateProxy ) {
			final HibernateProxy hibernateProxy = (HibernateProxy) obj;
			return hibernateProxy.getHibernateLazyInitializer().getIdentifier();
		}

		return session.getEntityDescriptor( entityName, obj ).getIdentifier( obj, session );
	}

	public static Object getTargetFromProxy(SessionFactoryImplementor sessionFactoryImplementor, HibernateProxy proxy) {
		if ( !proxy.getHibernateLazyInitializer().isUninitialized() || activeProxySession( proxy ) ) {
			return proxy.getHibernateLazyInitializer().getImplementation();
		}

		final SharedSessionContractImplementor sessionImplementor = proxy.getHibernateLazyInitializer().getSession();
		final Session tempSession = sessionImplementor == null
				? sessionFactoryImplementor.openTemporarySession()
				: sessionImplementor.getFactory().openTemporarySession();
		try {
			return tempSession.get(
					proxy.getHibernateLazyInitializer().getEntityName(),
					proxy.getHibernateLazyInitializer().getIdentifier()
			);
		}
		finally {
			tempSession.close();
		}
	}

	private static boolean activeProxySession(HibernateProxy proxy) {
		final Session session = (Session) proxy.getHibernateLazyInitializer().getSession();
		return session != null && session.isOpen() && session.isConnected();
	}

	/**
	 * @param clazz Class wrapped with a proxy or not.
	 * @param <T> Class type.
	 *
	 * @return Returns target class in case it has been wrapped with a proxy. If {@code null} reference is passed,
	 *         method returns {@code null}.
	 */
	@SuppressWarnings({"unchecked"})
	public static <T> Class<T> getTargetClassIfProxied(Class<T> clazz) {
		if ( clazz == null ) {
			return null;
		}
		else if ( HibernateProxy.class.isAssignableFrom( clazz ) ) {
			// Get the source class of Javassist proxy instance.
			return (Class<T>) clazz.getSuperclass();
		}
		return clazz;
	}

	/**
	 * @return Java class mapped to specified entity name.
	 */
	public static Class getEntityClass(SessionImplementor sessionImplementor, String entityName) {
		return sessionImplementor.getFactory().getMetamodel().findEntityDescriptor( entityName ).getMappedClass();
	}

	/**
	 * Introduce legacy behavior of fetching property names from an {@link EntityDescriptor}.
	 *
	 * @param entityDescriptor The entity descriptor.
	 * @return A string array of property names.
	 */
	public static String[] getPropertyNames(EntityDescriptor entityDescriptor) {
		// todo (6.0) - Rework usage sites of this
		//		For now this was added to get functionality working.
		//		Since this is internal tooling, this won't affect any API/SPI.
		final Set<? extends Attribute> attributes = entityDescriptor.getAttributes();
		return attributes.stream()
				.map( Attribute::getName )
				.collect( Collectors.toList() )
				.toArray( new String[ attributes.size() ] );
	}
}
