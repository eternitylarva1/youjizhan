package Youjizhan.relics;

import Youjizhan.helpers.ModHelper;
import Youjizhan.monsters.GremlinLeader2;
import Youjizhan.utils.InstanceMaker;
import Youjizhan.utils.ScreenPartition;
import basemod.abstracts.CustomRelic;
import basemod.abstracts.CustomSavable;
import com.megacrit.cardcrawl.actions.AbstractGameAction;
import com.megacrit.cardcrawl.actions.GameActionManager;
import com.megacrit.cardcrawl.actions.animations.ShoutAction;
import com.megacrit.cardcrawl.actions.animations.TalkAction;
import com.megacrit.cardcrawl.actions.common.*;
import com.megacrit.cardcrawl.actions.unique.CanLoseAction;
import com.megacrit.cardcrawl.cards.DamageInfo;
import com.megacrit.cardcrawl.cards.purple.TalkToTheHand;
import com.megacrit.cardcrawl.dungeons.AbstractDungeon;
import com.megacrit.cardcrawl.helpers.ImageMaster;
import com.megacrit.cardcrawl.helpers.PowerTip;
import com.megacrit.cardcrawl.monsters.AbstractMonster;
import com.megacrit.cardcrawl.monsters.beyond.Darkling;
import com.megacrit.cardcrawl.monsters.city.GremlinLeader;
import com.megacrit.cardcrawl.monsters.exordium.Lagavulin;
import com.megacrit.cardcrawl.monsters.exordium.Sentry;
import com.megacrit.cardcrawl.powers.DexterityPower;
import com.megacrit.cardcrawl.powers.MinionPower;
import com.megacrit.cardcrawl.relics.AbstractRelic;
import com.megacrit.cardcrawl.relics.GremlinHorn;
import com.megacrit.cardcrawl.rewards.RewardItem;
import com.megacrit.cardcrawl.rooms.AbstractRoom;
import com.megacrit.cardcrawl.rooms.MonsterRoom;
import com.megacrit.cardcrawl.rooms.MonsterRoomBoss;
import com.megacrit.cardcrawl.vfx.MegaSpeechBubble;

import java.util.ArrayList;
import java.util.Objects;

public class ArtOfYouji extends CustomRelic  implements CustomSavable<ArrayList<Customyoujisave>> {
    // 遗物ID（此处的ModHelper在“04 - 本地化”中提到）
    public static final String ID = ModHelper.makePath(ArtOfYouji.class.getSimpleName());
    // 图片路径（大小128x128，可参考同目录的图片）
    // 遗物未解锁时的轮廓。可以不使用。如果要使用，取消注释
    // private static final String OUTLINE_PATH = "ExampleModResources/img/relics/MyRelic_Outline.png";
    // 遗物类型
    private static final RelicTier RELIC_TIER = RelicTier.SPECIAL;
    // 点击音效
    private static final LandingSound LANDING_SOUND = LandingSound.FLAT;
    private final ArrayList<Customyoujisave> MonstersEscaped = new ArrayList<>();
    private static  boolean hasused=false;

    public ArtOfYouji() {
        super(ID, ImageMaster.loadImage("YoujizhanResources/images/relics/artOfyouji.png"), RELIC_TIER, LANDING_SOUND);
        // 如果你需要轮廓图，取消注释下面一行并注释上面一行，不需要就删除
        // super(ID, ImageMaster.loadImage(IMG_PATH), ImageMaster.loadImage(OUTLINE_PATH), RELIC_TIER, LANDING_SOUND);
    }

    // 获取遗物描述，但原版游戏只在初始化和获取遗物时调用，故该方法等于初始描述

    public String getUpdatedDescription() {
        return this.DESCRIPTIONS[0];
    }

