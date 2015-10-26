/*
 * Hibernate, Relational Persistence for Idiomatic Java
 *
 * License: GNU Lesser General Public License (LGPL), version 2.1 or later.
 * See the lgpl.txt file in the root directory or <http://www.gnu.org/licenses/lgpl-2.1.html>.
 */
package org.hibernate.boot.model.source.internal.annotations.metadata.attribute;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import javax.persistence.AccessType;
import javax.persistence.FetchType;
import javax.persistence.GenerationType;

import org.hibernate.AnnotationException;
import org.hibernate.annotations.GenerationTime;
import org.hibernate.annotations.SourceType;
import org.hibernate.boot.model.process.jandex.spi.HibernateDotNames;
import org.hibernate.boot.model.process.jandex.spi.JpaDotNames;
import org.hibernate.boot.model.MemberDescriptor;
import org.hibernate.boot.model.source.internal.annotations.metadata.type.ManagedTypeMetadata;
import org.hibernate.boot.model.source.spi.AttributePath;
import org.hibernate.boot.model.source.spi.AttributeRole;
import org.hibernate.boot.model.source.spi.IdentifierGenerationInformation;
import org.hibernate.boot.model.source.spi.NaturalIdMutability;
import org.hibernate.mapping.PropertyGeneration;

import org.jboss.jandex.AnnotationInstance;
import org.jboss.jandex.AnnotationValue;
import org.jboss.jandex.DotName;

/**
 * Represents a singular persistent attribute that is non-composite and non-association.
 *
 * @author Steve Ebersole
 * @author Hardy Ferentschik
 */
public class BasicAttribute extends AbstractSingularAttribute {
	private final SourceType versionSourceType;
	private final IdentifierGenerationInformation identifierGenerationInformation;

	private boolean isLazy;
	private boolean isOptional;

	// todo :
	private final PropertyGeneration propertyGeneration;
	private final ColumnInclusion insertability;
	private final ColumnInclusion updateability;

	private final String customWriteFragment;
	private final String customReadFragment;

	public BasicAttribute(
			ManagedTypeMetadata container,
			String attributeName,
			AttributePath attributePath,
			AttributeRole attributeRole,
			MemberDescriptor backingMember,
			AccessType accessType,
			String accessStrategy) {
		super(
				container,
				attributeName,
				attributePath,
				attributeRole,
				backingMember,
				AttributeNature.BASIC,
				accessType,
				accessStrategy
		);

		this.insertability = new ColumnInclusion( container.canAttributesBeInsertable() );
		this.updateability = new ColumnInclusion( container.canAttributesBeUpdatable() );

		// a basic attribute can be a version attribute
		this.versionSourceType = isVersion() ? extractVersionSourceType( backingMember ) : null;

		if ( isId() ) {
			// an id must be unique and cannot be nullable
			for ( Column columnValue : getColumnValues() ) {
				columnValue.setNullable( false );
			}
			this.updateability.disable();
			this.identifierGenerationInformation = extractIdentifierGeneratorDefinition();
		}
		else {
			this.identifierGenerationInformation = null;
		}

		// @Basic
		final AnnotationInstance basicAnnotation = memberAnnotationMap().get( JpaDotNames.BASIC );
		if ( basicAnnotation == null ) {
			isLazy = false;
			isOptional = true;
		}
		else {
			FetchType fetchType = FetchType.EAGER;
			final AnnotationValue fetchValue = basicAnnotation.value( "fetch" );
			if ( fetchValue != null ) {
				fetchType = Enum.valueOf( FetchType.class, fetchValue.asEnum() );
			}
			this.isLazy = fetchType == FetchType.LAZY;

			boolean optional = true;
			final AnnotationValue optionalValue = basicAnnotation.value( "optional" );
			if ( optionalValue != null ) {
				optional = optionalValue.asBoolean();
			}
			this.isOptional = optional;
		}

		// @Generated
		// todo : hook in the new generation stuff
		final AnnotationInstance generatedAnnotation =memberAnnotationMap().get( HibernateDotNames.GENERATED );
		if ( generatedAnnotation == null ) {
			if ( isId() ) {
				this.updateability.disable();
				this.propertyGeneration = PropertyGeneration.INSERT;
			}
			else {
				this.propertyGeneration = PropertyGeneration.ALWAYS;
			}
		}
		else {
			this.insertability.disable();

			PropertyGeneration propertyGeneration = PropertyGeneration.ALWAYS;
			AnnotationValue generationTimeValue = generatedAnnotation.value();
			if ( generationTimeValue != null ) {
				GenerationTime genTime = Enum.valueOf( GenerationTime.class, generationTimeValue.asEnum() );
				if ( GenerationTime.ALWAYS.equals( genTime ) ) {
					this.updateability.disable();
					propertyGeneration = PropertyGeneration.parse( genTime.toString().toLowerCase() );
				}
			}
			this.propertyGeneration = propertyGeneration;
		}

		if ( getNaturalIdMutability() == NaturalIdMutability.IMMUTABLE ) {
			this.updateability.disable();
		}

		List<AnnotationInstance> columnTransformerAnnotations = collectColumnTransformerAnnotations( backingMember );
		String[] readWrite = createCustomReadWrite( columnTransformerAnnotations );
		this.customReadFragment = readWrite[0];
		this.customWriteFragment = readWrite[1];
	}

	@Override
	protected void validatePresenceOfIdAnnotation() {
		// ok here
	}

