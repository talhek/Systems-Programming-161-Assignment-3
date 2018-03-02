package Server.tokenizer;

public interface TokenizerFactory<T> {
   MessageTokenizer<T> create();
}
