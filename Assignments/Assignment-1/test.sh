tar -czf 2019CS10399.tar.gz Predictor2400.java Predictor6400.java Predictor9999.java Predictor32000.java
python3 test.py 2019CS10399.tar.gz $1
python3 test.py clean
rm 2019CS10399.tar.gz
