package rpgsystem;

import rpgbattle.PlayerSkills;

/**
 * Modifiers change the base effects of a skill.
 * @author Peter
 */
public enum SkillModifier implements Pickupable{
    MOD_DAMAGE_BOOST("Dmg Boost","Increases base power of a skill.", 1),
    MOD_EFFICIENCY("Efficiency","Halves mana cost for a skill.", 1),
    MOD_POWER_BOOST("Overclock","Doubles power and mana cost.", 3),
    MOD_ACCURACY("Accuracy","Doubles accuracy of a skill.", 3),
    MOD_MULTI_TARGET("Multi-Target","The skill targets all enemies.\nAccuracy is reduced,\nCost is increased.", 5);

    public final int cost;
    public final String name, desc;
    SkillModifier(String n, String d, int c) {
        name = n;
        desc = d;
        cost = c;
    }
    
    public String getFullInfoString() {
        Skill s = PlayerSkills.getLinkedSkill(this);
        if (s != null)
            return desc + "\n\nApplied to: "+s.name;
        else return desc;
    }

    @Override
    public String pickupName() { return name+" modifier"; };
    @Override
    public String overworldSprite() { return "skill_mod"; }; // TODO change this
    @Override
    public void pickup() {
        PlayerSkills.addAvailableModifier(this);
    }
}