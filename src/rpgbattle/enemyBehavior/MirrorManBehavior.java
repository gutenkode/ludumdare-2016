package rpgbattle.enemyBehavior;
import java.util.ArrayList;
import rpgsystem.Inventory;
import mote4.util.matrix.ModelMatrix;
import rpgbattle.BattleManager;
import rpgbattle.fighter.EnemyFighter;
import ui.BattleUIManager;
import rpgbattle.PlayerSkills;
import rpgsystem.Element;
import rpgsystem.Item;
import rpgsystem.Skill;

/**
 *
 * @author Peter and Chance
 */
public class MirrorManBehavior extends EnemyBehavior {
    private int potions, energyDrinks, manaHypos, grenades, action;
    private boolean hasFire = false;
    private boolean hasBolt = false;
    private boolean hasIce = false;
    private boolean hasCure = false;
    private float deathCycle;
    
    public MirrorManBehavior(EnemyFighter f){
        super(f);
        performActTime = 40;
        deathCycle = 1;
        ArrayList<Item> inv = Inventory.get();
        for (int i = 0; i < inv.size(); i++){
            String str = inv.get(i).name;
            if (str.equals("Potion")) potions += 1;
            else if (str.equals("Energy Drink")) energyDrinks += 1;
            else if (str.equals("Mana Hypo")) manaHypos += 1;
            else if (str.equals("Grenade")) grenades += 1;
        }
        ArrayList<Skill> skills = PlayerSkills.getEquippedSkills();
        for (int i = 0; i < skills.size(); i++){
            String str = skills.get(i).name;
            if (str.equals("Fireball")) hasFire = true;
            else if (str.equals("Thunderbolt")) hasBolt = true;
            else if (str.equals("Ice")) hasIce = true;
            else if (str.equals("Cure")) hasCure = true;
        }
    }
    
    @Override
    public void initAct() {
        actDelay = 60;
        if (fighter.stats.health <= fighter.stats.maxHealth-50 && fighter.stats.mana >= 10 && hasCure){
            BattleUIManager.logMessage("The Mirror Man casts Cure!");
            action = 0;
        }
        else if (fighter.stats.health <= fighter.stats.maxHealth-25 && potions > 0){
            BattleUIManager.logMessage("The Mirror Man drinks a potion.");
            action = 1;
        }
        else if (fighter.stats.stamina <= fighter.stats.maxStamina-60 && energyDrinks > 0){
            BattleUIManager.logMessage("The Mirror Man drinks an energy drink.");
            action = 2;
        }
        else if (fighter.stats.mana <= fighter.stats.maxMana-30 && manaHypos > 0){
            BattleUIManager.logMessage("The Mirror Man injects a Mana Hypo.");
            action = 3;
        }
        else if (grenades > 0 && Math.random() > .75){
            BattleUIManager.logMessage("The Mirror Man throws a Grenade!");
            action = 4;
        }
        else if (hasFire && fighter.stats.mana >= 5 &&  Math.random() > .8){
            BattleUIManager.logMessage("The Mirror Man casts Fireball!");
            action = 5;
        }
        else if (hasBolt && fighter.stats.mana >= 7 && Math.random() > .8){
            BattleUIManager.logMessage("The Mirror Man casts Thunderbolt!");
            action = 6;
        }
        else if (hasIce && fighter.stats.mana >= 7 && Math.random() > .8){
            BattleUIManager.logMessage("The Mirror Man casts Ice!");
            action = 7;
        }
        else{
            BattleUIManager.logMessage("The Mirror Man attacks!");
            action = 8;
        }
    }
    
    @Override
    void performAct() {
        switch (action) {
            case 0:
                fighter.restoreHealth(50);
                fighter.stats.mana -= 10;
            case 1:
                fighter.restoreHealth(25);
                potions -= 1;
                break;
            case 2:
                fighter.restoreStamina(60);
                energyDrinks -= 1;
                break;
            case 3:
                fighter.restoreMana(40);
                manaHypos -= 1;
                break;
            case 4:
                BattleManager.getPlayer().damage(Element.NONE, 10, 40, 100, false);
                grenades -= 1;
                break;
            case 5:
                BattleManager.getPlayer().damage(Element.FIRE, 10, 30, 100, false);
                fighter.stats.mana -= 5;
                break;
            case 6:
                BattleManager.getPlayer().damage(Element.ELEC, 10, 35, 80, false);
                fighter.stats.mana -= 7;
                break;
            case 7:
                boolean crit = Math.random() > .35;
                BattleManager.getPlayer().damage(Element.ICE, 10, 25, 100, crit);
                fighter.stats.mana -= 7;
                break;
            case 8:
                useAttack();
                break;
        }
    }

    @Override
    public void runDeathAnimation(ModelMatrix model) {
        model.translate(0, (1-deathCycle)*96);
        model.scale(1, deathCycle, 1);
        //if (deathCycle > .1)
            deathCycle *= .95;
    }
}