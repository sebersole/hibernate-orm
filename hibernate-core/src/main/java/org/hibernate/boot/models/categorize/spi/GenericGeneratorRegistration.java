/*
 * Hibernate, Relational Persistence for Idiomatic Java
 *
 * License: GNU Lesser General Public License (LGPL), version 2.1 or later.
 * See the lgpl.txt file in the root directory or http://www.gnu.org/licenses/lgpl-2.1.html.
 */
package org.hibernate.boot.models.categorize.spi;

import org.hibernate.annotations.GenericGenerator;
import org.hibernate.models.spi.AnnotationUsage;

/**
 * Global registration of a generic generator
 *
 * @author Steve Ebersole
 * @see GenericGenerator
 * @see org.hibernate.boot.jaxb.mapping.spi.JaxbGenericIdGeneratorImpl
 */
public record GenericGeneratorRegistration(String name, AnnotationUsage<GenericGenerator> configuration) {
}
