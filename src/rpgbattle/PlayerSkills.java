package rpgbattle;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import rpgsystem.Skill;
import rpgsystem.SkillModifier;

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
        maxCapacity = -999;
        availableSkills = new ArrayList<>();
        equippedSkills = new ArrayList<>();
        availableModifiers = new ArrayList<>();
        appliedModifiers1 = new HashMap<>();
        appliedModifiers2 = new HashMap<>();
        
        // for now, the player has access to all skills and modifiers
        availableSkills.addAll(Arrays.asList(Skill.values()));
        availableModifiers.addAll(Arrays.asList(SkillModifier.values()));
        
        // equip skills by default
        equippedSkills.addAll(Arrays.asList(Skill.values()));
    }
    
    public static int maxCapacity() { return maxCapacity; }
    public static int currentCapacity() {
        return 0;
    }
    
    public static void toggleEquipped(Skill s) {
        if (availableSkills.contains(s)) {
            // remove an applied modifier
            SkillModifier m = appliedModifiers1.get(s);
            appliedModifiers1.remove(s);
            appliedModifiers2.remove(m);
            
            if (equippedSkills.contains(s)) {
                equippedSkills.remove(s);
            } else
                equippedSkills.add(s);
        }
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
    public static void applyModifier(SkillModifier m, Skill s) {
        if (availableSkills.contains(s) && availableModifiers.contains(m))
        {
            appliedModifiers1.remove(s);
            appliedModifiers1.values().remove(m);
            appliedModifiers2.remove(m);
            appliedModifiers2.values().remove(s);

            appliedModifiers1.put(s, m);
            appliedModifiers2.put(m, s);
        }
    }
    public static boolean isSkillEquipped(Skill s) { return equippedSkills.contains(s); }
    public static boolean isModifierApplied(SkillModifier m) { return appliedModifiers1.containsValue(m); }
    public static Skill getLinkedSkill(SkillModifier m) { return appliedModifiers2.get(m); }
    public static SkillModifier getLinkedModifier(Skill s) { return appliedModifiers1.get(s); }
    
    public static void addAvailableSkill(Skill s) {
        if (availableSkills.contains(s))
            availableSkills.add(s);
    }
    public static void addAvailableModifier(SkillModifier m) {
        if (availableModifiers.contains(m))
            availableModifiers.add(m);
    }
}