package rpgbattle;

import java.util.ArrayList;
import java.util.HashMap;
import rpgsystem.Skill;
import rpgsystem.SkillModifier;
import ui.components.SkillCostMenu;

/**
 * Manages the list of skills the player has equipped, and their modifiers.
 * @author Peter
 */
public class PlayerSkills {
    private static int maxCapacity;
    private static final ArrayList<Skill> availableSkills, equippedSkills;
    private static final ArrayList<SkillModifier> availableModifiers;
    private static final HashMap<Skill,SkillModifier> appliedModifiers1;
    private static final HashMap<SkillModifier,Skill> appliedModifiers2;
    
    static {
        maxCapacity = 60;
        availableSkills = new ArrayList<>();
        equippedSkills = new ArrayList<>();
        availableModifiers = new ArrayList<>();
        appliedModifiers1 = new HashMap<>();
        appliedModifiers2 = new HashMap<>();
        
        // for now, the player has access to all skills and modifiers
        //availableSkills.addAll(Arrays.asList(Skill.values()));
        //availableModifiers.addAll(Arrays.asList(SkillModifier.values()));
        //availableSkills.add(Skill.SKILL_DEFENSE);
        availableSkills.add(Skill.SKILL_POISON);
        availableSkills.add(Skill.SKILL_PHYS);
        //availableSkills.add(Skill.SKILL_FIRE2);
        //availableSkills.add(Skill.SKILL_LIGHT);

        //for (SkillModifier m : SkillModifier.values())
        //    availableModifiers.add(m);
        
        // ...and equip all skills by default
        equippedSkills.addAll(availableSkills);
    }
    
    public static int maxCapacity() { return maxCapacity; }
    public static int currentCapacity() {
        int capacity = 0;
        // add up the cost of all applied skills and modifiers
        for (Skill s : equippedSkills) {
            capacity += s.data.equipCost();
            SkillModifier m = getLinkedModifier(s);
            if (m != null)
                capacity += m.cost;
        }
        return capacity;
    }
    
    public static boolean toggleEquipped(Skill s) {
        if (availableSkills.contains(s)) {

            SkillCostMenu.setRedraw();

            // remove an applied modifier
            SkillModifier m = appliedModifiers1.get(s);
            appliedModifiers1.remove(s);
            appliedModifiers2.remove(m);
            
            if (equippedSkills.contains(s)) {
                equippedSkills.remove(s);
                return true;
            } else {
                if (currentCapacity()+s.data.equipCost() <= maxCapacity) {
                    equippedSkills.add(s);
                    return true;
                } else
                    return false;
            }
        }
        return false;
    }
    
    public static ArrayList<Skill> getAvailableSkills() { return availableSkills; }
    public static ArrayList<Skill> getEquippedSkills() { return equippedSkills; }
    public static ArrayList<SkillModifier> getAvailableModifiers() { return availableModifiers; }
    
    /**
     * Applies a Modifier to a Skill.  Any previous links between the skill and
     * modifier will be erased.  Will only change links if both the skill and
     * modifier are in the list of available skills/modifiers.
     * @param m
     * @param s 
     */
    public static boolean applyModifier(SkillModifier m, Skill s) {
        if (availableSkills.contains(s) && availableModifiers.contains(m))
        {
            appliedModifiers1.remove(s);
            appliedModifiers1.values().remove(m);
            appliedModifiers2.remove(m);
            appliedModifiers2.values().remove(s);

            appliedModifiers1.put(s, m);
            appliedModifiers2.put(m, s);

            SkillCostMenu.setRedraw();

            // modifiers add their own cost in addition to a potential increase in cost for the skill
            if (currentCapacity() <= maxCapacity) // if we are still under capacity this is fine
                return true;

            removeModifier(m, s); // otherwise undo the modifier
        }
        return false;
    }
    public static void removeModifier(SkillModifier m) {
        removeModifier(m, getLinkedSkill(m));
    }
    private static void removeModifier(SkillModifier m, Skill s) {
        if (equippedSkills.contains(s) && availableModifiers.contains(m)) {
            appliedModifiers1.remove(s);
            appliedModifiers1.values().remove(m);
            appliedModifiers2.remove(m);
            appliedModifiers2.values().remove(s);

            SkillCostMenu.setRedraw();
        }
    }
    public static boolean isSkillEquipped(Skill s) { return equippedSkills.contains(s); }
    public static boolean isModifierApplied(SkillModifier m) { return appliedModifiers1.containsValue(m); }
    public static Skill getLinkedSkill(SkillModifier m) { return appliedModifiers2.get(m); }
    public static SkillModifier getLinkedModifier(Skill s) { return appliedModifiers1.get(s); }

    /**
     * Add a new skill to the list of available ones, if not already present.
     * @param s
     */
    public static void addAvailableSkill(Skill s) {
        if (!availableSkills.contains(s))
            availableSkills.add(s);

    }
    /**
     * Add a new modifier to the list of available ones, if not already present.
     * @param m
     */
    public static void addAvailableModifier(SkillModifier m) {
        if (!availableModifiers.contains(m))
            availableModifiers.add(m);
    }
}