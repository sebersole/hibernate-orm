/*
 * Hibernate, Relational Persistence for Idiomatic Java
 *
 * License: GNU Lesser General Public License (LGPL), version 2.1 or later.
 * See the lgpl.txt file in the root directory or <http://www.gnu.org/licenses/lgpl-2.1.html>.
 */

/*
 * Hibernate, Relational Persistence for Idiomatic Java
 *
 * Copyright (c) 2013, Red Hat Inc. or third-party contributors as
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
package org.hibernate.boot.model.source.internal.annotations.metadata.util;

import java.util.Collection;
import java.util.Locale;

import org.hibernate.HibernateException;
import org.hibernate.boot.jandex.spi.JpaDotNames;
import org.hibernate.boot.jaxb.mapping.spi.JaxbEntityListener;
import org.hibernate.boot.jaxb.mapping.spi.LifecycleCallback;
import org.hibernate.internal.util.StringHelper;

import org.jboss.jandex.AnnotationInstance;
import org.jboss.jandex.ClassInfo;
import org.jboss.jandex.DotName;
import org.jboss.jandex.MethodInfo;
import org.jboss.jandex.Type;
import org.jboss.logging.Logger;

/**
 * Helper for working with JPA callbacks/listeners
 *
 * @author Steve Ebersole
 * @author Strong Liu
 */
public class JpaListenerHelper {
	private static final Logger log = Logger.getLogger( JpaListenerHelper.class );

	/**
	 * Find the name of the method in the class (described by the descriptor) that
	 * is annotated with the given lifecycle callback annotation.
	 *
	 * @param callbackClassInfo The descriptor for the class in which to find
	 * the lifecycle callback method
	 * @param eventType The type of lifecycle callback to look for
	 * @param listener Is the {@code callbackClassInfo} a listener, as opposed to
	 * an Entity/MappedSuperclass?  Used here to validate method signatures.
	 *
	 * @return The name of the callback method, or {@code null} indicating none was found
	 */
	public static MethodInfo findCallback(
			ClassInfo callbackClassInfo,
			DotName eventType,
			boolean listener) {
		final Collection<AnnotationInstance> listenerAnnotations = callbackClassInfo.annotations().get( eventType );
		if ( listenerAnnotations == null || listenerAnnotations.isEmpty() ) {
			return null;
		}

		// todo : these log messages on skip really should be warn, which means i18n message logging

		for ( AnnotationInstance listenerAnnotation : listenerAnnotations ) {
			if ( !MethodInfo.class.isInstance( listenerAnnotation.target() ) ) {
				log.debugf(
						"Skipping callback annotation [%s] for class [%s] as it was " +
								"applied to target other than a method : %s",
						eventType,
						callbackClassInfo.name().toString(),
						listenerAnnotation.target()
				);
				continue;
			}

			final MethodInfo methodInfo = (MethodInfo) listenerAnnotation.target();

			// validate return is `void`
			//		NOTE : we do NOT skip here, just a warning
			if ( methodInfo.returnType().kind() != Type.Kind.VOID ) {
				log.debugf(
						"Callback annotation [%s] on class [%s] was applied to method [%s] with a non-void return type",
						eventType,
						callbackClassInfo.name().toString(),
						methodInfo.name()
				);
			}

			// validate arguments
			if ( listener ) {
				// should have a single argument
				if ( methodInfo.parameters().size() != 1 ) {
					log.debugf(
							"Callback annotation [%s] on listener class [%s] was applied to method [%s] " +
									"which does not define a single-arg signature [%s]",
							eventType,
							callbackClassInfo.name().toString(),
							methodInfo.name(),
							methodInfo.parameters().size()
					);
					continue;
				}
			}
			else {
				// should have no arguments
				if ( methodInfo.parameters().size() != 0 ) {
					log.debugf(
							"Callback annotation [%s] on entity class [%s] was applied to method [%s] " +
									"which does not define a no-arg signature [%s]",
							eventType,
							callbackClassInfo.name().toString(),
							methodInfo.name(),
							methodInfo.parameters().size()
					);
					continue;
				}
			}

			// if we did not opt-out above (with a continue statement) then we found it...
			return methodInfo;
		}

		return null;
	}

