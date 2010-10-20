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
package org.hibernate.metamodel.source;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Map;
import java.util.Set;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import org.hibernate.DuplicateMappingException;
import org.hibernate.MappingException;
import org.hibernate.cfg.EJB3NamingStrategy;
import org.hibernate.cfg.ExtendsQueueEntry;
import org.hibernate.cfg.HbmBinder;
import org.hibernate.cfg.NamingStrategy;
import org.hibernate.mapping.FetchProfile;
import org.hibernate.mapping.MetadataSource;
import org.hibernate.metamodel.binding.EntityBinding;
import org.hibernate.metamodel.relational.Database;
import org.hibernate.metamodel.relational.ObjectName;
import org.hibernate.metamodel.relational.TableSpecification;
import org.hibernate.metamodel.source.hbm.HibernateXmlBinder;

/**
 * TODO : javadoc
 *
 * @author Steve Ebersole
 */
public class Metadata {
	private static final Logger log = LoggerFactory.getLogger( Metadata.class );

	private final HibernateXmlBinder hibernateXmlBinder = new HibernateXmlBinder( this );

	public HibernateXmlBinder getHibernateXmlBinder() {
		return hibernateXmlBinder;
	}

	private final Database database = new Database();

	public Database getDatabase() {
		return database;
	}

	private NamingStrategy namingStrategy = EJB3NamingStrategy.INSTANCE;

	public NamingStrategy getNamingStrategy() {
		return namingStrategy;
	}

	public void setNamingStrategy(NamingStrategy namingStrategy) {
		this.namingStrategy = namingStrategy;
	}

	private Set<ExtendsQueueEntry> extendsQueue = new HashSet<ExtendsQueueEntry>();

	public void addToExtendsQueue(ExtendsQueueEntry extendsQueueEntry) {
		extendsQueue.add( extendsQueueEntry );
	}

	private Map<String,EntityBinding> entityBindingMap = new HashMap<String, EntityBinding>();

	public EntityBinding getEntityBinding(String entityName) {
		return entityBindingMap.get( entityName );
	}

	public Iterable<EntityBinding> getEntityBindings() {
		return entityBindingMap.values();
	}

	public void addEntity(EntityBinding entityBinding) {
		final String entityName = entityBinding.getEntity().getName();
		if ( entityBindingMap.containsKey( entityName ) ) {
			throw new DuplicateMappingException( DuplicateMappingException.Type.ENTITY, entityName );
		}
		entityBindingMap.put( entityBinding.getEntity().getName(), entityBinding );
	}

	private Map<String,String> imports;

	public void addImport(String importName, String entityName) {
		if ( imports == null ) {
			imports = new HashMap<String, String>();
		}
		log.trace( "Import: " + importName + " -> " + entityName );
		String old = imports.put( importName, entityName );
		if ( old != null ) {
			log.debug( "import name [{}] overrode previous [{}]", importName, old  );
		}
	}

	private Map<String,FetchProfile> fetchProfiles = new HashMap<String, FetchProfile>();

	public Iterable<FetchProfile> getFetchProfiles() {
		return fetchProfiles.values();
	}

	public FetchProfile findOrCreateFetchProfile(String profileName, MetadataSource source) {
		FetchProfile profile = fetchProfiles.get( profileName );
		if ( profile == null ) {
			profile = new FetchProfile( profileName, source );
			fetchProfiles.put( profileName, profile );
		}
		return profile;
	}
}
