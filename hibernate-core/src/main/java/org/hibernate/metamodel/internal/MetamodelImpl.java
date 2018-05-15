/*
 * Hibernate, Relational Persistence for Idiomatic Java
 *
 * License: GNU Lesser General Public License (LGPL), version 2.1 or later.
 * See the lgpl.txt file in the root directory or <http://www.gnu.org/licenses/lgpl-2.1.html>.
 */
package org.hibernate.metamodel.internal;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import javax.persistence.metamodel.EmbeddableType;
import javax.persistence.metamodel.EntityType;
import javax.persistence.metamodel.ManagedType;

import org.hibernate.EntityNameResolver;
import org.hibernate.MappingException;
import org.hibernate.UnknownEntityTypeException;
import org.hibernate.boot.registry.classloading.spi.ClassLoaderService;
import org.hibernate.boot.registry.classloading.spi.ClassLoadingException;
import org.hibernate.engine.spi.SessionFactoryImplementor;
import org.hibernate.metamodel.model.domain.spi.EmbeddedTypeDescriptor;
import org.hibernate.metamodel.model.domain.spi.EntityDescriptor;
import org.hibernate.metamodel.model.domain.spi.MappedSuperclassDescriptor;
import org.hibernate.metamodel.spi.MetamodelImplementor;
import org.hibernate.type.descriptor.java.spi.ManagedJavaDescriptor;
import org.hibernate.type.spi.TypeConfiguration;

/**
 * Standard implementation of Hibernate's extension to the JPA
 * {@link javax.persistence.metamodel.Metamodel} contract.
 *
 * @author Steve Ebersole
 * @author Emmanuel Bernard
 */
public class MetamodelImpl implements MetamodelImplementor, Serializable {
	private final SessionFactoryImplementor sessionFactory;
	private final TypeConfiguration typeConfiguration;

	private final Set<EntityHierarchy> entityHierarchies = ConcurrentHashMap.newKeySet();
	private final Map<String,EntityDescriptor<?>> entityDescriptorMap = new ConcurrentHashMap<>();
	private final Map<String, MappedSuperclassDescriptor> mappedSuperclassDescriptorMap = new ConcurrentHashMap<>();
	private final Map<String,PersistentCollectionDescriptor<?,?,?>> collectionDescriptorMap = new ConcurrentHashMap<>();
	private final Map<String,EmbeddedTypeDescriptor<?>> embeddableDescriptorMap = new ConcurrentHashMap<>();

	private final Map<String,String> importMap = new ConcurrentHashMap<>();
	private final Set<EntityNameResolver> entityNameResolvers = ConcurrentHashMap.newKeySet();

	private final Map<JavaTypeDescriptor, PolymorphicEntityValuedExpressableType<?>> polymorphicEntityReferenceMap = new HashMap<>();
	private final Map<JavaTypeDescriptor,String> entityProxyInterfaceMap = new ConcurrentHashMap<>();
	private final Map<String,Set<String>> collectionRolesByEntityParticipant = new ConcurrentHashMap<>();
	private final Map<EmbeddableJavaDescriptor<?>,Set<String>> embeddedRolesByEmbeddableType = new ConcurrentHashMap<>();

	private final Map<ManagedJavaDescriptor<?>, MappedSuperclassDescriptor<?>> jpaMappedSuperclassTypeMap = new ConcurrentHashMap<>();

	private final Map<String,EntityGraph> entityGraphMap = new ConcurrentHashMap<>();


	MetamodelImpl(
			SessionFactoryImplementor sessionFactory,
			TypeConfiguration typeConfiguration,

			// todo (6.0) : we only need the base data passed in...
			//		would be better to build the "xref" structures here
			//		e.g. `jpaMappedSuperclassTypeMap`, `collectionRolesByEntityParticipant` or `embeddedRolesByEmbeddableType`

			Set<EntityHierarchy> entityHierarchies,
			Map<String,EntityDescriptor<?>> entityDescriptorMap,
			Map<String, MappedSuperclassDescriptor> mappedSuperclassDescriptorMap,
			Map<String,PersistentCollectionDescriptor<?,?,?>> collectionDescriptorMap,
			Map<String,EmbeddedTypeDescriptor<?>> embeddableDescriptorMap,
			Map<String,String> importMap,
			Set<EntityNameResolver> entityNameResolvers) {
		// todo (6.0) : build the other structures
	}

	@Override
	public TypeConfiguration getTypeConfiguration() {
		return typeConfiguration;
	}

