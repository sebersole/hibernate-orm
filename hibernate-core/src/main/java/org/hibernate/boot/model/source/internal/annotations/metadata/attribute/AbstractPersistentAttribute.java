/*
 * Hibernate, Relational Persistence for Idiomatic Java
 *
 * License: GNU Lesser General Public License (LGPL), version 2.1 or later.
 * See the lgpl.txt file in the root directory or <http://www.gnu.org/licenses/lgpl-2.1.html>.
 */
package org.hibernate.boot.model.source.internal.annotations.metadata.attribute;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import javax.persistence.AccessType;

import org.hibernate.boot.model.process.jandex.spi.HibernateDotNames;
import org.hibernate.boot.model.process.jandex.spi.JpaDotNames;
import org.hibernate.boot.model.MemberDescriptor;
import org.hibernate.boot.model.source.internal.annotations.metadata.type.EntityBindingContext;
import org.hibernate.boot.model.source.internal.annotations.metadata.type.ManagedTypeMetadata;
import org.hibernate.boot.model.source.internal.annotations.util.AnnotationBindingHelper;
import org.hibernate.boot.model.source.spi.AttributePath;
import org.hibernate.boot.model.source.spi.AttributeRole;
import org.hibernate.boot.model.source.spi.NaturalIdMutability;
import org.hibernate.internal.util.StringHelper;
import org.hibernate.mapping.PropertyGeneration;

import org.jboss.jandex.AnnotationInstance;
import org.jboss.jandex.AnnotationValue;
import org.jboss.jandex.DotName;
import org.jboss.logging.Logger;

/**
 * Base class for the different types of persistent attributes
 *
 * @author Steve Ebersole
 * @author Hardy Ferentschik
 */
public abstract class AbstractPersistentAttribute implements PersistentAttribute {
	private static final Logger log = Logger.getLogger( AbstractPersistentAttribute.class );

	private final ManagedTypeMetadata container;
	private final String attributeName;
	private final AttributePath attributePath;
	private final AttributeRole attributeRole;
	private final MemberDescriptor backingMember;
	private final AttributeNature attributeNature;
	private final AccessType accessType;
	private final String accessorStrategy;

	private final boolean includeInOptimisticLocking;

	private final boolean isId;
	private final boolean isVersioned;
	private final NaturalIdMutability naturalIdMutability;

	private final List<Column> columnValues;
	private final FormulaValue formulaValue;

	private final String checkCondition;

	protected AbstractPersistentAttribute(
			ManagedTypeMetadata container,
			String attributeName,
			AttributePath attributePath,
			AttributeRole attributeRole,
			MemberDescriptor backingMember,
			AttributeNature attributeNature,
			AccessType accessType,
			String accessorStrategy) {
		this.container = container;
		this.attributeName = attributeName;
		this.attributePath = attributePath;
		this.attributeRole = attributeRole;
		this.backingMember = backingMember;
		this.accessType = accessType;
		this.accessorStrategy = accessorStrategy;

		this.isId = determineWhetherIsId( backingMember );
		this.isVersioned = determineWhetherIsVersion( backingMember );
		this.naturalIdMutability = determineNaturalIdMutability( container, backingMember );

		this.attributeNature = attributeNature;

		this.includeInOptimisticLocking = determineInclusionInOptimisticLocking( backingMember );

		this.columnValues = extractColumnValues( backingMember );
		this.formulaValue = extractFormulaValue( backingMember );

		validateColumnsAndFormulas( columnValues, formulaValue );

		this.checkCondition = extractCheckCondition( backingMember );
	}

	@Override
	public Map<DotName,AnnotationInstance> memberAnnotationMap() {
		return container.getLocalBindingContext().getMemberAnnotationInstances( backingMember );
	}

	private boolean determineInclusionInOptimisticLocking(MemberDescriptor backingMember) {
		// NOTE : default is `true`, the annotation is used to opt out of inclusion

		final AnnotationInstance optimisticLockAnnotation = memberAnnotationMap().get( HibernateDotNames.OPTIMISTIC_LOCK );
		if ( optimisticLockAnnotation == null ) {
			return true;
		}

		final boolean excludedFromLocking = optimisticLockAnnotation.value( "excluded" ).asBoolean();
		if ( excludedFromLocking ) {
			if ( isId() || isVersion() ) {
				throw getContext().makeMappingException(
						"@OptimisticLock.exclude=true incompatible with @Id, @EmbeddedId and @Version : "
								+ backingMember.toString()
				);
			}
		}
		return !excludedFromLocking;
	}

