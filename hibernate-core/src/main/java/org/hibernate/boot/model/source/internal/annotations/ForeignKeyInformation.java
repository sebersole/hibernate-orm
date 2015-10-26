/*
 * Hibernate, Relational Persistence for Idiomatic Java
 *
 * License: GNU Lesser General Public License (LGPL), version 2.1 or later.
 * See the lgpl.txt file in the root directory or <http://www.gnu.org/licenses/lgpl-2.1.html>.
 */
package org.hibernate.boot.model.source.internal.annotations;

import javax.persistence.ConstraintMode;

import org.hibernate.boot.model.source.internal.annotations.metadata.type.EntityBindingContext;

import org.jboss.jandex.AnnotationInstance;

/**
 * Describes information about a modeled foreign-key.  Specifics depend on context, but generaly
 * this is explicitly provided information.
 *
 * @author Brett Meyer
 * @author Steve Ebersole
 */
public class ForeignKeyInformation {
	public static ForeignKeyInformation from(AnnotationInstance sourceAnnotation, EntityBindingContext context) {
		if ( sourceAnnotation == null ) {
			return new ForeignKeyInformation();
		}

		final AnnotationInstance fkAnnotation = context.getTypedValueExtractor( AnnotationInstance.class ).extract(
				sourceAnnotation,
				"foreignKey"
		);
		final AnnotationInstance inverseFkAnnotation = context.getTypedValueExtractor( AnnotationInstance.class ).extract(
				sourceAnnotation,
				"inverseForeignKey"
		);
		return new ForeignKeyInformation(
				extractForeignKeyName( fkAnnotation, context ),
				extractInverseForeignKeyName( inverseFkAnnotation, context ),
				extractConstraintMode( fkAnnotation, context )
		);
	}

	private static String extractForeignKeyName(
			AnnotationInstance fkAnnotation,
			EntityBindingContext context) {
		if ( fkAnnotation == null ) {
			return null;
		}

		return context.getTypedValueExtractor( String.class ).extract( fkAnnotation, "name" );
	}

	private static String extractInverseForeignKeyName(
			AnnotationInstance inverseFkAnnotation,
			EntityBindingContext context) {
		if ( inverseFkAnnotation == null ) {
			return null;
		}

		return context.getTypedValueExtractor( String.class ).extract( inverseFkAnnotation, "name" );
	}

	private static ConstraintMode extractConstraintMode(
			AnnotationInstance fkAnnotation,
			EntityBindingContext context) {
		if ( fkAnnotation == null ) {
			return ConstraintMode.PROVIDER_DEFAULT;
		}

		return context.getTypedValueExtractor( ConstraintMode.class ).extract( fkAnnotation, "value" );
	}

	private final String explicitForeignKeyName;
	private final String inverseForeignKeyName;
	private final ConstraintMode constraintMode;

	// TODO: do we need inverseCreateForeignKeyConstraint?

	public ForeignKeyInformation() {
		this( null, null, ConstraintMode.PROVIDER_DEFAULT );
	}

	public ForeignKeyInformation(
			String explicitForeignKeyName,
			String inverseForeignKeyName,
			ConstraintMode constraintMode) {
		this.explicitForeignKeyName = explicitForeignKeyName;
		this.inverseForeignKeyName = inverseForeignKeyName;
		this.constraintMode = constraintMode;
	}

	public String getExplicitForeignKeyName() {
		return explicitForeignKeyName;
	}

	public String getInverseForeignKeyName() {
		return inverseForeignKeyName;
	}

	public ConstraintMode getConstraintMode() {
		return constraintMode;
	}
}
