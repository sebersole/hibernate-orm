/*
 * Hibernate, Relational Persistence for Idiomatic Java
 *
 * License: GNU Lesser General Public License (LGPL), version 2.1 or later.
 * See the lgpl.txt file in the root directory or http://www.gnu.org/licenses/lgpl-2.1.html.
 */
package org.hibernate.boot.annotations.model.spi;

import java.lang.annotation.Annotation;

import org.hibernate.boot.annotations.source.spi.ClassDetails;
import org.hibernate.boot.annotations.source.spi.MethodDetails;

import jakarta.persistence.PostLoad;
import jakarta.persistence.PostPersist;
import jakarta.persistence.PostRemove;
import jakarta.persistence.PostUpdate;
import jakarta.persistence.PrePersist;
import jakarta.persistence.PreRemove;
import jakarta.persistence.PreUpdate;

/**
 * Models information about a JPA callback (either entity-local or listener)
 *
 * @author Hardy Ferentschik
 * @author Steve Ebersole
 */
public class CallbacksMetadata {
	private final ClassDetails callbackTarget;
	private final boolean isEntityListener;
	private final MethodDetails prePersistCallback;
	private final MethodDetails preRemoveCallback;
	private final MethodDetails preUpdateCallback;
	private final MethodDetails postLoadCallback;
	private final MethodDetails postPersistCallback;
	private final MethodDetails postRemoveCallback;
	private final MethodDetails postUpdateCallback;

	public CallbacksMetadata(
			ClassDetails callbackTarget,
			boolean isEntityListener,
			MethodDetails prePersistCallback,
			MethodDetails preRemoveCallback,
			MethodDetails preUpdateCallback,
			MethodDetails postLoadCallback,
			MethodDetails postPersistCallback,
			MethodDetails postRemoveCallback,
			MethodDetails postUpdateCallback) {
		this.callbackTarget = callbackTarget;
		this.isEntityListener = isEntityListener;
		this.prePersistCallback = prePersistCallback;
		this.preRemoveCallback = preRemoveCallback;
		this.preUpdateCallback = preUpdateCallback;
		this.postLoadCallback = postLoadCallback;
		this.postPersistCallback = postPersistCallback;
		this.postRemoveCallback = postRemoveCallback;
		this.postUpdateCallback = postUpdateCallback;
	}

	public ClassDetails getCallbackTarget() {
		return callbackTarget;
	}

	public boolean isListener() {
		return isEntityListener;
	}

	public MethodDetails getCallbackMethod(Class<? extends Annotation> callbackType) {
		if ( PrePersist.class.equals( callbackType ) ) {
			return prePersistCallback;
		}
		else if ( PreRemove.class.equals( callbackType ) ) {
			return preRemoveCallback;
		}
		else if ( PreUpdate.class.equals( callbackType ) ) {
			return preUpdateCallback;
		}
		else if ( PostLoad.class.equals( callbackType ) ) {
			return postLoadCallback;
		}
		else if ( PostPersist.class.equals( callbackType ) ) {
			return postPersistCallback;
		}
		else if ( PostRemove.class.equals( callbackType ) ) {
			return postRemoveCallback;
		}
		else if ( PostUpdate.class.equals( callbackType ) ) {
			return postUpdateCallback;
		}

		throw new IllegalArgumentException( "Unknown callback type requested : " + callbackType.getName() );
	}

	@Override
	public String toString() {
		return "JpaCallbackSourceImpl{callbackTarget=" + callbackTarget +
				", isEntityListener=" + isEntityListener + '}';
	}
}


