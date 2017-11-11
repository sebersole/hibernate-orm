/*
 * Hibernate, Relational Persistence for Idiomatic Java
 *
 * License: GNU Lesser General Public License (LGPL), version 2.1 or later.
 * See the lgpl.txt file in the root directory or <http://www.gnu.org/licenses/lgpl-2.1.html>.
 */
package org.hibernate.metamodel.model.relational.spi;

import java.util.Collection;

import org.hibernate.boot.model.relational.InitCommand;
import org.hibernate.engine.jdbc.env.spi.JdbcEnvironment;

/**
 * @author Andrea Boriero
 */
public interface DataBaseModelExtended {

	DatabaseModel getDataBaseModel();

	Collection<AuxiliaryDatabaseObject> getAuxiliaryDatabaseObjects();

	Collection<InitCommand> getInitCommands();

	default Collection<Namespace> getNamespaces(){
		return getDataBaseModel().getNamespaces();
	}

	default Namespace getDefaultNamespace(){
		return getDataBaseModel().getDefaultNamespace();
	}

	default JdbcEnvironment getJdbcEnvironment(){
		return getDataBaseModel().getJdbcEnvironment();
	}
}
