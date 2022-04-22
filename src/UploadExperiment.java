
import java.util.ArrayList;
import java.util.concurrent.ExecutionException;

/**
 * 送信バッファサイズとソケット数を変化させ， 連続データを送信するクライアント側の機能の実装
 */
public class UploadExperiment
{
    // クラス変数（定数）：
    private static final int KILO_BYTE = 1024;
    private static final int MEGA_BYTE = KILO_BYTE * 1024;
    private static final int GIGA_BYTE = MEGA_BYTE * 1024;
    
    // 送信する総データサイズと送信試行回数
    private static final int TOTAL_SIZE = 1 * MEGA_BYTE;
    private static final int TRY_COUNT  = 10;
    
    // 送信バッファサイズの配列
    private static final int[] BUF_SIZE_ARRAY = {
        8, 16, 32, 64, 128, 256
    };
    
    // 同時に利用するソケット数の配列
    private static final int[] NUM_SOCKS_ARRAY = {
        1, 2, 3, 4, 5, 6, 7, 8
    };
    
    /**
     * サーバへのデータ送信実験用クライアントを利用するための main メソッド
     */
    public static void main(String[] args)
    {
        // 引数の数を確認する．
        if (args.length != 2) {
            System.out.println("Parameters: <Server> <Port>");
            return;
        }
        try {
            // サーバ名(またはIPアドレス)とサーバのポート番号を引数から取得する．
            String servAddr = args[0];
            int    servPort = Integer.parseInt(args[1]);
            
            // コンソールへのログ出力用のインスタンスを生成する．
            ConsoleLogger clog = new ConsoleLogger();
            
            // ファイルへのログ出力用のインスタンスを生成する．
            FileLogger flog = new FileLogger(
                String.format("log_%d.txt", System.currentTimeMillis())
            );
            // IOException
            
            // データ送信実験用クライアントのインスタンスを生成する．
            UploadExperiment client = new UploadExperiment(
                servAddr, servPort, clog, flog
            );
            
            // サーバへのアップロードを繰り返し，結果を出力する．
            client.execute();
        }
        catch (Exception ex) {
            System.out.println("例外発生：" + ex.getMessage());
            ex.printStackTrace();
        }
    }
    
    // インスタンス変数：
    private final String        servAddr;   // サーバのIPアドレス(またはホスト名)
    private final int           servPort;   // サーバのポート番号
    private final ConsoleLogger clog;       // コンソールへの出力用 Logger
    private final FileLogger    flog;       // ファイルへの出力用 Logger
    
    /**
     * UploadExperiment のインスタンスを生成する．
     */
    UploadExperiment(
        String servAddr, int servPort, ConsoleLogger clog, FileLogger flog
    )
    {
        this.servAddr = servAddr;
        this.servPort = servPort;
        this.clog = clog;
        this.flog = flog;
    }
    
    /**
     * サーバへのアップロードを繰り返し，結果を出力する．
     */
    void execute()
    {
        // バッファサイズとソケット数を変化させ，
        // TOTAL_SIZE バイト分のデータを TRY_COUNT 回サーバへアップロードし，
        // バッファサイズとソケット数毎のアップロード時間(TRY_COUNT 回分の配列)を取得する．
        Object[][] timesArray = uploadAll();
        
        // バッファサイズとソケット数毎のスループット(TRY_COUNT 回分の配列)を格納する配列
        Object[][] throughputsArray = new Object[BUF_SIZE_ARRAY.length][NUM_SOCKS_ARRAY.length];
        
        // 平均スループットを格納する配列とスループットの標準偏差を格納する配列
        int[][] averages_mbps = new int[BUF_SIZE_ARRAY.length][NUM_SOCKS_ARRAY.length];
        int[][] stdDevs_mbps  = new int[BUF_SIZE_ARRAY.length][NUM_SOCKS_ARRAY.length];
        
        // バッファサイズの配列とソケット数の配列を走査する．
        for (int bs = 0; bs < BUF_SIZE_ARRAY.length; bs++) {
            for (int ns = 0; ns < NUM_SOCKS_ARRAY.length; ns++) {
                
                // 経過時間(ms)の配列を取得し，スループット(mbps)を求める．
                long[] elapsedTimes_ms  = (long[])timesArray[bs][ns];
                int[]  throughputs_mbps = calcThroughput(elapsedTimes_ms);
                throughputsArray[bs][ns] = throughputs_mbps;
                
                // 平均スループットとスループットの標準偏差を求める．
                averages_mbps[bs][ns] = calcAverage(throughputs_mbps);
                stdDevs_mbps[bs][ns] = calcStdDev(
                    averages_mbps[bs][ns], throughputs_mbps
                );
            }
        }
        // 下記(a)～(c)をファイルへ出力する．
        // (a) 平均スループット
        // (b) スループットの標準偏差
        // (c) バッファサイズとソケット数毎のスループット(TRY_COUNT 回分の値)
        writeResults(averages_mbps);
        writeResults(stdDevs_mbps);
        writeAllThroughputs(throughputsArray);
        
        // ファイルへのログ出力を終了する．
        this.flog.close();
    }
    
