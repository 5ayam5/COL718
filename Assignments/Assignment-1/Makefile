jar/BranchPredictor.jar: *.java
	ant > /dev/null
	ant make-jar > /dev/null

SIZES := 2400 6400 9999 32000

$(SIZES): jar/BranchPredictor.jar
	@java -jar jar/BranchPredictor.jar traces/trace1 $@
	@java -jar jar/BranchPredictor.jar traces/trace2 $@
	@java -jar jar/BranchPredictor.jar traces/trace3 $@
	@java -jar jar/BranchPredictor.jar traces/trace4 $@
	@java -jar jar/BranchPredictor.jar traces/trace5 $@

test:
	@./test.sh $(size)

clean:
	rm -rf jar bin *.pyc *.tar.gz __pycache__
