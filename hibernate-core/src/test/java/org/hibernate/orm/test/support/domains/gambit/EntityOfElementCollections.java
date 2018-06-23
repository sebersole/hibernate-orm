/*
 * Hibernate, Relational Persistence for Idiomatic Java
 *
 * License: GNU Lesser General Public License (LGPL), version 2.1 or later
 * See the lgpl.txt file in the root directory or http://www.gnu.org/licenses/lgpl-2.1.html
 */
package org.hibernate.orm.test.support.domains.gambit;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import javax.persistence.ElementCollection;
import javax.persistence.Entity;
import javax.persistence.Id;

/**
 * @author Steve Ebersole
 */
@Entity
public class EntityOfElementCollections {
	private Integer id;

	private String name;

	private List<String> stringList;
	private Set<String> stringSet;
	private Map<String,String> stringMap;
	private List<Component> componentList;
	private Set<Component> componentSet;
	private Map<String,Component> componentMap;

	public EntityOfElementCollections() {
	}

	public EntityOfElementCollections(Integer id, String name) {
		this.id = id;
		this.name = name;

		this.stringList = new ArrayList<>();
		this.stringSet = new HashSet<>();
		this.stringMap = new HashMap<>();

		this.componentList = new ArrayList<>();
		this.componentSet = new HashSet<>();
		this.componentMap = new HashMap<>();
	}

	@Id
	public Integer getId() {
		return id;
	}

	public void setId(Integer id) {
		this.id = id;
	}

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	@ElementCollection
	public List<String> getStringList() {
		return stringList;
	}

	public void setStringList(List<String> stringList) {
		this.stringList = stringList;
	}

	@ElementCollection
	public Set<String> getStringSet() {
		return stringSet;
	}

	public void setStringSet(Set<String> stringSet) {
		this.stringSet = stringSet;
	}

	@ElementCollection
	public Map<String, String> getStringMap() {
		return stringMap;
	}

	public void setStringMap(Map<String, String> stringMap) {
		this.stringMap = stringMap;
	}

	@ElementCollection
	public List<Component> getComponentList() {
		return componentList;
	}

	public void setComponentList(List<Component> componentList) {
		this.componentList = componentList;
	}

	@ElementCollection
	public Set<Component> getComponentSet() {
		return componentSet;
	}

	public void setComponentSet(Set<Component> componentSet) {
		this.componentSet = componentSet;
	}

	@ElementCollection
	public Map<String, Component> getComponentMap() {
		return componentMap;
	}

	public void setComponentMap(Map<String, Component> componentMap) {
		this.componentMap = componentMap;
	}
}
