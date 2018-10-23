/*
 * Hibernate, Relational Persistence for Idiomatic Java
 *
 * License: GNU Lesser General Public License (LGPL), version 2.1 or later.
 * See the lgpl.txt file in the root directory or <http://www.gnu.org/licenses/lgpl-2.1.html>.
 */
package org.jboss.perf.model;

import java.util.HashMap;
import java.util.Map;

public class Zoo {
    private Long id;
    private String name;
    //private Classification classification;
    private Map<String, Human> directors = new HashMap<>();
    private Map<String, Animal> animals = new HashMap<>();
    private Map<String, Mammal> mammals = new HashMap<>();
    private Address address;

    public Zoo() {
    }

    public Zoo(String name, Address address) {
        this.name = name;
        this.address = address;
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public Map<String, Human> getDirectors() {
        return directors;
    }

    public void setDirectors(Map<String, Human> directors) {
        this.directors = directors;
    }

    public Map<String, Mammal> getMammals() {
        return mammals;
    }

    public void setMammals(Map<String, Mammal> mammals) {
        this.mammals = mammals;
    }

    public Map<String, Animal> getAnimals() {
        return animals;
    }

    public void setAnimals(Map<String, Animal> animals) {
        this.animals = animals;
    }

    public Address getAddress() {
        return address;
    }

    public void setAddress(Address address) {
        this.address = address;
    }

    /*
    public Classification getClassification() {
        return classification;
    }

    public void setClassification(Classification classification) {
        this.classification = classification;
    }
    */

    @Override
    public boolean equals(Object o) {
        if ( this == o ) {
            return true;
        }
        if ( !( o instanceof Zoo ) ) {
            return false;
        }

        Zoo zoo = (Zoo) o;

        if ( address != null ? !address.equals( zoo.address ) : zoo.address != null ) {
            return false;
        }
        return name != null ? name.equals( zoo.name ) : zoo.name == null;
    }

    @Override
    public int hashCode() {
        int result = name != null ? name.hashCode() : 0;
        result = 31 * result + ( address != null ? address.hashCode() : 0 );
        return result;
    }
}
