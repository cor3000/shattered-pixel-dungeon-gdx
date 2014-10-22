package com.watabou.pixeldungeon.items.food;

import com.watabou.pixeldungeon.Assets;
import com.watabou.pixeldungeon.Badges;
import com.watabou.pixeldungeon.Statistics;
import com.watabou.pixeldungeon.actors.buffs.*;
import com.watabou.pixeldungeon.actors.hero.Hero;
import com.watabou.pixeldungeon.actors.mobs.npcs.Wandmaker;
import com.watabou.pixeldungeon.effects.Speck;
import com.watabou.pixeldungeon.effects.SpellSprite;
import com.watabou.pixeldungeon.items.Item;
import com.watabou.pixeldungeon.items.potions.*;
import com.watabou.pixeldungeon.items.scrolls.ScrollOfRecharging;
import com.watabou.pixeldungeon.plants.Plant.Seed;
import com.watabou.pixeldungeon.plants.*;
import com.watabou.pixeldungeon.scenes.GameScene;
import com.watabou.pixeldungeon.sprites.ItemSprite;
import com.watabou.pixeldungeon.sprites.ItemSpriteSheet;
import com.watabou.pixeldungeon.utils.GLog;
import com.watabou.noosa.audio.Sample;
import com.watabou.utils.Bundle;
import com.watabou.utils.Random;

/**
 * Created by debenhame on 12/08/2014.
 */
public class Blandfruit extends Food {

    public String message = "You eat the Blandfruit, bleugh!";
    public String info = "So dry and insubstantial, perhaps stewing it with another ingredient would improve it.";

    public Potion potionAttrib = null;
    public ItemSprite.Glowing potionGlow = null;

    {
        name = "Blandfruit";
        stackable = true;
        image = ItemSpriteSheet.BLANDFRUIT;
        energy = (Hunger.STARVING - Hunger.HUNGRY)/2;
        hornValue = 6; //only applies when blandfruit is cooked
    }

    @Override
    public boolean isSimilar( Item item ) {
        if (item instanceof Blandfruit){
            if (potionAttrib == null){
                if (((Blandfruit)item).potionAttrib == null)
                    return true;
            } else if (((Blandfruit)item).potionAttrib != null){
                if (((Blandfruit)item).potionAttrib.getClass() == potionAttrib.getClass())
                    return true;
            }
        }
        return false;
    }

    @Override
    public void execute( Hero hero, String action ) {
        if (action.equals( AC_EAT )){

            if (potionAttrib == null) {

                detach(hero.belongings.backpack);

                ((Hunger) hero.buff(Hunger.class)).satisfy(energy);
                GLog.i(message);

                hero.sprite.operate(hero.pos);
                hero.busy();
                SpellSprite.show(hero, SpellSprite.FOOD);
                Sample.INSTANCE.play(Assets.SND_EAT);

                hero.spend(1f);

                Statistics.foodEaten++;
                Badges.validateFoodEaten();
            } else {

                ((Hunger) hero.buff(Hunger.class)).satisfy(Hunger.HUNGRY);

                detach(hero.belongings.backpack);

                hero.spend(1f);
                hero.busy();

                if (potionAttrib instanceof PotionOfFrost) {
                    GLog.i("the Frostfruit tastes a bit like Frozen Carpaccio.");
                    switch (Random.Int(5)) {
                        case 0:
                            GLog.i("You see your hands turn invisible!");
                            Buff.affect(hero, Invisibility.class, Invisibility.DURATION);
                            break;
                        case 1:
                            GLog.i("You feel your skin harden!");
                            Buff.affect(hero, Barkskin.class).level(hero.HT / 4);
                            break;
                        case 2:
                            GLog.i("Refreshing!");
                            Buff.detach(hero, Poison.class);
                            Buff.detach(hero, Cripple.class);
                            Buff.detach(hero, Weakness.class);
                            Buff.detach(hero, Bleeding.class);
                            break;
                        case 3:
                            GLog.i("You feel better!");
                            if (hero.HP < hero.HT) {
                                hero.HP = Math.min(hero.HP + hero.HT / 4, hero.HT);
                                hero.sprite.emitter().burst(Speck.factory(Speck.HEALING), 1);
                            }
                            break;
                    }
                } else
                    potionAttrib.apply(hero);

                Sample.INSTANCE.play( Assets.SND_EAT );

                hero.sprite.operate(hero.pos);

                switch (hero.heroClass) {
                    case WARRIOR:
                        if (hero.HP < hero.HT) {
                            hero.HP = Math.min( hero.HP + 5, hero.HT );
                            hero.sprite.emitter().burst( Speck.factory(Speck.HEALING), 1 );
                        }
                        break;
                    case MAGE:
                        hero.belongings.charge( false );
                        ScrollOfRecharging.charge(hero);
                        break;
                    case ROGUE:
                    case HUNTRESS:
                        break;
                }
            }
        } else {
            super.execute(hero, action);
        }
    }