    @Override
    public void atBattleStart() {
        super.atBattleStart();
        hasused=false;
        for (Customyoujisave customyoujisave : this.MonstersEscaped) {
            try {
                Class  monsterClass = Class.forName(customyoujisave.className.substring(6, customyoujisave.className.length()));

                 AbstractMonster am2= InstanceMaker.getInstanceByClass(monsterClass);
                 if (am2==null){
                     continue;
                 }
                 if (customyoujisave.hp<=0) {continue;
            }
                if (Objects.equals(am2.id, GremlinLeader.ID)){
                    am2=new GremlinLeader2();
                } if (Objects.equals(am2.id, Lagavulin.ID)){
                    am2=new Lagavulin(true);
                }
                    AbstractDungeon.actionManager.addToTop(new SpawnMonsterAction(am2, false));
                AbstractMonster finalAm = am2;
                AbstractDungeon.actionManager.addToBottom(new AbstractGameAction() {
                    public void update() {
                     finalAm.createIntent();
                        finalAm.healthBarUpdatedEvent();
                        this.isDone = true;
                    }
                });
            am2.usePreBattleAction();

                    am2.maxHealth=customyoujisave.maxhp;
                    am2.currentHealth=customyoujisave.hp;
                    if (AbstractDungeon.cardRandomRng.randomBoolean()) {
                        ScreenPartition.assignSequentialPosition(null, am2);
                    }else{
                        ScreenPartition.assignSequentialPosition(am2, null);
                    }

            } catch (ClassNotFoundException e) {
                throw new RuntimeException(e);
            }
        }
        this.MonstersEscaped.clear();
        this.tips.clear();
    }

    public void onMonsterDeath(AbstractMonster m) {
        boolean hasboss=false;
        this.addToBot(new CanLoseAction());
        for (AbstractMonster monster : AbstractDungeon.getCurrRoom().monsters.monsters) {
            if (monster.type ==AbstractMonster.EnemyType.BOSS&&!monster.isDeadOrEscaped()){
                hasboss=true;
            }
        }
        if (m instanceof Darkling){
            ArtOfYouji.this.addToBot(new EscapeAction(m));

        }

        if (AbstractDungeon.getCurrRoom() instanceof MonsterRoomBoss&&!(m.type== AbstractMonster.EnemyType.BOSS)&&hasboss) {
            return;
        }

      this.addToBot(new AbstractGameAction() {
          @Override
          public void update() {
              isDone=true;
              if (hasused){
                  return;
              }
              if (m.currentHealth == 0 && !AbstractDungeon.getMonsters().areMonstersBasicallyDead()&&!m.hasPower(MinionPower.POWER_ID)) {
                  ArtOfYouji.this.flash();
                  this.addToBot(new RelicAboveCreatureAction(m, ArtOfYouji.this));
                  for (AbstractMonster monster : AbstractDungeon.getMonsters().monsters) {
                      if (!monster.isDeadOrEscaped()&&!monster.isDying){

                          String chat=Yulu[AbstractDungeon.cardRandomRng.random.nextInt(Yulu.length)];
                          if (AbstractDungeon.cardRandomRng.randomBoolean(0.45F)) {
                              AbstractDungeon.effectList.add(new MegaSpeechBubble(monster.hb.cX + monster.dialogX, monster.hb.cY + monster.dialogY, 2.0F, chat, monster.isPlayer));
                          }

                          ArtOfYouji.this.addToBot(new EscapeAction(monster));
                          monster.damage(new DamageInfo(monster,(int )(monster.maxHealth*0.15f)));

                          if ((monster.currentHealth<=0)){
                            AbstractDungeon.getCurrRoom().addCardReward(new RewardItem());
                              AbstractDungeon.getCurrRoom().addGoldToRewards(15);
                              continue;
                          }
                          ArtOfYouji.this.MonstersEscaped.add(new Customyoujisave(monster.getClass().toString(),monster.currentHealth,monster.maxHealth));
                              ArtOfYouji.this.tips.add(new PowerTip(monster.name,"剩余血量："+String.valueOf(monster.currentHealth)));
                              hasused=true;

                      }

                  }
              }
          }
      });




    }

String[] Yulu=new String[] {
        "留得青山在,不怕没柴烧",
        "我去找援军",
        "撤退啊！",
        "前面的，等等我！"
        ,"我去叫老大"
        ,"我队友已经连续逃一百步了，我只跑了五十步",
        "咱们去下个火堆埋伏他",
        "咱们去下个问号埋伏他",
        "咱们去下个精英埋伏他",
        "咱们去下个宝箱房间埋伏他",
        "我点的外卖到了,我去看看",
        "唉，唉唉",
        "一时兴起了",
        "轮椅职业闹麻了",
        "老大说让我过去一趟",
        "我战未来去了",
        "嘻嘻，我要活下来",
        "下播喽",
        "我要开灵动步伐了",
        "将军走此小道",
    };
    public AbstractRelic makeCopy() {
        return new ArtOfYouji();

    }


    @Override
    public ArrayList<Customyoujisave> onSave() {
        return MonstersEscaped;
    }

    @Override
    public void onLoad(ArrayList<Customyoujisave> customyoujisaves) {
        if (customyoujisaves==null){
            return;
        }
        MonstersEscaped.addAll(customyoujisaves);
    }
}