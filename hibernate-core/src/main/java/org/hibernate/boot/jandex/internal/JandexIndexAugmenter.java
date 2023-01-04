/*
 * Hibernate, Relational Persistence for Idiomatic Java
 *
 * License: GNU Lesser General Public License (LGPL), version 2.1 or later.
 * See the lgpl.txt file in the root directory or <http://www.gnu.org/licenses/lgpl-2.1.html>.
 */
package org.hibernate.boot.jandex.internal;

import java.util.Collection;

import org.hibernate.boot.internal.MetadataBuildingContextRootImpl;
import org.hibernate.boot.jaxb.spi.Binding;
import org.hibernate.boot.model.process.spi.ManagedResources;

import org.jboss.jandex.IndexView;

/**
 * Augments the Jandex index with information from XML mappings.
 *
 * @author Steve Ebersole
 */
public class JandexIndexAugmenter {
	public static IndexView buildAugmentedIndex(
			ManagedResources managedResources,
			MetadataBuildingContextRootImpl rootContext) {
		final IndexView baselineJandexIndex = managedResources.getJandexIndexBuilder().buildIndex();

		final Collection<Binding<?>> xmlMappingBindings = managedResources.getXmlMappingBindings();
		if ( xmlMappingBindings.isEmpty() ) {
			// if there is no XML information, just return the original index
			return baselineJandexIndex;
		}

		// todo (jandex) : implement augmentation
		return baselineJandexIndex;

//		final JandexIndexAugmenter augmenter = new JandexIndexAugmenter( baselineJandexIndex, rootContext );
//
//		for ( Binding<?> binding : xmlMappingBindings ) {
//			if ( binding.getRoot() instanceof JaxbEntityMappings ) {
//				//noinspection unchecked
//				augmenter.applyXmlMapping( (Binding<JaxbEntityMappings>) binding );
//			}
//		}
//
//		return augmenter.createIndexView();
	}

//	private final MetadataBuildingContextRootImpl rootContext;
//
//	private final Map<DotName, ClassInfoBuilder> classBuilderMap = new HashMap<>();
//	private final Map<DotName, List<ClassInfo>> subclassesMap = new HashMap<>();
//	private final Map<DotName, List<ClassInfo>> implementorsMap = new HashMap<>();
//
//	private final Map<DotName, List<AnnotationInstance>> annotationInstanceMap = new HashMap<>();
//
//	public JandexIndexAugmenter(IndexView baselineJandexIndex, MetadataBuildingContextRootImpl rootContext) {
//		this.rootContext = rootContext;
//
//		final Collection<ClassInfo> knownClasses = baselineJandexIndex.getKnownClasses();
//		knownClasses.forEach(
//				knownClass -> {
//					final ClassInfoBuilder classInfoBuilder = new ClassInfoBuilder( knownClass, this::collectAnnotationInstance );
//					classBuilderMap.put( knownClass.name(), classInfoBuilder );
//				}
//		);
//	}
//
//	private void collectAnnotationInstance(AnnotationInstance annotationInstance) {
//		final List<AnnotationInstance> list = annotationInstanceMap.computeIfAbsent(
//				annotationInstance.name(),
//				dotName -> new ArrayList<>()
//		);
//		list.add( annotationInstance );
//	}
//
//	public void applyXmlMapping(Binding<JaxbEntityMappings> binding) {
//		final JaxbEntityMappings entityMappings = binding.getRoot();
//		final BindingMetadataBuildingContext localContext = new BindingMetadataBuildingContext( rootContext, binding );
//
//		final List<JaxbEntity> entities = entityMappings.getEntities();
//
//		for ( int i = 0; i < entities.size(); i++ ) {
//			final JaxbEntity jaxbEntity = entities.get( i );
//			final boolean isCompleteMapping = ( jaxbEntity.isMetadataComplete() != null && jaxbEntity.isMetadataComplete() )
//					|| StringHelper.isEmpty( jaxbEntity.getClazz() );
//			if ( isCompleteMapping ) {
//				applyCompleteMapping( jaxbEntity, localContext );
//			}
//			else {
//				applyXmlOverride( jaxbEntity, localContext );
//			}
//		}
//	}
//
//	private void applyXmlOverride(JaxbEntity jaxbEntity, BindingMetadataBuildingContext localContext) {
//		assert StringHelper.isNotEmpty( jaxbEntity.getClazz() );
//		final DotName entityClassName = DotName.createSimple( jaxbEntity.getClazz() );
//
//		final ClassInfo existingEntityMapping = classesMap.get( entityClassName );
//		assert existingEntityMapping != null;
//
//		applyEntityOverrides( existingEntityMapping, jaxbEntity, localContext );
//	}
//
//	private void applyEntityOverrides(
//			ClassInfo existingEntityMapping,
//			JaxbEntity jaxbEntity,
//			BindingMetadataBuildingContext localContext) {
//		if ( jaxbEntity.getBatchSize() != null ) {
//			existingEntityMapping.declaredAnnotation( BATCH_SIZE.getJandexName() );
//		}
//		jaxbEntity.getBatchSize()
//	}
//
//	private void applyCompleteMapping(JaxbEntity jaxbEntity, BindingMetadataBuildingContext localContext) {
//		throw new NotYetImplementedException( "Applying complete orm.xml entity mapping not yet implemented" );
//	}
//
//
//
//	public IndexView createIndexView() {
//		classesMap.
//
//		for (int i = 0; i < classesSize; i++) {
//			ClassInfo clazz = readClassEntry(stream, masterAnnotations, version);
//			addClassToMap( subclassesMap, clazz.superName(), clazz);
//			for ( Type interfaceType : clazz.interfaceTypeArray()) {
//				addClassToMap( implementorsMap, interfaceType.name(), clazz);
//			}
//			classes.put(clazz.name(), clazz);
//		}
//
//	}
}
