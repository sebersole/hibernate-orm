/*
 * Hibernate, Relational Persistence for Idiomatic Java
 *
 * License: GNU Lesser General Public License (LGPL), version 2.1 or later.
 * See the lgpl.txt file in the root directory or <http://www.gnu.org/licenses/lgpl-2.1.html>.
 */
package org.hibernate.annotations;

import java.lang.String;import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;

import org.hibernate.boot.model.TruthValue;

import static java.lang.annotation.ElementType.TYPE;
import static java.lang.annotation.RetentionPolicy.RUNTIME;

/**
 * Defines defaults local to the context in which the annotation is placed.  Generally,
 * the intention is for this to be defined with XML overrides.
 * <p/>
 * Initially only {@link ElementType#TYPE} is supported as a target; additionally the expectation
 * is that the TYPE is an entity class.  So the annotation defines defaults in effect for that
 * entity.
 * <p/>
 * Longer term we hope to allow this for mapped-superclasses as well.  And eventually targeting
 * PACKAGE would be great, allowing a hierarchical resolution of "local defaults" in effect for
 * a given entity.
 *
 * @author Steve Ebersole
 */
@java.lang.annotation.Target( TYPE )
@Retention( RUNTIME )
public @interface LocalDefaults {
	/**
	 * The default database catalog.
	 * <p/>
	 * Can also be set "globally" using {@code <persistence-unit-defaults/>} in XML
	 * or by calling {@link org.hibernate.boot.MetadataBuilder#applyImplicitCatalogName}.
	 * If defined here, would override "global" definition in the "local context"
	 */
	String catalog() default "";

	/**
	 * The default database schema.
	 * <p/>
	 * Can also be set "globally" using {@code <persistence-unit-defaults/>} in XML
	 * or by calling {@link org.hibernate.boot.MetadataBuilder#applyImplicitSchemaName}.
	 * If defined here, would override "global" definition in the "local context"
	 */
	String schema() default "";

	/**
	 * Identifies the default access strategy to use.  Can be any value recognized by
	 * {@link org.hibernate.property.access.spi.PropertyAccessStrategyResolver#resolvePropertyAccessStrategy},
	 * including the "short name" of any of the built-in strategies (see
	 * {@link org.hibernate.property.access.spi.BuiltInPropertyAccessStrategies}).
	 */
	String attributeAccessStrategy() default "";

	/**
	 * The default cascades to apply to the associations.
	 */
	CascadeType[] cascades() default {};

	/**
	 * Are singular associations (many-to-one, one-to-one) in this context lazy?
	 */
	TruthValue singularAssociationsLazy() default TruthValue.UNKNOWN;

	/**
	 * Are plural associations (many-to-one, one-to-one) in this context lazy?
	 */
	TruthValue pluralAssociationsLazy() default TruthValue.UNKNOWN;
}
