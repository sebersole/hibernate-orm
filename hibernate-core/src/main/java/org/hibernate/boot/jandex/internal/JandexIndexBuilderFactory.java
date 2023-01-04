/*
 * Hibernate, Relational Persistence for Idiomatic Java
 *
 * License: GNU Lesser General Public License (LGPL), version 2.1 or later.
 * See the lgpl.txt file in the root directory or <http://www.gnu.org/licenses/lgpl-2.1.html>.
 */
package org.hibernate.boot.jandex.internal;

import org.hibernate.boot.internal.StandardResourceLocator;
import org.hibernate.boot.jandex.spi.JandexIndexBuilder;
import org.hibernate.boot.registry.classloading.spi.ClassLoaderService;
import org.hibernate.boot.spi.BootstrapContext;
import org.hibernate.boot.spi.MetadataBuildingOptions;

import org.jboss.jandex.IndexView;

/**
 * A factory for JandexIndexBuilder instances.
 *
 * @author Steve Ebersole
 */
public class JandexIndexBuilderFactory {
	public static JandexIndexBuilder buildJandexIndexBuilder(BootstrapContext bootstrapContext) {
		final MetadataBuildingOptions options = bootstrapContext.getMetadataBuildingOptions();
		// some tests...
		if ( options == null ) {
			return new JandexIndexBuilderStandard();
		}

		final IndexView suppliedJandexIndex = options.getSuppliedJandexIndex();
		if ( suppliedJandexIndex != null ) {
			return new JandexIndexBuilderSupplied( suppliedJandexIndex );
		}

		return new JandexIndexBuilderStandard(
				options,
				new StandardResourceLocator( options.getServiceRegistry().getService( ClassLoaderService.class ) )
		);

	}
}
