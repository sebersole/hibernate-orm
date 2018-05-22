/*
 * Hibernate, Relational Persistence for Idiomatic Java
 *
 * License: GNU Lesser General Public License (LGPL), version 2.1 or later
 * See the lgpl.txt file in the root directory or http://www.gnu.org/licenses/lgpl-2.1.html
 */
package org.hibernate.query.spi;

import java.util.Map;

import org.hibernate.HibernateException;
import org.hibernate.Incubating;
import org.hibernate.query.named.spi.NamedCallableQueryMemento;
import org.hibernate.query.named.spi.NamedHqlQueryMemento;
import org.hibernate.query.named.spi.NamedNativeQueryMemento;

/**
 * Repository for named query-related objects
 *
 * @author Steve Ebersole
 */
@Incubating
public interface NamedQueryRepository {
	void registerNamedHqlQueryDescriptor(String name, NamedHqlQueryMemento descriptor);
	NamedHqlQueryMemento getNamedHqlDescriptor(String queryName);

	void registerNamedNativeQueryDescriptor(String name, NamedNativeQueryMemento descriptor);
	NamedNativeQueryMemento getNamedNativeDescriptor(String queryName);

	void registerNamedCallableQueryDescriptor(String name, NamedCallableQueryMemento memento);
	NamedCallableQueryMemento getNamedCallableQueryDescriptor(String name);

	ResultSetMappingDescriptor getResultSetMappingDescriptor(String mappingName);

	Map<String,HibernateException> checkNamedQueries(QueryEngine queryPlanCache);

	void close();
}
