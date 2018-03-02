package Server.protocol;

public interface ServerProtocolFactory<T> {
   AsyncServerProtocol<T> create();
}
