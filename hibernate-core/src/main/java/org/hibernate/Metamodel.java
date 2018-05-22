/*
 * Hibernate, Relational Persistence for Idiomatic Java
 *
 * License: GNU Lesser General Public License (LGPL), version 2.1 or later.
 * See the lgpl.txt file in the root directory or <http://www.gnu.org/licenses/lgpl-2.1.html>.
 */
package org.hibernate;

import java.util.List;
import javax.persistence.EntityGraph;
import javax.persistence.metamodel.EntityType;

import org.hibernate.metamodel.RuntimeModel;
import org.hibernate.metamodel.model.domain.spi.EntityDescriptor;
import org.hibernate.type.spi.TypeConfiguration;

/**
 * Hibernate's extension to the JPA {@link javax.persistence.metamodel.Metamodel}
 * contract.  Most calls simply get delegated in some form to {@link TypeConfiguration}
 * via {@link #getTypeConfiguration()}
 *
 * @author Steve Ebersole
 */
public interface Metamodel extends javax.persistence.metamodel.Metamodel, RuntimeModel {
	/**
	 * Access to the TypeConfiguration in effect for this SessionFactory/Metamodel
	 *
	 * @return Access to the TypeConfiguration
	 */
	TypeConfiguration getTypeConfiguration();

	@Override
	@SuppressWarnings("unchecked")
	default <X> EntityType<X> entity(Class<X> cls) {
		final EntityDescriptor<X> descriptor = getEntityDescriptor( cls );
		if ( descriptor == null ) {
			// per JPA, this condition needs to be an (illegal argument) exception
			throw new IllegalArgumentException( "Not an entity: " + cls );
		}
		return descriptor;
	}

	/**
	 * Access to an entity descriptor supporting Hibernate's entity-name feature
	 *
	 * @param entityName The entity-name
	 *
	 * @return The entity descriptor
	 */
	default <X> EntityType<X> entity(String entityName) {
		final EntityDescriptor<X> descriptor = findEntityDescriptor( entityName );
		if ( descriptor == null ) {
			// consistent with the JPA requirement above
			throw new IllegalArgumentException( "Not an entity: " + entityName );
		}
		return descriptor;
	}

	<T> void addNamedEntityGraph(String graphName, EntityGraph<T> entityGraph);

	@SuppressWarnings("unchecked")
	default <T> EntityGraph<T> findEntityGraphByName(String name) {
		return (EntityGraph) this.findEntityGraph( name );
	}

	@SuppressWarnings("unchecked")
	default <T> List<EntityGraph<T>> findEntityGraphsByType(Class<T> entityClass) {
		return (List) findEntityGraphForType( entityClass );
	}
}
