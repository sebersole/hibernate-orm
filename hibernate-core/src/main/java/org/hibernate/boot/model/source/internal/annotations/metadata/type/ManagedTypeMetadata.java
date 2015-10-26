/*
 * Hibernate, Relational Persistence for Idiomatic Java
 *
 * License: GNU Lesser General Public License (LGPL), version 2.1 or later.
 * See the lgpl.txt file in the root directory or <http://www.gnu.org/licenses/lgpl-2.1.html>.
 */
package org.hibernate.boot.model.source.internal.annotations.metadata.type;

import java.lang.reflect.Modifier;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Set;
import javax.persistence.AccessType;

import org.hibernate.boot.MappingException;
import org.hibernate.boot.model.process.jandex.spi.HibernateDotNames;
import org.hibernate.boot.model.process.jandex.spi.JpaDotNames;
import org.hibernate.boot.jaxb.Origin;
import org.hibernate.boot.jaxb.SourceType;
import org.hibernate.boot.model.MemberDescriptor;
import org.hibernate.boot.model.source.internal.annotations.RootAnnotationBindingContext;
import org.hibernate.boot.model.source.internal.annotations.ConvertConversionInfo;
import org.hibernate.boot.model.source.internal.annotations.metadata.attribute.AssociationOverride;
import org.hibernate.boot.model.source.internal.annotations.metadata.attribute.AttributeFactory;
import org.hibernate.boot.model.source.internal.annotations.metadata.attribute.AttributeOverride;
import org.hibernate.boot.model.source.internal.annotations.metadata.attribute.OverrideAndConverterCollector;
import org.hibernate.boot.model.source.internal.annotations.metadata.attribute.PersistentAttribute;
import org.hibernate.boot.model.source.spi.AttributePath;
import org.hibernate.boot.model.source.spi.AttributeRole;
import org.hibernate.boot.model.source.spi.NaturalIdMutability;
import org.hibernate.internal.CoreLogging;
import org.hibernate.internal.CoreMessageLogger;
import org.hibernate.internal.util.StringHelper;
import org.hibernate.tuple.entity.PojoEntityTuplizer;

import org.jboss.jandex.AnnotationInstance;
import org.jboss.jandex.AnnotationValue;
import org.jboss.jandex.ClassInfo;
import org.jboss.jandex.DotName;

/**
 * Models metadata about what JPA calls a {@link javax.persistence.metamodel.ManagedType}.
 * <p/>
 * Concretely, may be:<ul>
 *     <li>
 *         IdentifiableTypeMetadata<ul>
 *             <li>EntityTypeMetadata</li>
 *             <li>MappedSuperclassTypeMetadata</li>
 *         </ul>
 *     </li>
 *     <li>
 *         EmbeddableTypeMetadata
 *     </li>
 * </ul>
 *
 * @author Hardy Ferentschik
 * @author Steve Ebersole
 * @author Brett Meyer
 */
public abstract class ManagedTypeMetadata implements OverrideAndConverterCollector {
    private static final CoreMessageLogger LOG = CoreLogging.messageLogger( ManagedTypeMetadata.class );

	private final ClassInfo classInfo;
	private final EntityBindingContext localBindingContext;

	private final ManagedTypeMetadata superType;
	private Set<ManagedTypeMetadata> subclasses;
	private final AttributePath attributePathBase;
	private final AttributeRole attributeRoleBase;

	private final AccessType classLevelAccessType;
	private final String explicitClassLevelAccessorStrategy;
	private final String customTuplizerClassName;

	private Map<String, PersistentAttribute> persistentAttributeMap;


	/**
	 * This form is intended for construction of the root of an entity hierarchy,
	 * and its MappedSuperclasses
	 *
	 * @param classInfo Metadata for the Entity/MappedSuperclass
	 * @param defaultAccessType The default AccessType for the entity hierarchy
	 * @param bindingContext The binding context
	 */
	public ManagedTypeMetadata(
			ClassInfo classInfo,
			AccessType defaultAccessType,
			boolean isRootEntity,
			RootAnnotationBindingContext bindingContext) {
		this.classInfo =  classInfo;
		this.localBindingContext = new EntityBindingContext( bindingContext, this );

		this.classLevelAccessType = determineAccessType( defaultAccessType );
		this.explicitClassLevelAccessorStrategy = determineExplicitAccessorStrategy( null );
		this.customTuplizerClassName = determineCustomTuplizer();

		// walk up
		this.superType = walkRootSuperclasses( classInfo, defaultAccessType, bindingContext );
		if ( superType != null ) {
			superType.addSubclass( this );
		}

		if ( isRootEntity ) {
			// walk down
			walkSubclasses( classInfo, (IdentifiableTypeMetadata) this, defaultAccessType, bindingContext );
		}

		this.attributeRoleBase = new AttributeRole( classInfo.name().toString() );
		this.attributePathBase = new AttributePath();
	}

