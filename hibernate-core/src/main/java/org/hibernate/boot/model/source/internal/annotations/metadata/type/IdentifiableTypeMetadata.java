/*
 * Hibernate, Relational Persistence for Idiomatic Java
 *
 * License: GNU Lesser General Public License (LGPL), version 2.1 or later.
 * See the lgpl.txt file in the root directory or <http://www.gnu.org/licenses/lgpl-2.1.html>.
 */
package org.hibernate.boot.model.source.internal.annotations.metadata.type;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import javax.persistence.AccessType;

import org.hibernate.boot.model.process.jandex.spi.JpaDotNames;
import org.hibernate.boot.model.source.internal.annotations.RootAnnotationBindingContext;
import org.hibernate.boot.model.source.internal.annotations.ConvertConversionInfo;
import org.hibernate.boot.model.source.internal.annotations.JpaCallbackInformation;
import org.hibernate.boot.model.source.internal.annotations.metadata.attribute.AssociationOverride;
import org.hibernate.boot.model.source.internal.annotations.metadata.attribute.AttributeOverride;
import org.hibernate.boot.model.source.internal.annotations.metadata.attribute.BasicAttribute;
import org.hibernate.boot.model.source.internal.annotations.metadata.attribute.PersistentAttribute;
import org.hibernate.boot.model.source.internal.annotations.metadata.attribute.SingularAssociationAttribute;
import org.hibernate.boot.model.source.internal.annotations.metadata.attribute.SingularAttribute;
import org.hibernate.boot.model.source.internal.annotations.metadata.util.JpaListenerHelper;
import org.hibernate.boot.model.source.internal.annotations.util.AnnotationBindingHelper;
import org.hibernate.boot.model.source.spi.AttributePath;
import org.hibernate.boot.model.source.spi.InheritanceType;
import org.hibernate.boot.model.source.spi.JpaCallbackSource;

import org.jboss.jandex.AnnotationInstance;
import org.jboss.jandex.AnnotationValue;
import org.jboss.jandex.ClassInfo;
import org.jboss.jandex.DotName;
import org.jboss.jandex.MethodInfo;
import org.jboss.jandex.Type;
import org.jboss.logging.Logger;

/**
 * Representation of metadata (configured via annotations or orm.xml) attached
 * to an Entity or a MappedSuperclass.
 *
 * @author Steve Ebersole
 */
public abstract class IdentifiableTypeMetadata extends ManagedTypeMetadata {
	private static final Logger log = Logger.getLogger( IdentifiableTypeMetadata.class );

	private IdType idType;
	private List<SingularAttribute> identifierAttributes;
	private List<SingularAssociationAttribute> mapsIdAssociationAttributes;
	private BasicAttribute versionAttribute;

	private final Map<AttributePath, ConvertConversionInfo> conversionInfoMap = new HashMap<AttributePath, ConvertConversionInfo>();
	private final Map<AttributePath, AttributeOverride> attributeOverrideMap = new HashMap<AttributePath, AttributeOverride>();
	private final Map<AttributePath, AssociationOverride> associationOverrideMap = new HashMap<AttributePath, AssociationOverride>();

	private List<JpaCallbackSource> collectedJpaCallbackSources;

	/**
	 * This form is intended for construction of root Entity, and any of
	 * its MappedSuperclasses
	 *
	 * @param classInfo The Entity/MappedSuperclass class descriptor
	 * @param defaultAccessType The default AccessType for the hierarchy
	 * @param isRoot Is this the root entity?
	 * @param bindingContext The context
	 */
	public IdentifiableTypeMetadata(
			ClassInfo classInfo,
			AccessType defaultAccessType,
			boolean isRoot,
			RootAnnotationBindingContext bindingContext) {
		super( classInfo, defaultAccessType, isRoot, bindingContext );

		// the idea here is to collect up class-level annotations and to apply
		// the maps from supers
		collectConversionInfo();
		collectAttributeOverrides();
		collectAssociationOverrides();
	}

	protected void collectConversionInfo() {
		// we only need to do this on root
	}

	protected void collectAttributeOverrides() {
		// we only need to do this on root
	}

