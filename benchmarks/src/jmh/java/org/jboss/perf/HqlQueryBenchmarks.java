package org.jboss.perf;

import org.jboss.perf.annotations.BenchmarkQuery;
import org.jboss.perf.annotations.BenchmarkQueryMethod;

// --- //

@BenchmarkQuery(name = "simpleSelect", query = "select a from Animal a")
@BenchmarkQuery(name = "simpleSelect_multiple", query = "select a.serialNumber, a.mother, a.father, a.description, a.zoo from Animal a")
@BenchmarkQuery(name = "simpleWhere", query = "select a from Animal a where a.serialNumber = '1337'")
@BenchmarkQuery(name = "simpleWhere_multiple", query = "select a from Animal a where a.serialNumber = '1337' and a.zoo.address.city = 'London' and a.zoo.address.country = 'US'")
public class HqlQueryBenchmarks {

	// --- benchmarks are auto generated based on this methods and queries defined above

	//	@BenchmarkQueryMethod("Parser")
	public Object getHQLParser(String query, BaseBenchmarkState benchmarkState) {
		return benchmarkState.getHqlParser().getHQLParser( query );
	}

	@BenchmarkQueryMethod("SemanticModel")
	public Object getSemanticModel(String query, BaseBenchmarkState benchmarkState) {
		return benchmarkState.getHQLInterpreter().getSemanticModel( query );
	}
}
