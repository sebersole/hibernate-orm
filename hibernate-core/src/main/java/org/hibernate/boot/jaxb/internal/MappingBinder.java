/*
 * Hibernate, Relational Persistence for Idiomatic Java
 *
 * License: GNU Lesser General Public License (LGPL), version 2.1 or later.
 * See the lgpl.txt file in the root directory or <http://www.gnu.org/licenses/lgpl-2.1.html>.
 */
package org.hibernate.boot.jaxb.internal;

import javax.xml.bind.JAXBContext;
import javax.xml.bind.JAXBException;
import javax.xml.stream.XMLEventFactory;
import javax.xml.stream.XMLEventReader;
import javax.xml.stream.events.StartElement;

import org.hibernate.boot.UnsupportedOrmXsdVersionException;
import org.hibernate.boot.jaxb.Origin;
import org.hibernate.boot.jaxb.hbm.spi.JaxbHbmHibernateMapping;
import org.hibernate.boot.jaxb.internal.stax.HbmEventReader;
import org.hibernate.boot.jaxb.internal.stax.LocalSchema;
import org.hibernate.boot.jaxb.internal.stax.UnifiedMappingEventReader;
import org.hibernate.boot.jaxb.mapping.spi.JaxbEntityMappings;
import org.hibernate.boot.jaxb.spi.Binding;
import org.hibernate.boot.model.process.spi.ResourceLocator;
import org.hibernate.boot.model.source.internal.hbm.HbmXmlTransformer;
import org.hibernate.internal.log.DeprecationLogger;
import org.hibernate.internal.util.config.ConfigurationException;

import org.jboss.logging.Logger;

/**
 * @author Steve Ebersole
 */
public class MappingBinder extends AbstractBinder {
	private static final Logger log = Logger.getLogger( MappingBinder.class );

	private final XMLEventFactory xmlEventFactory = XMLEventFactory.newInstance();
	private JAXBContext hbmJaxbContext;
	private JAXBContext jpaJaxbContext;

	public MappingBinder(ResourceLocator resourceLocator, boolean validateXml) {
		super( resourceLocator, validateXml );
	}

	@Override
	protected Binding doBind(
			XMLEventReader staxEventReader,
			StartElement rootElementStartEvent,
			Origin origin) {
		final String rootElementLocalName = rootElementStartEvent.getName().getLocalPart();
		if ( "hibernate-mapping".equals( rootElementLocalName ) ) {
			log.debugf( "Performing JAXB binding of hbm.xml document : %s", origin.toString() );
			DeprecationLogger.DEPRECATION_LOGGER.logDeprecationOfHbmXml( origin.toString() );

			final XMLEventReader hbmReader = new HbmEventReader( staxEventReader, xmlEventFactory );
			final JaxbHbmHibernateMapping hbmBindings = jaxb( hbmReader, LocalSchema.HBM.getSchema(), hbmJaxbContext(), origin );
			final JaxbEntityMappings jpaBindings = HbmXmlTransformer.transform( hbmBindings, origin, getResourceLocator() );
			return new Binding<JaxbEntityMappings>( jpaBindings, origin );
		}
		else {
			log.debugf( "Performing JAXB binding of orm.xml document : %s", origin.toString() );
			try {
				final XMLEventReader reader = new UnifiedMappingEventReader( staxEventReader, xmlEventFactory );
				final JaxbEntityMappings jpaBindings = jaxb( reader, LocalSchema.MAPPING.getSchema(), jpaJaxbContext(), origin );
				return new Binding<JaxbEntityMappings>( jpaBindings, origin );
			}
			catch (UnifiedMappingEventReader.BadVersionException e) {
				throw new UnsupportedOrmXsdVersionException( e.getRequestedVersion(), origin );
			}
		}
	}

	private JAXBContext hbmJaxbContext() {
		if ( hbmJaxbContext == null ) {
			try {
				hbmJaxbContext = JAXBContext.newInstance( JaxbHbmHibernateMapping.class );
			}
			catch ( JAXBException e ) {
				throw new ConfigurationException( "Unable to build hbm.xml JAXBContext", e );
			}
		}
		return hbmJaxbContext;
	}

	private JAXBContext jpaJaxbContext() {
		if ( jpaJaxbContext == null ) {
			try {
				jpaJaxbContext = JAXBContext.newInstance( JaxbEntityMappings.class );
			}
			catch ( JAXBException e ) {
				throw new ConfigurationException( "Unable to build JPA orm.xml JAXBContext", e );
			}
		}
		return jpaJaxbContext;
	}
}