	public static CallbackMethodGroup extractCallbackMethods(ClassInfo listenerClass, JaxbEntityListener jaxbListenerDefinition) {
		// According to sec, we should prefer the XML-specified method name (if there is one) over
		// any annotated methods.  The general approach here is to apply the annotated methods (if any) first
		// and then overlay the XML names

		// Apply the annotated callbacks first...
		final CallbackMethodGroupBuilder callbackMethodGroupBuilder = CallbackMethodGroupBuilder.from(
				listenerClass,
				true
		);

		// Apply Names explicitly defined in XML...
		JaxbCallbackMethodOverrideAugmenter augmenter = JaxbCallbackMethodOverrideAugmenter.prepare( jaxbListenerDefinition );
		augmenter.augment( callbackMethodGroupBuilder );

		return callbackMethodGroupBuilder.createCallbackMethodGroup();
	}

	private static String extractMethodName(LifecycleCallback jaxbCallbackMethod) {
		if ( jaxbCallbackMethod == null ) {
			return null;
		}

		return StringHelper.nullIfEmpty( jaxbCallbackMethod.getMethodName() );
	}

	/**
	 * Coordinates applying callback method overrides defined in XML over top the methods
	 * already defined within a CallbackMethodGroupBuilder
	 */
	private static class JaxbCallbackMethodOverrideAugmenter {
		/**
		 * Build an augmenter from the XML defined overrides.
		 *
		 * @param jaxbListenerDefinition The listener XML config
		 *
		 * @return The augmenter
		 */
		static JaxbCallbackMethodOverrideAugmenter prepare(JaxbEntityListener jaxbListenerDefinition) {
			return new JaxbCallbackMethodOverrideAugmenter(
					extractMethodName( jaxbListenerDefinition.getPrePersist() ),
					extractMethodName( jaxbListenerDefinition.getPreUpdate() ),
					extractMethodName( jaxbListenerDefinition.getPreRemove() ),
					extractMethodName( jaxbListenerDefinition.getPostLoad() ),
					extractMethodName( jaxbListenerDefinition.getPostPersist() ),
					extractMethodName( jaxbListenerDefinition.getPostUpdate() ),
					extractMethodName( jaxbListenerDefinition.getPostRemove() )
			);
		}

		private String prePersistName;
		private String preUpdateName;
		private String preRemoveName;
		private String postLoadName;
		private String postPersistName;
		private String postUpdateName;
		private String postRemoveName;

		private JaxbCallbackMethodOverrideAugmenter(
				String prePersistName,
				String preUpdateName,
				String preRemoveName,
				String postLoadName,
				String postPersistName,
				String postUpdateName,
				String postRemoveName) {
			this.prePersistName = prePersistName;
			this.preUpdateName = preUpdateName;
			this.preRemoveName = preRemoveName;
			this.postLoadName = postLoadName;
			this.postPersistName = postPersistName;
			this.postUpdateName = postUpdateName;
			this.postRemoveName = postRemoveName;
		}

