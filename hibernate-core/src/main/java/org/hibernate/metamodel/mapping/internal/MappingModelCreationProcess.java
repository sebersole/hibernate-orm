/*
 * Hibernate, Relational Persistence for Idiomatic Java
 *
 * License: GNU Lesser General Public License (LGPL), version 2.1 or later
 * See the lgpl.txt file in the root directory or http://www.gnu.org/licenses/lgpl-2.1.html
 */
package org.hibernate.metamodel.mapping.internal;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import org.hibernate.NotYetImplementedFor6Exception;
import org.hibernate.internal.util.MutableString;
import org.hibernate.metamodel.mapping.EntityMappingType;
import org.hibernate.metamodel.mapping.MappingModelCreationLogger;
import org.hibernate.metamodel.mapping.ModelPart;
import org.hibernate.metamodel.mapping.ModelPartContainer;
import org.hibernate.metamodel.mapping.NonTransientException;
import org.hibernate.metamodel.spi.RuntimeModelCreationContext;
import org.hibernate.persister.entity.EntityPersister;

/**
 * @author Steve Ebersole
 */
public class MappingModelCreationProcess {
	/**
	 * Triggers creation of the mapping model
	 */
	public static void process(
			Map<String,EntityPersister> entityPersisterMap,
			RuntimeModelCreationContext creationContext) {
		final MappingModelCreationProcess process = new MappingModelCreationProcess(
				entityPersisterMap,
				creationContext
		);
		process.execute();
	}

	private final Map<String,EntityPersister> entityPersisterMap;

	private final RuntimeModelCreationContext creationContext;

	private String currentlyProcessingRole;

	private List<PostInitCallbackEntry> postInitCallbacks;
	private List<PostInitCallbackEntry> foreignKeyPostInitCallbacks;

	private MappingModelCreationProcess(
			Map<String,EntityPersister> entityPersisterMap,
			RuntimeModelCreationContext creationContext) {
		this.entityPersisterMap = entityPersisterMap;
		this.creationContext = creationContext;
	}

	public RuntimeModelCreationContext getCreationContext() {
		return creationContext;
	}

	public EntityPersister getEntityPersister(String name) {
		return entityPersisterMap.get( name );
	}

	/**
	 * Instance-level trigger for {@link #process}
	 */
	private void execute() {
		for ( EntityPersister entityPersister : entityPersisterMap.values() ) {
			entityPersister.linkWithSuperType( this );
		}

		for ( EntityPersister entityPersister : entityPersisterMap.values() ) {
			currentlyProcessingRole = entityPersister.getEntityName();

			entityPersister.prepareMappingModel( this );
		}

		MappingModelCreationLogger.LOGGER.debugf( "Starting generic post-init callbacks" );
		executePostInitCallbacks( postInitCallbacks );

		MappingModelCreationLogger.LOGGER.debugf( "Starting foreign-key post-init callbacks" );
		executePostInitCallbacks( foreignKeyPostInitCallbacks );
	}

	private void executePostInitCallbacks(List<PostInitCallbackEntry> postInitCallbacks) {
		while ( postInitCallbacks != null && !postInitCallbacks.isEmpty() ) {

			// NOTE: copy to avoid CCME via iterate-copy pattern
			final ArrayList<PostInitCallbackEntry> copy = new ArrayList<>( postInitCallbacks );

			copy.forEach(
					(callbackEntry) -> {
						try {
							final boolean completed = callbackEntry.process();
							if ( completed ) {
								postInitCallbacks.remove( callbackEntry );
							}
						}
						catch (Exception e) {
							if ( e instanceof NonTransientException
									|| e instanceof ClassCastException
									|| e instanceof NotYetImplementedFor6Exception ) {
								MappingModelCreationLogger.LOGGER.debugf(
										"Mapping-model creation encountered non-transient error (%s) : %s",
										callbackEntry.description,
										e
								);
								throw e;
							}

							final String format = "Mapping-model creation encountered (possibly) transient error (%s) : %s";
							if ( MappingModelCreationLogger.TRACE_ENABLED ) {
								MappingModelCreationLogger.LOGGER.tracef( e, format, callbackEntry.description, e );
							}
							else {
								MappingModelCreationLogger.LOGGER.debugf( format, callbackEntry.description, e );
							}
						}
					}
			);

			if ( copy.size() == postInitCallbacks.size() ) {
				// none of the remaining callbacks could complete fully, this is an error
				final StringBuilder buffer = new StringBuilder(
						"No post-init callbacks could complete.  Remaining:" + System.lineSeparator()
				);

				final MutableString separationChar = new MutableString();

				postInitCallbacks.forEach(
						postInitCallback -> {
							buffer.append( "\t" ).append( postInitCallback.description );
							if ( separationChar.getValue() != null ) {
								buffer.append( separationChar.getValue() );
							}
							else {
								separationChar.setValue( "," );
							}
							buffer.append( System.lineSeparator() );
						}
				);

				throw new IllegalStateException( buffer.toString() );
			}
		}
	}

