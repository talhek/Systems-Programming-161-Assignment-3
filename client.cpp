#include <stdlib.h>
#include <boost/locale.hpp>
#include "../include/connectionHandler.h"
#include <thread>

using namespace std;
bool b = true;
void sendMsg(ConnectionHandler* ch){
	while(b){

		const short bufsize = 1024;
		char buf[bufsize];
		cin.getline(buf, bufsize);
		string line(buf);

	  	if (!ch->sendLine(line)) {
			break;
		}
		
	}
}

void getMsg(ConnectionHandler* ch){
	while(b){
		string answer;
		if (!ch->getLine(answer)) {
			cout << "Disconnected. Exiting...\n" << endl;
			break;
		}
	
		int len=answer.length();
		answer.resize(len-1);
		cout << answer <<endl;
			if (answer == "SYSMSG QUIT ACCEPTED") {
			cout << "Exiting...\n" << endl;
			b=false;
			break;
		} 
		

	}
}
int main (int argc, char *argv[]) {
	if (argc < 3) {
	  cerr << "Usage: " << argv[0] << " host port" << endl << endl;
	  return -1;
    }

	string host = argv[1];
	short port = atoi(argv[2]);

	ConnectionHandler* ch = new ConnectionHandler(host, port);
	if (!ch->connect()) {
		cerr << "Cannot connect to " << host << ":" << port << endl;
		delete ch;
		return 1;
	}
	std::thread t1 (getMsg,ch);
	std::thread t2 (sendMsg,ch);
	t1.join();
	t2.join();
	delete ch;

	return 0;
}