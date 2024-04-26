
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.io.PrintWriter;
import java.net.InetAddress;
import java.net.Socket;
import java.net.UnknownHostException;

/**
 * メールの送信実験
 */
public class MailSendingTest
{
    // SMTPサーバのポート番号
    private static final int SMTP_PORT = 25;
    
    // 送信するメールの Subject と本文
    private static final String SUBJECT = "電子メールの送信テスト";
    private static final String[] DATA_LINES = {
        "SMTP(Simple Mail Transfer Protocol)の仕様に沿い、",
        "SMTPサーバと双方向の通信を行うことで、",
        "電子メールを送信できます。"
    };
    
    /**
     * ExperimentalSmtpSocket を利用してメールを送信する．
     */
    public static void main(String args[])
    {
        // 引数の数を確認する．
        if ((args.length < 2) || (args.length > 3)) {
            System.out.println("Parameters: <SMTP Server> <From> <To>");
            return;
        }
        String smtpServer = args[0];    // SMTPサーバ名(またはIPアドレス)
        String from       = args[1];    // 送信元メールアドレス
        String to         = args[2];    // 宛先メールアドレス
        
        ExperimentalSmtpSocket smtpSocket = null;
        try {
            // ExperimentalSmtpSocket のインスタンスを生成する．
            smtpSocket = new ExperimentalSmtpSocket(smtpServer, SMTP_PORT);
            
            // メールを送信する．
            smtpSocket.sendMail(from, to, SUBJECT, DATA_LINES);
        }
        catch (UnknownHostException ex) {
            // ExperimentalSmtpSocket のインスタンス生成時に例外が発生した．
            ex.printStackTrace();
        }
        catch (IOException ex) {
            // メール送信時に例外が発生した．
            ex.printStackTrace();
        }
        finally {
            try {
                // 利用した ExperimentalSmtpSocket を終了する．
                if (smtpSocket != null) {
                    smtpSocket.close();
                }
            }
            catch (IOException ex) {
                // ExperimentalSmtpSocket 終了処理時に例外が発生した．
                ex.printStackTrace();
            }
        }
    }
}

/**
 * SMTPの実験用ソケット
 */
class ExperimentalSmtpSocket
{
    private final Socket socket;        // SMTPサーバとのTCPコネクションの端点となるソケット
    private final BufferedReader reader;// 行単位で受信を処理するための BufferedReader
    private final PrintWriter writer;   // 行単位で送信を処理するための PrintWriter
    
    /**
     * ExperimentalSmtpSocket のインスタンスを生成する．
     */
    ExperimentalSmtpSocket(String smtpServer, int smtpPort)
        throws UnknownHostException,
            IOException
    {
        // SMTPサーバとのTCPコネクションを確立し，
        // その端点となるソケットのインスタンスを生成する．
        this.socket = new Socket(
            InetAddress.getByName(smtpServer), smtpPort
        );
        // 行単位で受信を処理するための BufferedReader を生成する．
        this.reader = new BufferedReader(
            new InputStreamReader(this.socket.getInputStream())
        );
        // 行単位で送信を処理するための PrintWriter を生成する．
        this.writer = new PrintWriter(
            new OutputStreamWriter(this.socket.getOutputStream()),
            true    // auto flush 機能を有効にする．
        );
    }
    
    /**
     * SMTPの手順に沿ってメールを送信する．
     */
    void sendMail(String from, String to, String subject, String[] dataLines)
        throws IOException
    {
        // SMTPサーバとのTCPコネクション確立直後に、
        // サーバから送信されてくるメッセージを受信する．
        recvLine();
        
        // 通信開始コマンドを送信し，その応答を受信する．
        String localHostName = InetAddress.getLocalHost().getHostName();
        sendLine("HELO " + localHostName);
        recvLine();
        
        // 送信者のメールアドレスを送信し，その応答を受信する．
        sendLine("MAIL From:<" + from + ">");
        recvLine();
        
        // 受信者のメールアドレスを送信し，その応答を受信する．
        sendLine("RCPT TO:<" + to + ">");
        recvLine();
        
        // 電子メール送信開始コマンドを送信し，その応答を受信する．
        sendLine("DATA");
        recvLine();
        
        // 電子メールのヘッダ行を送信する．
        sendLine("To: " + to);
        sendLine("Subject: " + subject);
        
        // ヘッダ行と本文を分ける空行を送信する．
        sendLine("");
        
        // 電子メールの本文を送信する．
        for (String line : dataLines) {
            sendLine(line);
        }
        // 電子メール送信終了コマンド(".")を送信し，その応答を受信する．
        sendLine(".");
        recvLine();
        
        // 終了コマンドを送信し，その応答を受信する．
        sendLine("QUIT");
        recvLine();
    }
    
    /**
     * ExperimentalSmtpSocket を終了する．
     */
    void close()
        throws IOException
    {
        try {
            // 行単位で送受信を処理するために利用していた
            // PrintWriter と BufferedReader を終了する．
            this.writer.close();
            this.reader.close();
        }
        catch (IOException ex) {
            // BufferedReader の close メソッドで例外が発生した．
            ex.printStackTrace();
        }
        finally {
            // SMTPサーバとのTCPコネクションの
            // 端点として利用していたソケットを終了する．
            this.socket.close();
        }
    }
    
    // メールサーバへ 1行送信し、送信した 1行を標準出力へ出力する．
    private void sendLine(String line)
    {
        this.writer.println(line);
        System.out.println(line);
    }
    
    // メールサーバから 1行受信し，受信した 1行を標準出力へ出力する．
    private void recvLine()
        throws IOException
    {
        String line = this.reader.readLine();
        System.out.println(line);
    }
}
