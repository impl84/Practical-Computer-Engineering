
import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.Collection;

/**
 * ファイルへのログ出力用クラス
 */
public class FileLogger
    implements
        Logger
{
    // インスタンス変数：
    PrintWriter out = null;	// ファイルへのログ出力用 PrintWriter
    
    /**
     * 文字列をファイルへ出力する．
     */
    public FileLogger(String filename)
        throws IOException
    {
        boolean autoFlush = true;
        this.out = new PrintWriter(
            new FileWriter(filename), autoFlush
        );
    }
    
    /**
     * 文字列をファイルへ出力する．
     */
    @Override
    public synchronized void println(String line)
    {
        this.out.println(line);
    }
    
    /**
     * フォーマットを指定した文字列をファイルへ出力する．
     */
    @Override
    public synchronized void printf(String format, Object... args)
    {
        this.out.print(String.format(format, args));
    }
    
    /**
     * 行のリストをファイルへ出力する．
     */
    @Override
    public synchronized void printlist(Collection<String> entry)
    {
        for (String line : entry) {
            this.out.println(line);
        }
        this.out.println();
    }
    
    /**
     * ファイルへのログ出力を終了する．
     */
    public synchronized void close()
    {
        try {
            if (this.out != null) {
                this.out.close();
            }
        }
        finally {
            this.out = null;
        }
    }
}