		public void augment(CallbackMethodGroupBuilder callbackMethodGroupBuilder) {
			if ( !anyRemainingOverrides() ) {
				return;
			}


			for ( MethodInfo methodInfo : callbackMethodGroupBuilder.getClassInfo().methods() ) {
				// check if the method could be a callback

				// validate return is `void`
				if ( methodInfo.returnType().kind() != Type.Kind.VOID ) {
					continue;
				}

				// validate arguments
				if ( callbackMethodGroupBuilder.isListener() ) {
					// should have a single argument
					if ( methodInfo.parameters().size() != 1 ) {
						continue;
					}
				}
				else {
					// should have no arguments
					if ( methodInfo.parameters().size() != 0 ) {
						continue;
					}
				}

				// its possibly a callback method, try to match it to a named override
				if ( prePersistName != null
						&& prePersistName.equals( methodInfo.name() ) ) {
					callbackMethodGroupBuilder.setPrePersistCallback( methodInfo );
					prePersistName = null;
				}
				else if ( preUpdateName != null
						&& preUpdateName.equals( methodInfo.name() ) ) {
					callbackMethodGroupBuilder.setPreUpdateCallback( methodInfo );
					preUpdateName = null;
				}
				else if ( preRemoveName != null
						&& preRemoveName.equals( methodInfo.name() ) ) {
					callbackMethodGroupBuilder.setPreRemoveCallback( methodInfo );
					preRemoveName = null;
				}
				else if ( postLoadName != null
						&& postLoadName.equals( methodInfo.name() ) ) {
					callbackMethodGroupBuilder.setPostLoadCallback( methodInfo );
					postLoadName = null;
				}
				else if ( postPersistName != null
						&& postPersistName.equals( methodInfo.name() ) ) {
					callbackMethodGroupBuilder.setPostPersistCallback( methodInfo );
					postPersistName = null;
				}
				else if ( postUpdateName != null
						&& postUpdateName.equals( methodInfo.name() ) ) {
					callbackMethodGroupBuilder.setPostUpdateCallback( methodInfo );
					postUpdateName = null;
				}
				else if ( postRemoveName != null
						&& postRemoveName.equals( methodInfo.name() ) ) {
					callbackMethodGroupBuilder.setPostRemoveCallback( methodInfo );
					postRemoveName = null;
				}

				if ( !anyRemainingOverrides() ) {
					break;
				}
			}

			if ( anyRemainingOverrides() ) {
				throw new HibernateException(
						String.format(
								Locale.ROOT,
								"orm.xml named entity-listener method overrides where the method(s) could not be found [%s] : %s",
								callbackMethodGroupBuilder.getClassInfo().name().toString(),
								allRemainingMethodNames()
						)
				);
			}
		}

		private boolean anyRemainingOverrides() {
			return prePersistName != null
					|| preUpdateName != null
					|| preRemoveName != null
					|| postLoadName != null
					|| postPersistName != null
					|| postUpdateName != null
					|| postRemoveName != null;
		}

		private String allRemainingMethodNames() {
			final StringBuilder buffer = new StringBuilder();

			boolean anyMatchesYet = false;
			if ( prePersistName != null ) {
				append( buffer, prePersistName, anyMatchesYet );
				anyMatchesYet = true;
			}

			if ( preUpdateName != null ) {
				append( buffer, preUpdateName, anyMatchesYet );
				anyMatchesYet = true;
			}

			if ( preRemoveName != null ) {
				append( buffer, preRemoveName, anyMatchesYet );
				anyMatchesYet = true;
			}

			if ( postLoadName != null ) {
				append( buffer, postLoadName, anyMatchesYet );
				anyMatchesYet = true;
			}

			if ( postPersistName != null ) {
				append( buffer, postPersistName, anyMatchesYet );
				anyMatchesYet = true;
			}

			if ( postUpdateName != null ) {
				append( buffer, postUpdateName, anyMatchesYet );
				anyMatchesYet = true;
			}

			if ( postRemoveName != null ) {
				append( buffer, postRemoveName, anyMatchesYet );
				anyMatchesYet = true;
			}

			return buffer.toString();
		}

		private void append(StringBuilder buffer, String name, boolean anyMatchesYet) {
			if ( anyMatchesYet ) {
				buffer.append( ", " );
			}
			buffer.append( name );
		}
	}

	private static class CallbackMethodGroupImpl implements CallbackMethodGroup {
		private final MethodInfo prePersistCallback;
		private final MethodInfo preRemoveCallback;
		private final MethodInfo preUpdateCallback;
		private final MethodInfo postLoadCallback;
		private final MethodInfo postPersistCallback;
		private final MethodInfo postRemoveCallback;
		private final MethodInfo postUpdateCallback;