	public <T extends ModelPart> T processSubPart(
			String localName,
			SubPartMappingProducer<T> subPartMappingProducer) {
		assert currentlyProcessingRole != null;

		final String initialRole = currentlyProcessingRole;
		currentlyProcessingRole = currentlyProcessingRole + '#' + localName;

		try {
			return subPartMappingProducer.produceSubMapping( currentlyProcessingRole, this );
		}
		finally {
			currentlyProcessingRole = initialRole;
		}
	}

	public void registerInitializationCallback(String description, PostInitCallback callback) {
		if ( postInitCallbacks == null ) {
			postInitCallbacks = new ArrayList<>();
		}

		postInitCallbacks.add( new PostInitCallbackEntry( description, callback ) );
	}

	/**
	 * todo (6.0) : is this needed anymore with the addition of {@link #registerSubPartGroupInitializationListener}?
	 */
	public void registerForeignKeyPostInitCallbacks(String description, PostInitCallback callback) {
		if ( foreignKeyPostInitCallbacks == null ) {
			foreignKeyPostInitCallbacks = new ArrayList<>();
		}

		foreignKeyPostInitCallbacks.add( new PostInitCallbackEntry( description, callback ) );
	}

	private static class PostInitCallbackEntry {
		private final String description;
		private final PostInitCallback callback;

		public PostInitCallbackEntry(String description, PostInitCallback callback) {
			this.description = description;
			this.callback = callback;
		}

		private boolean process() {
			MappingModelCreationLogger.LOGGER.debugf(
					"Starting MappingModelCreationProcess.PostInitCallbackEntry processing : %s",
					description
			);
			return callback.process();
		}

		@Override
		public String toString() {
			return "PostInitCallbackEntry - " + description;
		}

	}

	public enum SubPartGroup {
		/**
		 * Called after identifier has been initialized.  Technically it happens after all
		 * "root model parts" have been initialized:<ul>
		 *     <li>{@link EntityMappingType#getIdentifierMapping()}</li>
		 *     <li>{@link EntityMappingType#getVersionMapping()}</li>
		 *     <li>{@link EntityMappingType#getDiscriminatorMapping()}</li>
		 *     <li>{@link EntityMappingType#getNaturalIdMapping()}</li>
		 *     <li>{@link EntityMappingType#getTenancyMapping()}</li>
		 *     <li>{@link EntityMappingType#getRowIdMapping()}</li>
		 * </ul>
		 */
		ROOT,

		/**
		 * Called after all attributes have been initialized
		 */
		NORMAL
	}

	@FunctionalInterface
	public interface InitializationListener {
		void initialized();
	}

	public interface InitializableContainer {
		default boolean isInitialized(SubPartGroup group) {
			return true;
		}
	}

	private static class InitializationListenerGroup {
		private final InitializationListener initial;

		private List<InitializationListener> subsequent;

		public InitializationListenerGroup(InitializationListener initial) {
			this.initial = initial;
		}
		public void addListener(InitializationListener listener) {
			if ( subsequent == null ) {
				subsequent = new ArrayList<>();
			}
			subsequent.add( listener );
		}

	}

	private Map<InitializableContainer,InitializationListenerGroup> rootListeners;
	private Map<InitializableContainer,InitializationListenerGroup> normalListeners;

	public void registerSubPartGroupInitializationListener(
			InitializableContainer container,
			SubPartGroup group,
			InitializationListener listener) {
		if ( container.isInitialized( group ) ) {
			listener.initialized();
			return;
		}

		final Map<InitializableContainer,InitializationListenerGroup> target;
		final InitializationListenerGroup existing;

		if ( group == SubPartGroup.ROOT ) {
			if ( rootListeners == null ) {
				rootListeners = new LinkedHashMap<>();
				existing = null;
			}
			else {
				existing = rootListeners.get( container );
			}
			target = rootListeners;
		}
		else {
			if ( normalListeners == null ) {
				normalListeners = new LinkedHashMap<>();
				existing = null;
			}
			else {
				existing = normalListeners.get( container );
			}
			target = normalListeners;
		}

		if ( existing == null ) {
			target.put( container, new InitializationListenerGroup( listener ) );
		}
		else {
			existing.addListener( listener );
		}
	}

	public void subPartGroupInitialized(InitializableContainer container, SubPartGroup groupType) {
		final Map<InitializableContainer,InitializationListenerGroup> target;
		if ( groupType == SubPartGroup.ROOT ) {
			target = rootListeners;
		}
		else {
			target = normalListeners;
		}

		if ( target == null || target.isEmpty() ) {
			return;
		}

		final InitializationListenerGroup group = target.get( container );
		if ( group == null ) {
			return;
		}

		group.initial.initialized();
		if ( group.subsequent != null ) {
			group.subsequent.forEach( InitializationListener::initialized );
		}
	}

	@FunctionalInterface
	public interface PostInitCallback {
		boolean process();
	}

	/**
	 * Explicitly defined to better control (for now) the args
	 */
	@FunctionalInterface
	public interface SubPartMappingProducer<T> {
		T produceSubMapping(String role, MappingModelCreationProcess creationProcess);
	}
}
