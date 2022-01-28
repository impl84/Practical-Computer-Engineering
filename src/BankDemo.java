
public class BankDemo
{
    private final static int NUMCUSTOMERS = 10;
    
    public static void main(String args[])
    {
        Account  account     = new Account();
        Customer customers[] = new Customer[NUMCUSTOMERS];
        
        for (int i = 0; i < NUMCUSTOMERS; i++) {
            customers[i] = new Customer(account);
            customers[i].start();
        }
        for (int i = 0; i < NUMCUSTOMERS; i++) {
            try {
                customers[i].join();
            }
            catch (InterruptedException ex) {
                ex.printStackTrace();
            }
        }
        System.out.println(account.getBalance());
    }
}

class Account
{
    private int balance = 0;
    
    void deposit(int amount)
// synchronized void deposit(int amount)
    {
        balance += amount;
    }
    
    int getBalance()
    {
        return balance;
    }
}

class Customer
    extends
        Thread
{
    Account account;
    
    Customer(Account account)
    {
        this.account = account;
    }
    
    @Override
    public void run()
    {
        try {
            for (int i = 0; i < 10000; i++) {
                this.account.deposit(1);
            }
        }
        catch (Exception ex) {
            ex.printStackTrace();
        }
    }
}
