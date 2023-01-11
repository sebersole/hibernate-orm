/*
 * Hibernate, Relational Persistence for Idiomatic Java
 *
 * License: GNU Lesser General Public License (LGPL), version 2.1 or later.
 * See the lgpl.txt file in the root directory or http://www.gnu.org/licenses/lgpl-2.1.html.
 */
package org.hibernate.orm.boot.model.source.annotations;

import org.hibernate.annotations.Comment;

import jakarta.persistence.Basic;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.NamedQuery;
import jakarta.persistence.Table;

/**
 * @author Steve Ebersole
 */
@Entity(name = "SimpleColumnEntity")
@Table(name = "simple_entities")
@CustomAnnotation()
@NamedQuery( name = "abc", query = "select me" )
@NamedQuery( name = "xyz", query = "select you" )
public class SimpleColumnEntity {
	@Id
	@Column(name = "id")
	@Comment("SimpleColumnEntity PK column")
	private Integer id;

	@Basic
	@Column(name = "description", nullable = false, unique = true, insertable = false)
	@Comment("SimpleColumnEntity#name")
	private String name;

	@Basic
	@Column(table = "another_table", columnDefinition = "special_type")
	private String name2;

	private SimpleColumnEntity() {
		// for use by Hibernate
	}

	public SimpleColumnEntity(Integer id, String name) {
		this.id = id;
		this.name = name;
	}

	public Integer getId() {
		return id;
	}

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}
}
