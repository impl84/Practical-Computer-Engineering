
import java.util.Collection;

/**
 * ログ出力用のインターフェース
 */
public interface Logger
{
	/**
	 * 文字列を出力する．
	 */
	public void println(String line);
	
	/**
	 * フォーマットを指定した文字列を出力する．
	 */
	public void printf(String format, Object... args);
	
	/**
	 * 行のリストを出力する．
	 */
	public void printlist(Collection<String> entry);
}
