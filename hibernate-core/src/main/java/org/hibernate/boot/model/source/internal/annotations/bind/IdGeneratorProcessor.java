/*
 * Hibernate, Relational Persistence for Idiomatic Java
 *
 * License: GNU Lesser General Public License (LGPL), version 2.1 or later.
 * See the lgpl.txt file in the root directory or <http://www.gnu.org/licenses/lgpl-2.1.html>.
 */

/*
 * Hibernate, Relational Persistence for Idiomatic Java
 *
 * Copyright (c) 2012, Red Hat Inc. or third-party contributors as
 * indicated by the @author tags or express copyright attribution
 * statements applied by the authors.  All third-party contributions are
 * distributed under license by Red Hat Inc.
 *
 * This copyrighted material is made available to anyone wishing to use, modify,
 * copy, or redistribute it subject to the terms and conditions of the GNU
 * Lesser General Public License, as published by the Free Software Foundation.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY
 * or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU Lesser General Public License
 * for more details.
 *
 * You should have received a copy of the GNU Lesser General Public License
 * along with this distribution; if not, write to:
 * Free Software Foundation, Inc.
 * 51 Franklin Street, Fifth Floor
 * Boston, MA  02110-1301  USA
 */
package org.hibernate.boot.model.source.internal.annotations.bind;

import java.util.List;
import javax.persistence.GenerationType;
import javax.persistence.SequenceGenerator;

import org.hibernate.annotations.GenericGenerator;
import org.hibernate.annotations.GenericGenerators;
import org.hibernate.boot.model.process.jandex.spi.HibernateDotNames;
import org.hibernate.boot.model.process.jandex.spi.JpaDotNames;
import org.hibernate.boot.model.IdGeneratorStrategyInterpreter.GeneratorNameDeterminationContext;
import org.hibernate.boot.model.IdentifierGeneratorDefinition;
import org.hibernate.boot.model.source.internal.annotations.RootAnnotationBindingContext;
import org.hibernate.boot.model.source.internal.annotations.util.AnnotationBindingHelper;
import org.hibernate.id.PersistentIdentifierGenerator;
import org.hibernate.internal.CoreLogging;
import org.hibernate.internal.CoreMessageLogger;

import org.jboss.jandex.AnnotationInstance;

/**
 * Binds {@link SequenceGenerator}, {@link javax.persistence.TableGenerator}, {@link GenericGenerator}, and
 * {@link GenericGenerators} annotations.
 *
 * @author Hardy Ferentschik
 * @author Steve Ebersole
 */
public class IdGeneratorProcessor {
	private static final CoreMessageLogger LOG = CoreLogging.messageLogger( IdGeneratorProcessor.class );

	private IdGeneratorProcessor() {
	}

	public static void bind(RootAnnotationBindingContext bindingContext) {
		// @SequeceGenerators
		for ( AnnotationInstance sequenceGeneratorAnnotation :
				bindingContext.getJandexIndex().getAnnotations( JpaDotNames.SEQUENCE_GENERATOR ) ) {
			bindSequenceGenerator( sequenceGeneratorAnnotation, bindingContext );
		}

		// @TableGenerators
		for ( AnnotationInstance tableGeneratorAnnotation :
				bindingContext.getJandexIndex().getAnnotations( JpaDotNames.TABLE_GENERATOR ) ) {
			bindTableGenerator( tableGeneratorAnnotation, bindingContext );
		}

		final List<AnnotationInstance> genericGeneratorAnnotations = AnnotationBindingHelper.collectionAnnotations(
				bindingContext.getJandexIndex(),
				HibernateDotNames.GENERIC_GENERATOR,
				HibernateDotNames.GENERIC_GENERATORS
		);
		for ( AnnotationInstance genericGeneratorAnnotation : genericGeneratorAnnotations ) {
			bindGenericGenerator( genericGeneratorAnnotation, bindingContext );
		}
	}

	private static final GeneratorNameDeterminationContext NO_CLASS_ACCESS = new GeneratorNameDeterminationContext() {
		@Override
		public Class getIdType() {
			return null;
		}
	};