	@Override
	public TypeConfiguration getTypeConfiguration() {
		return typeConfiguration;
	}

	@Override
	@SuppressWarnings({"unchecked"})
	public <X> EntityType<X> entity(Class<X> cls) {
		final EntityDescriptor descriptor = findEntityDescriptor( cls );
		if ( descriptor == null ) {
			throw new IllegalArgumentException( "Not an entity: " + cls );
		}
		return descriptor;
	}

	@Override
	@SuppressWarnings({"unchecked"})
	public <X> ManagedType<X> managedType(Class<X> cls) {
		final EntityDescriptor entityDescriptor = findEntityDescriptor( cls );
		if ( entityDescriptor != null ) {
			return entityDescriptor;
		}

		final EmbeddedTypeDescriptor embeddedDescriptor = findEmbeddableDescriptor( cls );
		if ( embeddedDescriptor != null ) {
			return embeddedDescriptor;
		}

		final MappedSuperclassDescriptor msDescriptor = findMappedSuperclassDescriptor( cls );
		if ( msDescriptor != null ) {
			return msDescriptor;
		}

		throw new IllegalArgumentException( "Not a managed type: " + cls );
	}

	@Override
	@SuppressWarnings({"unchecked"})
	public <X> EmbeddableType<X> embeddable(Class<X> cls) {
		final EmbeddedTypeDescriptor embeddedDescriptor = findEmbeddableDescriptor( cls );
		if ( embeddedDescriptor != null ) {
			return embeddedDescriptor;
		}

		throw new IllegalArgumentException( "Not an embeddable: " + cls );
	}

	@Override
	@SuppressWarnings({"unchecked"})
	public Set<ManagedType<?>> getManagedTypes() {
		// todo (6.0) : cache this?  lazily?
		final Set<ManagedType<?>> managedTypes = new HashSet<ManagedType<?>>();
		managedTypes.addAll( jpaEntityTypeMap.values() );
		managedTypes.addAll( jpaMappedSuperclassTypeMap.values() );
		managedTypes.addAll( jpaEmbeddableTypeMap.values() );
		return managedTypes;
	}

	@Override
	public Set<EntityType<?>> getEntities() {
		return new HashSet<>( jpaEntityTypesByEntityName.values() );
	}

	@Override
	public Set<EmbeddableType<?>> getEmbeddables() {
		return new HashSet<>( jpaEmbeddableTypeMap.values() );
	}

	@Override
	@SuppressWarnings("unchecked")
	public <X> EntityType<X> entity(String entityName) {
		return (EntityType<X>) jpaEntityTypesByEntityName.get( entityName );
	}

	@Override
	public String getImportedClassName(String className) {
		String result = imports.get( className );
		if ( result == null ) {
			try {
				sessionFactory.getServiceRegistry().getService( ClassLoaderService.class ).classForName( className );
				imports.put( className, className );
				return className;
			}
			catch ( ClassLoadingException cnfe ) {
				imports.put( className, INVALID_IMPORT );
				return null;
			}
		}
		else if ( result == INVALID_IMPORT ) {
			return null;
		}
		else {
			return result;
		}
	}

	/**
	 * Given the name of an entity class, determine all the class and interface names by which it can be
	 * referenced in an HQL query.
	 *
	 * @param className The name of the entity class
	 *
	 * @return the names of all persistent (mapped) classes that extend or implement the
	 *     given class or interface, accounting for implicit/explicit polymorphism settings
	 *     and excluding mapped subclasses/joined-subclasses of other classes in the result.
	 * @throws MappingException
	 */
	public String[] getImplementors(String className) throws MappingException {

		final Class clazz;
		try {
			clazz = getSessionFactory().getServiceRegistry().getService( ClassLoaderService.class ).classForName( className );
		}
		catch (ClassLoadingException e) {
			return new String[] { className }; //for a dynamic-class
		}

		ArrayList<String> results = new ArrayList<>();
		for ( EntityPersister checkPersister : entityPersisters().values() ) {
			if ( ! Queryable.class.isInstance( checkPersister ) ) {
				continue;
			}
			final Queryable checkQueryable = Queryable.class.cast( checkPersister );
			final String checkQueryableEntityName = checkQueryable.getEntityName();
			final boolean isMappedClass = className.equals( checkQueryableEntityName );
			if ( checkQueryable.isExplicitPolymorphism() ) {
				if ( isMappedClass ) {
					return new String[] { className }; //NOTE EARLY EXIT
				}
			}
			else {
				if ( isMappedClass ) {
					results.add( checkQueryableEntityName );
				}
				else {
					final Class mappedClass = checkQueryable.getMappedClass();
					if ( mappedClass != null && clazz.isAssignableFrom( mappedClass ) ) {
						final boolean assignableSuperclass;
						if ( checkQueryable.isInherited() ) {
							Class mappedSuperclass = entityPersister( checkQueryable.getMappedSuperclass() ).getMappedClass();
							assignableSuperclass = clazz.isAssignableFrom( mappedSuperclass );
						}
						else {
							assignableSuperclass = false;
						}
						if ( !assignableSuperclass ) {
							results.add( checkQueryableEntityName );
						}
					}
				}
			}
		}
		return results.toArray( new String[results.size()] );
	}