	protected final boolean hasOptimisticLockAnnotation() {
		return memberAnnotationMap().containsKey( HibernateDotNames.OPTIMISTIC_LOCK );
	}

	private String extractCheckCondition(MemberDescriptor backingMember) {
		final AnnotationInstance checkAnnotation = memberAnnotationMap().get( HibernateDotNames.CHECK );
		if ( checkAnnotation == null ) {
			return null;
		}

		final AnnotationValue constraintsValue = checkAnnotation.value( "constraints" );
		if ( constraintsValue == null ) {
			return null;
		}

		final String constraintsString = constraintsValue.asString();
		if ( StringHelper.isEmpty( constraintsString ) ) {
			return null;
		}

		return constraintsString;
	}

	@SuppressWarnings("RedundantIfStatement")
	protected boolean determineWhetherIsId(MemberDescriptor backingMember) {
		// if this attribute has either @Id or @EmbeddedId, then it is an id attribute
		final AnnotationInstance idAnnotation = memberAnnotationMap().get( JpaDotNames.ID );
		if ( idAnnotation != null ) {
			validatePresenceOfIdAnnotation();
			if ( getContext().getTypeAnnotationInstances( backingMember.type().name() ).containsKey( JpaDotNames.EMBEDDABLE ) ) {
				log.warn(
						"Attribute was annotated with @Id, but attribute type was annotated as @Embeddable; " +
								"did you mean to use @EmbeddedId on the attribute rather than @Id?"
				);
			}
			return true;
		}

		final AnnotationInstance embeddedIdAnnotation =  memberAnnotationMap().get( JpaDotNames.EMBEDDED_ID );
		if ( embeddedIdAnnotation != null ) {
			validatePresenceOfEmbeddedIdAnnotation();
			return true;
		}

		return false;
	}

	protected void validatePresenceOfIdAnnotation() {
//		throw container.getLocalBindingContext().makeMappingException(
//				"Unexpected presence of @Id annotation : " + backingMember.toLoggableForm()
//		);
	}

	protected void validatePresenceOfEmbeddedIdAnnotation() {
//		throw container.getLocalBindingContext().makeMappingException(
//				"Unexpected presence of @EmbeddedId annotation : " + backingMember.toLoggableForm()
//		);
	}

	protected boolean determineWhetherIsVersion(MemberDescriptor backingMember) {
		final AnnotationInstance versionAnnotation = memberAnnotationMap().get( JpaDotNames.VERSION );
		if ( versionAnnotation != null ) {
			validatePresenceOfVersionAnnotation();
			return true;
		}

		return false;
	}

	protected void validatePresenceOfVersionAnnotation() {
		throw container.getLocalBindingContext().makeMappingException(
				"Unexpected presence of @Version annotation : " + backingMember.toString()
		);
	}

	protected NaturalIdMutability determineNaturalIdMutability(
			ManagedTypeMetadata container,
			MemberDescriptor backingMember) {
		final NaturalIdMutability result = AnnotationBindingHelper.determineNaturalIdMutability(
				container,
				backingMember
		);
		if ( result != NaturalIdMutability.NOT_NATURAL_ID ) {
			validatePresenceOfNaturalIdAnnotation();
		}
		return result;
	}

	protected void validatePresenceOfNaturalIdAnnotation() {
//		throw container.getLocalBindingContext().makeMappingException(
//				"Unexpected presence of @NaturalId annotation : " + backingMember.toString()
//		);
	}

