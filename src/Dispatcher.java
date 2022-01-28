
import java.net.ServerSocket;

/**
 * ディスパッチャ用のインターフェース
 */
public interface Dispatcher
{
    /**
     * ディスパッチ処理を開始する．
     */
    public void startDispatching(
        ServerSocket servSock, ProtocolFactory protoFactory, Logger logger
    );
}
