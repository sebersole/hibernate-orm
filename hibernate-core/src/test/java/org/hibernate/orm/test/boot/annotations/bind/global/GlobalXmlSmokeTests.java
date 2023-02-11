/*
 * Hibernate, Relational Persistence for Idiomatic Java
 *
 * License: GNU Lesser General Public License (LGPL), version 2.1 or later.
 * See the lgpl.txt file in the root directory or http://www.gnu.org/licenses/lgpl-2.1.html.
 */
package org.hibernate.orm.test.boot.annotations.bind.global;

import java.util.Map;

import org.hibernate.boot.annotations.bind.internal.ManagedResourcesProcessor;
import org.hibernate.boot.internal.InFlightMetadataCollectorImpl;
import org.hibernate.boot.model.convert.internal.AttributeConverterManager;
import org.hibernate.boot.model.convert.spi.RegisteredConversion;
import org.hibernate.boot.model.process.internal.ManagedResourcesImpl;
import org.hibernate.boot.spi.XmlMappingBinderAccess;
import org.hibernate.orm.test.mapping.converted.converter.mutabiity.MapConverter;
import org.hibernate.type.SqlTypes;
import org.hibernate.type.descriptor.java.JavaType;
import org.hibernate.type.descriptor.jdbc.JdbcType;
import org.hibernate.type.spi.TypeConfiguration;

import org.hibernate.testing.boot.MetadataBuildingContextTestingImpl;
import org.hibernate.testing.orm.junit.ServiceRegistry;
import org.hibernate.testing.orm.junit.ServiceRegistryScope;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * @author Steve Ebersole
 */
@ServiceRegistry
public class GlobalXmlSmokeTests {
	@Test
	void simpleTest(ServiceRegistryScope scope) {
		final MetadataBuildingContextTestingImpl buildingContext = new MetadataBuildingContextTestingImpl( scope.getRegistry() );
		final XmlMappingBinderAccess xmlMappingBinderAccess = new XmlMappingBinderAccess( scope.getRegistry() );
		final TypeConfiguration typeConfiguration = buildingContext.getBootstrapContext().getTypeConfiguration();
		final InFlightMetadataCollectorImpl metadataCollector = (InFlightMetadataCollectorImpl) buildingContext.getMetadataCollector();

		final ManagedResourcesImpl managedResources = new ManagedResourcesImpl();
		managedResources.addXmlBinding( xmlMappingBinderAccess.bind( "mappings/boot/global-defs.xml" ) );

		ManagedResourcesProcessor.bindBootModel( managedResources, buildingContext );

		// @JavaTypeRegistration
		final JavaType<?> javaType = typeConfiguration.getJavaTypeRegistry().getDescriptor( String.class );
		assertThat( javaType ).isInstanceOf( CustomStringJavaType.class );

		// @JdbcTypeRegistration
		final JdbcType jdbcType = typeConfiguration.getJdbcTypeRegistry().getDescriptor( SqlTypes.VARCHAR );
		assertThat( jdbcType ).isInstanceOf( CustomVarcharJdbcType.class );

		// @ConverterRegistration
		final AttributeConverterManager converterManager = metadataCollector.getAttributeConverterManager();
		final RegisteredConversion registeredConversion = converterManager.findRegisteredConversion( Map.class );
		assertThat( registeredConversion ).isNotNull();
		assertThat( registeredConversion.getConverterDescriptor().getAttributeConverterClass() ).isEqualTo( MapConverter.class );

	}
}