	private String determineCustomTuplizer() {
		final AnnotationInstance tuplizerAnnotation = typeAnnotationMap().get( HibernateDotNames.TUPLIZER );

		if ( tuplizerAnnotation != null ) {
			final AnnotationValue implValue = tuplizerAnnotation.value( "impl" );
			if ( implValue != null ) {
				final String value = implValue.asString();
				if ( StringHelper.isNotEmpty( value ) ) {
					return value;
				}
			}
		}

		return PojoEntityTuplizer.class.getName();
	}

	private void addSubclass(ManagedTypeMetadata subclass) {
		if ( subclasses == null ) {
			subclasses = new HashSet<ManagedTypeMetadata>();
		}
		subclasses.add( subclass );
	}

	/**
	 * This form is intended for construction of entity hierarchy subclasses.
	 *
	 * @param classInfo Metadata for the Entity/MappedSuperclass
	 * @param superType Metadata for the super type
	 * @param defaultAccessType The default AccessType for the entity hierarchy
	 * @param bindingContext The binding context
	 */
	public ManagedTypeMetadata(
			ClassInfo classInfo,
			ManagedTypeMetadata superType,
			AccessType defaultAccessType,
			RootAnnotationBindingContext bindingContext) {
		this.classInfo =  classInfo;
		this.localBindingContext = new EntityBindingContext( bindingContext, this );

		this.classLevelAccessType = determineAccessType( defaultAccessType );

		// does the mapping defaults define a default?
		// 		trouble is that the defaults do define a default.. ALL THE TIME
		//final String mappingDefault = bindingContext.getMappingDefaults().getPropertyAccessorName();
		//if
		this.explicitClassLevelAccessorStrategy = determineExplicitAccessorStrategy( null );

		this.customTuplizerClassName = determineCustomTuplizer();

		this.superType = superType;

		this.attributeRoleBase = new AttributeRole( classInfo.name().toString() );
		this.attributePathBase = new AttributePath();
	}

	/**
	 * This form is used to create Embedded references
	 *
	 * @param classInfo The Embeddable descriptor
	 * @param attributeRoleBase The base for the roles of attributes created *from* here
	 * @param attributePathBase The base for the paths of attributes created *from* here
	 * @param defaultAccessType The default AccessType from the context of this Embedded
	 * @param defaultAccessorStrategy The default accessor strategy from the context of this Embedded
	 * @param bindingContext The binding context
	 */
	public ManagedTypeMetadata(
			ClassInfo classInfo,
			AttributeRole attributeRoleBase,
			AttributePath attributePathBase,
			AccessType defaultAccessType,
			String defaultAccessorStrategy,
			RootAnnotationBindingContext bindingContext) {
		this.classInfo = classInfo;
		this.localBindingContext = new EntityBindingContext( bindingContext, this );

		this.classLevelAccessType = determineAccessType( defaultAccessType );
		this.explicitClassLevelAccessorStrategy = determineExplicitAccessorStrategy( defaultAccessorStrategy );
		this.customTuplizerClassName = determineCustomTuplizer();

		this.superType = null;

		this.attributeRoleBase = attributeRoleBase;
		this.attributePathBase = attributePathBase;
	}

