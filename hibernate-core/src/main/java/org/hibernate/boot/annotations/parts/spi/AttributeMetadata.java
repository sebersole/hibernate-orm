/*
 * Hibernate, Relational Persistence for Idiomatic Java
 *
 * License: GNU Lesser General Public License (LGPL), version 2.1 or later.
 * See the lgpl.txt file in the root directory or http://www.gnu.org/licenses/lgpl-2.1.html.
 */
package org.hibernate.boot.annotations.parts.spi;

import org.hibernate.boot.annotations.source.spi.MemberDetails;

/**
 * @author Steve Ebersole
 */
public class AttributeMetadata {
	private final String name;
	private final MemberDetails member;

	public AttributeMetadata(String name, MemberDetails member) {
		this.name = name;
		this.member = member;
	}

	public String getName() {
		return name;
	}

	public MemberDetails getMember() {
		return member;
	}

	/**
	 * An enum defining the nature (categorization) of a persistent attribute.
	 */
	enum AttributeNature {
		BASIC,
		EMBEDDED,
		ANY,
		TO_ONE,
		PLURAL
	}
}
