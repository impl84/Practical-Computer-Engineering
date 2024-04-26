/**
 * 10人の預金者がそれぞれ1万円を1つの銀行口座へ預金する機能の実験
 */
public class MultipleDepositorsTest
{
    // 預金者の人数
    private final static int NUM_OF_DEPOSITORS = 10;
    
    // 10人の預金者がそれぞれ1万円を1つの銀行口座へ預金する．
    public static void main(String args[])
    {
        // 銀行口座を1つ生成する．
        Account account = new Account();
        
        // 預金者毎の預金処理を実行するスレッドの配列を生成する．
        Thread threads[] = new Thread[10];
        
        // 預金者1人につき1つのスレッドを割り当て，
        // それぞれのスレッドにおいて預金処理を開始する．
        for (int i = 0; i < NUM_OF_DEPOSITORS; i++) {
            // 預金者と，預金処理を実行するためのスレッドを生成する．
            Depositor depositor = new Depositor(account);
            threads[i] = new Thread(depositor);
            
            // スレッドの処理を開始する．
            // 処理を開始したスレッド内部からは，
            // 預金者の預金処理（run メソッド）が呼び出される．
            // ここで呼び出す start メソッドは即時復帰する．
            threads[i].start();
        }
        // 生成した全てのスレッドの終了を待つ．
        for (Thread thread : threads) {
            try {
                thread.join();
            }
            catch (InterruptedException ex) {
                ex.printStackTrace();
            }
        }
        // 預金結果を表示する．
        System.out.println(account.getBalance());
    }
}

/**
 * 銀行口座
 */
class Account
{
    // 口座残高   
    private int balance = 0;
    
    // 預金する
    void deposit(int amount)
    {
        // 口座残高に預金額を加える．
        balance += amount;
    }
    
    // 口座残高を取得する．
    int getBalance()
    {
        return balance;
    }
}

/**
 * 預金者
 */
class Depositor
    implements
        Runnable
{
    // 銀行口座
    private final Account account;
    
    // 預金者を生成する．
    Depositor(Account account)
    {
        // 与えられた銀行口座を保持する．
        this.account = account;
    }
    
    // 銀行口座へ預金する．
    //
    // Runnable インターフェースにおける run メソッドの実装：
    // この預金者に対応するスレッドから呼ばれることを想定している．
    @Override
    public void run()
    {
        // 銀行口座への預金を 10000 回繰り返す．
        for (int i = 0; i < 10000; i++) {
            // 銀行口座へ 1円分預金する．
            this.account.deposit(1);
        }
    }
}
