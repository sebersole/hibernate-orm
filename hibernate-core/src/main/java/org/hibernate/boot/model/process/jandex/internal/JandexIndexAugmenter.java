/*
 * Hibernate, Relational Persistence for Idiomatic Java
 *
 * License: GNU Lesser General Public License (LGPL), version 2.1 or later.
 * See the lgpl.txt file in the root directory or <http://www.gnu.org/licenses/lgpl-2.1.html>.
 */
package org.hibernate.boot.model.process.jandex.internal;

import java.util.ArrayList;
import java.util.List;

import org.hibernate.boot.internal.MetadataBuildingContextRootImpl;
import org.hibernate.boot.jaxb.mapping.spi.JaxbEntityMappings;
import org.hibernate.boot.jaxb.spi.Binding;
import org.hibernate.boot.model.process.spi.ManagedResources;

import org.jboss.jandex.IndexView;

/**
 * Augments the Jandex index with information from XML mappings.
 *
 * @author Steve Ebersole
 */
public class JandexIndexAugmenter {
	public static IndexView buildAugmentedIndex(
			ManagedResources managedResources,
			MetadataBuildingContextRootImpl rootContext) {
		final List<Binding<JaxbEntityMappings>> jpaXmlBindings = new ArrayList<Binding<JaxbEntityMappings>>();
		for ( Binding binding : managedResources.getXmlMappingBindings() ) {
			if ( JaxbEntityMappings.class.isInstance( binding.getRoot() ) ) {
				// todo : this will be checked after hbm transformation is in place.
				//noinspection unchecked
				jpaXmlBindings.add( binding );
			}
		}

		final IndexView baselineJandexIndex = managedResources.getJandexIndexBuilder().buildIndexView();

		if ( jpaXmlBindings.isEmpty() ) {
			// if there is no XML information, just return the original index
			return baselineJandexIndex;
		}

		return baselineJandexIndex;
	}
}
