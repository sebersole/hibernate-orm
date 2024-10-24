/*
 * Hibernate, Relational Persistence for Idiomatic Java
 *
 * License: GNU Lesser General Public License (LGPL), version 2.1 or later.
 * See the lgpl.txt file in the root directory or <http://www.gnu.org/licenses/lgpl-2.1.html>.
 */
package org.hibernate.relational.internal;

import org.hibernate.boot.spi.MetadataImplementor;
import org.hibernate.cfg.AvailableSettings;
import org.hibernate.engine.spi.SessionFactoryImplementor;
import org.hibernate.relational.SchemaManager;
import org.hibernate.tool.schema.Action;
import org.hibernate.tool.schema.spi.SchemaManagementToolCoordinator;

import java.util.HashMap;
import java.util.Map;

import jakarta.persistence.SchemaValidationException;

/**
 * Implementation of {@link SchemaManager}, backed by a {@link SessionFactoryImplementor}
 * and {@link SchemaManagementToolCoordinator}.
 *
 * @author Gavin King
 */
public class SchemaManagerImpl implements SchemaManager {
	private final SessionFactoryImplementor sessionFactory;
	private final MetadataImplementor metadata;

	public SchemaManagerImpl(
			SessionFactoryImplementor sessionFactory,
			MetadataImplementor metadata) {
		this.sessionFactory = sessionFactory;
		this.metadata = metadata;
	}

	@Override
	public void exportMappedObjects(boolean createSchemas) {
		Map<String, Object> properties = new HashMap<>( sessionFactory.getProperties() );
		properties.put( AvailableSettings.JAKARTA_HBM2DDL_DATABASE_ACTION, Action.CREATE_ONLY );
		properties.put( AvailableSettings.JAKARTA_HBM2DDL_SCRIPTS_ACTION, Action.NONE );
		properties.put( AvailableSettings.JAKARTA_HBM2DDL_CREATE_SCHEMAS, createSchemas );
		SchemaManagementToolCoordinator.process(
				metadata,
				sessionFactory.getServiceRegistry(),
				properties,
				action -> {}
		);
	}

	@Override
	public void dropMappedObjects(boolean dropSchemas) {
		Map<String, Object> properties = new HashMap<>( sessionFactory.getProperties() );
		properties.put( AvailableSettings.JAKARTA_HBM2DDL_DATABASE_ACTION, Action.DROP );
		properties.put( AvailableSettings.JAKARTA_HBM2DDL_SCRIPTS_ACTION, Action.NONE );
		properties.put( AvailableSettings.JAKARTA_HBM2DDL_CREATE_SCHEMAS, dropSchemas );
		SchemaManagementToolCoordinator.process(
				metadata,
				sessionFactory.getServiceRegistry(),
				properties,
				action -> {}
		);
	}

	@Override
	public void validateMappedObjects() {
		Map<String, Object> properties = new HashMap<>( sessionFactory.getProperties() );
		properties.put( AvailableSettings.JAKARTA_HBM2DDL_DATABASE_ACTION, Action.VALIDATE );
		properties.put( AvailableSettings.JAKARTA_HBM2DDL_SCRIPTS_ACTION, Action.NONE );
		properties.put( AvailableSettings.JAKARTA_HBM2DDL_CREATE_SCHEMAS, false );
		SchemaManagementToolCoordinator.process(
				metadata,
				sessionFactory.getServiceRegistry(),
				properties,
				action -> {}
		);
	}

	@Override
	public void truncateMappedObjects() {
		Map<String, Object> properties = new HashMap<>( sessionFactory.getProperties() );
		properties.put( AvailableSettings.JAKARTA_HBM2DDL_DATABASE_ACTION, Action.TRUNCATE );
		properties.put( AvailableSettings.JAKARTA_HBM2DDL_SCRIPTS_ACTION, Action.NONE );
		SchemaManagementToolCoordinator.process(
				metadata,
				sessionFactory.getServiceRegistry(),
				properties,
				action -> {}
		);
	}

	@Override
	public void validate() throws SchemaValidationException {
		validateMappedObjects();
	}

	@Override
	public void truncate() {
		truncateMappedObjects();
	}
}