	@Override
	public Map<String, EntityPersister> entityPersisters() {
		return entityPersisterMap;
	}

	@Override
	public CollectionPersister collectionPersister(String role) {
		final CollectionPersister persister = collectionPersisterMap.get( role );
		if ( persister == null ) {
			throw new MappingException( "Could not locate CollectionPersister for role : " + role );
		}
		return persister;
	}

	@Override
	public Map<String, CollectionPersister> collectionPersisters() {
		return collectionPersisterMap;
	}

	@Override
	public EntityPersister entityPersister(Class entityClass) {
		return entityPersister( entityClass.getName() );
	}

	@Override
	public EntityPersister entityPersister(String entityName) throws MappingException {
		EntityPersister result = entityPersisterMap.get( entityName );
		if ( result == null ) {
			throw new MappingException( "Unknown entity: " + entityName );
		}
		return result;
	}


	@Override
	public EntityPersister locateEntityPersister(Class byClass) {
		EntityPersister entityPersister = entityPersisterMap.get( byClass.getName() );
		if ( entityPersister == null ) {
			String mappedEntityName = entityProxyInterfaceMap.get( byClass );
			if ( mappedEntityName != null ) {
				entityPersister = entityPersisterMap.get( mappedEntityName );
			}
		}

		if ( entityPersister == null ) {
			throw new UnknownEntityTypeException( "Unable to locate persister: " + byClass.getName() );
		}

		return entityPersister;
	}

	@Override
	public EntityPersister locateEntityPersister(String byName) {
		final EntityPersister entityPersister = entityPersisterMap.get( byName );
		if ( entityPersister == null ) {
			throw new UnknownEntityTypeException( "Unable to locate persister: " + byName );
		}
		return entityPersister;
	}

	@Override
	public Set<String> getCollectionRolesByEntityParticipant(String entityName) {
		return collectionRolesByEntityParticipant.get( entityName );
	}

	@Override
	public String[] getAllEntityNames() {
		return ArrayHelper.toStringArray( entityPersisterMap.keySet() );
	}

	@Override
	public String[] getAllCollectionRoles() {
		return ArrayHelper.toStringArray( entityPersisterMap.keySet() );
	}

	@Override
	public <T> void addNamedEntityGraph(String graphName, EntityGraph<T> entityGraph) {
		if ( entityGraph instanceof EntityGraphImplementor ) {
			entityGraph = ( (EntityGraphImplementor<T>) entityGraph ).makeImmutableCopy( graphName );
		}
		final EntityGraph old = entityGraphMap.put( graphName, entityGraph );
		if ( old != null ) {
			log.debugf( "EntityGraph being replaced on EntityManagerFactory for name %s", graphName );
		}
	}

	@Override
	@SuppressWarnings("unchecked")
	public <T> EntityGraph<T> findEntityGraphByName(String name) {
		return entityGraphMap.get( name );
	}

	@Override
	@SuppressWarnings("unchecked")
	public <T> List<EntityGraph<? super T>> findEntityGraphsByType(Class<T> entityClass) {
		final EntityType<T> entityType = entity( entityClass );
		if ( entityType == null ) {
			throw new IllegalArgumentException( "Given class is not an entity : " + entityClass.getName() );
		}

		final List<EntityGraph<? super T>> results = new ArrayList<>();

		for ( EntityGraph entityGraph : entityGraphMap.values() ) {
			if ( !EntityGraphImplementor.class.isInstance( entityGraph ) ) {
				continue;
			}

			final EntityGraphImplementor egi = (EntityGraphImplementor) entityGraph;
			if ( egi.appliesTo( entityType ) ) {
				results.add( egi );
			}
		}

		return results;
	}

	@Override
	public void close() {
		// anything to do ?
	}

}
