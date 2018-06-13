/*
 * Hibernate, Relational Persistence for Idiomatic Java
 *
 * License: GNU Lesser General Public License (LGPL), version 2.1 or later
 * See the lgpl.txt file in the root directory or http://www.gnu.org/licenses/lgpl-2.1.html
 */
package org.hibernate.orm.test.query.sqm.produce;

import java.util.Objects;

import java.util.stream.Stream;
import org.hibernate.query.sqm.produce.internal.hql.grammar.HqlLexer;
import org.hibernate.query.sqm.produce.internal.hql.grammar.HqlParser;

import org.hibernate.testing.junit5.FailureExpected;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;
import org.junit.runners.Parameterized;

import org.antlr.v4.runtime.CharStreams;
import org.antlr.v4.runtime.CodePointCharStream;
import org.antlr.v4.runtime.CommonTokenStream;

import static org.junit.Assert.fail;

@FailureExpected("Disabled while HQL grammar is beign developed.")
public class HqlParserTest {

	@ParameterizedTest
	@MethodSource("data")
	public void parseStatement(String source) {
		try {
			final CodePointCharStream input =
					CharStreams.fromString( Objects.requireNonNull( source, "The statement cannot be null" ) );
			final HqlLexer lexer = new HqlLexer( input );
			final CommonTokenStream tokens = new CommonTokenStream( lexer );
			final HqlParser parser = new HqlParser( tokens );
			parser.statement();
		}
		catch (Exception e) {
			final String message = e.getMessage();
			fail( "failed to parse HQL:\n" + source + "\n\n" + message );
		}
	}

