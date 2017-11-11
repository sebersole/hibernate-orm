/*
 * Hibernate, Relational Persistence for Idiomatic Java
 *
 * License: GNU Lesser General Public License (LGPL), version 2.1 or later.
 * See the lgpl.txt file in the root directory or <http://www.gnu.org/licenses/lgpl-2.1.html>.
 */
package org.hibernate.metamodel.model.relational.internal;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;

import org.hibernate.boot.model.relational.InitCommand;
import org.hibernate.metamodel.model.relational.spi.AuxiliaryDatabaseObject;
import org.hibernate.metamodel.model.relational.spi.DataBaseModelExtended;
import org.hibernate.metamodel.model.relational.spi.DatabaseModel;

/**
 * @author Andrea Boriero
 */
public class DataBaseModelExtendedImpl implements DataBaseModelExtended {

	private final DatabaseModel databaseModel;
	private final List<AuxiliaryDatabaseObject> auxiliaryDatabaseObjects = new ArrayList<>();
	private final List<InitCommand> initCommands = new ArrayList<>();

	public DataBaseModelExtendedImpl(DatabaseModel databaseModel) {
		this.databaseModel = databaseModel;
	}

	@Override
	public DatabaseModel getDataBaseModel() {
		return databaseModel;
	}

	@Override
	public Collection<AuxiliaryDatabaseObject> getAuxiliaryDatabaseObjects() {
		return auxiliaryDatabaseObjects;
	}

	@Override
	public Collection<InitCommand> getInitCommands() {
		return initCommands;
	}

	public void addInitCommand(InitCommand initCommand) {
		initCommands.add( initCommand );
	}

	public void addAuxiliaryDatabaseObject(AuxiliaryDatabaseObject auxiliaryDatabaseObject) {
		this.auxiliaryDatabaseObjects.add( auxiliaryDatabaseObject );
	}
}