    /**
     * バッファサイズとソケット数を変化させ， TOTAL_SIZE バイト分のデータを TRY_COUNT 回サーバへアップロードする．
     */
    private Object[][] uploadAll()
    {
        // バッファサイズとソケット数毎の
        // アップロード時間の配列(TRY_COUNT回分)を格納するための配列を生成する．
        Object[][] timesArray = new Object[BUF_SIZE_ARRAY.length][NUM_SOCKS_ARRAY.length];
        
        // バッファサイズの配列とソケット数の配列を走査する．
        for (int bs = 0; bs < BUF_SIZE_ARRAY.length; bs++) {
            for (int ns = 0; ns < NUM_SOCKS_ARRAY.length; ns++) {
                
                // バッファサイズとソケット数の配列の要素である
                // バッファサイズとソケット数を取得する．
                int bufferSize = BUF_SIZE_ARRAY[bs];
                int numSockets = NUM_SOCKS_ARRAY[ns];
                
                // 取得したバッファサイズとソケット数で，
                // TOTAL_SIZE バイト分のデータを TRY_COUNT 回
                // サーバへアップロードし，
                // その各回の経過時間を取得する．
                long[] elapsedTimes_ms = upload(bufferSize, numSockets);
                
                // 経過時間の配列を保持しておく．
                timesArray[bs][ns] = elapsedTimes_ms;
            }
        }
        // 全計測結果を含む配列を返す．
        return timesArray;
    }
    
    /**
     * 与えられたバッファサイズとソケット数で， TOTAL_SIZE バイト分のデータを TRY_COUNT 回サーバへアップロードする．
     */
    private long[] upload(int bufferSize, int numSockets)
    {
        // 経過時間を記録するための配列を生成し，要素を -1 で初期化する．
        long[] elapsedTimes_ms = new long[TRY_COUNT];
        for (int i = 0; i < TRY_COUNT; i++) {
            elapsedTimes_ms[i] = -1;
        }
        // 与えられたバッファサイズとソケット数における処理の概説をコンソールへ出力する．
        this.clog.printf("%s [", getDescription(bufferSize, numSockets));
        
        // 経過時間の累計と，正常にアップロードできた回数
        long totalTime_ms = 0;
        int  count        = 0;
        
        // 例外発生時のメッセージを格納しておく文字列のリスト
        ArrayList<String> entry = new ArrayList<String>();
        
        // サーバへのデータアップロードを TRY_COUNT 回繰り返す．
        for (int i = 0; i < TRY_COUNT; i++) {
            try {
                // サーバへデータをアップロードするためのインスタンスを生成し，
                // アップロード開始時刻を取得した後，TOTAL_SIZE 分の送信処理を実行する．
                MultiThreadUploader uploader = new MultiThreadUploader(
                    this.servAddr, this.servPort, TOTAL_SIZE, bufferSize,
                    numSockets
                );
                // アップロード開始時刻を取得する．
                long startTime_ms = System.currentTimeMillis();
                
                // アップロード処理を実行する．
                uploader.upload();
                // InterruptedException, ExecutionException
                
                // アップロード終了時刻を取得し，経過時間と経過時間の累計を求める．
                long endTime_ms = System.currentTimeMillis();
                elapsedTimes_ms[i] = endTime_ms - startTime_ms;
                if (elapsedTimes_ms[i] == 0) {
                    // ミリ秒の精度では経過時間が 0ms となる場合がある．
                    // その場合は，最小値である 1ms 経過したものとする． 
                    elapsedTimes_ms[i] = 1;
                }
                totalTime_ms += elapsedTimes_ms[i];
                
                // 正常にアップロードできた回数をカウントし，
                // アップロード終了を示す "o" をコンソールへ出力する．
                count++;
                this.clog.printf("o");
            }
            catch (InterruptedException | ExecutionException ex) {
                // 例外が発生した．
                // アップロード処理が正常に終了していないことを示す "x" を
                // コンソールへ出力し，例外メッセージをリストへ追加しておく．
                this.clog.printf("x");
                entry.add("  " + ex.getMessage());
            }
        }
        // 正常にアップロードできた回数を確認し，1回でも正常にアップロードできていれば，
        // TOTAL_SIZE 分のアップロード時間の平均を求めてコンソールへ出力する．
        if (count > 0) {
            double average_sec = (totalTime_ms / count) / 1000.0;
            this.clog.printf("], average time: %6.3f sec.\n", average_sec);
        }
        else {
            this.clog.println("]");
        }
        // 例外が発生していた場合はそのメッセージをコンソールへ出力する．
        if (entry.size() > 0) {
            this.clog.printlist(entry);
        }
        // 経過時間の配列を返す．
        return elapsedTimes_ms;
    }
    
