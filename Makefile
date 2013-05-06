all: compile lib

compile:
	groovyc -cp lib/gson-2.2.2.jar:lib/mysql-connector-java-5.1.23-bin.jar -d bin `find ./src -name *.groovy` `find ./src -name *.java` 

lib:
	cd bin; jar cf ../lib/freq-itemset-gen.jar `find . -name *.class`

start:
	java -cp lib/mysql-connector-java-5.1.23-bin.jar:lib/gson-2.2.2.jar:lib/freq-itemset-gen.jar:lib/groovy-all-2.0.5.jar:. me.therin.brovine.ItemsetRequester me.therin.mining.itemsets.fpgrowth.FPTree me.therin.brovine.TransfacBaskets > out.log