	@Override
	protected void validatePresenceOfEmbeddedIdAnnotation() {
		// ok here
	}

	@Override
	protected void validatePresenceOfVersionAnnotation() {
		// ok here
	}

	@Override
	protected void validatePresenceOfNaturalIdAnnotation() {
		// ok here
	}

	@Override
	protected void validatePresenceOfColumnAnnotation() {
		// ok here
	}

	@Override
	protected void validatePresenceOfColumnsAnnotation() {
		// ok here
	}

	private SourceType extractVersionSourceType(MemberDescriptor backingMember) {
		final AnnotationInstance sourceAnnotation = memberAnnotationMap().get( HibernateDotNames.SOURCE );
		if ( sourceAnnotation == null ) {
			return null;
		}

		return getContext().getTypedValueExtractor( SourceType.class ).extract(
				sourceAnnotation,
				"value"
		);
	}

	private IdentifierGenerationInformation extractIdentifierGeneratorDefinition() {
		final AnnotationInstance generatedValueAnnotation = memberAnnotationMap().get( JpaDotNames.GENERATED_VALUE );
		if ( generatedValueAnnotation == null ) {
			return null;
		}

		final GenerationType generationType = getContext().getTypedValueExtractor( GenerationType.class ).extract(
				generatedValueAnnotation,
				"strategy"
		);
		final String localName = getContainer().getLocalBindingContext().getTypedValueExtractor( String.class ).extract(
				generatedValueAnnotation,
				"generator"
		);
		return new IdentifierGenerationInformation( generationType, localName );
	}

	private List<AnnotationInstance> collectColumnTransformerAnnotations(MemberDescriptor backingMember) {
		List<AnnotationInstance> allColumnTransformerAnnotations = new ArrayList<AnnotationInstance>();

		final AnnotationInstance columnTransformerAnnotation = memberAnnotationMap().get(
				HibernateDotNames.COLUMN_TRANSFORMER
		);
		final AnnotationInstance columnTransformersAnnotations = memberAnnotationMap().get(
				HibernateDotNames.COLUMN_TRANSFORMERS
		);

		if ( columnTransformerAnnotation != null && columnTransformersAnnotations != null ) {
			throw getContext().makeMappingException(
					"Should not mix @ColumnTransformer and @ColumnTransformers annotations " +
							"on same attribute : " + backingMember.toString()
			);
		}

		if ( columnTransformersAnnotations != null ) {
			AnnotationInstance[] annotationInstances = allColumnTransformerAnnotations.get( 0 ).value().asNestedArray();
			allColumnTransformerAnnotations.addAll( Arrays.asList( annotationInstances ) );
		}

		if ( columnTransformerAnnotation != null ) {
			allColumnTransformerAnnotations.add( columnTransformerAnnotation );
		}
		return allColumnTransformerAnnotations;
	}

	private String[] createCustomReadWrite(List<AnnotationInstance> columnTransformerAnnotations) {
		String[] readWrite = new String[2];

		boolean alreadyProcessedForColumn = false;
		for ( AnnotationInstance annotationInstance : columnTransformerAnnotations ) {
			String forColumn = annotationInstance.value( "forColumn" ) == null
					? null
					: annotationInstance.value( "forColumn" ).asString();
			if ( forColumn != null && !isColumnPresentForTransformer( forColumn ) ) {
				continue;
			}

			if ( alreadyProcessedForColumn ) {
				throw new AnnotationException( "Multiple definition of read/write conditions for column " + getRole() );
			}

			readWrite[0] = annotationInstance.value( "read" ) == null
					? null
					: annotationInstance.value( "read" ).asString();
			readWrite[1] = annotationInstance.value( "write" ) == null
					? null :
					annotationInstance.value( "write" ).asString();

			alreadyProcessedForColumn = true;
		}
		return readWrite;
	}

	private boolean isColumnPresentForTransformer(final String forColumn) {
		assert forColumn != null;
		List<Column> columns = getColumnValues();
		for ( final Column column : columns ) {
			if ( forColumn.equals( column.getName() ) ) {
				return true;
			}
		}
		return forColumn.equals( getName() );
	}

	@Override
	public AnnotationInstance findAnnotation(DotName annotationName) {
		return memberAnnotationMap().get( annotationName );
	}

	@Override
	public boolean isLazy() {
		return isLazy;
	}

	@Override
	public boolean isOptional() {
		return isOptional;
	}

	@Override
	public boolean isInsertable() {
		return insertability.shouldInclude();
	}

	@Override
	public boolean isUpdatable() {
		return updateability.shouldInclude();
	}

	@Override
	public PropertyGeneration getPropertyGeneration() {
		return propertyGeneration;
	}

	public String getCustomWriteFragment() {
		return customWriteFragment;
	}

	public String getCustomReadFragment() {
		return customReadFragment;
	}

	public IdentifierGenerationInformation getIdentifierGeneratorInformation() {
		// NOTE : this works with the 5.0 idea of org.hibernate.boot.model.IdGeneratorStrategyInterpreter
		return identifierGenerationInformation;
	}

	public SourceType getVersionSourceType() {
		return versionSourceType;
	}

	public IdentifierGenerationInformation getIdentifierGenerationInformation() {
		return identifierGenerationInformation;
	}

	@Override
	public String toString() {
		return "BasicAttribute{name=" + getBackingMember().toString() + '}';
	}
}
