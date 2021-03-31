
import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;

/**
 * コネクション毎に新しいスレッド利用するディスパッチャ用のクラス
 */
class ThreadPerConnectionDispatcher
	implements	Dispatcher
{
	/**
	 * コネクション毎に新しいスレッドを割り当てる方法を用いて
	 * ディスパッチ処理を開始する．
	 */
	public void startDispatching(
		ServerSocket servSock, ProtocolFactory factory, Logger logger
	) {
		// クライアントとのコネクション毎にスレッドを生成し，
		// プロトコルの処理を開始する．
		while (true) {
			try {
				// クライアントとのコネクションの確立を待つ．
				Socket clntSock = servSock.accept();
				
				// プロトコル処理用のインスタンスを生成する．
				Runnable protocol = factory.createProtocol(
					clntSock, logger
				);
				// 生成したインスタンスの run() メソッドを呼び出す
				// スレッドを生成し，プロトコルの処理を開始する．
				Thread thread = new Thread(protocol);
				thread.start();
				
				// 処理を開始したスレッド名をログに出力する．
				logger.printf("処理開始（スレッド名：%s）\n",
					thread.getName()
				);
			}
			catch (IOException ex) {
				logger.printf("例外発生：%s\n", ex.getMessage());
			}
		}
	}
}
