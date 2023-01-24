/*
 * Hibernate, Relational Persistence for Idiomatic Java
 *
 * License: GNU Lesser General Public License (LGPL), version 2.1 or later.
 * See the lgpl.txt file in the root directory or http://www.gnu.org/licenses/lgpl-2.1.html.
 */
package org.hibernate.orm.boot.model.annotations;

import jakarta.persistence.Basic;
import jakarta.persistence.Entity;

/**
 * @author Steve Ebersole
 */
@Entity
public class JoinedLeaf extends JoinedRoot {
	@Basic
	private String summary;

	protected JoinedLeaf() {
		// for Hibernate use
	}

	public JoinedLeaf(Integer id, String name, String summary) {
		super( id, name );
		this.summary = summary;
	}

	public String getSummary() {
		return summary;
	}

	public void setSummary(String summary) {
		this.summary = summary;
	}
}
