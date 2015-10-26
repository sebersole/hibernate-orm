/*
 * Hibernate, Relational Persistence for Idiomatic Java
 *
 * License: GNU Lesser General Public License (LGPL), version 2.1 or later.
 * See the lgpl.txt file in the root directory or <http://www.gnu.org/licenses/lgpl-2.1.html>.
 */
package org.hibernate.boot.model.process.jandex.spi;

import org.hibernate.boot.model.process.jandex.internal.JandexIndexBuilderImpl;
import org.hibernate.boot.model.process.jandex.internal.JandexIndexBuilderSuppliedImpl;
import org.hibernate.boot.model.process.spi.ResourceLocator;
import org.hibernate.boot.registry.classloading.spi.ClassLoaderService;
import org.hibernate.boot.spi.MetadataBuildingOptions;

import org.jboss.jandex.IndexView;

/**
 * A factory for JandexIndexBuilder instances.
 *
 * @author Steve Ebersole
 */
public class JandexIndexBuilderFactory {
	public static JandexIndexBuilder buildJandexIndexBuilder(MetadataBuildingOptions options) {
		final IndexView jandexView = options.getJandexView();
		if ( jandexView != null ) {
			return new JandexIndexBuilderSuppliedImpl( jandexView );
		}
		else {
			return new JandexIndexBuilderImpl(
					options,
					new ResourceLocator( options.getServiceRegistry().getService( ClassLoaderService.class ) )
			);
		}
	}
}