	private AccessType determineAccessType(AccessType defaultAccessType) {
		final AnnotationInstance localAccessAnnotation = typeAnnotationMap().get( JpaDotNames.ACCESS );
		if ( localAccessAnnotation != null ) {
			final AnnotationValue accessTypeValue = localAccessAnnotation.value();
			if ( accessTypeValue != null ) {
				return AccessType.valueOf( accessTypeValue.asEnum() );
			}
		}

		// legacy alert!
		// In the absence of a JPA @Access annotation, we interpret our custom
		// @AttributeAccessor as indicating access *if* it is "field" or "property"
		final AnnotationInstance accessorAnnotation = typeAnnotationMap().get( HibernateDotNames.ATTRIBUTE_ACCESSOR );
		if ( accessorAnnotation != null ) {
			final AnnotationValue strategyValue = accessorAnnotation.value();
			if ( strategyValue != null ) {
				final String strategyName = strategyValue.asString();
				if ( StringHelper.isNotEmpty( strategyName ) ) {
					if ( "field".equals( strategyName ) ) {
						return AccessType.FIELD;
					}
					else if ( "property".equals( strategyName ) ) {
						return AccessType.PROPERTY;
					}
				}
			}
		}

		return defaultAccessType;
	}

	private String determineExplicitAccessorStrategy(String defaultValue) {
		// look for a @AttributeAccessor annotation
		final AnnotationInstance attributeAccessorAnnotation = typeAnnotationMap().get( HibernateDotNames.ATTRIBUTE_ACCESSOR );
		if ( attributeAccessorAnnotation != null ) {
			String explicitAccessorStrategy = attributeAccessorAnnotation.value().asString();
			if ( StringHelper.isEmpty( explicitAccessorStrategy ) ) {
				LOG.warnf(
						"Class [%s] specified @AttributeAccessor with empty value",
						classInfo.name().toString()
				);
			}
			else {
				return explicitAccessorStrategy;
			}
		}

		final AnnotationInstance localAccessAnnotation = typeAnnotationMap().get( JpaDotNames.ACCESS );
		if ( localAccessAnnotation != null ) {
			final AnnotationValue accessTypeValue = localAccessAnnotation.value();
			if ( accessTypeValue != null ) {
				return AccessType.valueOf( accessTypeValue.asEnum() ).name().toLowerCase();
			}
		}

		return defaultValue;
	}

	private IdentifiableTypeMetadata walkRootSuperclasses(
			ClassInfo classInfo,
			AccessType defaultAccessType,
			RootAnnotationBindingContext context) {
		if ( classInfo.superName() == null ) {
			return null;
		}
		ClassInfo superClassInfo = getLocalBindingContext().getJandexIndex().getClassByName( classInfo.superName() );
		if ( superClassInfo == null ) {
			// no super type
			return null;
		}

		// make triple sure there is no @Entity annotation
		if ( isEntity( superClassInfo ) ) {
			throw new MappingException(
					String.format(
							Locale.ENGLISH,
							"Unexpected @Entity [%s] as MappedSuperclass of entity hierarchy",
							superClassInfo.name()
					),
					new Origin( SourceType.ANNOTATION, superClassInfo.name().toString() )
			);
		}
		else if ( isMappedSuperclass( superClassInfo ) ) {
			return new MappedSuperclassTypeMetadata( superClassInfo, defaultAccessType, context );
		}
		else {
			// otherwise, we might have an "intermediate" subclass
			if ( superClassInfo.superName() != null ) {
				return walkRootSuperclasses( superClassInfo, defaultAccessType, context );
			}
			else {
				return null;
			}
		}
	}

	private void walkSubclasses(
			ClassInfo classInfo,
			IdentifiableTypeMetadata superType,
			AccessType defaultAccessType,
			RootAnnotationBindingContext bindingContext) {
		// ask Jandex for all the known *direct* subclasses of `superType`
		// and iterate them to create the subclass metadata
		for ( ClassInfo subClassInfo : getLocalBindingContext().getJandexIndex().getKnownDirectSubclasses( classInfo.name() ) ) {
			final IdentifiableTypeMetadata subclassMeta;
			if ( isEntity( subClassInfo ) ) {
				subclassMeta = new EntityTypeMetadata(
						subClassInfo,
						superType,
						defaultAccessType,
						bindingContext
				);
				( (ManagedTypeMetadata) superType ).addSubclass( subclassMeta );
			}
			else if ( isMappedSuperclass( subClassInfo ) ) {
				subclassMeta = new MappedSuperclassTypeMetadata(
						subClassInfo,
						superType,
						defaultAccessType,
						bindingContext
				);
				( (ManagedTypeMetadata) superType ).addSubclass( subclassMeta );
			}
			else {
				subclassMeta = superType;
			}

			walkSubclasses( subClassInfo, subclassMeta, defaultAccessType, bindingContext );
		}
	}