    @Override
    public String info() {
        return info;
    }

    @Override
    public int price() {
        return 20 * quantity;
    }

    public Item cook(Seed seed){
        Class<? extends Item> plant = seed.alchemyClass;


        try {
            potionAttrib = (Potion)plant.newInstance();
            potionAttrib.ownedByFruit = true;
        } catch (Exception e) {
            return null;
        }

        potionAttrib.image = ItemSpriteSheet.BLANDFRUIT;


        info = "The fruit has plumped up from its time soaking in the pot and has even absorbed the properties "+
               "of the " + seed.name() + " seed it was cooked with.\n\n";

        if (potionAttrib instanceof PotionOfHealing){

            name = "Healthfruit";
            potionGlow = new ItemSprite.Glowing( 0x2EE62E );
            info += "It looks delicious and hearty, ready to be eaten!";

        } else if (potionAttrib instanceof PotionOfStrength){

            name = "Powerfruit";
            potionGlow = new ItemSprite.Glowing( 0xCC0022 );
            info += "It looks delicious and powerful, ready to be eaten!";

        } else if (potionAttrib instanceof PotionOfParalyticGas){

            name = "Paralyzefruit";
            potionGlow = new ItemSprite.Glowing( 0x67583D );
            info += "It looks firm and volatile, eating this might be unsafe.";

        } else if (potionAttrib instanceof PotionOfInvisibility){

            name = "Invisifruit";
            potionGlow = new ItemSprite.Glowing( 0xE5D273 );
            info += "It looks delicious and shiny, ready to be eaten!";

        } else if (potionAttrib instanceof PotionOfLiquidFlame){

            name = "Flamefruit";
            potionGlow = new ItemSprite.Glowing( 0xFF7F00 );
            info += "It looks spicy and volatile, eating this might be unsafe.";

        } else if (potionAttrib instanceof PotionOfFrost){

            name = "Frostfruit";
            potionGlow = new ItemSprite.Glowing( 0x66B3FF );
            info += "It looks delicious and refreshing, ready to be eaten!";

        } else if (potionAttrib instanceof PotionOfMindVision){

            name = "Visionfruit";
            potionGlow = new ItemSprite.Glowing( 0xB8E6CF );
            info += "It looks delicious and shadowy, ready to be eaten!";

        } else if (potionAttrib instanceof PotionOfToxicGas){

            name = "Toxicfruit";
            potionGlow = new ItemSprite.Glowing( 0xA15CE5 );
            info += "It looks crisp and volatile, eating this might be unsafe.";

        }

        return this;
    }

    public static final String NAME = "name";

    @Override
    public void cast( final Hero user, int dst ) {
        if (potionAttrib instanceof PotionOfLiquidFlame ||
                potionAttrib instanceof PotionOfToxicGas ||
                potionAttrib instanceof PotionOfParalyticGas ||
                potionAttrib instanceof PotionOfFrost) {
            potionAttrib.cast(user, dst);
            detach( user.belongings.backpack );
        } else {
            super.cast(user, dst);
        }

    }

    @Override
    public void storeInBundle(Bundle bundle){
        super.storeInBundle(bundle);
        bundle.put( NAME, name );
    }

    @Override
    public void restoreFromBundle(Bundle bundle){
        super.restoreFromBundle(bundle);
        name = bundle.getString(NAME);

        if (name.equals("Healthfruit"))
            cook(new Sungrass.Seed());
        else if (name.equals("Powerfruit"))
            cook(new Wandmaker.Rotberry.Seed());
        else if (name.equals("Paralyzefruit"))
            cook(new Earthroot.Seed());
        else if (name.equals("Invisifruit"))
            cook(new Blindweed.Seed());
        else if (name.equals("Flamefruit"))
            cook(new Firebloom.Seed());
        else if (name.equals("Frostfruit"))
            cook(new Icecap.Seed());
        else if (name.equals("Visionfruit"))
            cook(new Fadeleaf.Seed());
        else if (name.equals("Toxicfruit"))
            cook(new Sorrowmoss.Seed());

    }


    @Override
    public ItemSprite.Glowing glowing() {
        return potionGlow;
    }

}
