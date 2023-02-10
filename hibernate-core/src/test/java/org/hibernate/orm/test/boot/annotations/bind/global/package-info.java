/*
 * Hibernate, Relational Persistence for Idiomatic Java
 *
 * License: GNU Lesser General Public License (LGPL), version 2.1 or later.
 * See the lgpl.txt file in the root directory or http://www.gnu.org/licenses/lgpl-2.1.html.
 */

/**
 * @author Steve Ebersole
 */
@GenericGenerator(name = "gen1")
@JavaTypeRegistration( javaType = String.class, descriptorClass = CustomStringJavaType.class )
@JdbcTypeRegistration(CustomVarcharJdbcType.class)
@TypeRegistration(basicClass = BitSet.class, userType = BitSetUserType.class)
@ConverterRegistration(domainType = Map.class, converter = MapConverter.class)
@NamedQuery(name = "query1", query = "select * from BasicEntity")
@NamedNativeQuery(name = "query2", query = "select * from BasicEntity")
@FilterDef(name = "filter-def1")
package org.hibernate.orm.test.boot.annotations.bind.global;

import java.util.BitSet;
import java.util.Map;

import org.hibernate.annotations.ConverterRegistration;
import org.hibernate.annotations.FilterDef;
import org.hibernate.annotations.GenericGenerator;
import org.hibernate.annotations.JavaTypeRegistration;
import org.hibernate.annotations.JdbcTypeRegistration;
import org.hibernate.annotations.NamedNativeQuery;
import org.hibernate.annotations.NamedQuery;
import org.hibernate.annotations.TypeRegistration;
import org.hibernate.orm.test.mapping.converted.converter.mutabiity.MapConverter;
