/*
 * Hibernate, Relational Persistence for Idiomatic Java
 *
 * License: GNU Lesser General Public License (LGPL), version 2.1 or later
 * See the lgpl.txt file in the root directory or http://www.gnu.org/licenses/lgpl-2.1.html
 */
package org.hibernate.metamodel;

import java.util.List;
import java.util.Set;
import java.util.function.Consumer;

import javax.persistence.EntityGraph;

import org.hibernate.EntityNameResolver;
import org.hibernate.graph.spi.EntityGraphImplementor;
import org.hibernate.metamodel.model.domain.NavigableRole;
import org.hibernate.metamodel.model.domain.spi.EmbeddedTypeDescriptor;
import org.hibernate.metamodel.model.domain.spi.EntityDescriptor;
import org.hibernate.metamodel.model.domain.spi.EntityHierarchy;
import org.hibernate.metamodel.model.domain.spi.MappedSuperclassDescriptor;
import org.hibernate.metamodel.model.domain.spi.PersistentCollectionDescriptor;

/**
 * @author Steve Ebersole
 */
public interface RuntimeModel {
	void visitEntityHierarchies(Consumer<EntityHierarchy> action);

	<T> EntityDescriptor<T> getEntityDescriptor(NavigableRole name) throws NotNavigableException;
	<T> EntityDescriptor<T> getEntityDescriptor(Class<T> javaType) throws NotNavigableException;
	<T> EntityDescriptor<T> getEntityDescriptor(String name) throws NotNavigableException;
	<T> EntityDescriptor<T> findEntityDescriptor(Class<T> javaType);
	<T> EntityDescriptor<T> findEntityDescriptor(String name);
	void visitEntityDescriptors(Consumer<EntityDescriptor<?>> action);

	<T> MappedSuperclassDescriptor<T> getMappedSuperclassDescriptor(NavigableRole name) throws NotNavigableException;
	<T> MappedSuperclassDescriptor<T> getMappedSuperclassDescriptor(Class<T> javaType) throws NotNavigableException;
	<T> MappedSuperclassDescriptor<T> getMappedSuperclassDescriptor(String name) throws NotNavigableException;
	<T> MappedSuperclassDescriptor<T> findMappedSuperclassDescriptor(Class<T> javaType);
	<T> MappedSuperclassDescriptor<T> findMappedSuperclassDescriptor(String name);
	void visitMappedSuperclassDescriptors(Consumer<MappedSuperclassDescriptor<?>> action);

	<T> EmbeddedTypeDescriptor<T> findEmbeddedDescriptor(Class<T> javaType);
	<T> EmbeddedTypeDescriptor<T> findEmbeddedDescriptor(NavigableRole name);
	<T> EmbeddedTypeDescriptor<T> findEmbeddedDescriptor(String name);
	void visitEmbeddedDescriptors(Consumer<EmbeddedTypeDescriptor<?>> action);

	<O,C,E> PersistentCollectionDescriptor<O,C,E> getCollectionDescriptor(NavigableRole name) throws NotNavigableException;
	<O,C,E> PersistentCollectionDescriptor<O,C,E> getCollectionDescriptor(String name) throws NotNavigableException;
	<O,C,E> PersistentCollectionDescriptor<O,C,E> findCollectionDescriptor(NavigableRole name);
	<O,C,E> PersistentCollectionDescriptor<O,C,E> findCollectionDescriptor(String name);
	void visitCollectionDescriptors(Consumer<PersistentCollectionDescriptor<?,?,?>> action);

	<T> EntityGraphImplementor<? super T> findEntityGraph(String name);
	<T> List<EntityGraph<? super T>> findEntityGraphForType(Class<T> baseType);
	<T> List<EntityGraph<? super T>> findEntityGraphForType(String baseTypeName);
	void visitEntityGraphs(Consumer<EntityGraph<?>> action);

	// todo (6.0) : default-for-type as well?
	//		aka:
	//<T> EntityGraphImplementor<T> defaultGraph(Class<T> entityJavaType);

	String getImportedName(String name);

	Set<EntityNameResolver> getEntityNameResolvers();
	void visitEntityNameResolvers(Consumer<EntityNameResolver> action);
}
