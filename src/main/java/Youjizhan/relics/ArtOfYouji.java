package Youjizhan.relics;

import Youjizhan.helpers.ModHelper;
import basemod.abstracts.CustomRelic;
import com.megacrit.cardcrawl.actions.animations.ShoutAction;
import com.megacrit.cardcrawl.actions.common.*;
import com.megacrit.cardcrawl.dungeons.AbstractDungeon;
import com.megacrit.cardcrawl.helpers.ImageMaster;
import com.megacrit.cardcrawl.monsters.AbstractMonster;
import com.megacrit.cardcrawl.powers.DexterityPower;
import com.megacrit.cardcrawl.relics.AbstractRelic;
import com.megacrit.cardcrawl.relics.GremlinHorn;
import com.megacrit.cardcrawl.rooms.MonsterRoom;
import com.megacrit.cardcrawl.rooms.MonsterRoomBoss;

public class ArtOfYouji extends CustomRelic {
    // 遗物ID（此处的ModHelper在“04 - 本地化”中提到）
    public static final String ID = ModHelper.makePath(ArtOfYouji.class.getSimpleName());
    // 图片路径（大小128x128，可参考同目录的图片）
    private static final String IMG_PATH = "MubanResources/images/relics/heikun i.png";
    // 遗物未解锁时的轮廓。可以不使用。如果要使用，取消注释
    // private static final String OUTLINE_PATH = "ExampleModResources/img/relics/MyRelic_Outline.png";
    // 遗物类型
    private static final RelicTier RELIC_TIER = RelicTier.UNCOMMON;
    // 点击音效
    private static final LandingSound LANDING_SOUND = LandingSound.FLAT;

    public ArtOfYouji() {
        super(ID, ImageMaster.loadImage(IMG_PATH), RELIC_TIER, LANDING_SOUND);
        // 如果你需要轮廓图，取消注释下面一行并注释上面一行，不需要就删除
        // super(ID, ImageMaster.loadImage(IMG_PATH), ImageMaster.loadImage(OUTLINE_PATH), RELIC_TIER, LANDING_SOUND);
    }

    // 获取遗物描述，但原版游戏只在初始化和获取遗物时调用，故该方法等于初始描述

    public String getUpdatedDescription() {
        return this.DESCRIPTIONS[0];
    }

    public void atBattleStart() {
        this.flash();
        this.addToBot(new RelicAboveCreatureAction(AbstractDungeon.player, this));
        this.addToBot(new ApplyPowerAction(AbstractDungeon.player, AbstractDungeon.player, new DexterityPower(AbstractDungeon.player, -6), -6));
        this.grayscale = true;
    }

    public int onLoseHpLast(int damageAmount) {
        if (damageAmount > 0) {
            this.flash();
            return damageAmount/2;
        } else {
            return damageAmount;
        }

    }
    public void onMonsterDeath(AbstractMonster m) {
        if (AbstractDungeon.getCurrRoom() instanceof MonsterRoomBoss) {
            return;
        }
        if (m.currentHealth == 0 && !AbstractDungeon.getMonsters().areMonstersBasicallyDead()) {
            this.flash();
            this.addToBot(new RelicAboveCreatureAction(m, this));
            for (AbstractMonster monster : AbstractDungeon.getMonsters().monsters) {
                if (!monster.isDeadOrEscaped()){
                    String chat=Yulu[AbstractDungeon.cardRandomRng.random.nextInt(Yulu.length)];
                    this.addToTop(new ShoutAction(monster,"chat"));
                this.addToTop(new EscapeAction(monster));
                }
            }

        }

    }
String[] Yulu=new String[] {
        "留得青山在,不怕没柴烧","我去找援军","撤退啊！","为什么我没烟雾弹也能跑？"
    };
    public AbstractRelic makeCopy() {
        return new ArtOfYouji();

    }
}