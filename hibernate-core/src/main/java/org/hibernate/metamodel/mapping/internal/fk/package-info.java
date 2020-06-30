/*
 * Hibernate, Relational Persistence for Idiomatic Java
 *
 * License: GNU Lesser General Public License (LGPL), version 2.1 or later
 * See the lgpl.txt file in the root directory or http://www.gnu.org/licenses/lgpl-2.1.html
 */

/**
 * @asciidoctor
 * @asciidoc
 *
 * Throughout this package, concepts are defined using specific relational models
 * exhibiting each association cardinality.
 *
 *
 // orders( id, ... )
 // lines( id, order_id, ... )
 //
 // lines.order_id -> order.id
 //
 // orders( id, invoice_nbr, ... )
 // lines( id, order_key, ... )
 //
 // lines.order_key -> order.invoice_nbr
 //
 //
 // class Order {
 //		...
 //		@Id
 //		Integer id;

 //		@Basic
 //		String invoiceNumber;
 //
 //		@OneToMany( ..., mappedBy="order" )
 //		List<Line> lines;
 // }
 // class Line {
 //		...
 //		@ManyToOne( ... )
 //		@PropertyRef( "invoiceNumber" )
 //		Order order;
 // }
 // class Line {
 //		...
 //		@ManyToOne( ... )
 //		@PropertyRef( "invoiceNumber" )
 //		Order order;
 // }
 * ManyToOne( line#order )
 *
 * select o from Order o join fetch o.lines l join fetch l.order
 *
 * @author Steve Ebersole
 */
package org.hibernate.metamodel.mapping.internal.fk;