		public CallbackMethodGroupImpl(
				MethodInfo prePersistCallback,
				MethodInfo preRemoveCallback,
				MethodInfo preUpdateCallback,
				MethodInfo postLoadCallback,
				MethodInfo postPersistCallback,
				MethodInfo postRemoveCallback,
				MethodInfo postUpdateCallback) {
			this.prePersistCallback = prePersistCallback;
			this.preRemoveCallback = preRemoveCallback;
			this.preUpdateCallback = preUpdateCallback;
			this.postLoadCallback = postLoadCallback;
			this.postPersistCallback = postPersistCallback;
			this.postRemoveCallback = postRemoveCallback;
			this.postUpdateCallback = postUpdateCallback;
		}

		@Override
		public MethodInfo getPrePersistCallbackMethod() {
			return prePersistCallback;
		}

		@Override
		public MethodInfo getPreRemoveCallbackMethod() {
			return preRemoveCallback;
		}

		@Override
		public MethodInfo getPreUpdateCallbackMethod() {
			return preUpdateCallback;
		}

		@Override
		public MethodInfo getPostLoadCallbackMethod() {
			return postLoadCallback;
		}

		@Override
		public MethodInfo getPostPersistCallbackMethod() {
			return postPersistCallback;
		}

		@Override
		public MethodInfo getPostRemoveCallbackMethod() {
			return postRemoveCallback;
		}

		@Override
		public MethodInfo getPostUpdateCallbackMethod() {
			return postUpdateCallback;
		}
	}
	public static class CallbackMethodGroupBuilder {
		public static CallbackMethodGroupBuilder from(ClassInfo listenerClass, boolean isListener) {
			return new CallbackMethodGroupBuilder(
					listenerClass,
					isListener,
					findCallback( listenerClass, JpaDotNames.PRE_PERSIST, isListener ),
					findCallback( listenerClass, JpaDotNames.PRE_UPDATE, isListener ),
					findCallback( listenerClass, JpaDotNames.PRE_REMOVE, isListener ),
					findCallback( listenerClass, JpaDotNames.POST_LOAD, isListener ),
					findCallback( listenerClass, JpaDotNames.POST_PERSIST, isListener ),
					findCallback( listenerClass, JpaDotNames.POST_UPDATE, isListener ),
					findCallback( listenerClass, JpaDotNames.POST_REMOVE, isListener )
			);
		}

		private final ClassInfo classInfo;
		private final boolean isListener;

		private MethodInfo prePersistCallback;
		private MethodInfo preRemoveCallback;
		private MethodInfo preUpdateCallback;
		private MethodInfo postLoadCallback;
		private MethodInfo postPersistCallback;
		private MethodInfo postRemoveCallback;
		private MethodInfo postUpdateCallback;

		public CallbackMethodGroupBuilder(
				ClassInfo classInfo,
				boolean isListener,
				MethodInfo prePersistCallback,
				MethodInfo preRemoveCallback,
				MethodInfo preUpdateCallback,
				MethodInfo postLoadCallback,
				MethodInfo postPersistCallback,
				MethodInfo postRemoveCallback,
				MethodInfo postUpdateCallback) {
			this.classInfo = classInfo;
			this.isListener = isListener;
			this.prePersistCallback = prePersistCallback;
			this.preRemoveCallback = preRemoveCallback;
			this.preUpdateCallback = preUpdateCallback;
			this.postLoadCallback = postLoadCallback;
			this.postPersistCallback = postPersistCallback;
			this.postRemoveCallback = postRemoveCallback;
			this.postUpdateCallback = postUpdateCallback;
		}

		public ClassInfo getClassInfo() {
			return classInfo;
		}

		public boolean isListener() {
			return isListener;
		}

		public CallbackMethodGroupBuilder setPrePersistCallback(MethodInfo prePersistCallback) {
			this.prePersistCallback = prePersistCallback;
			return this;
		}

