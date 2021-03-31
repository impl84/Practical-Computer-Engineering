
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.Socket;
import java.nio.ByteBuffer;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;

/**
 * ソケット数と同数のスレッドを利用して連続データを送信する機能の実装
 */
class MultiThreadUploader
{
	// インスタンス変数：
	private String servAddr = null;	// サーバのIPアドレス(またはホスト名)
	private int servPort = 0;		// サーバのポート番号
	private int totalSize = 0;		// 送信する総データサイズ
	private int bufferSize = 0;		// 送信バッファサイズ
	private int numSockets = 0;		// ソケット数
	
	/**
	 * MultiThreadUploader のインスタンスを生成する．
	 */
	MultiThreadUploader(String servAddr, int servPort, int totalSize, int bufferSize, int numSockets)
	{
		// 下記のインスタンス変数を，引数で初期化する．
		this.servAddr = servAddr;
		this.servPort = servPort;
		this.totalSize = totalSize;
		this.bufferSize = bufferSize;
		this.numSockets = numSockets;
	}
	
	/**
	 * 送信データサイズ分の送信処理を実行する．
	 */
	void upload()
		throws	InterruptedException,
				ExecutionException
	{
		// ソケット毎の送信データサイズを求める．
		// 総送信データサイズをソケット数で割った余りがある場合を想定し，
		// その余りを含む送信データサイズも求めておく．
		int sizeWithoutRemainder = this.totalSize / this.numSockets;
		int sizeWithRemainder = sizeWithoutRemainder + this.totalSize % this.numSockets;
		
		// ソケット数と同数のスレッドを利用するためのスレッドプールを生成する．
		int numThreads = this.numSockets;
		ExecutorService threadPool = Executors.newFixedThreadPool(numThreads);
		
		// 送信処理メソッドの完了を確認するための Future インスタンスのリストを生成する．
		List<Future<Integer>> futureList = new ArrayList<Future<Integer>>();
		
		// ソケット数と同数のスレッドへ送信処理メソッドを渡すためのループ
		for (int i = 0; i < this.numSockets; i++) {
			// スレッドプール内のスレッドで処理する送信データサイズを決める．
			// 最後のスレッドのみ，上記の余りを含むサイズの送信データを処理する．
			int sizePerSocket;
			if (i == this.numSockets - 1) {
				sizePerSocket = sizeWithRemainder;
			}
			else {
				sizePerSocket = sizeWithoutRemainder;
			}
			// スレッドプール内のスレッドへ送信処理メソッドを渡す．
			Future<Integer> future = threadPool.submit(
				() -> uploadPerSocket(sizePerSocket),
				Integer.valueOf(sizePerSocket)
			);
			// スレッドプール内のスレッドで実効される送信処理メソッドの
			// 完了を確認するための Future インスタンスをリストへ追加する．
			futureList.add(future);
		}
		try {
			// Future インスタンスのリストを走査し，
			// 該当するスレッドにおける送信処理の完了を確認する．
			for(Future<Integer> future : futureList) {
				// 送信処理メソッド内で RuntimeException が発生していた場合，
				// 下記 get メソッドは ExecutionException を投げる．
				Integer size = future.get();
					// InterruptedException, ExecutionException
				
				// 送信処理メソッドが結果として返す，
				// ソケット毎の送信データサイズの値を確認する．
				if ((size != sizeWithoutRemainder) && (size != sizeWithRemainder)) {
					throw new Error("送信処理メソッドが予期せぬ結果を返しました．");
				}
			}
		}
		finally {
			// スレッドプールを終了させる．
			shutdownAndAwaitTermination(threadPool);
		}
	}
	
	/**
	 * ソケット毎の送信処理を実行する．
	 * このメソッドはスレッドプール内のスレッドから呼ばれる．
	 */
	private void uploadPerSocket(int sizePerSocket)
		throws RuntimeException
	{
		// 与えられているバッファサイズ +4バイト分の送信バッファを生成し，乱数で初期化する．
		int allocSize = this.bufferSize + 4;
		byte[] sendBuffer = new byte[allocSize];
		Random rand = new Random(System.currentTimeMillis());
		rand.nextBytes(sendBuffer);
		
		// ソケット毎の送信データサイズを送信バッファの先頭 4バイトへ格納する．
		byte[] bytes = ByteBuffer.allocate(4).putInt(sizePerSocket).array();
		System.arraycopy(bytes, 0, sendBuffer, 0, 4);
		
		Socket socket = null;
		try {
			// ソケットを生成し，入出力ストリームを取得する．
			socket = new Socket(this.servAddr, this.servPort);
			InputStream in = socket.getInputStream();
			OutputStream out = socket.getOutputStream();
			
			boolean isFirstData = true;		// 最初に送信するデータか否かを示すフラグ
			int remainder = sizePerSocket;	// 送信すべきデータの残量
			
			// ソケット毎の送信データを，バッファサイズ分のデータ毎にサーバへ送信する．
			for (;;) {
				// 送信データ長を求める．
				int sendLength
					= remainder > this.bufferSize ? this.bufferSize : remainder;  
				
				// サーバへデータを送信する．
				if (isFirstData) {
					// 最初に送信するデータは，
					// ソケット毎の送信データサイズが先頭 4バイトに格納されているデータ．
					out.write(sendBuffer, 0, sendLength);
					isFirstData = false;
				}
				else {
					// 最初に送信するデータ以外は，全て乱数が格納されているデータ．
					out.write(sendBuffer, 4, sendLength);
				}
				out.flush();
				
				// 送信すべきデータの残量が 0 であればループを抜ける．
				remainder -= sendLength;
				if (remainder <= 0) {
					break;
				}
			}
			// サーバ側がコネクションを切断するまで待つ．
			byte[] recvBuffer = new byte[4];
			while (in.read(recvBuffer) != -1) {
				;
			}
		}
		catch (IOException ex) {
			// 例外が発生した場合，その例外を含む RuntimeException を投げる．
			throw new RuntimeException(ex);
		}
		finally {
			// ソケットを閉じる．
			try {
				if (socket != null) {
					socket.close();
				}
			}
			catch (IOException ex) {
				throw new RuntimeException(ex);
			}
		}
	}
	
	/**
	 * ExecutorService を2段階でシャットダウンする．
	 */
	private void shutdownAndAwaitTermination(ExecutorService threadPool)
	{
		// 最初に shutdown メソッドを呼び出して着信タスクを拒否する．
		threadPool.shutdown();
		try {
			// 実行中のタスクの終了を待つ．
			if (!threadPool.awaitTermination(60, TimeUnit.SECONDS)) {
				// InterruptedException
				
				// 実行中のタスクの終了前に待ち時間が切れたので，
				// 実行中のタスクすべての停止を試み，待機中のタスクの処理を停止する．
				// その上で，再度，実行中のタスクの終了を待つ．
				threadPool.shutdownNow();
				if (!threadPool.awaitTermination(60, TimeUnit.SECONDS)) {
					// InterruptedException
					
					// 実行中のタスクの終了前に待ち時間が切れた．
					// RuntimeException を投げて，シャットダウン処理を終了する．
					throw new RuntimeException(
						"ExecutorService を正常に終了できませんでした．"
					);
				}
			}
		}
		catch (InterruptedException ex) {
			// InterruptedException が発生した．
			// 実行中のタスクすべての停止を試み，待機中のタスクの処理を停止する．
			threadPool.shutdownNow();
			
			// 現在のスレッドの割り込みステータスを保持する．
			Thread.currentThread().interrupt();
		}
	}
}
