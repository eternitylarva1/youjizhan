package Youjizhan.relics;

import java.io.Serializable;
import java.util.ArrayList;

public class Customyoujisave implements Serializable {
    public String className;
    public Integer hp;
    public Integer maxhp;
    public Customyoujisave(String classesName, Integer hp, Integer maxhp) {
        this.className =classesName ;
        this.hp=hp;
        this.maxhp=maxhp;
    }
}
