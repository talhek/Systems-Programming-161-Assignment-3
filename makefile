CFLAGS:=-c -Wall -Weffc++ -g -std=c++11
LDFLAGS:=-lboost_system -lboost_locale -lpthread

all: client
	g++ -o bin/client bin/connectionHandler.o bin/client.o $(LDFLAGS) 

client: bin/connectionHandler.o bin/client.o
	
bin/connectionHandler.o: src/connectionHandler.cpp
	g++ $(CFLAGS) -o bin/connectionHandler.o src/connectionHandler.cpp

bin/client.o: src/client.cpp
	g++ $(CFLAGS) -o bin/client.o src/client.cpp
	
	
.PHONY: clean
clean:
	rm -f bin/*