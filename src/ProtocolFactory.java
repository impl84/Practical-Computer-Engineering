
import java.net.Socket;

/**
 * プロトコルファクトリ用のインターフェース
 */
public interface ProtocolFactory
{
    /**
     * プロトコル処理用のインスタンスを生成し， Runnable インターフェースとして返す．
     */
    public Runnable createProtocol(Socket clntSock, Logger logger);
}
