jar/BranchPredictor.jar: *.java
	ant
	ant make-jar

SIZES := 2400 6400 9999 32000

$(SIZES): jar/BranchPredictor.jar
	java -jar jar/BranchPredictor.jar traces/trace1 $@
	java -jar jar/BranchPredictor.jar traces/trace2 $@
	java -jar jar/BranchPredictor.jar traces/trace3 $@
	java -jar jar/BranchPredictor.jar traces/trace4 $@
	java -jar jar/BranchPredictor.jar traces/trace5 $@

clean:
	rm -rf jar bin
