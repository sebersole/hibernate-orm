/*
 * Hibernate, Relational Persistence for Idiomatic Java
 *
 * SPDX-License-Identifier: Apache-2.0
 * Copyright: Red Hat Inc. and Hibernate Authors
 */
package org.hibernate.boot.models.bind.spi;

import java.util.EnumSet;

import org.hibernate.boot.model.naming.Identifier;

/**
 * @author Steve Ebersole
 */
public interface BindingOptions {
	Identifier getDefaultCatalogName();
	Identifier getDefaultSchemaName();

	EnumSet<QuotedIdentifierTarget> getGloballyQuotedIdentifierTargets();
}
