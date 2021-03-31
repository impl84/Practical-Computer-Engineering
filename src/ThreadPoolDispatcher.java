
import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;

/**
 * スレッドプールを利用するディスパッチャ用のクラス
 */
class ThreadPoolDispatcher
	implements	Dispatcher
{
	// クラス変数（定数）：
	static final int NUM_THREADS = 8;	// スレッドプール内のスレッド数
	
	/**
	 * スレッドプールによるディスパッチ処理を開始する．
	 */
	public void startDispatching(
		ServerSocket servSock, ProtocolFactory factory, Logger logger
	) {
		// (NUM_THREADS - 1) 個のスレッドを生成・開始する．
		for (int i = 0; i < (NUM_THREADS - 1); i++) {
			// dispatchLoop() メソッドを呼び出すためのスレッドを生成する．
			// ※Thread のコンストラクタにはラムダ式を利用している．
			//   このラムダ式は，次の Runnable インスタンスと同じ：
			//		new Runnable() {
			//			public void run() {
			//				dispatchLoop(servSock, factory, logger);
			//			}
			//		}
			Thread thread = new Thread(
				() -> dispatchLoop(servSock, factory, logger)
			);
			// dispatchLoop() メソッドを呼び出すためのスレッドを開始する．
			thread.start();
		}
		// main スレッド(この処理を実行しているスレッド)を
		// NUM_THREADS 番目のスレッドとして，
		// dispatchLoop() メソッドを呼び出す．
		dispatchLoop(servSock, factory, logger);
	}
	
	/**
	 * クライアントからの要求を繰り返し処理する．
	 */
	private void dispatchLoop(
		ServerSocket servSock, ProtocolFactory factory, Logger logger
	) {
		// このメソッドを呼び出しているスレッド名を取得する．
		String threadName = Thread.currentThread().getName();
		
		// このメソッドを呼び出しているスレッド名をログに出力する．
		logger.printf("処理開始（スレッド名：%s）\n", threadName);

		// クライアントからの要求を繰り返し処理する．
		while (true) {
			try {
				// クライアントとのコネクションの確立を待つ．
				Socket clntSock = servSock.accept();
				
				// プロトコル処理用のインスタンスを生成する．
				Runnable protocol = factory.createProtocol(
					clntSock, logger
				);
				// プロトコルの処理をこのスレッドで実行する．
				protocol.run();
			}
			catch (IOException ex) {
				logger.printf("例外発生（%s）：%s\n",
					threadName, ex.getMessage()
				);
			}
		}
	}
}