	@Parameterized.Parameters(name = "{index}: {0}")
	public static Stream<Arguments> data() {
		return Stream.of(
				Arguments.of("DELETE FROM Customer c WHERE c.status = 'inactive'"),
				Arguments.of("DELETE FROM Customer c WHERE c.status = 'inactive' AND c.orders IS EMPTY"),
				Arguments.of("DELETE FROM Employee e"),
				Arguments.of("DELETE FROM Employee e WHERE 1 <> 1"),
				Arguments.of("DELETE FROM TestBean bean"),
				Arguments.of("DELETE FROM TestBean bean WHERE bean.id = 0"),
				Arguments.of("SELECT AVG(bean) FROM TestBean bean WHERE bean.id = :id"),
				Arguments.of("SELECT AVG(o.quantity) as q, a.zipcode FROM Customer c JOIN c.orders o JOIN c.address a WHERE a.state = 'CA' GROUP BY a.zipcode ORDER BY q DESC"),
				Arguments.of("SELECT AVG(o.quantity) FROM Order o"),
				Arguments.of("SELECT b.name, b.ISBN FROM Order o JOIN TREAT(o.product AS Book) b"),
				Arguments.of("SELECT bean FROM org.jpasecurity.model.TestBean bean WHERE bean.id = :id"),
				Arguments.of("SELECT bean FROM TestBean bean INNER JOIN bean.name name WHERE bean.id = :id"),
				Arguments.of("SELECT bean FROM TestBean bean INNER JOIN bean.parent parent ON (parent.name = 'Parent') WHERE bean.id = :id"),
				Arguments.of("SELECT bean FROM TestBean bean INNER JOIN bean.parent parent ON parent.name = 'Parent' WHERE bean.id = :id"),
				Arguments.of("SELECT bean FROM TestBean bean INNER JOIN bean.related related WHERE ENTRY(related) IS NOT NULL"),
				Arguments.of("SELECT bean FROM TestBean bean INNER JOIN bean.related related WHERE ENTRY(related) IS NULL"),
				Arguments.of("SELECT bean FROM TestBean bean INNER JOIN bean.related related WHERE KEY(related).name = 'name 1'"),
				Arguments.of("SELECT bean FROM TestBean bean INNER JOIN bean.related related WHERE VALUE(related).name = 'name 1'"),
				Arguments.of("SELECT bean FROM TestBean bean INNER JOIN FETCH bean.name WHERE bean.id = :id"),
				Arguments.of("SELECT bean FROM TestBean bean LEFT OUTER JOIN bean.name name WHERE bean.id = :id"),
				Arguments.of("SELECT bean FROM TestBean bean LEFT OUTER JOIN FETCH bean.name beanName WHERE bean.id = :id"),
				Arguments.of("SELECT bean FROM TestBean bean LEFT OUTER JOIN FETCH bean.name WHERE bean.id = :id"),
				Arguments.of("SELECT bean FROM TestBean bean ORDER BY bean.id ASC, bean.name DESC"),
				Arguments.of("SELECT bean FROM TestBean bean WHERE 'Horst' = COALESCE(bean.name, 'Doe')"),
				Arguments.of("SELECT bean FROM TestBean bean WHERE 'Horst' = COALESCE(bean.name, 'Horst')"),
				Arguments.of("SELECT bean FROM TestBean bean WHERE (bean.collectionProperty IS NULL OR SIZE(bean.collectionProperty) = 0)"),
				Arguments.of("SELECT bean FROM TestBean bean WHERE (bean.id = 0 AND bean.name = 'Test')"),
				Arguments.of("SELECT bean FROM TestBean bean WHERE (bean.id BETWEEN 5 AND 7)"),
				Arguments.of("SELECT bean FROM TestBean bean WHERE (bean.id NOT BETWEEN 5 AND 7)"),
				Arguments.of("SELECT bean FROM TestBean bean WHERE -MOD(bean.id, 2) = -1"),
				Arguments.of("SELECT bean FROM TestBean bean WHERE bean MEMBER OF bean.collectionProperty"),
				Arguments.of("SELECT bean FROM TestBean bean WHERE bean NOT MEMBER OF bean.collectionProperty"),
				Arguments.of("SELECT bean FROM TestBean bean WHERE bean.collectionProperty IS EMPTY"),
				Arguments.of("SELECT bean FROM TestBean bean WHERE bean.collectionProperty IS NOT EMPTY"),
				Arguments.of("SELECT bean FROM TestBean bean WHERE bean.created < CURRENT_TIME"),
				Arguments.of("SELECT bean FROM TestBean bean WHERE bean.created < CURRENT_TIMESTAMP"),
				Arguments.of("SELECT bean FROM TestBean bean WHERE bean.dateProperty = CURRENT_DATE"),
				Arguments.of("SELECT bean FROM TestBean bean WHERE bean.entry = ?1"),
				Arguments.of("SELECT bean FROM TestBean bean WHERE bean.id = :id"),
				Arguments.of("SELECT bean FROM TestBean bean WHERE bean.id = MAX(DISTINCT bean.id)"),
				Arguments.of("SELECT bean FROM TestBean bean WHERE bean.id = MIN(DISTINCT bean.id)"),
				Arguments.of("SELECT bean FROM TestBean bean WHERE bean.id > 0"),
				Arguments.of("SELECT bean FROM TestBean bean WHERE bean.key = ?1"),
				Arguments.of("SELECT bean FROM TestBean bean WHERE bean.name = ?1"),
				Arguments.of("SELECT bean FROM TestBean bean WHERE bean.name = ALL (SELECT bean.collectionProperty.name FROM TestBean bean)"),
				Arguments.of("SELECT bean FROM TestBean bean WHERE bean.name = ANY (SELECT bean.collectionProperty.name FROM TestBean bean)"),
				Arguments.of("SELECT bean FROM TestBean bean WHERE bean.name = SOME (SELECT bean.collectionProperty.name FROM TestBean bean)"),
				Arguments.of("SELECT bean FROM TestBean bean WHERE bean.name BETWEEN bean.name AND COALESCE(bean.name, 'Doe')"),
				Arguments.of("SELECT bean FROM TestBean bean WHERE bean.name BETWEEN COALESCE(bean.name, 'Doe') AND bean.name"),
				Arguments.of("SELECT bean FROM TestBean bean WHERE bean.name BETWEEN COALESCE(bean.name, 'Doe') AND COALESCE(bean.name, 'Doe')"),
				Arguments.of("SELECT bean FROM TestBean bean WHERE bean.name IN ('name 1', 'name 2')"),
				Arguments.of("SELECT bean FROM TestBean bean WHERE bean.name IS NOT NULL"),
				Arguments.of("SELECT bean FROM TestBean bean WHERE bean.name LIKE '%beanName%'"),
				Arguments.of("SELECT bean FROM TestBean bean WHERE bean.name LIKE '*beanName*' ESCAPE '*'"),
				Arguments.of("SELECT bean FROM TestBean bean WHERE bean.name LIKE '\\beanName\\' ESCAPE '\\'"),
				Arguments.of("SELECT bean FROM TestBean bean WHERE bean.name LIKE '/beanName/' ESCAPE '/'"),
				Arguments.of("SELECT bean FROM TestBean bean WHERE bean.name NOT BETWEEN bean.name AND COALESCE(bean.name, 'Doe')"),
				Arguments.of("SELECT bean FROM TestBean bean WHERE bean.name NOT BETWEEN COALESCE(bean.name, 'Name') AND bean.name"),
				Arguments.of("SELECT bean FROM TestBean bean WHERE bean.name NOT BETWEEN COALESCE(bean.name, 'Name') AND COALESCE(bean.name, 'Name')"),
				Arguments.of("SELECT bean FROM TestBean bean WHERE bean.name NOT IN ('name 1', 'name 2')"),
				Arguments.of("SELECT bean FROM TestBean bean WHERE bean.name NOT LIKE '%beanName%'"),
				Arguments.of("SELECT bean FROM TestBean bean WHERE bean.value = ?1"),
				Arguments.of("SELECT bean FROM TestBean bean WHERE COALESCE(bean.name, 'Doe') = 'Name'"),
				Arguments.of("SELECT bean FROM TestBean bean WHERE COALESCE(bean.name, 'Doe') BETWEEN bean.name AND COALESCE(bean.name, 'Doe')"),
				Arguments.of("SELECT bean FROM TestBean bean WHERE COALESCE(bean.name, 'Doe') BETWEEN COALESCE(bean.name, 'Doe') AND COALESCE(bean.name, 'Doe')"),
				Arguments.of("SELECT bean FROM TestBean bean WHERE COALESCE(bean.name, 'Doe') BETWEEN COALESCE(bean.name, 'Doe') AND bean.name"),
				Arguments.of("SELECT bean FROM TestBean bean WHERE COALESCE(bean.name, 'Doe') IN ('Name')"),
				Arguments.of("SELECT bean FROM TestBean bean WHERE COALESCE(bean.name, 'Doe') NOT BETWEEN bean.name AND bean.name"),
				Arguments.of("SELECT bean FROM TestBean bean WHERE COALESCE(bean.name, 'Doe') NOT BETWEEN bean.name AND COALESCE(bean.name, 'Doe')"),
				Arguments.of("SELECT bean FROM TestBean bean WHERE COALESCE(bean.name, 'Doe') NOT BETWEEN COALESCE(bean.name, 'Doe') AND bean.name"),
				Arguments.of("SELECT bean FROM TestBean bean WHERE COALESCE(bean.name, 'Doe') NOT BETWEEN COALESCE(bean.name, 'Doe') AND COALESCE(bean.name, 'Doe')"),
				Arguments.of("SELECT bean FROM TestBean bean WHERE COALESCE(bean.name, 'Doe') NOT BETWEEN COALESCE(bean.name, 'Doe') AND COALESCE(bean.name, 'Name')"),
				Arguments.of("SELECT bean FROM TestBean bean WHERE CONCAT(bean.name, 'est') = 'Nameest'"),
				Arguments.of("SELECT bean FROM TestBean bean WHERE EXISTS (SELECT bean FROM TestBean bean WHERE bean.id = :id)"),
				Arguments.of("SELECT bean FROM TestBean bean WHERE LENGTH(bean.name) = 0"),
				Arguments.of("SELECT bean FROM TestBean bean WHERE LOCATE(bean.name, 'est') = 2"),
				Arguments.of("SELECT bean FROM TestBean bean WHERE LOCATE(bean.name, 'est', 2) = -1"),
				Arguments.of("SELECT bean FROM TestBean bean WHERE LOWER(bean.name) = 'name'"),
				Arguments.of("SELECT bean FROM TestBean bean WHERE LOWER(bean.name) = 'test'"),
				Arguments.of("SELECT bean FROM TestBean bean WHERE NOT (bean.id = SQRT(2))"),
				Arguments.of("SELECT bean FROM TestBean bean WHERE NOT EXISTS (SELECT bean FROM TestBean bean WHERE bean.id = :id)"),
				Arguments.of("SELECT bean FROM TestBean bean WHERE SIZE(bean.collectionProperty) = 0"),
				Arguments.of("SELECT bean FROM TestBean bean WHERE SUBSTRING(bean.name, 2, 3) = 'est'"),
				Arguments.of("SELECT bean FROM TestBean bean WHERE TRIM(bean.name) = TRIM(BOTH FROM bean.name)"),
				Arguments.of("SELECT bean FROM TestBean bean WHERE TRIM(LEADING ' ' FROM bean.name) = TRIM(TRAILING FROM bean.name)"),
				Arguments.of("SELECT bean FROM TestBean bean WHERE TYPE(bean) = TestBeanSubclass"),
				Arguments.of("SELECT bean FROM TestBean bean WHERE UPPER(bean.name) = 'NAME'"),
				Arguments.of("SELECT bean FROM TestBean bean WHERE UPPER(bean.name) IN ('NAME')"),
				Arguments.of("SELECT bean, 'name', CASE bean.name WHEN 'name 1' THEN bean.name ELSE 'name 2' END FROM TestBean bean WHERE bean.name NOT IN ('name 1', 'name 2')"),
				Arguments.of("SELECT bean, 'name', CASE WHEN bean.name = 'name 1' THEN bean.name ELSE 'name 2' END FROM TestBean bean WHERE bean.name NOT IN ('name 1', 'name 2')"),
				Arguments.of("SELECT bean, COUNT(DISTINCT bean) AS beanCount FROM TestBean bean WHERE bean.name = 'name 1'"),
				Arguments.of("SELECT bean.id FROM TestBean bean WHERE ABS(bean.id) = 1 GROUP BY bean.id HAVING COUNT(bean.id) > 0"),
				Arguments.of("SELECT bean.id FROM TestBean bean WHERE ABS(bean.id) = 1 GROUP BY bean.id HAVING COUNT(bean.id) > 0 ORDER BY bean.name ASC"),
				Arguments.of("SELECT bean.id FROM TestBean bean WHERE ABS(bean.id) = 1 HAVING bean.id > 0"),
				Arguments.of("SELECT bean.id, bean.name, COUNT(bean.collectionProperty.id) FROM TestBean bean GROUP BY bean.id, bean.name"),
				Arguments.of("SELECT bean.id, bean.name, COUNT(DISTINCT bean.collectionProperty.id) FROM TestBean bean GROUP BY bean.id, bean.name ORDER BY bean.name DESC"),
				Arguments.of("SELECT bean.name FROM TestBean bean WHERE ((3 + 2) * 2) = 10.0"),
				Arguments.of("SELECT bean.name FROM TestBean bean WHERE ((3 + 2) * 2) >= 10.0"),
				Arguments.of("SELECT bean.name FROM TestBean bean WHERE ((3 + 2) / 2) = 10.0"),
				Arguments.of("SELECT bean.name FROM TestBean bean WHERE ((3 - 2) * 2) <= 10.0"),
				Arguments.of("SELECT bean.name FROM TestBean bean WHERE (bean.name <> 'testBean')"),
				Arguments.of("SELECT bean.name FROM TestBean bean WHERE bean.booleanValue = true"),
				Arguments.of("SELECT bean1 FROM TestBean bean1, TestBean bean2 WHERE bean1.id < bean2.id"),
				Arguments.of("SELECT bean1.name FROM TestBean bean1, TestBean bean2 WHERE (bean1.id - (bean2.id - 1)) = 5"),
				Arguments.of("SELECT c FROM Customer c INNER JOIN c.orders o WHERE c.status = 1"),
				Arguments.of("SELECT c FROM Customer c JOIN c.orders o WHERE c.status = 1"),
				Arguments.of("SELECT c FROM Customer c WHERE (SELECT AVG(o.price) FROM c.orders o) > 100"),
				Arguments.of("SELECT c FROM Customer c WHERE c.status = :stat"),
				Arguments.of("SELECT c FROM Customer c WHERE FUNCTION('hasGoodCredit', c.balance, c.creditLimit)"),
				Arguments.of("SELECT c FROM Customer c, Employee e WHERE c.hatsize = e.shoesize"),
				Arguments.of("SELECT c, COUNT(l) AS itemCount FROM Customer c JOIN c.Orders o JOIN o.lineItems l WHERE c.address.state = 'CA' GROUP BY c ORDER BY itemCount"),
				Arguments.of("SELECT c, COUNT(o) FROM Customer c JOIN c.orders o GROUP BY c HAVING COUNT(o) >= 5"),
				Arguments.of("SELECT c.country, COUNT(c) FROM Customer c GROUP BY c.country HAVING COUNT(c) > 30"),
				Arguments.of("SELECT c.id, c.status FROM Customer c JOIN c.orders o WHERE o.count > 100"),
				Arguments.of("SELECT c.status, AVG(c.filledOrderCount), COUNT(c) FROM Customer c GROUP BY c.status HAVING c.status IN (1, 2)"),
				Arguments.of("SELECT CASE l.id WHEN (SELECT COUNT(r.id) FROM Root r) THEN 1 ELSE 0 END FROM Leaf l"),
				Arguments.of("SELECT COALESCE(parent.name, KEY(related).name, VALUE(related).name, bean.name) FROM TestBean bean LEFT OUTER JOIN bean.parent parent LEFT OUTER JOIN bean.related related"),
				Arguments.of("SELECT COUNT(bean) FROM TestBean bean WHERE bean.id = :id"),
				Arguments.of("SELECT COUNT(DISTINCT bean.id) FROM TestBean bean WHERE bean.id = :id"),
				Arguments.of("SELECT d FROM Department d LEFT JOIN FETCH d.employees WHERE d.deptno = 1"),
				Arguments.of("SELECT DISTINCT bean FROM TestBean bean LEFT OUTER JOIN FETCH bean.beanProperty"),
				Arguments.of("SELECT DISTINCT bean FROM TestBean bean, TestBean bean2 INNER JOIN FETCH bean.collectionProperty"),
				Arguments.of("SELECT DISTINCT bean, bean.id FROM TestBean bean WHERE :id = bean.id"),
				Arguments.of("SELECT DISTINCT e FROM Employee e"),
				Arguments.of("SELECT DISTINCT emp FROM Employee emp WHERE EXISTS (SELECT spouseEmp FROM Employee spouseEmp WHERE spouseEmp = emp.spouse)"),
				Arguments.of("SELECT DISTINCT l.product FROM Order AS o JOIN o.lineItems l"),
				Arguments.of("SELECT DISTINCT o FROM Order o JOIN o.lineItems l WHERE l.product.name = ?1"),
				Arguments.of("SELECT DISTINCT o FROM Order o JOIN o.lineItems l WHERE l.product.productType = 'office_supplies'"),
				Arguments.of("SELECT DISTINCT o FROM Order o, IN(o.lineItems) l WHERE l.product.productType = 'office_supplies'"),
				Arguments.of("SELECT DISTINCT o1 FROM Order o1, Order o2 WHERE o1.quantity > o2.quantity AND o2.customer.lastname = 'Smith' AND o2.customer.firstname = 'John'"),
				Arguments.of("SELECT e FROM Employee e"),
				Arguments.of("SELECT e FROM Employee e JOIN e.projects p WHERE TREAT(p AS LargeProject).budget > 1000 OR TREAT(p AS SmallProject).name LIKE 'Persist%' OR p.description LIKE 'cost overrun'"),
				Arguments.of("SELECT e FROM Employee e JOIN TREAT(e.projects AS LargeProject) lp WHERE lp.budget > 1000"),
				Arguments.of("SELECT e FROM Employee e WHERE 1 <> 1"),
				Arguments.of("SELECT e FROM Employee e WHERE TREAT(e AS Exempt).vacationDays > 10 OR TREAT(e AS Contractor).hours > 100"),
				Arguments.of("SELECT e FROM Employee e WHERE TYPE(e) = :empTypes"),
				Arguments.of("SELECT e FROM Employee e WHERE TYPE(e) IN (:empType1, :empType2)"),
				Arguments.of("SELECT e FROM Employee e WHERE TYPE(e) IN (:empTypes)"),
				Arguments.of("SELECT e FROM Employee e WHERE TYPE(e) IN (Exempt, Contractor)"),
				Arguments.of("SELECT e.name, CASE TYPE(e) WHEN Exempt THEN 'Exempt' WHEN Contractor THEN 'Contractor' WHEN Intern THEN 'Intern' ELSE 'NonExempt' END FROM Employee e WHERE e.dept.name = 'Engineering'"),
				Arguments.of("SELECT e.name, f.name, CONCAT(CASE WHEN f.annualMiles > 50000 THEN 'Platinum' WHEN f.annualMiles > 25000 THEN 'Gold' ELSE '' END, 'Frequent Flyer') FROM Employee e JOIN e.frequentFlierPlan f"),
				Arguments.of("SELECT emp FROM Employee emp WHERE emp.salary > ALL (SELECT m.salary FROM Manager m WHERE m.department = emp.department)"),
				Arguments.of("SELECT goodCustomer FROM Customer goodCustomer WHERE goodCustomer.balanceOwed < (SELECT AVG(c.balanceOwed) / 2.0 FROM Customer c)"),
				Arguments.of("SELECT i.name, VALUE(p) FROM Item i JOIN i.photos p WHERE KEY(p) LIKE '%egret'"),
				Arguments.of("SELECT NEW com.acme.example.CustomerDetails(c.id, c.status, o.count) FROM Customer c JOIN c.orders o WHERE o.count > 100"),
				Arguments.of("SELECT NEW org.jpasecurity.TestBean(bean.id, bean.name) FROM TestBean bean"),
				Arguments.of("SELECT NULLIF(bean.name, 'Test') FROM TestBean bean"),
				Arguments.of("SELECT o FROM EntityWith$InnerClass o LEFT JOIN FETCH o.orderPosition pos LEFT JOIN FETCH pos.product LEFT JOIN FETCH o.shippingAddress"),
				Arguments.of("SELECT o FROM Order AS o JOIN o.lineItems l JOIN l.product p"),
				Arguments.of("SELECT o FROM Order o WHERE o.lineItems IS EMPTY"),
				Arguments.of("SELECT OBJECT(c) FROM Customer c, IN(c.orders) o WHERE c.status = 1"),
				Arguments.of("SELECT p FROM Person p WHERE 'Joe' MEMBER OF p.nicknames"),
				Arguments.of("SELECT p.vendor FROM Employee e JOIN e.contactInfo c JOIN c.phones p WHERE c.address.zipcode = '95054'"),
				Arguments.of("SELECT s.name, COUNT(p) FROM Suppliers s LEFT JOIN s.products p GROUP BY s.name"),
				Arguments.of("SELECT s.name, COUNT(p) FROM Suppliers s LEFT JOIN s.products p ON p.status = 'inStock' GROUP BY s.name"),
				Arguments.of("SELECT SUM(bean) FROM TestBean bean WHERE bean.id = :id"),
				Arguments.of("SELECT SUM(l.price) FROM Order o JOIN o.lineItems l JOIN o.customer c WHERE c.lastname = 'Smith' AND c.firstname = 'John'"),
				Arguments.of("SELECT t FROM CreditCard c JOIN c.transactionHistory t WHERE c.holder.name = 'John Doe' AND INDEX(t) BETWEEN 0 AND 9"),
				Arguments.of("SELECT TYPE(e) FROM Employee e WHERE TYPE(e) <> Exempt"),
				Arguments.of("SELECT v.location.street, KEY(i).title, VALUE(i) FROM VideoStore v JOIN v.videoInventory i WHERE v.location.zipcode = '94301' AND VALUE(i) > 0"),
				Arguments.of("SELECT w.name FROM Course c JOIN c.studentWaitlist w WHERE c.name = 'Calculus' AND INDEX(w) = 0"),
				Arguments.of("UPDATE Customer c SET c.status = 'outstanding' WHERE c.balance < 10000"),
				Arguments.of("UPDATE Employee e SET e.address.building = 22 WHERE e.address.building = 14 AND e.address.city = 'Santa Clara' AND e.project = 'Java EE'"),
				Arguments.of("UPDATE Employee e SET e.salary = CASE e.rating WHEN 1 THEN e.salary * 1.1 WHEN 2 THEN e.salary * 1.05 ELSE e.salary * 1.01 END"),
				Arguments.of("UPDATE Employee e SET e.salary = CASE WHEN e.rating = 1 THEN e.salary * 1.1 WHEN e.rating = 2 THEN e.salary * 1.05 ELSE e.salary * 1.01 END"),
				Arguments.of("UPDATE TestBean bean SET bean.name = 'test', bean.id = 0"),
				Arguments.of("UPDATE TestBean bean SET bean.name = 'test', bean.id = 1 WHERE bean.id = 0")
		);
	}
}
