/*
 * Hibernate, Relational Persistence for Idiomatic Java
 *
 * License: GNU Lesser General Public License (LGPL), version 2.1 or later
 * See the lgpl.txt file in the root directory or http://www.gnu.org/licenses/lgpl-2.1.html
 */
package org.hibernate.metamodel.model.domain.internal.mode;

import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.util.Collection;
import java.util.Set;

import org.hibernate.HibernateException;
import org.hibernate.MappingException;
import org.hibernate.engine.spi.SessionFactoryImplementor;
import org.hibernate.internal.CoreLogging;
import org.hibernate.internal.CoreMessageLogger;
import org.hibernate.internal.util.ReflectHelper;
import org.hibernate.metamodel.model.creation.spi.RuntimeModelCreationContext;
import org.hibernate.metamodel.model.domain.spi.AbstractEntityDescriptor;
import org.hibernate.metamodel.model.domain.spi.EmbeddedTypeDescriptor;
import org.hibernate.metamodel.model.domain.spi.EntityIdentifier;
import org.hibernate.metamodel.model.domain.spi.InheritanceCapable;
import org.hibernate.metamodel.model.domain.spi.ProxyFactoryInstantiator;
import org.hibernate.property.access.spi.Getter;
import org.hibernate.property.access.spi.PropertyAccess;
import org.hibernate.property.access.spi.Setter;
import org.hibernate.proxy.HibernateProxy;
import org.hibernate.proxy.ProxyFactory;

/**
 * @author Chris Cranford
 */
public class StandardPojoProxyFactoryInstantiator<J> implements ProxyFactoryInstantiator<J> {
	public static final StandardPojoProxyFactoryInstantiator INSTANCE = new StandardPojoProxyFactoryInstantiator();

	private static final CoreMessageLogger LOG = CoreLogging.messageLogger( StandardPojoProxyFactoryInstantiator.class );

	// todo (6.0) - We may want to merge this into StandardPojoRepresentationStrategy?

	@Override
	public ProxyFactory instantiate(
			AbstractEntityDescriptor<J> runtimeDescriptor,
			RuntimeModelCreationContext creationContext) {
		final EntityIdentifier identifierDescriptor = runtimeDescriptor.getHierarchy().getIdentifierDescriptor();

		// todo (6.0) - can refactor some of this out with the bytecode provider
		PropertyAccess propertyAccess = identifierDescriptor.asAttribute( identifierDescriptor.getJavaType() )
				.getPropertyAccess();
		final Getter idGetter = propertyAccess.getGetter();
		final Setter idSetter = propertyAccess.getSetter();
		Set<Class> proxyInterfaces = new java.util.LinkedHashSet<>();

		Class mappedClass = runtimeDescriptor.getJavaTypeDescriptor().getJavaType();
		addProxyInterfaces( runtimeDescriptor, proxyInterfaces );

		if ( mappedClass.isInterface() ) {
			proxyInterfaces.add( mappedClass );
		}

		Collection<InheritanceCapable<? extends J>> subclassTypes = runtimeDescriptor.getSubclassTypes();
		subclassTypes.forEach( inheritanceCapable -> {
			if ( AbstractEntityDescriptor.class.isInstance( inheritanceCapable ) ) {
				addProxyInterfaces( runtimeDescriptor, proxyInterfaces );
			}
		} );

		proxyInterfaces.add( HibernateProxy.class );
		runtimeDescriptor.visitAttributes( attribute -> {
			PropertyAccess attributePropertyAccess = attribute.getPropertyAccess();
			Method method = attributePropertyAccess.getGetter().getMethod();
			if ( method != null && Modifier.isFinal( method.getModifiers() ) ) {
				LOG.gettersOfLazyClassesCannotBeFinal( runtimeDescriptor.getEntityName(), attribute.getAttributeName() );
			}
			method = attributePropertyAccess.getSetter().getMethod();
			if ( method != null && Modifier.isFinal( method.getModifiers() ) ) {
				LOG.settersOfLazyClassesCannotBeFinal( runtimeDescriptor.getEntityName(), attribute.getAttributeName() );
			}
		} );

		Method idGetterMethod = idGetter == null ? null : idGetter.getMethod();
		Method idSetterMethod = idSetter == null ? null : idSetter.getMethod();

		final Class proxyInterface = runtimeDescriptor.getProxyInterface();

		Method proxyGetIdentifierMethod = idGetterMethod == null || proxyInterface == null ?
				null :
				ReflectHelper.getMethod( proxyInterface, idGetterMethod );
		Method proxySetIdentifierMethod = idSetterMethod == null || proxyInterface == null ?
				null :
				ReflectHelper.getMethod( proxyInterface, idSetterMethod );

		ProxyFactory pf = buildProxyFactoryInternal( idGetter, idSetter, creationContext );
		EmbeddedTypeDescriptor embeddedIdTypeDescritor = null;
		if ( EmbeddedTypeDescriptor.class.isInstance( embeddedIdTypeDescritor ) ) {
			embeddedIdTypeDescritor = (EmbeddedTypeDescriptor) identifierDescriptor;
		}
		try {
			pf.postInstantiate(
					runtimeDescriptor.getEntityName(),
					mappedClass,
					proxyInterfaces,
					proxyGetIdentifierMethod,
					proxySetIdentifierMethod,
					embeddedIdTypeDescritor
			);
		}
		catch ( HibernateException he) {
			LOG.unableToCreateProxyFactory( runtimeDescriptor.getEntityName(), he );
			pf = null;
		}
		return pf;
	}

	private ProxyFactory buildProxyFactoryInternal(
			Getter idGetter,
			Setter idSetter,
			RuntimeModelCreationContext creationContext) {
		// TODO : YUCK!!!  fix after HHH-1907 is complete
		final SessionFactoryImplementor sessionFactory = creationContext.getSessionFactory();

		return sessionFactory.getSessionFactoryOptions()
				.getBytecodeProvider()
				.getProxyFactoryFactory()
				.buildProxyFactory( sessionFactory );
	}

	private void addProxyInterfaces(AbstractEntityDescriptor runtimeDescriptor, Set<Class> proxyInterfaces){
		final Class javaClass = runtimeDescriptor.getJavaTypeDescriptor().getJavaType();
		final Class proxyInterface = runtimeDescriptor.getProxyInterface();
		if ( proxyInterface != null && !javaClass.equals( proxyInterface ) ) {
			if ( !proxyInterface.isInterface() ) {
				throw new MappingException(
						"proxy must be either an interface, or the class itself: " + runtimeDescriptor.getNavigableName()
				);
			}
			proxyInterfaces.add( proxyInterface );
		}
	}

	private StandardPojoProxyFactoryInstantiator() {

	}
}
