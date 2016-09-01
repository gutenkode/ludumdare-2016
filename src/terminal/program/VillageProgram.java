package terminal.program;
import java.util.ArrayList;
import map.MapLevelManager;
import terminal.TerminalSession;
import rpgsystem.Item;
import rpgsystem.Inventory;
import rpgbattle.BattleManager;

/**
 *
 * @author Peter and Chance
 */
public class VillageProgram implements Program {

    private TerminalSession session;
    private boolean checkHowMany;
    private String it;

    @Override
    public String name() { return "villag"; }
    
    
    @Override
    
    public void init(TerminalSession s){
        session = s;
        if (MapLevelManager.getCurrentLevel() == 1) session.addLine("Going to Xe");
        else if (MapLevelManager.getCurrentLevel() == 2) session.addLine("Going to Village Xe");
        else if (MapLevelManager.getCurrentLevel() == 3) session.addLine("Running to VillageXe");
        else if (MapLevelManager.getCurrentLevel() == 4) session.addLine("Running VILLAG.EXE");
        session.addLine("Hi! You can SEND items over or LISTEN");
        session.addLine("to a story! If you want to send ");
        session.addLine("something over, you should ask yourself");
        session.addLine("\"WHAT CAN I SEND\" first.");
        checkHowMany = false;
    }
    
    @Override
    public void close() {
        session.addLine("Village is now closed.");
    }
    
    private void sendStuff(String s) {
        Inventory.removeItem(s);
        if (s.toLowerCase().equals("potion")){
            BattleManager.getPlayer().stats.health += 10;
            BattleManager.getPlayer().stats.maxHealth += 10;
            session.addLine("Thank you for the potion! Here's to your health!");
            session.addLine("Your HP and MAX HP increased by 10!");
        }
        else if (s.toLowerCase().equals("energy drink")){
            BattleManager.getPlayer().stats.stamina += 20;
            BattleManager.getPlayer().stats.maxStamina += 20;
            session.addLine("Thank you for the energy drink!");
            session.addLine("Don't get too hyper, now!");
            session.addLine("Your STAMINA and MAX STAMINA increased by 20!");
        }
        else if (s.toLowerCase().equals("mana hypo")){
            BattleManager.getPlayer().stats.mana += 5;
            BattleManager.getPlayer().stats.maxMana += 5;
            session.addLine("Thank you for the mana hypo! The hypo is real.");
            session.addLine("Your MANA and MAX MANA increased by 5!");
        }
        else if (s.toLowerCase().equals("grenade")){
            BattleManager.getPlayer().stats.attack += 2;
            session.addLine("Thank you for the grenade!");
            session.addLine("Your punches will now pack an explosive power!");
            session.addLine("Your ATTACK increased by 2!");
        }
        /*else if (s.toLowerCase().substring(0,2) == "lv"){
            session.addLine("I have no idea how to implement this yet... :(");
        }*/ //If we need keycards. :|
    }
    
