package rpgsystem;

import rpgbattle.PlayerSkills;

/**
 * Modifiers change the base effects of a skill.
 * @author Peter
 */
public enum SkillModifier implements Pickupable{
    MOD_DAMAGE_BOOST("Boost","Increases base power.", 3),
    MOD_EFFICIENCY("Efficiency","Lowers base cost.", 8),
    MOD_OVERCLOCK("Overclock","Doubles power and cost.", 5),
    MOD_BESERK("Beserk","Greatly increases crit rate.\nLowers accuracy.", 6),
    MOD_ACCURACY("Accuracy","Increases base accuracy.", 5),
    MOD_MULTI_TARGET("Multi-Target","The skill targets all enemies.\nAccuracy is reduced,\nCost is increased.", 6);

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
            return desc + "\n\nCost: "+cost+"\nApplied to: "+s.data.name;
        else return desc + "\n\nCost: "+cost;
    }

    @Override
    public String pickupName() { return name+" modifier"; };
    @Override
    public String overworldSprite() { return "skill_mod"; };
    @Override
    public void pickup() {
        PlayerSkills.addAvailableModifier(this);
    }
}