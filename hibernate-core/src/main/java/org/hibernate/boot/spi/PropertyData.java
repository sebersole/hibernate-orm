/*
 * Hibernate, Relational Persistence for Idiomatic Java
 *
 * License: GNU Lesser General Public License (LGPL), version 2.1 or later.
 * See the lgpl.txt file in the root directory or http://www.gnu.org/licenses/lgpl-2.1.html.
 */
package org.hibernate.boot.spi;

import org.hibernate.MappingException;
import org.hibernate.models.spi.ClassDetails;
import org.hibernate.models.spi.MemberDetails;
import org.hibernate.models.spi.TypeDetails;

/**
 * Details about an attribute as we process the {@linkplain org.hibernate.mapping boot model}.
 */
public interface PropertyData {

	/**
	 * @return default member access (whether field or property)
	 * @throws MappingException No getter or field found or wrong JavaBean spec usage
	 */
	AccessType getDefaultAccess();

	/**
	 * @return property name
	 * @throws MappingException No getter or field found or wrong JavaBean spec usage
	 */
	String getPropertyName() throws MappingException;

	/**
	 * Returns the returned class itself or the element type if an array
	 */
	TypeDetails getClassOrElementType() throws MappingException;

	/**
	 * Return the class itself
	 */
	TypeDetails getPropertyType() throws MappingException;

	/**
	 * Returns the returned class name itself or the element type if an array
	 */
	String getClassOrElementName() throws MappingException;

	/**
	 * Returns the returned class name itself
	 */
	String getTypeName() throws MappingException;

	/**
	 * Return the Hibernate mapping property
	 */
	MemberDetails getAttributeMember();

	/**
	 * Return the Class the property is declared on
	 * If the property is declared on a @MappedSuperclass,
	 * this class will be different than the PersistentClass's class
	 */
	ClassDetails getDeclaringClass();
}
