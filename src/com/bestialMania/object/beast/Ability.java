package com.bestialMania.object.beast;

public class Ability {

    /*All fields in here are suggestive only*/
    private String abilityName;
    private int abilityKeyBinding; //Don't know what data type this should be
    private int baseDamageToDeal; //Note some abilities can deal damage over time
    private int range; //Should be relative to the range of the beast that the ability is for? - Applies both to ranged attacking abilities and escapes?
    private int cooldown;

    public Ability(String abilityName, int baseDamageToDeal, int range, int abilityKeyBinding, int coolDown){
        this.abilityName = abilityName;
        this.baseDamageToDeal = baseDamageToDeal;
        this.range = range;
        this.abilityKeyBinding = abilityKeyBinding;
        this.cooldown = coolDown;
    }
}