    @Override
    public void parse(String s) {
        switch (s.toLowerCase()) {
            case "help":
                session.addLine("Available Commands:");
                session.addLine("send <item>: Send over an item for rewards.\n");
                session.addLine("send <item>s: Send over more than one of the\n");
                session.addLine("same item for rewards.\n");
                session.addLine("listen: Listen to a quick story!");
                break;
            case "listen":
                session.clear();
                if (MapLevelManager.getCurrentLevel() == 1){
                    session.addLine("My dear son come to me.");
                    session.addLine("Have you heard the tale of our people?");
                    session.addLine("It is a story of hope and strength,");
                    session.addLine("of hard work and fortune.");
                    session.addLine("We got our start long ago and far away.");
                    session.addLine("We are the hope of our people.");
                }
                else if (MapLevelManager.getCurrentLevel() == 2){
                    session.addLine("We received our start when we heard about");
                    session.addLine("the new plague. Scientists called it");
                    session.addLine("Digititus Umbos, a fancy latin name.");
                    session.addLine("Our class simply referred to it in HORROR as");
                    session.addLine("Umbos, and we feared for our lives.");
                }
                else if (MapLevelManager.getCurrentLevel() == 3){
                    session.addLine("It would attack through the lower classes,");
                    session.addLine("slowly infecting the whole society.");
                    session.addLine("But we had a savior. His scientific knowledge");
                    session.addLine("had been ignored to this point, and instead");
                    session.addLine("he rose as a politician. On that fateful day");
                    session.addLine("he held two press conferences, one for the");
                    session.addLine("other classes to tell them a fix had been");
                    session.addLine("found, and one to us, the most vulnerable.");
                }
                else if (MapLevelManager.getCurrentLevel() == 4){
                    session.addLine("\"My heroes,\" he called us, \"you have");
                    session.addLine("always been on the underside of our society,");
                    session.addLine("leeching away at its strength.");
                    session.addLine("Well no longer!");
                    session.addLine("I have found a way for you all to save us");
                    session.addLine("from Umbos. I call it the Uckers.\"");
                    session.addLine("\"The Uckers will quarantine you to remove the");
                    session.addLine("danger of Umbos. You will no longer leech from us.");
                    session.addLine("I believe that this great device will be complete");
                    session.addLine("in a week, and then we shall get you on board!");
                    session.addLine("The Uckers will save us all!\"");
                }
                break;
            case "exit":
                session.addLine("Goodbye!");
                session.closeProgram();
                break;
            case "what can i send":
                session.clear();
                ArrayList<Item> inv = Inventory.get();
                int[] arr = new int[100];
                for (int i = 0; i < inv.size(); i++){
                    String str = inv.get(i).name;
                    /*if (str.equals("Lv1 Keycard") || str.equals("Lv2 Keycard") || str.equals("Lv3 Keycard") || str.equals("Lv4 Keycard") || str.equals("Lv5 Keycard")){
                        arr[Integer.parseInt(str.substring(2,3))] += 1;
                    }*/
                    if (str.equals("Potion")) arr[0] += 1;
                    else if (str.equals("Energy Drink")) arr[1] += 1;
                    else if (str.equals("Mana Hypo")) arr[2] += 1;
                    else if (str.equals("Grenade")) arr[3] += 1;
                }
                /*if (arr[0] > 0) session.addLine("The LV1 KEYCARD can be given for passage to Level 2.");
                if (arr[1] > 0) session.addLine("The LV2 KEYCARD can be given for passage to Level 3.");
                if (arr[2] > 0) session.addLine("The LV3 KEYCARD can be given for passage to Level 4.");
                if (arr[3] > 0) session.addLine("The LV4 KEYCARD can be given for passage to Level 5.\n  NOTE: YOU NEED 3 TO PROGRESS. ("+Integer.toString(arr[3])+")");
                if (arr[4] > 0) session.addLine("The LV5 KEYCARD will activate the Uckers.\n  NOTE: YOU NEED 4 TO DO SO. ("+Integer.toString(arr[4])+")");*/
                if (arr[0] > 0){
                        session.addLine("A POTION can be given for a 10 MAX HP and 10 HP");
                        session.addLine("increase.\n  We really like the grapes! ("+Integer.toString(arr[0])+")");
                }
                if (arr[1] > 0){
                        session.addLine("An ENERGY DRINK can be given for a 20 MAX STAMINA");
                        session.addLine("and 20 STAMINA increase.");
                        session.addLine("In the meantime, we will have heart attacks. ("+Integer.toString(arr[1])+")");
                }
                if (arr[2] > 0){
                        session.addLine("A MANA HYPO can be given for a 5 MAX MP and 5 MP");
                        session.addLine("increase. We could also develop some new skills");
                        session.addLine("using it, I think... ("+Integer.toString(arr[2])+")");
                }
                if (arr[3] > 0){
                        session.addLine("A GRENADE can be given for a 2 ATTACK increase.");
                        session.addLine("We could also develop skills to annihilate. (" + Integer.toString(arr[3])+")");
                }
                break;
            default:
                if (s.toLowerCase().startsWith("send ")){
                    String str = s.substring(5);
                    if (Inventory.hasItem(str) && !str.substring(0,2).equals("Lv") && !str.equals("Monster Bait")){
                        sendStuff(str);
                    }
                    else if (Inventory.hasItem(str.substring(0,str.length()-1)) && str.substring(str.length()-1).toLowerCase().equals("s")){
                        session.addLine("How many " + str.substring(0)+ "would you like to send?");
                        it = str.substring(0,str.length()-1);
                        checkHowMany = true;
                    }
                    else session.addLine("You can't do that.");
                }
                else if (checkHowMany){
                    try {
                        int times = Integer.parseInt(s);
                        if (times > 0){
                            for (int i = 0; i < times; i++){
                                if (Inventory.hasItem(it)) sendStuff(it);
                                else{
                                   session.addLine("You have no more " + it + "s to send!");
                                    break;
                                }
                            }
                            checkHowMany = false;
                        }
                        else session.addLine("You can't send negative items.");
                    }
                    catch (NumberFormatException e) {
                        session.addLine("That is not a number.");
                    }
                }
                else
                    session.addLine("Invalid command.");
                break;
        }
    }
}