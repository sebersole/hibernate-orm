/*
 * Hibernate, Relational Persistence for Idiomatic Java
 *
 * License: GNU Lesser General Public License (LGPL), version 2.1 or later.
 * See the lgpl.txt file in the root directory or <http://www.gnu.org/licenses/lgpl-2.1.html>.
 */

/*
 * Hibernate, Relational Persistence for Idiomatic Java
 *
 * Copyright (c) 2014, Red Hat Inc. or third-party contributors as
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
package org.hibernate.boot.jaxb.mapping.spi;

import java.util.List;
import javax.persistence.EnumType;
import javax.persistence.TemporalType;

/**
 * Common interface for JAXB bindings that represent persistent collection attributes.
 *
 * @author Brett Meyer
 */
public interface CollectionAttribute extends FetchableAttribute {

    public String getOrderBy();

    public void setOrderBy(String value);

    public JaxbOrderColumn getOrderColumn();

    public void setOrderColumn(JaxbOrderColumn value);
	
	public JaxbMapKey getMapKey();

    public void setMapKey(JaxbMapKey value);

    public JaxbMapKeyClass getMapKeyClass();

    public void setMapKeyClass(JaxbMapKeyClass value);

    public TemporalType getMapKeyTemporal();

    public void setMapKeyTemporal(TemporalType value);

    public EnumType getMapKeyEnumerated();

    public void setMapKeyEnumerated(EnumType value);

    public JaxbHbmType getMapKeyType();

    public void setMapKeyType(JaxbHbmType value);

    public List<JaxbAttributeOverride> getMapKeyAttributeOverride();

    public List<JaxbConvert> getMapKeyConvert();

    public JaxbMapKeyColumn getMapKeyColumn();

    public void setMapKeyColumn(JaxbMapKeyColumn value);

    public List<JaxbMapKeyJoinColumn> getMapKeyJoinColumn();

    public JaxbForeignKey getMapKeyForeignKey();

    public void setMapKeyForeignKey(JaxbForeignKey value);
}
