
class InstanceofClass
{
    public final static int NUMFISH = 4;
    
    public static void main(String args[])
    {
        // 魚類の配列
        Fish fishes[] = new Fish[NUMFISH];
        
        fishes[0] = new Trout();    // マス
        fishes[1] = new Flounder(); // カレイ 
        fishes[2] = new Tuna();     // マグロ
        fishes[3] = new Trout();    // マス
        
        // 海水魚を表示する．
        for (int i = 0; i < NUMFISH; i++) {
            Fish fish = fishes[i];
            if (fish instanceof SaltWaterFish) {
                fish.display();
            }
        }
    }
}

abstract class Fish
{
    abstract void display();
}

abstract class FreshWaterFish
    extends
        Fish
{
}

abstract class SaltWaterFish
    extends
        Fish
{
}

class Trout
    extends
        FreshWaterFish
{
    @Override
    void display()
    {
        System.out.println("Trout");
    }
}

class Flounder
    extends
        SaltWaterFish
{
    @Override
    void display()
    {
        System.out.println("Flounder");
    }
}

class Tuna
    extends
        SaltWaterFish
{
    @Override
    void display()
    {
        System.out.println("Tuna");
    }
}