	protected void collectAssociationOverrides() {
		// we only need to do this on root
	}

//		// todo : account for supers/subs...
//		if ( CollectionHelper.isEmpty( getMappedSuperclassTypeMetadatas() ) ) {
//			return RootEntityTypeMetadata.super.getAssociationOverrideMap();
//		}
//		Map<String, AssociationOverride> map = new HashMap<String, AssociationOverride>();
//		for ( MappedSuperclassTypeMetadata mappedSuperclassTypeMetadata : getMappedSuperclassTypeMetadatas() ) {
//			map.putAll( mappedSuperclassTypeMetadata.getAssociationOverrideMap() );
//		}
//		map.putAll( RootEntityTypeMetadata.super.getAssociationOverrideMap() );
//		return map;

	/**
	 * This form is intended for cases where the Entity/MappedSuperclass
	 * is part of the root subclass tree.
	 *
	 * @param classInfo The Entity/MappedSuperclass class descriptor
	 * @param superType The metadata for the super type.
	 * @param defaultAccessType The default AccessType for the entity hierarchy
	 * @param context The binding context
	 */
	public IdentifiableTypeMetadata(
			ClassInfo classInfo,
			IdentifiableTypeMetadata superType,
			AccessType defaultAccessType,
			RootAnnotationBindingContext context) {
		super( classInfo, superType, defaultAccessType, context );

		// the idea here is to collect up class-level annotations and to apply
		// the maps from supers
		collectConversionInfo();
		collectAttributeOverrides();
		collectAssociationOverrides();
	}

	@Override
	public boolean isAbstract() {
		return super.isAbstract();
	}

	@Override
	public ConvertConversionInfo locateConversionInfo(AttributePath attributePath) {
		return conversionInfoMap.get( attributePath );
	}

	@Override
	public AttributeOverride locateAttributeOverride(AttributePath attributePath) {
		return attributeOverrideMap.get( attributePath );
	}

	@Override
	public AssociationOverride locateAssociationOverride(AttributePath attributePath) {
		return associationOverrideMap.get( attributePath );
	}

	@Override
	public IdentifiableTypeMetadata getSuperType() {
		return (IdentifiableTypeMetadata) super.getSuperType();
	}

	/**
	 * Obtain the InheritanceType defined locally within this class
	 *
	 * @return Return the InheritanceType locally defined; {@code null} indicates
	 * no InheritanceType was locally defined.
	 */
	public InheritanceType getLocallyDefinedInheritanceType() {
		AnnotationInstance inheritanceAnnotation = getLocalBindingContext().getTypeAnnotationInstances( getClassInfo().name() ).get(
				JpaDotNames.INHERITANCE
		);
		if ( inheritanceAnnotation != null ) {
			final AnnotationValue strategyValue = inheritanceAnnotation.value( "strategy" );
			if ( strategyValue != null ) {
				return InheritanceType.valueOf( strategyValue.asEnum() );
			}
			else {
				// the @Inheritance#strategy default value
				return InheritanceType.DISCRIMINATED;
			}
		}

		return null;
	}

	/**
	 * Obtain all JPA callbacks that should be applied for the given entity, including
	 * walking super-types.  This includes both method callbacks and listener callbacks.
	 *
	 * @return The callbacks.  {@code null} is never returned
	 */
	public List<JpaCallbackSource> getJpaCallbacks() {
		if ( collectedJpaCallbackSources == null ) {
			collectedJpaCallbackSources = collectJpaCallbacks();
		}
		return collectedJpaCallbackSources;
	}

	private List<JpaCallbackSource> collectJpaCallbacks() {
		// JPA (2.1, section 3.5.5) explicitly defines the order callbacks should be called at run time
		// we follow that order in collecting the callbacks here...
		//		1) default entity listeners (by spec, these come from orm.xml)
		//		2) listeners, those defined on super types first
		//		3) methods, those defined on super types first
		//
		// within each "bucket" the listeners are executed in the order the are defined
		//
		// @ExcludeDefaultListeners and @ExcludeSuperClassListeners effect when to stop walking up durng
		// collection
		final ArrayList<JpaCallbackSource> callbacks = new ArrayList<JpaCallbackSource>();


		//	1 - default entity listeners
		if ( AnnotationBindingHelper.findTypeAnnotation( JpaDotNames.EXCLUDE_DEFAULT_LISTENERS, this ) == null ) {
			// exclusion of default listeners was *not* requested,
			// so add any default listeners defined as default in orm.xml

			// todo : atm extracting default listeners from orm.xml is not done.  see todo.md
			final List<String> defaultListenerClassNames = Collections.emptyList();
			for ( String defaultListenerClassName : defaultListenerClassNames ) {
				final ClassInfo defaultListenerClassInfo = getLocalBindingContext().getJandexIndex().getClassByName(
						DotName.createSimple( defaultListenerClassName )
				);
				collectCallbacks( defaultListenerClassInfo, true, callbacks );
			}
		}

		// 2 - listeners, super-type-defined first
		collectEntityListenerCallbacks( this, callbacks );

		// 3 - methods, super-type-defined first
		collectMethodCallbacks( this, callbacks );

		return Collections.unmodifiableList( callbacks );
	}

