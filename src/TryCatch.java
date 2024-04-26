
public class TryCatch
{
    public static void main(String[] args)
    {
        int result = 1;
        try {
            System.out.println("> try catch finally の実験");
            result /= 0;
        }
        catch (ArithmeticException ex) {
            System.out.println("> catch ブロックの中 (ArithmeticException)");
            //return;
            //throw ex;
            //System.exit(-1);
            //throw new IOException("例外発生？");
        }
        catch (Exception ex) {
            System.out.println("> catch ブロックの中 (Exception)");
        }
        finally {
            System.out.println("> finally ブロックの中");
        }
        System.out.println("> main メソッドの最後：" + result);
    }
}
