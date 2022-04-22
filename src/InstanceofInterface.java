
class InstanceofInterface
{
    public final static int NUMMAMMALS = 4;
    
    public static void main(String args[])
    {
        // 哺乳類の配列
        Mammal mammals[] = new Mammal[NUMMAMMALS];
        
        mammals[0] = new Bear();        // 熊
        mammals[1] = new Elephant();    // 象
        mammals[2] = new Horse();       // 馬
        mammals[3] = new Lion();        // ライオン
        
        // 乗り物の機能を実装している哺乳類を利用する．
        for (int i = 0; i < NUMMAMMALS; i++) {
            if (mammals[i] instanceof Vehicle) {
                Vehicle v = (Vehicle)mammals[i];
                v.drive();
            }
        }
    }
}

interface Vehicle
{
    void drive();
}

abstract class Mammal
{
}

class Bear
    extends
        Mammal
{
}

class Elephant
    extends
        Mammal
    implements
        Vehicle
{
    @Override
    public void drive()
    {
        System.out.println("Elephant.drive()");
    }
}

class Horse
    extends
        Mammal
    implements
        Vehicle
{
    @Override
    public void drive()
    {
        System.out.println("Horse.drive()");
    }
}

class Lion
    extends
        Mammal
{
}
