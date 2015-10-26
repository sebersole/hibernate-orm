/*
 * Hibernate, Relational Persistence for Idiomatic Java
 *
 * License: GNU Lesser General Public License (LGPL), version 2.1 or later.
 * See the lgpl.txt file in the root directory or <http://www.gnu.org/licenses/lgpl-2.1.html>.
 */
package org.hibernate.boot.model.process.spi;

import java.net.URL;

import org.hibernate.boot.registry.classloading.spi.ClassLoaderService;

/**
 * Access to locate classpath resources during Jandex initialization
 */
public class ResourceLocator {
	private final ClassLoaderService classLoaderService;

	public ResourceLocator(ClassLoaderService classLoaderService) {
		this.classLoaderService = classLoaderService;
	}

	/**
	 * Locate the named resource
	 *
	 * @param resourceName The resource name to locate
	 *
	 * @return The located URL, or {@code null} if no match found
	 */
	public URL locateResource(String resourceName) {
		return classLoaderService.locateResource( resourceName );
	}
}
