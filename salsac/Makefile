compiler:
	jjtree Salsa0.3.jjt
	javacc Salsa0.3.jj
	javac *.java definitions/*.java
	echo "In salsac/SalsaParser.java, add the line \"nodeTable.current.addToken(token);\" before the return statement in jj_consume_token()"

clean:
	rm Token.java TokenMgrError.java ParseException.java JavaCharStream.java -f
	rm *.class definitions/*.class -f

	
