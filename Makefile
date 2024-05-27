make:
	javac -d bin/ client/*.java
	javac -d bin/ server/*.java
	javac -d bin/ utils/*.java

run_client:
	java -cp bin/ client/Client $(HOST) $(PORT)

run_server:
	java -cp bin/ server/Server $(PORT) $(NUM_PLAYERS)

HOST ?= localhost
PORT ?= 8080
NUM_PLAYERS ?= 3

clean:
	rm -rf bin
