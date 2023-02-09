/*
 * Hibernate, Relational Persistence for Idiomatic Java
 *
 * License: GNU Lesser General Public License (LGPL), version 2.1 or later.
 * See the lgpl.txt file in the root directory or http://www.gnu.org/licenses/lgpl-2.1.html.
 */
package org.hibernate.orm.test.boot.annotations.bind;

import org.hibernate.boot.annotations.bind.internal.BindingCoordinator;
import org.hibernate.boot.model.process.internal.ManagedResourcesImpl;
import org.hibernate.mapping.Property;
import org.hibernate.mapping.RootClass;
import org.hibernate.orm.test.boot.annotations.source.SimpleColumnEntity;

import org.hibernate.testing.boot.MetadataBuildingContextTestingImpl;
import org.hibernate.testing.orm.junit.ServiceRegistry;
import org.hibernate.testing.orm.junit.ServiceRegistryScope;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * @author Steve Ebersole
 */
@ServiceRegistry
public class BindingCoordinatorSmokeTests {
	@Test
	void simpleTest(ServiceRegistryScope scope) {
		final MetadataBuildingContextTestingImpl buildingContext = new MetadataBuildingContextTestingImpl( scope.getRegistry() );
		final ManagedResourcesImpl managedResources = new ManagedResourcesImpl();
		managedResources.addAnnotatedClassReference( SimpleColumnEntity.class );
		managedResources.addAnnotatedPackageName( getClass().getPackageName() );

		BindingCoordinator.bindBootModel( managedResources, buildingContext );

		final RootClass entityBinding = (RootClass) buildingContext
				.getMetadataCollector()
				.getEntityBinding( SimpleColumnEntity.class.getName() );
		PropertyBinderSmokeTests.verifySimpleColumnEntityDetails( entityBinding );

		assertThat( entityBinding.getProperties() ).hasSize( 2 );

		for ( Property property : entityBinding.getProperties() ) {
			PropertyBinderSmokeTests.verifySimpleColumnEntityProperty( entityBinding, property );
		}
	}
}
