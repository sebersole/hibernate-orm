/*
 * Hibernate, Relational Persistence for Idiomatic Java
 *
 * License: GNU Lesser General Public License (LGPL), version 2.1 or later.
 * See the lgpl.txt file in the root directory or <http://www.gnu.org/licenses/lgpl-2.1.html>.
 */
package org.jboss.perf.model;

import java.util.Collection;
import java.util.Map;
import java.util.Set;

public class Human extends Mammal {

    private Name name;
    private String nickName;
    private Collection<Human> friends;
    private Collection<DomesticAnimal> pets;
    private Map<String, Human> family;
//    private double heightInches;

//    private BigInteger bigIntegerValue;
//    private BigDecimal bigDecimalValue;
//    private int intValue;
//    private float floatValue;

    private Set<String> nickNames;
    private Map<String, Address> addresses;

    public Collection<Human> getFriends() {
        return friends;
    }

    public void setFriends(Collection<Human> friends) {
        this.friends = friends;
    }

    public Collection<DomesticAnimal> getPets() {
        return pets;
    }

    public void setPets(Collection<DomesticAnimal> pets) {
        this.pets = pets;
    }

    public Name getName() {
        return name;
    }

    public void setName(Name name) {
        this.name = name;
    }

    public String getNickName() {
        return nickName;
    }

    public void setNickName(String nickName) {
        this.nickName = nickName;
    }

    /*
    public double getHeightInches() {
        return heightInches;
    }

    public void setHeightInches(double height) {
        this.heightInches = height;
    }
    */

    public Map<String, Human> getFamily() {
        return family;
    }

    public void setFamily(Map<String, Human> family) {
        this.family = family;
    }

    public Set<String> getNickNames() {
        return nickNames;
    }

    public void setNickNames(Set<String> nickNames) {
        this.nickNames = nickNames;
    }

    public Map<String, Address> getAddresses() {
        return addresses;
    }

    public void setAddresses(Map<String, Address> addresses) {
        this.addresses = addresses;
    }

    /*
    public BigDecimal getBigDecimalValue() {
        return bigDecimalValue;
    }

    public void setBigDecimalValue(BigDecimal bigDecimalValue) {
        this.bigDecimalValue = bigDecimalValue;
    }

    public BigInteger getBigIntegerValue() {
        return bigIntegerValue;
    }

    public void setBigIntegerValue(BigInteger bigIntegerValue) {
        this.bigIntegerValue = bigIntegerValue;
    }

    public float getFloatValue() {
        return floatValue;
    }

    public void setFloatValue(float floatValue) {
        this.floatValue = floatValue;
    }

    public int getIntValue() {
        return intValue;
    }

    public void setIntValue(int intValue) {
        this.intValue = intValue;
    }
    */
}