		public CallbackMethodGroupBuilder setPreRemoveCallback(MethodInfo preRemoveCallback) {
			this.preRemoveCallback = preRemoveCallback;
			return this;
		}

		public CallbackMethodGroupBuilder setPreUpdateCallback(MethodInfo preUpdateCallback) {
			this.preUpdateCallback = preUpdateCallback;
			return this;
		}

		public CallbackMethodGroupBuilder setPostLoadCallback(MethodInfo postLoadCallback) {
			this.postLoadCallback = postLoadCallback;
			return this;
		}

		public CallbackMethodGroupBuilder setPostPersistCallback(MethodInfo postPersistCallback) {
			this.postPersistCallback = postPersistCallback;
			return this;
		}

		public CallbackMethodGroupBuilder setPostRemoveCallback(MethodInfo postRemoveCallback) {
			this.postRemoveCallback = postRemoveCallback;
			return this;
		}

		public CallbackMethodGroupBuilder setPostUpdateCallback(MethodInfo postUpdateCallback) {
			this.postUpdateCallback = postUpdateCallback;
			return this;
		}

		public CallbackMethodGroup createCallbackMethodGroup() {
			return new CallbackMethodGroupImpl(
					prePersistCallback,
					preRemoveCallback,
					preUpdateCallback,
					postLoadCallback,
					postPersistCallback,
					postRemoveCallback,
					postUpdateCallback
			);
		}
	}

	public enum CallbackEventTypes {
		PRE_PERSIST( JpaDotNames.PRE_PERSIST ),
		PRE_REMOVE( JpaDotNames.PRE_REMOVE ),
		PRE_UPDATE( JpaDotNames.PRE_UPDATE ),
		POST_LOAD( JpaDotNames.POST_LOAD ),
		POST_PERSIST( JpaDotNames.POST_PERSIST ),
		POST_REMOVE( JpaDotNames.POST_REMOVE ),
		POST_UPDATE( JpaDotNames.POST_UPDATE )
		;

		CallbackEventTypes(DotName annotationDotName) {
			this.annotationDotName = annotationDotName;
		}

		private final DotName annotationDotName;

