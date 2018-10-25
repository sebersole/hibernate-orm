/*
 * Hibernate, Relational Persistence for Idiomatic Java
 *
 * License: GNU Lesser General Public License (LGPL), version 2.1 or later.
 * See the lgpl.txt file in the root directory or <http://www.gnu.org/licenses/lgpl-2.1.html>.
 */
package org.hibernate.metamodel.spi;

import java.util.Set;
import java.util.function.Consumer;

import org.hibernate.EntityNameResolver;
import org.hibernate.Metamodel;
import org.hibernate.graph.spi.RootGraphImplementor;
import org.hibernate.metamodel.model.domain.spi.AllowableParameterType;
import org.hibernate.metamodel.model.domain.spi.EntityTypeDescriptor;
import org.hibernate.metamodel.model.domain.spi.ManagedTypeDescriptor;
import org.hibernate.metamodel.model.domain.spi.PersistentCollectionDescriptor;
import org.hibernate.sql.ast.produce.metamodel.spi.EntityValuedExpressableType;
import org.hibernate.type.spi.TypeConfiguration;

/**
 * An SPI extension to the JPA {@link javax.persistence.metamodel.Metamodel}
 * via ({@link org.hibernate.Metamodel}
 *
 * @apiNote Most of that functionality has been moved to {@link TypeConfiguration} instead,
 * accessible via {@link #getTypeConfiguration()}
 */
public interface MetamodelImplementor extends Metamodel {
	// todo (6.0) : would be awesome to expose the runtime database model here
	//		however that has some drawbacks that we need to discuss, namely
	//		that DatabaseModel holds state that we do not need beyond
	//		schema-management tooling - init-commands and aux-db-objects


	@Override
	<X> EntityTypeDescriptor<X> entity(Class<X> cls);

	@Override
	<X> EntityTypeDescriptor<X> entity(String entityName);

	@Override
	<X> ManagedTypeDescriptor<X> managedType(Class<X> cls);

	Set<PersistentCollectionDescriptor<?,?,?>> findCollectionsByEntityParticipant(EntityTypeDescriptor entityDescriptor);
	Set<String> findCollectionRolesByEntityParticipant(EntityTypeDescriptor entityDescriptor);

	void visitEntityNameResolvers(Consumer<EntityNameResolver> action);

	@Override
	<T> RootGraphImplementor<T> findRootGraph(String name);

	/**
	 * When a Class is referenced in a query, this method is invoked to resolve
	 * its set of valid "implementors" as a group.  The returned expressable type
	 * encapsulates all known implementors
	 */
	<T> EntityValuedExpressableType<T> resolveEntityReference(Class<T> javaType);
	EntityValuedExpressableType resolveEntityReference(String entityName);

	AllowableParameterType resolveAllowableParamterType(Class clazz);
	/**
	 * Close the Metamodel
	 */
	void close();
}
