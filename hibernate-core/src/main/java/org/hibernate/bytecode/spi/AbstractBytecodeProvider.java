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
package org.hibernate.bytecode.spi;

import org.jboss.logging.Logger;

import org.hibernate.internal.CoreMessageLogger;

/**
 * @author Steve Ebersole
 */
public abstract class AbstractBytecodeProvider implements BytecodeProvider {
	private static final CoreMessageLogger LOG = Logger.getMessageLogger( CoreMessageLogger.class, AbstractBytecodeProvider.class.getName() );

	private boolean useReflectionOptimization;

	protected AbstractBytecodeProvider() {
	}

	protected AbstractBytecodeProvider(boolean useReflectionOptimization) {
		setUseReflectionOptimization( useReflectionOptimization );
	}

	public void setUseReflectionOptimization(boolean useReflectionOptimization) {
		LOG.usingReflectionOptimizer( useReflectionOptimization );
		this.useReflectionOptimization = useReflectionOptimization;
	}

	public boolean isReflectionOptimizerEnabled() {
		return useReflectionOptimization;
	}
}