    /**
     * 与えられたバッファサイズとソケット数における処理の概説を返す．
     */
    private String getDescription(int bufferSize, int numSockets)
    {
        StringBuilder sb = new StringBuilder();
        sb.append(
            String.format(
                "%s (%,6d x %,7d + %-4d), %3d",
                toKiloByte(TOTAL_SIZE),
                bufferSize,
                TOTAL_SIZE / bufferSize,
                TOTAL_SIZE % bufferSize,
                numSockets
            )
        );
        if (numSockets <= 1) {
            sb.append(" socket ");
        }
        else {
            sb.append(" sockets");
        }
        return sb.toString();
    }
    
    /**
     * バイト単位の数値を，単位付きの文字列に変換する．
     */
    private String toKiloByte(int val_byte)
    {
        String result = null;
        
        if (val_byte >= GIGA_BYTE) {
            result = new String(val_byte / GIGA_BYTE + "GB");
        }
        else if (val_byte >= MEGA_BYTE) {
            result = new String(val_byte / MEGA_BYTE + "MB");
        }
        else if (val_byte >= KILO_BYTE) {
            result = new String(val_byte / KILO_BYTE + "KB");
        }
        else {
            result = new String(val_byte + "B");
        }
        return result;
    }
    
    /**
     * 経過時間からスループットを求める．
     */
    private int[] calcThroughput(long[] elapsedTimes_ms)
    {
        // スループットを格納するための配列を生成し，要素を -1 で初期化する．
        int[] throughputs_mbps = new int[TRY_COUNT];
        for (int i = 0; i < TRY_COUNT; i++) {
            throughputs_mbps[i] = -1;
        }
        // 経過時間からスループットを求める．
        for (int i = 0; i < TRY_COUNT; i++) {
            long elapsedTime_ms = elapsedTimes_ms[i];
            if (elapsedTime_ms == -1) {
                continue;
            }
            long throughput_bps = 1000 * (TOTAL_SIZE * 8 / elapsedTime_ms);
            throughputs_mbps[i] = (int)Math.round(throughput_bps / 1000000.0);
        }
        // 求めたスループットの配列を返す．
        return throughputs_mbps;
    }
    
    /**
     * 平均スループットを求める．
     */
    private int calcAverage(int[] throughputs_mbps)
    {
        // 平均スループット
        int average_mbps = 0;
        
        int sum   = 0;
        int count = 0;
        
        // スループットの平均を求める．
        for (int i = 0; i < TRY_COUNT; i++) {
            int throughput_mbps = throughputs_mbps[i];
            if (throughput_mbps == -1) {
                continue;
            }
            sum += throughput_mbps;
            count++;
        }
        // 1回以上，計測に成功していることを確認した上で，
        // 平均スループットを求める．
        if (count > 0) {
            average_mbps = sum / count;
        }
        // 平均スループットを返す．
        return average_mbps;
    }
    
    /**
     * スループットの標準偏差を求める．
     */
    private int calcStdDev(int average_mbps, int[] throughputs_mbps)
    {
        // スループットの標準偏差
        int stdDev_mbps = 0;
        
        int sum   = 0;
        int count = 0;
        
        // スループットの標準偏差を求める．
        for (int i = 0; i < TRY_COUNT; i++) {
            int throughput_mbps = throughputs_mbps[i];
            if (throughput_mbps == -1) {
                continue;
            }
            sum += Math.pow(throughput_mbps - average_mbps, 2);
            count++;
        }
        // 1回以上，計測に成功していることを確認した上で，
        // 標準偏差を求める．
        if (count > 0) {
            stdDev_mbps = (int)Math.sqrt(sum / count);
        }
        // スループットの標準偏差を返す．
        return stdDev_mbps;
    }
    
    /**
     * バッファサイズとソケット数毎の実験結果をファイルへ出力する．
     */
    private void writeResults(int[][] results)
    {
        for (int ns = 0; ns < NUM_SOCKS_ARRAY.length; ns++) {
            this.flog.printf("\t%d", NUM_SOCKS_ARRAY[ns]);
        }
        this.flog.printf("\n");
        
        for (int bs = 0; bs < BUF_SIZE_ARRAY.length; bs++) {
            this.flog.printf("%d", BUF_SIZE_ARRAY[bs]);
            
            for (int ns = 0; ns < NUM_SOCKS_ARRAY.length; ns++) {
                this.flog.printf("\t%d", results[bs][ns]);
            }
            this.flog.printf("\n");
        }
        this.flog.printf("\n");
    }
    
    /**
     * バッファサイズとソケット数毎のスループット(TRY_COUNT 回分の値)をファイルへ出力する．
     */
    private void writeAllThroughputs(Object[][] throughputsArray)
    {
        // バッファサイズの配列とソケット数の配列を走査する．
        for (int bs = 0; bs < BUF_SIZE_ARRAY.length; bs++) {
            for (int ns = 0; ns < NUM_SOCKS_ARRAY.length; ns++) {
                this.flog.printf("%d\t%d", BUF_SIZE_ARRAY[bs], NUM_SOCKS_ARRAY[ns]);
                
                int[] throughputs_mbps = (int[])throughputsArray[bs][ns];
                for (int i = 0; i < TRY_COUNT; i++) {
                    this.flog.printf("\t%d", throughputs_mbps[i]);
                }
                this.flog.printf("\n");
            }
        }
    }
}