	public ClassInfo getClassInfo() {
		return classInfo;
	}

	public EntityBindingContext getLocalBindingContext() {
		return localBindingContext;
	}

	public Map<DotName,AnnotationInstance> typeAnnotationMap() {
		return getLocalBindingContext().getTypeAnnotationInstances( getClassInfo().name() );
	}

	public AnnotationInstance findLocalTypeAnnotation(DotName dotName) {
		return typeAnnotationMap().get( dotName );
	}

	public AnnotationInstance findTypeAnnotation(DotName dotName) {
		final AnnotationInstance local = typeAnnotationMap().get( dotName );
		if ( local != null ) {
			return local;
		}

		if ( getSuperType() != null ) {
			return getSuperType().findTypeAnnotation( dotName );
		}

		return null;
	}

	private boolean isMappedSuperclass(ClassInfo classInfo) {
		return getLocalBindingContext().getTypeAnnotationInstances( classInfo.name() ).containsKey( JpaDotNames.MAPPED_SUPERCLASS );
	}

	private boolean isEntity(ClassInfo classInfo) {
		return getLocalBindingContext().getTypeAnnotationInstances( classInfo.name() ).containsKey( JpaDotNames.ENTITY );
	}

	public AttributeRole getAttributeRoleBase() {
		return attributeRoleBase;
	}

	public AttributePath getAttributePathBase() {
		return attributePathBase;
	}

	public String getName() {
		return classInfo.name().toString();
	}

	public ManagedTypeMetadata getSuperType() {
		return superType;
	}

	public Set<ManagedTypeMetadata> getSubclasses() {
		return subclasses == null ? Collections.<ManagedTypeMetadata>emptySet() : subclasses;
	}

	public boolean isAbstract() {
		return Modifier.isAbstract( classInfo.flags() );
	}

	public Map<String, PersistentAttribute> getPersistentAttributeMap() {
		collectAttributesIfNeeded();
		return persistentAttributeMap;
	}

	protected void collectAttributesIfNeeded() {
		if ( persistentAttributeMap == null ) {
			persistentAttributeMap = new HashMap<String, PersistentAttribute>();
			collectPersistentAttributes();
		}
	}

	public AccessType getClassLevelAccessType() {
		return classLevelAccessType;
	}

	public String getCustomTuplizerClassName() {
		return customTuplizerClassName;
	}

	@Override
	public String toString() {
		return "ManagedTypeMetadata{javaType=" + classInfo.name().toString() + "}";
	}


	// ~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~
	// attribute handling

	/**
	 * Collect all persistent attributes for this managed type
	 */
	private void collectPersistentAttributes() {
		// Call the strategy responsible for resolving the members that identify a persistent attribute
		final List<MemberDescriptor> backingMembers = localBindingContext.getBuildingOptions()
				.getPersistentAttributeMemberResolver()
				.resolveAttributesMembers( classInfo, classLevelAccessType, localBindingContext );

		for ( MemberDescriptor backingMember : backingMembers ) {
			final PersistentAttribute attribute = AttributeFactory.buildAttribute(
					ManagedTypeMetadata.this,
					backingMember,
					localBindingContext
			);
			categorizeAttribute( attribute );
		}
	}

	protected void categorizeAttribute(PersistentAttribute attr) {
		persistentAttributeMap.put( attr.getName(), attr );
	}

	// NOTE : the idea here is gto route this call back to "the base",
	// 		the assumption being that all *relevant* converters have previously
	// 		been normalized to the base
	public abstract ConvertConversionInfo locateConversionInfo(AttributePath attributePath);

	public abstract AttributeOverride locateAttributeOverride(AttributePath attributePath);

	public abstract AssociationOverride locateAssociationOverride(AttributePath attributePath);


	// ~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~
	// Stuff affecting attributes built from this managed type.

	public boolean canAttributesBeInsertable() {
		return true;
	}

	public boolean canAttributesBeUpdatable() {
		return true;
	}

	public NaturalIdMutability getContainerNaturalIdMutability() {
		return NaturalIdMutability.NOT_NATURAL_ID;
	}

	public abstract boolean hasMultiTenancySourceInformation();
}
