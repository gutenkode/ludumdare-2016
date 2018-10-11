package rpgsystem;

import mote4.scenegraph.Window;
import mote4.util.FileIO;
import org.json.JSONObject;
import rpgbattle.PlayerSkills;
import ui.components.BattleAnimation;

import java.io.IOException;

/**
 * Manages loading data for skills from a json file.
 * Created by Peter on 5/31/17.
 */
public class SkillData {
    private static final JSONObject json;
    static {
        JSONObject temp = null;
        try {
            temp = new JSONObject(FileIO.getString("/res/files/skills.json"));
        } catch (IOException e) {
            e.printStackTrace();
            Window.destroy();
        }
        json = temp;
    }

    public final String name, desc, spriteName, sfxName;
    public final BattleEffect effect;
    public final BattleAnimation.Type animType;
    public final Element element;
    public final boolean usesSP;
    private final int basePower, baseAccuracy, baseCost, baseCritRate;
    private final Skill skill;

    public SkillData(Skill skill) {
        this.skill = skill;

        JSONObject skillJson = json.getJSONObject(skill.name());

        name = skillJson.getString("skillName");
        desc = skillJson.getString("description");
        spriteName = skillJson.getString("sprite");

        effect = BattleEffect.valueOf(skillJson.getString("effect"));
        animType = BattleAnimation.Type.valueOf(skillJson.getString("animation"));
        element = Element.valueOf(skillJson.getString("element"));

        if (skillJson.has("sfxName"))
            sfxName = skillJson.getString("sfxName");
        else
            sfxName = "sfx_skill_"+element.name().toLowerCase();

        // if the Json specifies a stat type to use, override the element type
        boolean b = (element == Element.PHYS || element == Element.BOMB);
        if (skillJson.has("useStamina"))
            usesSP = skillJson.getBoolean("useStamina");
        else
            usesSP = b;

        basePower = skillJson.getInt("power");
        baseAccuracy = skillJson.getInt("accuracy");
        baseCost = skillJson.getInt("cost");
        baseCritRate = skillJson.getInt("critrate");
    }


    public String getFullInfoString() {
        StringBuilder sb = new StringBuilder(desc);
        sb.append("\n");

        if (basePower != 0) {
            sb.append("\nPower: ").append(basePower);
            if (basePower != power())
                sb.append(" > ").append(power());
        }

        if (baseAccuracy != 0) {
            sb.append("\nAccuracy: ").append(baseAccuracy);
            if (baseAccuracy != accuracy())
                sb.append(" > ").append(accuracy());
        }

        if (baseCritRate != 0) {
            sb.append("\nCrit Rate: ").append(baseCritRate);
            if (baseCritRate != critRate())
                sb.append(" > ").append(critRate());
        }

        if (usesSP)
            sb.append("\nSP Cost: ");
        else
            sb.append("\nMP Cost: ");
        sb.append(baseCost);
        if (baseCost != cost())
            sb.append(" > ").append(cost());

        sb.append("\nElement: ").append(element);

        SkillModifier m = PlayerSkills.getLinkedModifier(skill);
        if (m != null)
            sb.append("\nModifier: ").append(m.name);
        else
            sb.append("\nModifier: ---");
        return sb.toString();
    }
    public int power() {
        SkillModifier m = PlayerSkills.getLinkedModifier(skill);
        if (m == null)
            return basePower;
        switch (m) {
            case MOD_DAMAGE_BOOST:
                return (int)(basePower * 1.5);
            case MOD_OVERCLOCK:
                return (int)(basePower * 2);
            default:
                return basePower;
        }
    }
    public int accuracy() {
        SkillModifier m = PlayerSkills.getLinkedModifier(skill);
        if (m == null)
            return baseAccuracy;
        switch (m) {
            case MOD_MULTI_TARGET:
                return (int)(baseAccuracy*.75);
            case MOD_ACCURACY:
                return Math.min(baseAccuracy +15, 100);
            case MOD_BESERK:
                return (int)(baseAccuracy*.66);
            default:
                return baseAccuracy;
        }
    }
    public int cost() {
        SkillModifier m = PlayerSkills.getLinkedModifier(skill);
        if (m == null)
            return baseCost;
        switch (m) {
            case MOD_EFFICIENCY:
                return baseCost/2;
            case MOD_OVERCLOCK:
                return baseCost*2;
            case MOD_MULTI_TARGET:
                return (int)(baseCost*1.5);
            default:
                return baseCost;
        }
    }
    public int equipCost() {
        if (usesSP)
            return cost()/3;
        return cost();
    }
    public int critRate() {
        SkillModifier m = PlayerSkills.getLinkedModifier(skill);
        if (m == null)
            return baseCritRate;
        switch (m) {
            case MOD_BESERK:
                return Math.min(baseCritRate*2, 100);
            default:
                return baseCritRate;
        }
    }
}
