
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.Socket;
import java.util.ArrayList;

/**
 * EchoProtocol のインスタンスを生成するためのファクトリクラス
 */
public class EchoProtocolFactory
    implements
        ProtocolFactory
{
    /**
     * EchoProtocol のインスタンスを生成し， Runnable インターフェースとして返す．
     */
    @Override
    public Runnable createProtocol(Socket clntSock, Logger logger)
    {
        return new EchoProtocol(clntSock, logger);
    }
}

/**
 * エコープロトコルのサーバ側の機能を実装したクラス
 */
class EchoProtocol
    implements
        Runnable
{
    // クラス変数（定数）：
    static public final int BUFSIZE = 256;	// エコーデータ格納用バッファサイズ
    
    // インスタンス変数：
    private Socket clntSock = null;	// クライアントと通信するためのソケット
    private Logger logger   = null;	// ログ出力用の Logger インスタンス
    
    /**
     * EchoProtocol のインスタンスを生成する．
     */
    public EchoProtocol(Socket clntSock, Logger logger)
    {
        this.clntSock = clntSock;
        this.logger = logger;
    }
    
    /**
     * クライアントからデータを受信し，同じデータをクライアントへ送り返す．
     */
    @Override
    public void run()
    {
        // このスレッドにおける処理結果を保持するための
        // 文字列のリスト（ログリスト）を生成する．
        ArrayList<String> logList = new ArrayList<String>();
        
        // スレッド名，クライアントのアドレスとポート番号を
        // ログリストに追加する．
        logList.add("▽スレッド：" + Thread.currentThread().getName());
        logList.add(
            "・クライアント："
                + this.clntSock.getInetAddress().getHostAddress() + "，"
                + this.clntSock.getPort()
        );
        
        try {
            // ソケットから入出力ストリームを取得する．
            InputStream  in  = this.clntSock.getInputStream();
            OutputStream out = this.clntSock.getOutputStream();
            
            // エコーデータ格納用バッフを生成する．
            byte[] echoBuffer = new byte[BUFSIZE];
            
            // 受信したメッセージのサイズと，
            // クライアントへの総送信バイト数
            int recvMsgSize      = 0;
            int totalBytesEchoed = 0;
            
            // クライアントから受信したデータを
            // エコーデータ格納用バッフに格納し，
            // そのデータをそのままクライアントへ送り返す．
            // コネクションが切断されるまで，この処理を繰り返す．
            while ((recvMsgSize = in.read(echoBuffer)) != -1) {
                out.write(echoBuffer, 0, recvMsgSize);
                totalBytesEchoed += recvMsgSize;
            }
            // 総送信バイト数をログリストに追加する．
            logList.add("・総送信バイト数：" + totalBytesEchoed);
        }
        catch (IOException e) {
            logList.add("・例外発生：" + e.getMessage());
        }
        finally {
            try {
                // ソケットを閉じる．
                this.clntSock.close();
            }
            catch (IOException ex) {
                logList.add("・例外発生：" + ex.getMessage());
            }
        }
        // ログリストをまとめて出力する．
        logger.printlist(logList);
    }
}
