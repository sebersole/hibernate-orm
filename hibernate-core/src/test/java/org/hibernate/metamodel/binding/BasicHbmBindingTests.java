/*
 * Hibernate, Relational Persistence for Idiomatic Java
 *
 * Copyright (c) 2010, Red Hat Inc. or third-party contributors as
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
package org.hibernate.metamodel.binding;

import org.xml.sax.InputSource;

import org.hibernate.metamodel.source.Metadata;
import org.hibernate.testing.junit.UnitTestCase;
import org.hibernate.util.ConfigHelper;
import org.hibernate.util.XMLHelper;
import org.hibernate.util.xml.MappingReader;
import org.hibernate.util.xml.Origin;
import org.hibernate.util.xml.XmlDocument;

/**
 * TODO : javadoc
 *
 * @author Steve Ebersole
 */
public class BasicHbmBindingTests extends UnitTestCase {
	public BasicHbmBindingTests(String string) {
		super( string );
	}

	public void testSuperSimpleMapping() {
		Metadata metadata = new Metadata();

		{
			XmlDocument xmlDocument = readResource( "/org/hibernate/test/id/Car.hbm.xml" );
			metadata.getHibernateXmlBinder().bindRoot( xmlDocument );
			EntityBinding carBinding = metadata.getEntityBinding( org.hibernate.test.id.Car.class.getName() );
			assertNotNull( carBinding );
			assertNotNull( carBinding.getEntityIdentifier() );
			assertNotNull( carBinding.getEntityIdentifier().getValueBinding() );
			assertNull( carBinding.getVersioningValueBinding() );
		}
		{
			XmlDocument xmlDocument = readResource( "/org/hibernate/test/version/PersonThing.hbm.xml" );
			metadata.getHibernateXmlBinder().bindRoot( xmlDocument );
			EntityBinding personBinding = metadata.getEntityBinding( org.hibernate.test.version.Person.class.getName() );
			assertNotNull( personBinding );
			assertNotNull( personBinding.getEntityIdentifier() );
			assertNotNull( personBinding.getEntityIdentifier().getValueBinding() );
			assertNotNull( personBinding.getVersioningValueBinding() );
			assertNotNull( personBinding.getVersioningValueBinding().getAttribute() );
			EntityBinding thingBinding = metadata.getEntityBinding( org.hibernate.test.version.Thing.class.getName() );
			assertNotNull( thingBinding );
			assertNotNull( thingBinding.getEntityIdentifier() );
			assertNotNull( thingBinding.getEntityIdentifier().getValueBinding() );
			assertNotNull( thingBinding.getVersioningValueBinding() );
			assertNotNull( thingBinding.getVersioningValueBinding().getAttribute() );
			EntityBinding taskBinding = metadata.getEntityBinding( org.hibernate.test.version.Task.class.getName() );
			assertNotNull( taskBinding );
			assertNotNull( taskBinding.getEntityIdentifier() );
			assertNotNull( taskBinding.getEntityIdentifier().getValueBinding() );
			assertNotNull( taskBinding.getVersioningValueBinding() );
			assertNotNull( taskBinding.getVersioningValueBinding().getAttribute() );
		}
	}

	private XmlDocument readResource(final String name) {
		final String path = "/org/hibernate/test/id/Car.hbm.xml";
		Origin origin = new Origin() {
			@Override
			public String getType() {
				return "resource";
			}

			@Override
			public String getName() {
				return name;
			}
		};
		InputSource inputSource = new InputSource( ConfigHelper.getResourceAsStream( name ) );
		return MappingReader.INSTANCE.readMappingDocument( XMLHelper.DEFAULT_DTD_RESOLVER, inputSource, origin );
	}
}
