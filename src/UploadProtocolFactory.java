
import java.io.IOException;
import java.io.InputStream;
import java.net.Socket;
import java.nio.ByteBuffer;

/**
 * UploadProtocol のインスタンスを生成するためのファクトリ
 */
public class UploadProtocolFactory
    implements
        ProtocolFactory
{
    /**
     * UploadProtocol のインスタンスを生成し，Runnable インターフェースとして返す．
     */
    @Override
    public Runnable createProtocol(Socket clntSock, Logger logger)
    {
        return new UploadProtocol(clntSock, logger);
    }
}

/**
 * 連続データを受信するサーバ側の機能の実装
 */
class UploadProtocol
    implements
        Runnable
{
    // クラス変数（定数）：
    static private final int BUF_SIZE = 8192;	// 受信バッファサイズ
    
    // インスタンス変数：
    private Socket clntSock = null;	// クライアントと通信するためのソケット
    private Logger logger   = null;	// ログ出力用の Logger インスタンス
    
    /**
     * UploadProtocol のインスタンスを生成する．
     */
    public UploadProtocol(Socket clntSock, Logger logger)
    {
        this.clntSock = clntSock;
        this.logger = logger;
    }
    
    /**
     * クライアントが要求するサイズ分のデータを受信する．
     */
    @Override
    public void run()
    {
        try {
            // ソケットから入力ストリームを取得する
            InputStream in = clntSock.getInputStream();
            // IOException
            
            // このソケットから受信する最初の 4バイトには，
            // ソケット毎に受信すべきデータサイズが格納されているので，それを求める．
            byte[] fourBytes = new byte[4];
            int    bytesRcvd = in.read(fourBytes);
            // IOException
            if (bytesRcvd != 4) {
                throw new IOException("ソケット毎に受信すべきデータサイズの取得に失敗しました．");
            }
            int totalSizePerSocket = ByteBuffer.wrap(fourBytes).getInt();
            
            // クライアントから受信した全データサイズを格納する変数を用意する．
            // 既に受信した 4(バイト)を初期値として代入しておく．
            int totalBytesRcvd = 4;
            
            // 受信バッファを生成する．
            byte[] recvBuffer = new byte[BUF_SIZE];
            
            // このソケットを利用して受信すべきデータを全て受信するまで，
            // 受信処理を繰り返す．
            while (totalBytesRcvd < totalSizePerSocket) {
                // データを受信する．
                bytesRcvd = in.read(recvBuffer);
                // IOException
                
                // クライアントがソケットを閉じた場合はループを抜ける．
                if (bytesRcvd == -1) {
                    break;
                }
                // ソケット毎に受信すべきデータサイズに，
                // 今回受信したデータのバイト数を加える．
                totalBytesRcvd += bytesRcvd;
            }
        }
        catch (IOException ex) {
            this.logger.println("例外発生：" + ex.getMessage());
        }
        finally {
            try {
                // ソケットを閉じる．
                this.clntSock.close();
                // IOException
            }
            catch (IOException ex) {
                this.logger.println("例外発生：" + ex.getMessage());
            }
        }
    }
}
