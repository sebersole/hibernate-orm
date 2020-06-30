/*
 * Hibernate, Relational Persistence for Idiomatic Java
 *
 * License: GNU Lesser General Public License (LGPL), version 2.1 or later
 * See the lgpl.txt file in the root directory or http://www.gnu.org/licenses/lgpl-2.1.html
 */

/**
 * Package defining "result graphs", which describe how Hibernate should
 * load an object graph (this is lazy, that is eager,etc).  This result-graph
 * is defined by {@link org.hibernate.sql.results.graph.DomainResult} and
 * {@link org.hibernate.sql.results.graph.Fetch}.  DomainResult defines all
 * objects to be read as root return objects, while a Fetch represents parts
 * of a DomainResult or a containing Fetch describing how the sub-parts should
 * be loaded.
 *
 * @implNote For details, see the `design/result-graphs.adoc`.
 *
 * Note that this is different from JPA entity-graph support; the name "graph"
 * can be confusing.
 */
package org.hibernate.sql.results.graph;