	private void collectEntityListenerCallbacks(
			IdentifiableTypeMetadata typeMetadata,
			ArrayList<JpaCallbackSource> callbacks) {
		if ( AnnotationBindingHelper.findTypeAnnotation( JpaDotNames.EXCLUDE_SUPERCLASS_LISTENERS, typeMetadata ) != null ) {
			// user requested exclusion of superclass-defined listeners
			return;
		}

		// walk super-type tree first, collecting callbacks
		if ( getSuperType() != null ) {
			collectEntityListenerCallbacks( getSuperType(), callbacks );
		}

		final AnnotationInstance entityListenersAnnotation = typeAnnotationMap().get( JpaDotNames.ENTITY_LISTENERS );
		if ( entityListenersAnnotation != null ) {
			final Type[] types = entityListenersAnnotation.value().asClassArray();
			for ( Type type : types ) {
				final ClassInfo entityListenerClassInfo = getLocalBindingContext().getJandexIndex().getClassByName( type.name() );
				collectCallbacks( entityListenerClassInfo, true, callbacks );
			}
		}
	}

	private void collectMethodCallbacks(
			IdentifiableTypeMetadata typeMetadata,
			ArrayList<JpaCallbackSource> callbacks) {
		if ( AnnotationBindingHelper.findTypeAnnotation( JpaDotNames.EXCLUDE_SUPERCLASS_LISTENERS, typeMetadata ) != null ) {
			// user requested exclusion of superclass-defined listeners
			return;
		}

		// walk super-type tree first, collecting callbacks
		if ( getSuperType() != null ) {
			collectMethodCallbacks( getSuperType(), callbacks );
		}

		collectCallbacks( typeMetadata.getClassInfo(), false, callbacks );
	}

	private void collectCallbacks(
			ClassInfo callbackClassInfo,
			boolean isListener,
			ArrayList<JpaCallbackSource> callbacks) {
		final MethodInfo prePersistCallback = JpaListenerHelper.findCallback(
				callbackClassInfo,
				JpaDotNames.PRE_PERSIST,
				isListener
		);
		final MethodInfo preRemoveCallback = JpaListenerHelper.findCallback(
				callbackClassInfo,
				JpaDotNames.PRE_REMOVE,
				isListener
		);
		final MethodInfo preUpdateCallback = JpaListenerHelper.findCallback(
				callbackClassInfo,
				JpaDotNames.PRE_UPDATE,
				isListener
		);
		final MethodInfo postLoadCallback = JpaListenerHelper.findCallback(
				callbackClassInfo,
				JpaDotNames.POST_LOAD,
				isListener
		);
		final MethodInfo postPersistCallback = JpaListenerHelper.findCallback(
				callbackClassInfo,
				JpaDotNames.POST_PERSIST,
				isListener
		);
		final MethodInfo postRemoveCallback = JpaListenerHelper.findCallback(
				callbackClassInfo,
				JpaDotNames.POST_REMOVE,
				isListener
		);
		final MethodInfo postUpdateCallback = JpaListenerHelper.findCallback(
				callbackClassInfo,
				JpaDotNames.POST_UPDATE,
				isListener
		);

		if ( prePersistCallback == null
				&& preRemoveCallback == null
				&& preUpdateCallback == null
				&& postLoadCallback == null
				&& postPersistCallback == null
				&& postRemoveCallback == null
				&& postUpdateCallback == null ) {
			if ( isListener ) {
				log.debugf(
						"Entity listener class [%s] named by @EntityListener on entity [%s] contained no callback methods",
						callbackClassInfo.name(),
						getClassInfo().name()
				);
			}
		}
		else {
			callbacks.add(
					new JpaCallbackInformation(
							callbackClassInfo,
							isListener,
							prePersistCallback,
							preRemoveCallback,
							preUpdateCallback,
							postLoadCallback,
							postPersistCallback,
							postRemoveCallback,
							postUpdateCallback
					)
			);
		}
	}

