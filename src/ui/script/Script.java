package ui.script;

/**
 *
 * @author Peter
 */
public enum Script {
    alertTrigger(
        "$SECRALRT_$FLAG",
        "$RESET",
        "$FLAG",
        "You managed to trigger a security alert.",
        "The facility is going into lockdown mode.\nYou won't be able to get through\nthat door until the alert is cancelled.",
        "Look for a command console or something!\nYou might be able to shut down the alert."
    ),
    walkwayRoomEnter(
        "Your goal is on the other side of this room.",
        "Keep going."
    ),
    boilerRoomEnter(
        "Not too much farther now."
        //"Is your health holding up?"
    ),
    doorIsLocked(
        "$INVCHECK_Lv1 Keycard_$PASS",
        "That door in front of you is locked.\nYou'll need a level one keycard to open it.",
        "Look around the control room downstairs.",
        "$GOTO $END",
        "$PASS",
        "Good, you got the keycard.\nHead on through.",
        "$END"
    ),
    beforeBoss(
        "$INVCHECK_Keycard2_$PASS",
        "$RESET",
        "$PASS",
        "...You've done well.",
        "Your target is on the other side of\nthat door.",
        "Kill them.",
        "$PROMPT_What?_$A_Do I have to?_$A",
        "$A",
        "There's no other way to get out.",
        "You don't want to be stuck here, do you?",
        "$PROMPT_Yes_$YES_No_$NO",
        "$YES",
        "...",
        "$GOTO $END",
        "$NO",
        "You have to do it.\nYou'll understand when you see them.",
        "$END"
    ),
    intro(
        "> Someone is talking through the radio\nin your backpack.",
        "Testing, testing. Hello?\nIs anyone there?",
        "$CHOICE",
        "Can you hear me?",
        "$PROMPT_Yes_$YES_No_$NO",
        "$NO",
        "...what?",
        "$GOTO $CHOICE",
        "$YES",
        "Thank god, I thought I was the only one\nleft. I've been stuck here by myself forever.",
        "Maybe if we work together, we can get out.",
        "You'll help me, right?",
        "$PROMPT_Yes._$YES2_Who are you?_$NO2",
        "$YES2",
        "We'll make it out of here, I promise.",
        "$GOTO $END",
        "$NO2",
        "I know you don't have much reason to\ntrust me right now, but...",
        "You're just as stuck as I am right now.\nI can help. I know this place.",
        "I can get you out too.",
        "$END"
    ),
    intro2(
        "Listen, you need to get to the room\nnorth of here.",
        "There's someone who doesn't want to let\nus out of here. I can't fight them, but\nmaybe you can.",
        "If you want to get out, you'll need to...",
        "...",
        "I'll explain everything once you're there.\nAlright?",
        "$PROMPT_Alright._$YES_I don't understand._$NO",
        "$YES",
        "See you soon.",
        "$GOTO $END",
        "$NO",
        "...",
        "It doesn't matter.\nYou'll have to decide eventually.",
        "$END"
    ),
    consoleTutorial(
        "$SECRALRT_$FLAG",
        "$RESET",
        "$FLAG",
        "There's a terminal right ahead of you.",
        "...",
        "You've used a computer before, right?\nOf course you have. This will be simple.",
        "There should be a program to shut down\nthe security alert on that terminal."
    ),
    test(
        "There's a keycard over there.\nYou'll need it to get through.",
        "Looks like you'll trigger a security alert if\nyou pass through that barrier, though..."
    ),
    enemyTutorial(
        "You need to watch out for\npatrolling enemies.",
        "If you can sneak around them you'll have\na better chance of making it out of\nhere alive."
    ),
    welcome(
        "Hello!",
        "Would you like a quick explanation\nof the controls?",
        "$PROMPT_Yes_$YES_No_$NO",
        "$NO",
        "Very well, then.",
        "$GOTO $A",
        "$YES",
        "Use the arrow keys to walk.\nZ is to interact.\nX is to cancel.",
        "You should check out the Skills\nmenu by pressing X!",
        "$A",
        "Good luck..."
    ),
    bossIntro1(
        "$SPRITE talk_boss",
        "You're here.",
        "I'm sorry, but I can't let you through.",
        "I don't know what you've been told,\nbut you're making a mistake.",
        "Stop. Now."
    ),
    bossIntro2(
        "$SPRITE talk_boss",
        "Is there anything I can say to change\nyour mind? ...Probably not.",
        "If you attack me, I will do everything in\nmy power to kill you.",
        "Are you ready for that?"
    );
    
    public final String[] dialogue;
    Script(String... d) { dialogue = d; }
    
    public static Script getScript(String name) {
        for (Script s : values())
            if (s.name().equals(name))
                return s;
        return null;
    }
}