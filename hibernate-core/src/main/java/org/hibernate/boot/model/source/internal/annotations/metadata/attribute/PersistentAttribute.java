/*
 * Hibernate, Relational Persistence for Idiomatic Java
 *
 * License: GNU Lesser General Public License (LGPL), version 2.1 or later.
 * See the lgpl.txt file in the root directory or <http://www.gnu.org/licenses/lgpl-2.1.html>.
 */
package org.hibernate.boot.model.source.internal.annotations.metadata.attribute;

import java.util.Map;
import javax.persistence.AccessType;

import org.hibernate.boot.model.MemberDescriptor;
import org.hibernate.boot.model.source.internal.annotations.metadata.type.EntityBindingContext;
import org.hibernate.boot.model.source.internal.annotations.metadata.type.ManagedTypeMetadata;
import org.hibernate.boot.model.source.spi.AttributePath;
import org.hibernate.boot.model.source.spi.AttributeRole;
import org.hibernate.mapping.PropertyGeneration;

import org.jboss.jandex.AnnotationInstance;
import org.jboss.jandex.DotName;

/**
 * Represents the most basic definition of a persistent attribute.  At
 * the "next level up" we categorize attributes as either:<ul>
 *     <li>singular - {@link SingularAttribute}</li>
 *     <li>plural - {@link PluralAttribute}</li>
 * </ul>
 *
 * @author Steve Ebersole
 */
public interface PersistentAttribute extends Comparable<PersistentAttribute> {
	/**
	 * Access the name of the attribute being modeled.
	 *
	 * @return The attribute name
	 */
	String getName();

	/**
	 * The nature (category) of the attribute being modeled.
	 *
	 * @return The attribute nature
	 */
	AttributeNature getAttributeNature();

	/**
	 * The container (class) for the attribute
	 *
	 * @return The attribute container
	 */
	ManagedTypeMetadata getContainer();

	/**
	 * The member on the container that represents the attribute *as defined
	 * by AccessType*.  In other words this is the class member where we can
	 * look for annotations.  It is not necessarily the same as the backing
	 * member used to inject/extract values during runtime.
	 *
	 * @return The backing member
	 */
	MemberDescriptor getBackingMember();

	AnnotationInstance findAnnotation(DotName annotationName);
	/**
	 * This is a unique name for the attribute within the entire mapping.
	 * Generally roles are rooted at an entity name.  Each path in the role
	 * is separated by hash (#) signs.
	 * <p/>
	 * Practically speaking, this is used to uniquely identify collection
	 * and embeddable mappings.
	 *
	 * @return The attribute role.
	 */
	AttributeRole getRole();

	/**
	 * This is a unique name for the attribute within a top-level container.
	 * Mainly this is a normalized name used to apply AttributeOverride and
	 * AssociationOverride annotations.
	 *
	 * @return The attribute path
	 */
	AttributePath getPath();

	/**
	 * Obtain the AccessType in use for this attribute.  The AccessType defines where
	 * to look for annotations and is never {@code null}
	 *
	 * @return The AccessType, never {@code null}.
	 */
	AccessType getAccessType();

	/**
	 * Obtain the runtime accessor strategy.  This defines how we inject and
	 * extract values to/from the attribute at runtime.
	 *
	 * @return The runtime accessor strategy
	 */
	String getAccessorStrategy();

	/**
	 * Is this attribute lazy?
	 * <p/>
	 * NOTE : Hibernate currently only really supports laziness for
	 * associations. But JPA metadata defines lazy for basic attributes
	 * too; so we report that info at the basic level
	 *
	 * @return Whether the attribute it lazy.
	 */
	boolean isLazy();

	/**
	 * Do changes in this attribute trigger optimistic locking checks?
	 *
	 * @return {@code true} (the default) indicates the attribute is included
	 * in optimistic locking; {@code false} indicates it is not.
	 */
	boolean isIncludeInOptimisticLocking();

	// ugh
	PropertyGeneration getPropertyGeneration();

	boolean isOptional();

	boolean isInsertable();

	boolean isUpdatable();

	EntityBindingContext getContext();

	Map<DotName,AnnotationInstance> memberAnnotationMap();

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
