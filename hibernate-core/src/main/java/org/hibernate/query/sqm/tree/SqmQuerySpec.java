/*
 * Hibernate, Relational Persistence for Idiomatic Java
 *
 * License: GNU Lesser General Public License (LGPL), version 2.1 or later
 * See the lgpl.txt file in the root directory or http://www.gnu.org/licenses/lgpl-2.1.html
 */
package org.hibernate.query.sqm.tree;

import org.hibernate.query.sqm.tree.from.SqmFromClauseContainer;
import org.hibernate.query.sqm.tree.order.SqmOrderByClause;
import org.hibernate.query.sqm.tree.predicate.SqmWhereClause;
import org.hibernate.query.sqm.tree.select.SqmSelectClause;
import org.hibernate.query.sqm.tree.from.SqmFromClause;
import org.hibernate.query.sqm.tree.paging.SqmLimitOffsetClause;
import org.hibernate.query.sqm.tree.predicate.SqmWhereClauseContainer;

/**
 * Defines the commonality between a root query and a subquery.
 *
 * @author Steve Ebersole
 */
public class SqmQuerySpec implements SqmFromClauseContainer, SqmWhereClauseContainer {
	private SqmFromClause fromClause;
	private SqmSelectClause selectClause;
	private SqmWhereClause whereClause;
	private SqmOrderByClause orderByClause;
	private SqmLimitOffsetClause limitOffsetClause;

	// todo : group-by + having

	public SqmQuerySpec() {
		this.fromClause = new SqmFromClause();
		this.selectClause = new SqmSelectClause( false );
		this.whereClause = new SqmWhereClause(  );
		this.orderByClause = new SqmOrderByClause();
		this.limitOffsetClause = new SqmLimitOffsetClause();
	}

	public SqmQuerySpec(
			SqmFromClause fromClause,
			SqmSelectClause selectClause,
			SqmWhereClause whereClause,
			SqmOrderByClause orderByClause,
			SqmLimitOffsetClause limitOffsetClause) {
		this.fromClause = fromClause;
		this.selectClause = selectClause;
		this.whereClause = whereClause;
		this.orderByClause = orderByClause;
		this.limitOffsetClause = limitOffsetClause;
	}

	public SqmSelectClause getSelectClause() {
		return selectClause;
	}

	public void setSelectClause(SqmSelectClause selectClause) {
		this.selectClause = selectClause;
	}

	@Override
	public SqmFromClause getFromClause() {
		return fromClause;
	}

	public void setFromClause(SqmFromClause fromClause) {
		this.fromClause = fromClause;
	}

	public SqmWhereClause getWhereClause() {
		return whereClause;
	}

	public void setWhereClause(SqmWhereClause whereClause) {
		this.whereClause = whereClause;
	}

	public SqmOrderByClause getOrderByClause() {
		return orderByClause;
	}

	public void setOrderByClause(SqmOrderByClause orderByClause) {
		this.orderByClause = orderByClause;
	}

	public SqmLimitOffsetClause getLimitOffsetClause() {
		return limitOffsetClause;
	}

	public void setLimitOffsetClause(SqmLimitOffsetClause limitOffsetClause) {
		this.limitOffsetClause = limitOffsetClause;
	}
}
