/*
 * Hibernate, Relational Persistence for Idiomatic Java
 *
 * License: GNU Lesser General Public License (LGPL), version 2.1 or later
 * See the lgpl.txt file in the root directory or http://www.gnu.org/licenses/lgpl-2.1.html
 */
package org.hibernate.metamodel.mapping.internal.fk;

import java.util.Collections;
import java.util.List;
import java.util.Locale;
import java.util.Set;
import java.util.function.Consumer;
import java.util.function.Supplier;

import org.hibernate.LockMode;
import org.hibernate.query.NavigablePath;
import org.hibernate.sql.ast.spi.SqlAliasBase;
import org.hibernate.sql.ast.spi.SqlAliasBaseGenerator;
import org.hibernate.sql.ast.spi.SqlAstCreationState;
import org.hibernate.sql.ast.tree.from.TableGroup;
import org.hibernate.sql.ast.tree.from.TableGroupJoin;
import org.hibernate.sql.ast.tree.from.TableReference;
import org.hibernate.sql.ast.tree.from.TableReferenceJoin;
import org.hibernate.sql.ast.tree.from.VirtualTableGroup;
import org.hibernate.sql.results.graph.DomainResultCreationState;

/**
 * @author Steve Ebersole
 */
public class KeyTableGroupComposite implements VirtualTableGroup {
	private final NavigablePath navigablePath;
	private final KeyModelPartComposite modelPart;

	private final String groupAlias;

	private final String targetTableName;
	private final TableReference referring;

	public KeyTableGroupComposite(
			NavigablePath navigablePath,
			KeyModelPartComposite modelPart,
			TableGroup parentTableGroup,
			DomainResultCreationState creationState) {
		this.navigablePath = navigablePath;
		this.modelPart = modelPart;

		final SqlAstCreationState sqlAstCreationState = creationState.getSqlAstCreationState();
		final SqlAliasBaseGenerator aliasBaseGenerator = sqlAstCreationState.getSqlAliasBaseGenerator();
		final SqlAliasBase sqlAliasBase = aliasBaseGenerator.createSqlAliasBase( modelPart.getSqlAliasStem() );

		this.groupAlias = sqlAliasBase.getAliasStem();

		final ForeignKey foreignKeyDescriptor = modelPart.getForeignKeyDescriptor();
		this.targetTableName = foreignKeyDescriptor.getTargetSide().getTableName();
		this.referring = parentTableGroup.getTableReference( foreignKeyDescriptor.getReferringSide().getTableName() );
	}


	@Override
	public NavigablePath getNavigablePath() {
		return navigablePath;
	}

	@Override
	public KeyModelPartComposite getExpressionType() {
		return modelPart;
	}

	@Override
	public String getGroupAlias() {
		return groupAlias;
	}

	@Override
	public KeyModelPartComposite getModelPart() {
		return modelPart;
	}

	@Override
	public LockMode getLockMode() {
		return LockMode.READ;
	}

	@Override
	public Set<TableGroupJoin> getTableGroupJoins() {
		return Collections.emptySet();
	}

	@Override
	public boolean hasTableGroupJoins() {
		return false;
	}

	@Override
	public void setTableGroupJoins(Set<TableGroupJoin> joins) {
		throw new UnsupportedOperationException();
	}

	@Override
	public void addTableGroupJoin(TableGroupJoin join) {
		throw new UnsupportedOperationException();
	}

	@Override
	public void visitTableGroupJoins(Consumer<TableGroupJoin> consumer) {
	}

	@Override
	public void applyAffectedTableNames(Consumer<String> nameCollector) {
	}

	@Override
	public TableReference getPrimaryTableReference() {
		return referring;
	}

	@Override
	public List<TableReferenceJoin> getTableReferenceJoins() {
		return Collections.emptyList();
	}

	@Override
	public boolean isInnerJoinPossible() {
		return false;
	}

	@Override
	public TableReference resolveTableReference(
			String tableExpression,
			Supplier<TableReference> creator) {
		return resolveTableReference( tableExpression );
	}

	@Override
	public TableReference resolveTableReference(String tableExpression) {
		final TableReference tableReference = getTableReference( tableExpression );
		if ( tableReference != null ) {
			return tableReference;
		}

		throw new IllegalArgumentException( "Unknown table name : " + tableExpression );
	}

	@Override
	public TableReference getTableReference(String tableExpression) {
		if ( tableExpression.equals( referring.getTableExpression() ) ) {
			return referring;
		}

		throw new IllegalArgumentException(
				String.format(
						Locale.ROOT,
						"Table `%s` was requested, but expecting `%s`",
						tableExpression,
						referring.getTableExpression()
				)
		);
	}
}
