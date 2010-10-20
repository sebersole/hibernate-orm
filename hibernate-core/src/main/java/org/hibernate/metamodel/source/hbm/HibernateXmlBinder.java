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
package org.hibernate.metamodel.source.hbm;

import java.util.Collections;
import java.util.Map;
import java.util.Set;

import org.dom4j.Document;
import org.dom4j.Element;

import org.hibernate.mapping.MetaAttribute;
import org.hibernate.metamodel.source.Metadata;
import org.hibernate.util.xml.XmlDocument;

/**
 * Binder for {@code hbm.xml} files
 *
 * @author Steve Ebersole
 */
public class HibernateXmlBinder {
	private final Metadata metadata;
	private final Map<String, MetaAttribute> globalMetas;
	private final Set<String> entityNames;

	// hibernate-mapping defined defaults

	public HibernateXmlBinder(Metadata metadata) {
		this( metadata, Collections.<String, MetaAttribute>emptyMap(), Collections.<String>emptySet() );
	}

	public HibernateXmlBinder(Metadata metadata, Map<String, MetaAttribute> globalMetas, Set<String> entityNames) {
		this.metadata = metadata;
		this.globalMetas = globalMetas;
		this.entityNames = entityNames;
	}

	public void bindRoot(XmlDocument metadataXml) {
		final Document doc = metadataXml.getDocumentTree();
		final Element hibernateMappingElement = doc.getRootElement();

//		java.util.List<String> names = HbmBinder.getExtendsNeeded( metadataXml, mappings );
//		if ( !names.isEmpty() ) {
//			// classes mentioned in extends not available - so put it in queue
//			Attribute packageAttribute = hibernateMappingElement.attribute( "package" );
//			String packageName = packageAttribute == null ? null : packageAttribute.getValue();
//			for ( String name : names ) {
//				metadata.addToExtendsQueue( new ExtendsQueueEntry( name, packageName, metadataXml, entityNames ) );
//			}
//			return;
//		}

		new HibernateMappingBinder( this, metadataXml ).processElement();
	}

	Metadata getMetadata() {
		return metadata;
	}

	Map<String, MetaAttribute> getGlobalMetas() {
		return globalMetas;
	}

}