	private static void bindSequenceGenerator(
			AnnotationInstance sequenceGeneratorAnnotation,
			RootAnnotationBindingContext bindingContext) {
		IdentifierGeneratorDefinition.Builder definitionBuilder = new IdentifierGeneratorDefinition.Builder();

		if ( bindingContext.getMappingDefaults().getImplicitSchemaName() != null ) {
			definitionBuilder.addParam(
					PersistentIdentifierGenerator.SCHEMA,
					bindingContext.getMappingDefaults().getImplicitSchemaName()
			);
		}

		if ( bindingContext.getMappingDefaults().getImplicitCatalogName() != null ) {
			definitionBuilder.addParam(
					PersistentIdentifierGenerator.CATALOG,
					bindingContext.getMappingDefaults().getImplicitCatalogName()
			);
		}

		definitionBuilder.setName( bindingContext.stringExtractor.extract( sequenceGeneratorAnnotation, "name" ) );
		definitionBuilder.setStrategy(
				bindingContext.getBuildingOptions().getIdGenerationTypeInterpreter().determineGeneratorName(
						GenerationType.SEQUENCE,
						NO_CLASS_ACCESS
				)
		);

		LOG.tracef( "Add sequence generator with name: %s", definitionBuilder.getName() );
		bindingContext.getMetadataCollector().addIdentifierGenerator( definitionBuilder.build() );
	}

	private static void bindTableGenerator(
			AnnotationInstance tableGeneratorAnnotation,
			RootAnnotationBindingContext bindingContext) {
		IdentifierGeneratorDefinition.Builder definitionBuilder = new IdentifierGeneratorDefinition.Builder();

		if ( bindingContext.getMappingDefaults().getImplicitSchemaName() != null ) {
			definitionBuilder.addParam(
					PersistentIdentifierGenerator.SCHEMA,
					bindingContext.getMappingDefaults().getImplicitSchemaName()
			);
		}

		if ( bindingContext.getMappingDefaults().getImplicitCatalogName() != null ) {
			definitionBuilder.addParam(
					PersistentIdentifierGenerator.CATALOG,
					bindingContext.getMappingDefaults().getImplicitCatalogName()
			);
		}

		definitionBuilder.setName( bindingContext.stringExtractor.extract( tableGeneratorAnnotation, "name" ) );
		definitionBuilder.setStrategy(
				bindingContext.getBuildingOptions().getIdGenerationTypeInterpreter().determineGeneratorName(
						GenerationType.TABLE,
						NO_CLASS_ACCESS
				)
		);

		LOG.tracef( "Add table generator with name: %s", definitionBuilder.getName() );
		bindingContext.getMetadataCollector().addIdentifierGenerator( definitionBuilder.build() );
	}

	private static void bindGenericGenerator(
			AnnotationInstance genericGeneratorAnnotation,
			RootAnnotationBindingContext bindingContext) {
		IdentifierGeneratorDefinition.Builder definitionBuilder = new IdentifierGeneratorDefinition.Builder();

		if ( bindingContext.getMappingDefaults().getImplicitSchemaName() != null ) {
			definitionBuilder.addParam(
					PersistentIdentifierGenerator.SCHEMA,
					bindingContext.getMappingDefaults().getImplicitSchemaName()
			);
		}

		if ( bindingContext.getMappingDefaults().getImplicitCatalogName() != null ) {
			definitionBuilder.addParam(
					PersistentIdentifierGenerator.CATALOG,
					bindingContext.getMappingDefaults().getImplicitCatalogName()
			);
		}

		definitionBuilder.setName( bindingContext.stringExtractor.extract( genericGeneratorAnnotation, "name" ) );
		definitionBuilder.setStrategy(
				bindingContext.stringExtractor.extract(
						genericGeneratorAnnotation,
						"strategy"
				)
		);

		for ( AnnotationInstance parameterAnnotation :
				bindingContext.nestedArrayExtractor.extract( genericGeneratorAnnotation, "value" ) ) {
			definitionBuilder.addParam(
					bindingContext.stringExtractor.extract( parameterAnnotation, "name" ),
					bindingContext.stringExtractor.extract( parameterAnnotation, "value" )
			);
		}

		LOG.tracef(
				"Add generic generator to inflight-metadata [name: %s, strategy: %s]",
				definitionBuilder.getName(),
				definitionBuilder.getStrategy()
		);
		bindingContext.getMetadataCollector().addIdentifierGenerator( definitionBuilder.build() );
	}

}
