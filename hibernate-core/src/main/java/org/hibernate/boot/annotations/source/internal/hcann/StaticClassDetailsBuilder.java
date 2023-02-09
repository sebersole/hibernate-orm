/*
 * Hibernate, Relational Persistence for Idiomatic Java
 *
 * License: GNU Lesser General Public License (LGPL), version 2.1 or later.
 * See the lgpl.txt file in the root directory or http://www.gnu.org/licenses/lgpl-2.1.html.
 */
package org.hibernate.boot.annotations.source.internal.hcann;

import org.hibernate.annotations.common.reflection.ReflectionManager;
import org.hibernate.annotations.common.reflection.XClass;
import org.hibernate.boot.annotations.source.spi.ClassDetails;
import org.hibernate.boot.annotations.source.spi.ClassDetailsBuilder;
import org.hibernate.boot.annotations.spi.AnnotationProcessingContext;
import org.hibernate.boot.registry.classloading.spi.ClassLoaderService;

/**
 * @author Steve Ebersole
 */
public class StaticClassDetailsBuilder implements ClassDetailsBuilder {
	@Override
	public ClassDetails buildClassDetails(String name, AnnotationProcessingContext processingContext) {
		final ClassLoaderService classLoaderService = processingContext.getMetadataBuildingContext()
				.getBootstrapContext()
				.getServiceRegistry()
				.getService( ClassLoaderService.class );
		final ReflectionManager hcannReflectionManager = processingContext.getMetadataBuildingContext()
				.getBootstrapContext()
				.getReflectionManager();

		final Class<?> classForName = classLoaderService.classForName( name );
		final XClass xClassForName = hcannReflectionManager.toXClass( classForName );
		return new ClassDetailsImpl( xClassForName, processingContext );
	}
}
