
import java.io.IOException;
import java.lang.reflect.InvocationTargetException;
import java.net.ServerSocket;

/**
 * 各種プロトコル起動用のクラス
 */
public class ProtocolLauncher
{
    // クラス変数（定数）：
    static private final int BACKLOG = 8;   // TCPコネクション要求処理用のキューの長さ
    
    /**
     * 引数で指定されたプロトコルとディスパッチャにより， サーバ側のプロトコル処理を開始する．
     */
    public static void main(String[] args)
    {
        // 引数の数を確認する．
        if (args.length != 3) {
            System.err.println("Parameter(s): <Port> <Protocol> <Dispatcher>");
            return;
        }
        // 引数から下記(a)〜(c)を取得する．
        // (a) サーバのポート番号
        // (b) 利用するプロトコルファクトリクラス名の接頭辞
        // (c) 利用するディスパッチャクラス名の接頭辞
        int    servPort       = Integer.parseInt(args[0]);
        String factoryName    = args[1] + "ProtocolFactory";
        String dispatcherName = args[2] + "Dispatcher";
        
        try {
            // TCPのコネクション要求を処理するためのソケットを生成する．
            ServerSocket servSock = new ServerSocket(servPort, BACKLOG);
            // 例外：IOException
            
            // プロトコルファクトリのインスタンスを取得する．
            // 発生する可能性のある例外は以下の通り...(A)
            // forName():
            //      ClassNotFoundException
            // getDeclaredConstructor():
            //      NoSuchMethodException
            // newInstance():
            //      InstantiationException, IllegalAccessException, InvocationTargetException
            ProtocolFactory factory = (ProtocolFactory)Class
                .forName(factoryName)
                .getDeclaredConstructor()
                .newInstance();
            
            // ディスパッチャのインスタンスを取得する．
            // 発生する可能性のある例外は，上記(A)と同じ．
            Dispatcher dispatcher = (Dispatcher)Class
                .forName(dispatcherName)
                .getDeclaredConstructor()
                .newInstance();
            
            // コンソール出力用の Logger を生成する．
            Logger logger = new ConsoleLogger();
            
            // ディスパッチャ内でプロトコルファクトリを利用し，
            // サーバ側のプロトコル処理を開始する．
            dispatcher.startDispatching(servSock, factory, logger);
        }
        catch (IOException ex) {
            System.err.println("ServerSocket の生成に失敗しました：" + ex.getMessage());
        }
        catch (
            ClassNotFoundException
            | NoSuchMethodException
            | InstantiationException | IllegalAccessException | InvocationTargetException ex
        ) {
            System.err.println("インスタンスの生成に失敗しました：" + ex.getMessage());
        }
    }
}
