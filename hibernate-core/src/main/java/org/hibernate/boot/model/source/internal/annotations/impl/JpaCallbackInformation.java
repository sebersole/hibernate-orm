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
package org.hibernate.boot.model.source.internal.annotations.impl;

import java.lang.annotation.Annotation;
import javax.persistence.PostLoad;
import javax.persistence.PostPersist;
import javax.persistence.PostRemove;
import javax.persistence.PostUpdate;
import javax.persistence.PrePersist;
import javax.persistence.PreRemove;
import javax.persistence.PreUpdate;

import org.hibernate.boot.model.source.spi.JpaCallbackSource;

import org.jboss.jandex.ClassInfo;
import org.jboss.jandex.MethodInfo;

/**
 * Models information about a JPA callback (either entity-local or listener)
 *
 * @author Hardy Ferentschik
 * @author Steve Ebersole
 */
public class JpaCallbackInformation implements JpaCallbackSource {
	private final ClassInfo callbackTarget;
	private final boolean isEntityListener;
	private final MethodInfo prePersistCallback;
	private final MethodInfo preRemoveCallback;
	private final MethodInfo preUpdateCallback;
	private final MethodInfo postLoadCallback;
	private final MethodInfo postPersistCallback;
	private final MethodInfo postRemoveCallback;
	private final MethodInfo postUpdateCallback;

	public JpaCallbackInformation(
			ClassInfo callbackTarget,
			boolean isEntityListener,
			MethodInfo prePersistCallback,
			MethodInfo preRemoveCallback,
			MethodInfo preUpdateCallback,
			MethodInfo postLoadCallback,
			MethodInfo postPersistCallback,
			MethodInfo postRemoveCallback,
			MethodInfo postUpdateCallback) {
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

	@Override
	public String getName() {
		return callbackTarget.name().toString();
	}

	@Override
	public boolean isListener() {
		return isEntityListener;
	}

	@Override
	public MethodInfo getCallbackMethod(Class<? extends Annotation> callbackType) {
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


