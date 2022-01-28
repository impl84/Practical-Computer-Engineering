
import java.util.Collection;

/**
 * コンソールへのログ出力用クラス
 */
public class ConsoleLogger
    implements
        Logger
{
    /**
     * 文字列をコンソールへ出力する．
     */
    @Override
    public synchronized void println(String line)
    {
        System.out.println(line);
    }
    
    /**
     * フォーマットを指定した文字列をコンソールへ出力する．
     */
    @Override
    public synchronized void printf(String format, Object... args)
    {
        System.out.print(String.format(format, args));
    }
    
    /**
     * 行のリストをコンソールへ出力する．
     */
    @Override
    public synchronized void printlist(Collection<String> entry)
    {
        for (String line : entry) {
            System.out.println(line);
        }
        System.out.println();
    }
}
