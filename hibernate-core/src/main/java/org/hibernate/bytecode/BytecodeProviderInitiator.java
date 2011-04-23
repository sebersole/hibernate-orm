/*
 * Hibernate, Relational Persistence for Idiomatic Java
 *
 * Copyright (c) 2011, Red Hat Inc. or third-party contributors as
 * indicated by the @author tags or express copyright attribution
 * statements applied by the authors.  All third-party contributions are
 * distributed under license by Red Hat Inc.
 *
 * This copyrighted material is made available to anyone wishing to use, modify,
 * copy, or redistribute it subject to the terms and conditions of the GNU
 * Lesser General Public License, as published by the Free Software Foundation.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY
 * or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU Lesser General Public License
 * for more details.
 *
 * You should have received a copy of the GNU Lesser General Public License
 * along with this distribution; if not, write to:
 * Free Software Foundation, Inc.
 * 51 Franklin Street, Fifth Floor
 * Boston, MA  02110-1301  USA
 */
package org.hibernate.bytecode;

import java.util.Map;

import org.jboss.logging.Logger;

import org.hibernate.bytecode.internal.javassist.BytecodeProviderImpl;
import org.hibernate.bytecode.spi.BytecodeProvider;
import org.hibernate.cfg.AvailableSettings;
import org.hibernate.internal.util.config.ConfigurationHelper;
import org.hibernate.service.classloading.spi.ClassLoaderService;
import org.hibernate.service.spi.BasicServiceInitiator;
import org.hibernate.service.spi.ServiceException;
import org.hibernate.service.spi.ServiceRegistryImplementor;

/**
 * @author Steve Ebersole
 */
public class BytecodeProviderInitiator implements BasicServiceInitiator<BytecodeProvider> {
	private static final Logger log = Logger.getLogger( BytecodeProviderInitiator.class );

	public static final BytecodeProviderInitiator INSTANCE = new BytecodeProviderInitiator();

	@Override
	public Class<BytecodeProvider> getServiceInitiated() {
		return BytecodeProvider.class;
	}

	@Override
	public BytecodeProvider initiateService(Map configurationValues, ServiceRegistryImplementor registry) {
		final BytecodeProvider provider = resolveBytecodeProvider( configurationValues, registry );
		log.debugf( "Bytecode provider : %s", provider.getClass().getName() );
		return provider;
	}

	private BytecodeProvider resolveBytecodeProvider(Map configurationValues, ServiceRegistryImplementor registry) {
		final boolean useReflectionOptimization = ConfigurationHelper.getBoolean(
				AvailableSettings.USE_REFLECTION_OPTIMIZER,
				configurationValues,
				false
		);
		final Object provider = configurationValues.get( AvailableSettings.BYTECODE_PROVIDER );
		if ( provider == null ) {
			return javassistBytecodeProvider( useReflectionOptimization );
		}

		if ( BytecodeProvider.class.isInstance( provider ) ) {
			return (BytecodeProvider) provider;
		}

		final Class providerImplClass;
		if ( Class.class.isInstance( provider ) ) {
			providerImplClass = (Class) provider;
		}
		else {
			final String providerString = provider.toString();
			if ( "javassist".equals( providerString ) ) {
				// legacy , recognized short names.
				return javassistBytecodeProvider( useReflectionOptimization );
			}
			providerImplClass = registry.getService( ClassLoaderService.class ).classForName( providerString );
		}
		try {
			return (BytecodeProvider) providerImplClass.newInstance();
		}
		catch (Exception e) {
			throw new ServiceException( "Unable to instantiated requested bytecode provider implemntation [" + providerImplClass.getName() + "]", e );
		}
	}

	private BytecodeProvider javassistBytecodeProvider(boolean useReflectionOptimization) {
		return new BytecodeProviderImpl( useReflectionOptimization );
	}
}
