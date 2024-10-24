/*
 * SPDX-License-Identifier: LGPL-2.1-or-later
 * Copyright Red Hat Inc. and Hibernate Authors
 */
package org.hibernate.annotations;

import java.lang.annotation.Retention;
import java.lang.annotation.Target;

import jakarta.persistence.DiscriminatorType;
import org.hibernate.type.AnyDiscriminatorValueStrategy;

import static java.lang.annotation.ElementType.ANNOTATION_TYPE;
import static java.lang.annotation.ElementType.FIELD;
import static java.lang.annotation.ElementType.METHOD;
import static java.lang.annotation.RetentionPolicy.RUNTIME;
import static org.hibernate.type.AnyDiscriminatorValueStrategy.AUTO;

/**
 * A simplified way to specify the type of the discriminator in an {@link Any}
 * mapping, using the JPA-defined {@link DiscriminatorType}. This annotation
 * must be used in combination with {@link jakarta.persistence.Column} to fully
 * describe the discriminator column for an {@code @Any} relationship.
 * <p>
 * {@code @AnyDiscriminator} is quite similar to
 * {@link jakarta.persistence.DiscriminatorColumn#discriminatorType()} in
 * single-table inheritance mappings, but it describes a discriminator held
 * along with the foreign key in the referring side of a discriminated
 * relationship.
 * <p>
 * This annotation may be used in conjunction with {@link JdbcType} or
 * {@link JdbcTypeCode} to more precisely specify the type mapping. On the
 * other hand, {@link JdbcType} or {@link JdbcTypeCode} may be used without
 * {@code @AnyDiscriminator}.
 *
 * @see Any
 *
 * @since 6.0
 */
@Target({METHOD, FIELD, ANNOTATION_TYPE})
@Retention( RUNTIME )
public @interface AnyDiscriminator {
	/**
	 * The type of the discriminator, as a JPA {@link DiscriminatorType}.
	 * For more precise specification of the type, use {@link JdbcType}
	 * or {@link JdbcTypeCode}.
	 */
	DiscriminatorType value() default DiscriminatorType.STRING;

	/**
	 * How the discriminator value should be handled in regard to explicit
	 * {@linkplain AnyDiscriminatorValue} mappings, if any.
	 *
	 * @since 7.0
	 */
	AnyDiscriminatorValueStrategy valueStrategy() default AUTO;

	/**
	 * Whether the entity's short-name should be used as the discriminator value
	 * (as opposed to its full-name) in the case of implicit value mapping.
	 */
	boolean implicitEntityShortName() default false;
}
