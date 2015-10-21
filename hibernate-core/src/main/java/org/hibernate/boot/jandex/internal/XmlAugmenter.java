/*
 * Hibernate, Relational Persistence for Idiomatic Java
 *
 * License: GNU Lesser General Public License (LGPL), version 2.1 or later.
 * See the lgpl.txt file in the root directory or <http://www.gnu.org/licenses/lgpl-2.1.html>.
 */
package org.hibernate.boot.jandex.internal;

import java.util.List;

import org.hibernate.boot.jaxb.mapping.spi.JaxbEntityMappings;
import org.hibernate.boot.jaxb.spi.Binding;
import org.hibernate.boot.registry.StandardServiceRegistry;

import org.jboss.jandex.IndexView;

/**
 * Augments the Jandex index with information from XML mappings.
 *
 * @author Steve Ebersole
 */
public class XmlAugmenter {
	public static IndexView buildAugmentedIndex(
			IndexView baselineJandexIndex,
			List<Binding<JaxbEntityMappings>> xmlBindings,
			StandardServiceRegistry serviceRegistry) {
		return null;
	}
}