	@Override
	public void registerConverter(
			AttributePath attributePath,
			ConvertConversionInfo conversionInfo) {
		final ConvertConversionInfo old = conversionInfoMap.put( attributePath, conversionInfo );
		if ( old != null ) {
			// todo : is this the best option?  should it be an exception instead?
			// todo : should probably also consider/prefer disabled
			log.debugf(
					"@Convert-defined AttributeConverter"
			);
		}
	}

	@Override
	public void registerAttributeOverride(
			AttributePath attributePath,
			AttributeOverride override) {
		if ( attributeOverrideMap.containsKey( attributePath ) ) {
			// an already registered path indicates that a higher context has already
			// done a registration; ignore the incoming one.
			log.debugf(
					"On registration of @AttributeOverride we already had a " +
							"registered override for the given path [%s]; ignoring.  " +
							"This subsequent registration should indicate a 'lower " +
							"precedence' location."
			);
		}
		else {
			attributeOverrideMap.put( attributePath, override );
		}
	}

	@Override
	public void registerAssociationOverride(
			AttributePath attributePath,
			AssociationOverride override) {
		associationOverrideMap.put( attributePath, override );
	}

	@Override
	protected void categorizeAttribute(PersistentAttribute persistentAttribute) {
		if ( SingularAttribute.class.isInstance( persistentAttribute ) ) {
			final SingularAttribute singularAttribute = (SingularAttribute) persistentAttribute;
			if ( singularAttribute.isVersion() ) {
				if ( versionAttribute != null ) {
					throw getLocalBindingContext().makeMappingException(
							String.format(
									Locale.ENGLISH,
									"Multiple attributes [%s, %s] were indicated as Version",
									versionAttribute.getName(),
									singularAttribute.getName()
							)
					);
				}
				if ( singularAttribute.isId() ) {
					throw getLocalBindingContext().makeMappingException(
							String.format(
									Locale.ENGLISH,
									"Attributes [%s] was indicated as Id and as Version",
									singularAttribute.getName()
							)
					);
				}
				// only BasicAttributes can be versions
				versionAttribute = (BasicAttribute) singularAttribute;
				return;
			}

			if ( singularAttribute.isId() ) {
				if ( identifierAttributes == null ) {
					// first collected identifier attribute
					identifierAttributes = new ArrayList<SingularAttribute>();
					switch ( singularAttribute.getAttributeNature() ) {
						case EMBEDDED: {
							idType = IdType.AGGREGATED;
							break;
						}
						default: {
							idType = IdType.SIMPLE;
							break;
						}
					}
				}
				else {
					// multiple collected identifier attribute
					idType = IdType.NON_AGGREGATED;
				}
				identifierAttributes.add( singularAttribute );
				return;
			}

			if ( SingularAssociationAttribute.class.isInstance( singularAttribute ) ) {
				final SingularAssociationAttribute toOneAttribute = (SingularAssociationAttribute) singularAttribute;
				if ( toOneAttribute.getMapsIdAnnotation() != null ) {
					if ( mapsIdAssociationAttributes == null ) {
						mapsIdAssociationAttributes = new ArrayList<SingularAssociationAttribute>();
					}
					mapsIdAssociationAttributes.add( toOneAttribute );
					return;
				}
			}
		}

		super.categorizeAttribute( persistentAttribute );
	}


	public IdType getIdType() {
		collectAttributesIfNeeded();

		if ( idType == null ) {
			if ( getSuperType() != null ) {
				return getSuperType().getIdType();
			}
		}
		return idType == null ? IdType.NONE : idType;
	}

	public List<SingularAttribute> getIdentifierAttributes() {
		collectAttributesIfNeeded();

		if ( identifierAttributes == null ) {
			if ( getSuperType() != null ) {
				return getSuperType().getIdentifierAttributes();
			}
		}
		return identifierAttributes == null ? Collections.<SingularAttribute>emptyList() : identifierAttributes;
	}

	public List<SingularAssociationAttribute> getMapsIdAttributes() {
		collectAttributesIfNeeded();

		if ( mapsIdAssociationAttributes == null ) {
			return getSuperType() != null
					? getSuperType().getMapsIdAttributes()
					: Collections.<SingularAssociationAttribute>emptyList();
		}
		else {
			return mapsIdAssociationAttributes;
		}
	}

	public BasicAttribute getVersionAttribute() {
		collectAttributesIfNeeded();

		if ( versionAttribute == null ) {
			if ( getSuperType() != null ) {
				return getSuperType().getVersionAttribute();
			}
		}
		return versionAttribute;
	}
}