		public DotName getAnnotationDotName() {
			return annotationDotName;
		}
	}

//	private final EntityTypeMetadata entityTypeMetadata;
//	private final EntityBindingContext context;
//	private final List<ManagedTypeMetadata> mappingClassHierarchy;
//
//	public JPAListenerHelper(EntityTypeMetadata entityTypeMetadata) {
//		this.entityTypeMetadata = entityTypeMetadata;
//		this.context = entityTypeMetadata.getLocalBindingContext();
//		this.mappingClassHierarchy = buildHierarchy();
//	}
//
//	private List<ManagedTypeMetadata> buildHierarchy() {
//		List<ManagedTypeMetadata> list = new ArrayList<ManagedTypeMetadata>();
//		list.add( entityTypeMetadata );
//		if ( !excludeSuperClassListeners( entityTypeMetadata.getClassInfo() ) ) {
//			for ( MappedSuperclassTypeMetadata mappedSuperclassTypeMetadata : entityTypeMetadata.getMappedSuperclassTypeMetadatas() ) {
//				list.add( mappedSuperclassTypeMetadata );
//				if ( excludeSuperClassListeners( mappedSuperclassTypeMetadata.getClassInfo() ) ) {
//					break;
//				}
//
//			}
//		}
//		return list;
//	}
//
//	private List<AnnotationInstance> findAllEntityListeners() {
//		List<AnnotationInstance> result = new ArrayList<AnnotationInstance>();
//		for ( final ManagedTypeMetadata managedTypeMetadata : mappingClassHierarchy ) {
//			List<AnnotationInstance> list = managedTypeMetadata.getClassInfo()
//					.annotations()
//					.get( JPADotNames.ENTITY_LISTENERS );
//			if ( CollectionHelper.isNotEmpty( list ) ) {
//				result.addAll( list );
//			}
//		}
//		return result;
//	}
//
//
//	public List<JpaCallbackSource> bindJPAListeners() {
//		final List<JpaCallbackSource> callbackClassList = new ArrayList<JpaCallbackSource>();
//		bindEntityCallbackEvents( callbackClassList );
//		bindEntityListeners( callbackClassList );
//		bindDefaultListeners( callbackClassList );
//		return callbackClassList;
//	}
//
//	private void bindEntityCallbackEvents(List<JpaCallbackSource> callbackClassList) {
//		Map<String, Void> overrideMethodCheck = new HashMap<String, Void>();
//		for ( final ManagedTypeMetadata managedTypeMetadata : mappingClassHierarchy ) {
//			try {
//				internalProcessCallbacks(
//						managedTypeMetadata.getClassInfo(),
//						callbackClassList,
//						false,
//						false,
//						overrideMethodCheck
//				);
//			}
//			catch ( PersistenceException error ) {
//				throw new PersistenceException( error.getMessage() + "entity listener " + managedTypeMetadata.getName() );
//			}
//		}
//	}
//
//	private void bindEntityListeners(List<JpaCallbackSource> callbackClassList) {
//		List<AnnotationInstance> entityListenerAnnotations = findAllEntityListeners();
//		for ( AnnotationInstance annotation : entityListenerAnnotations ) {
//			Type[] types = annotation.value().asClassArray();
//			for ( int i = types.length - 1; i >= 0; i-- ) {
//				String callbackClassName = types[i].name().toString();
//				try {
//					processJpaCallbacks( callbackClassName, true, callbackClassList, null );
//				}
//				catch ( PersistenceException error ) {
//					throw new PersistenceException( error.getMessage() + "entity listener " + callbackClassName );
//				}
//			}
//		}
//	}
//
//	private void processJpaCallbacks(
//			final String instanceCallbackClassName,
//			final boolean isListener,
//			final List<JpaCallbackSource> callbackClassList,
//			final Map<String, Void> overrideMethodCheck) {
//		final ClassInfo callbackClassInfo = findClassInfoByName( instanceCallbackClassName );
//		internalProcessCallbacks( callbackClassInfo, callbackClassList, isListener, false, overrideMethodCheck );
//	}
//
//
//	private void bindDefaultListeners(final List<JpaCallbackSource> callbackClassList) {
//		// Bind default JPA entity listener callbacks (unless excluded), using superclasses first (unless excluded)
//
//		Collection<AnnotationInstance> defaultEntityListenerAnnotations = context
//				.getIndex()
//				.getAnnotations( PseudoJpaDotNames.DEFAULT_ENTITY_LISTENERS );
//		for ( AnnotationInstance annotation : defaultEntityListenerAnnotations ) {
//			for ( Type callbackClass : annotation.value().asClassArray() ) {
//				String callbackClassName = callbackClass.name().toString();
//				ClassInfo callbackClassInfo = findClassInfoByName( callbackClassName );
//				try {
//					processDefaultJpaCallbacks( callbackClassInfo, callbackClassList );
//				}
//				catch ( PersistenceException error ) {
//					throw new PersistenceException( error.getMessage() + "default entity listener " + callbackClassName );
//				}
//			}
//		}
//	}
//
//	private static boolean excludeDefaultListeners(ClassInfo classInfo) {
//		return classInfo.annotations().containsKey( JPADotNames.EXCLUDE_DEFAULT_LISTENERS );
//	}
//
//	private static boolean excludeSuperClassListeners(ClassInfo classInfo) {
//		return classInfo.annotations().containsKey( JPADotNames.EXCLUDE_SUPERCLASS_LISTENERS );
//	}
//
//	private static boolean isNotRootObject(DotName name) {
//		return name != null && !JandexHelper.OBJECT.equals( name );
//	}
//
//	private void processDefaultJpaCallbacks(
//			final ClassInfo callbackClassInfo,
//			final List<JpaCallbackSource> jpaCallbackClassList) {
//		if ( excludeDefaultListeners( callbackClassInfo ) ) {
//			return;
//		}
//		// Process superclass first if available and not excluded
//		if ( !excludeSuperClassListeners( callbackClassInfo ) ) {
//			DotName superName = callbackClassInfo.superName();
//			if ( isNotRootObject( superName ) ) {
//				processDefaultJpaCallbacks( findClassInfoByName( superName.toString() ), jpaCallbackClassList );
//			}
//		}
//		internalProcessCallbacks( callbackClassInfo, jpaCallbackClassList, true, true, null );
//	}
//
//	private ClassInfo findClassInfoByName(String name) {
//		ClassInfo classInfo = context.getClassInfo( name );
//		if ( classInfo == null ) {
//			JandexHelper.throwNotIndexException( name );
//		}
//		return classInfo;
//	}
//
//	private void internalProcessCallbacks(
//			final ClassInfo callbackClassInfo,
//			final List<JpaCallbackSource> callbackClassList,
//			final boolean isListener,
//			final boolean isDefault,
//			final Map<String, Void> overrideMethodCheck) {
//		final Map<Class<?>, String> callbacksByType = new HashMap<Class<?>, String>( 7 );
//		createCallback(
//				PrePersist.class, callbacksByType, callbackClassInfo, isListener, isDefault, overrideMethodCheck
//		);
//		createCallback(
//				PreRemove.class, callbacksByType, callbackClassInfo, isListener, isDefault, overrideMethodCheck
//		);
//		createCallback(
//				PreUpdate.class, callbacksByType, callbackClassInfo, isListener, isDefault, overrideMethodCheck
//		);
//		createCallback(
//				PostLoad.class, callbacksByType, callbackClassInfo, isListener, isDefault, overrideMethodCheck
//		);
//		createCallback(
//				PostPersist.class, callbacksByType, callbackClassInfo, isListener, isDefault, overrideMethodCheck
//		);
//		createCallback(
//				PostRemove.class, callbacksByType, callbackClassInfo, isListener, isDefault, overrideMethodCheck
//		);
//		createCallback(
//				PostUpdate.class, callbacksByType, callbackClassInfo, isListener, isDefault, overrideMethodCheck
//		);
//		if ( !callbacksByType.isEmpty() ) {
//			final String name = callbackClassInfo.name().toString();
//			final JpaCallbackSource callbackSource = new JpaCallbackSourceImpl( name, callbacksByType, isListener );
//			callbackClassList.add( 0, callbackSource );
//		}
//	}
//
//
//	/**
//	 * @param callbackTypeClass Lifecycle event type class, like {@link javax.persistence.PrePersist},
//	 * {@link javax.persistence.PreRemove}, {@link javax.persistence.PreUpdate}, {@link javax.persistence.PostLoad},
//	 * {@link javax.persistence.PostPersist}, {@link javax.persistence.PostRemove}, {@link javax.persistence.PostUpdate}.
//	 * @param callbacksByClass A map that keyed by the {@param callbackTypeClass} and value is callback method name.
//	 * @param callbackClassInfo Jandex ClassInfo of callback method's container, should be either entity/mapped superclass or entity listener class.
//	 * @param isListener Is this callback method defined in an entity listener class or not.
//	 */
//	private void createCallback(
//			final Class callbackTypeClass,
//			final Map<Class<?>, String> callbacksByClass,
//			final ClassInfo callbackClassInfo,
//			final boolean isListener,
//			final boolean isDefault,
//			final Map<String, Void> overrideMethodCheck) {
//
//		final Collection<AnnotationInstance> annotationInstances;
//		if ( isDefault ) {
//			annotationInstances = context.getIndex().getAnnotations( EVENT_TYPE.get( callbackTypeClass ) );
//		}
//		else {
//			List<AnnotationInstance> temp = callbackClassInfo.annotations().get( EVENT_TYPE.get( callbackTypeClass ) );
//			annotationInstances = temp != null ? temp : Collections.EMPTY_LIST;
//		}
//
//		//there should be only one callback method per callbackType, isn't it?
//		//so only one callbackAnnotation?
//		for ( AnnotationInstance callbackAnnotation : annotationInstances ) {
//			MethodInfo methodInfo = (MethodInfo) callbackAnnotation.target();
//			validateMethod( methodInfo, callbackTypeClass, callbacksByClass, isListener );
//			final String name = methodInfo.name();
//			if ( overrideMethodCheck != null && overrideMethodCheck.containsKey( name ) ) {
//				continue;
//			}
//			else if ( overrideMethodCheck != null ) {
//				overrideMethodCheck.put( name, null );
//			}
//			if ( !isDefault ) {
//				callbacksByClass.put( callbackTypeClass, name );
//			}
//			else if ( methodInfo.declaringClass().name().equals( callbackClassInfo.name() ) ) {
//				if ( methodInfo.args().length != 1 ) {
//					throw new PersistenceException(
//							String.format(
//									"Callback method %s must have exactly one argument defined as either Object or %s in ",
//									name,
//									entityTypeMetadata.getName()
//							)
//					);
//				}
//				callbacksByClass.put( callbackTypeClass, name );
//			}
//		}
//	}
//
//
//	/**
//	 * Applying JPA Spec rules to validate listener callback method mapping.
//	 *
//	 * @param methodInfo The lifecycle callback method.
//	 * @param callbackTypeClass Lifecycle event type class, like {@link javax.persistence.PrePersist},
//	 * {@link javax.persistence.PreRemove}, {@link javax.persistence.PreUpdate}, {@link javax.persistence.PostLoad},
//	 * {@link javax.persistence.PostPersist}, {@link javax.persistence.PostRemove}, {@link javax.persistence.PostUpdate}.
//	 * @param callbacksByClass A map that keyed by the {@param callbackTypeClass} and value is callback method name.
//	 * @param isListener Is this callback method defined in an entity listener class or not.
//	 */
//	private void validateMethod(
//			MethodInfo methodInfo,
//			Class callbackTypeClass,
//			Map<Class<?>, String> callbacksByClass,
//			boolean isListener) {
//		final String name = methodInfo.name();
//
//		if ( methodInfo.returnType().kind() != Type.Kind.VOID ) {
//			throw new PersistenceException( "Callback method " + name + " must have a void return type in " );
//		}
//		if ( Modifier.isStatic( methodInfo.flags() ) || Modifier.isFinal( methodInfo.flags() ) ) {
//			throw new PersistenceException( "Callback method " + name + " must not be static or final in " );
//		}
//		Type[] argTypes = methodInfo.args();
//
//		if ( isListener ) {
//			if ( argTypes.length != 1 ) {
//				throw new PersistenceException( "Callback method " + name + " must have exactly one argument in " );
//			}
//			String argTypeName = argTypes[0].name().toString();
//			if ( !argTypeName.equals( Object.class.getName() ) && !argTypeName.equals( entityTypeMetadata.getName() ) ) {
//				Class typeClass = entityTypeMetadata.getLocalBindingContext().locateClassByName( argTypeName );
//				if ( !typeClass.isAssignableFrom( entityTypeMetadata.getConfiguredClass() ) ) {
//					throw new PersistenceException(
//							"The argument for callback method " + name +
//									" must be defined as either Object or " + entityTypeMetadata.getName() + " in "
//					);
//				}
//			}
//		}
//		else if ( argTypes.length != 0 ) {
//			throw new PersistenceException( "Callback method " + name + " must have no arguments in " );
//		}
//		if ( callbacksByClass.containsKey( callbackTypeClass ) ) {
//			throw new PersistenceException(
//					"Only one method may be annotated as a " + callbackTypeClass.getSimpleName() +
//							" callback method in "
//			);
//		}
//
//	}

}
