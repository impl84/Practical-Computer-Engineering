
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.Socket;

/**
 * エコークライアント
 */
public class SimpleEchoClient
{
    /**
     * エコークライアントを利用するための main メソッド
     */
    public static void main(String[] args)
    {
        // 引数の数を確認する．
        if ((args.length < 2) || (args.length > 3)) {
            System.out.println("Parameters: <Server> <Port> <Word>");
            return;
        }
        // サーバ名(またはIPアドレス)とサーバのポート番号，
        // エコー文字列を引数から取得する．
        String servAddr   = args[0];
        int    servPort   = Integer.parseInt(args[1]);
        String echoString = args[2];
        
        SimpleEchoClient client = null;
        try {
            // エコークライアントのインスタンスを生成する．
            client = new SimpleEchoClient(servAddr, servPort);
            
            // 文字列をサーバへ送信し，同じ文字列を受信する．
            client.processEchoString(echoString);
        }
        catch (IOException ex) {
            ex.printStackTrace();
        }
        finally {
            try {
                // エコークライアントを終了する．
                if (client != null) {
                    client.close();
                }
            }
            catch (IOException ex) {
                ex.printStackTrace();
            }
        }
    }
    
    // インスタンス変数：
    private Socket       echoSocket = null;
    private InputStream  in         = null;
    private OutputStream out        = null;
    
    /**
     * SimpleEchoClient のインスタンスを生成する．
     */
    SimpleEchoClient(String servAddr, int servPort)
        throws IOException
    {
        // サーバとのコネクションを確立する．
        this.echoSocket = new Socket(servAddr, servPort);
        System.out.println("サーバとのコネクションを確立しました．");
        
        // ソケットから入出力ストリームを取得する．
        this.in = this.echoSocket.getInputStream();
        this.out = this.echoSocket.getOutputStream();
    }
    
    /**
     * 与えられた文字列をサーバへ送信し， 送信したバイト数と同じバイト数のデータを受信する．
     */
    void processEchoString(String echoString)
        throws IOException
    {
        // 送信する文字列をバイトデータに変換し，
        // そのバイトデータをサーバへ送信する．
        byte[] byteBuffer = echoString.getBytes();
        this.out.write(byteBuffer);
        System.out.println("送信文字列：" + echoString);
        
        // この後にサーバから受信する総バイト数
        int totalBytesRcvd = 0;
        
        // サーバから受信した総バイト数が，
        // 送信したバイト長未満で有る限り受信を続ける．
        while (totalBytesRcvd < byteBuffer.length) {
            
            // サーバからバイトデータを受信する．
            int bytesRcvd = this.in.read(
                byteBuffer,		// バイトデータを格納するバッファ
                totalBytesRcvd,	// 格納する場所となるオフセット値
                byteBuffer.length - totalBytesRcvd	// 受信すべきバイト数
            );
            // InputStream.read() の戻り値を確認する．
            if (bytesRcvd >= 0) {
                // 戻り値が 0 以上の場合は，
                // 受信したバイト数を表しているので，
                // サーバから受信した総バイト数に加算する．
                totalBytesRcvd += bytesRcvd;
            }
            else if (bytesRcvd == -1) {
                // 戻り値が -1 の場合は，
                // ストリームの最後に到達していることを表す．
                // サーバがコネクションを切断した場合 -1 が戻り値となる．
                // ここでは，受信すべきバイトデータ全てを受信する前に，
                // コネクションが切断されたことを意味する．
                throw new IOException(
                    "サーバがコネクションを切断しました．"
                );
            }
            else {
                // 戻り値が上記以外の場合は定義されていない．
                // もし，戻り値が -1 未満の場合は Error として処理する．
                throw new Error(
                    "InputStream.read() が "
                        + bytesRcvd + " を戻り値として返しました．"
                );
            }
        }
        System.out.println("受信文字列：" + new String(byteBuffer));
    }
    
    /**
     * エコークライアントを終了する．
     */
    void close()
        throws IOException
    {
        try {
            if (this.echoSocket != null) {
                this.echoSocket.close();
            }
            if (this.in != null) {
                this.in.close();
            }
            if (this.out != null) {
                this.out.close();
            }
        }
        finally {
            this.echoSocket = null;
            this.in = null;
            this.out = null;
        }
    }
}