	private List<Column> extractColumnValues(MemberDescriptor backingMember) {
		// @javax.persistence.Column
		final AnnotationInstance columnAnnotation = memberAnnotationMap().get( JpaDotNames.COLUMN );
		// @org.hibernate.annotations.Columns
		final AnnotationInstance columnsAnnotation = memberAnnotationMap().get( HibernateDotNames.COLUMNS );

		if ( columnAnnotation != null && columnsAnnotation != null ) {
			throw getContext().makeMappingException(
					"Should not mix @Column and @Columns annotations on same attribute : " +
							backingMember.toString()
			);
		}

		if ( columnAnnotation == null && columnsAnnotation == null ) {
			// try to avoid unnecessary List creation
			return Collections.emptyList();
		}

		final List<Column> columns = new ArrayList<Column>();
		if ( columnAnnotation != null ) {
			validatePresenceOfColumnAnnotation();
			columns.add( new Column( columnAnnotation ) );
		}
		else {
			validatePresenceOfColumnsAnnotation();
			final AnnotationInstance[] columnAnnotations = getContext().getTypedValueExtractor( AnnotationInstance[].class ).extract(
					columnsAnnotation,
					"columns"
			);
			for ( AnnotationInstance annotation : columnAnnotations ) {
				columns.add( new Column( annotation ) );
			}
		}
		return columns;
	}

	protected void validatePresenceOfColumnAnnotation() {
//		throw container.getLocalBindingContext().makeMappingException(
//				"Unexpected presence of @Column annotation : " + backingMember.toString()
//		);
	}

	protected void validatePresenceOfColumnsAnnotation() {
//		throw container.getLocalBindingContext().makeMappingException(
//				"Unexpected presence of @Columns annotation : " + backingMember.toString()
//		);
	}

	private FormulaValue extractFormulaValue(MemberDescriptor backingMember) {
		final AnnotationInstance formulaAnnotation = getContext().getMemberAnnotationInstances( backingMember ).get(
				HibernateDotNames.FORMULA
		);
		if ( formulaAnnotation == null ) {
			return null;
		}

		final String expression = formulaAnnotation.value().asString();
		if ( StringHelper.isEmpty( expression ) ) {
			throw getContext().makeMappingException( "Formula expression cannot be empty string" );
		}

		return new FormulaValue( null, expression );
	}

	private void validateColumnsAndFormulas(List<Column> columnValues, FormulaValue formulaValue) {
		if ( !columnValues.isEmpty() && formulaValue != null ) {
			throw getContext().makeMappingException(
					"Should not mix @Formula and @Column/@Columns annotations : " + backingMember.toString()
			);
		}
	}

	// ~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~
	// relevant to all attributes

	@Override
	public String getName() {
		return attributeName;
	}

	@Override
	public AttributeNature getAttributeNature() {
		return attributeNature;
	}

	@Override
	public ManagedTypeMetadata getContainer() {
		return container;
	}

	@Override
	public MemberDescriptor getBackingMember() {
		return backingMember;
	}

	@Override
	public AttributeRole getRole(){
		return attributeRole;
	}

	@Override
	public AttributePath getPath() {
		return attributePath;
	}

	@Override
	public AccessType getAccessType() {
		return accessType;
	}

	@Override
	public String getAccessorStrategy() {
		return accessorStrategy;
	}


	// ~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~
	// relevant to singular attributes

	public boolean isId() {
		return isId;
	}

	public boolean isVersion() {
		return isVersioned;
	}

	public NaturalIdMutability getNaturalIdMutability() {
		return naturalIdMutability;
	}

	public FormulaValue getFormulaValue() {
		return formulaValue;
	}

	public List<Column> getColumnValues() {
		return columnValues;
	}

	public EntityBindingContext getContext() {
		return container.getLocalBindingContext();
	}

	@Override
	public boolean isIncludeInOptimisticLocking() {
		return includeInOptimisticLocking;
	}

	public String getCheckCondition() {
		return checkCondition;
	}

	@Override
	public int compareTo(PersistentAttribute mappedProperty) {
		return attributeName.compareTo( mappedProperty.getName() );
	}

	@Override
	public String toString() {
		return "PersistentAttribute{attributeName='" + attributeName + '\'' + '}';
	}

	public abstract boolean isOptional();

	public abstract boolean isInsertable();

	public abstract boolean isUpdatable();

	public abstract PropertyGeneration getPropertyGeneration();

	@Override
	public AnnotationInstance findAnnotation(DotName annotationName) {
		return memberAnnotationMap().get( annotationName );
	}
}


