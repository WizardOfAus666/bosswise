# ---------------------------------------------------------------------------
# WARNING: this script writes equipment.json from hand-typed numbers.
# The stats in equipment.json are now synced from the OSRS Wiki's own
# dps-calc dataset by tools/sync_wiki_data.py. If you re-run this script,
# run the sync afterwards or you will put the old guessed numbers back:
#     python3 tools/sync_wiki_data.py . ../osrs-dps-calc
# See WIKI_DATA_FIX.md.
# ---------------------------------------------------------------------------
import json
import os

items = []

def add_item(
    item_id, name, slot, primary_style,
    attack_type="NONE", weapon_category="", speed=4, two_handed=False,
    price=1000, tier=1, untradeable=False,
    req_skills=None, quest_req=None,
    stab_atk=0, slash_atk=0, crush_atk=0, magic_atk=0, ranged_atk=0,
    stab_def=0, slash_def=0, crush_def=0, magic_def=0, ranged_def=0,
    melee_str=0, ranged_str=0, magic_dmg=0, prayer=0,
    dragonbane=False, demonbane=False, ratbane=False, undead_bonus=False,
    slayer_bonus=False, dragonfire_prot=False, venom_prot=False, wildy_bonus=False,
    spear=False, mirror_shield=False, leaf_bladed=False, boots_of_stone=False,
    void_knight=False, kalphitebane=False, ammo_type="NONE",
    has_spec=False, spec_energy=0, spec_effect="",
    special_effect="", source="OSRS Wiki", verified_date="2026-11-16"
):
    item = {
        "itemId": item_id,
        "name": name,
        "slot": slot,
        "primaryStyle": primary_style,
        "attackType": attack_type,
        "weaponCategory": weapon_category,
        "attackSpeed": speed,
        "twoHanded": two_handed,
        "defaultPrice": price,
        "tier": tier,
        "untradeable": untradeable,
        "reqSkills": req_skills if req_skills else {},
        "questReq": quest_req if quest_req else "",
        "stabAttack": stab_atk,
        "slashAttack": slash_atk,
        "crushAttack": crush_atk,
        "magicAttack": magic_atk,
        "rangedAttack": ranged_atk,
        "stabDefence": stab_def,
        "slashDefence": slash_def,
        "crushDefence": crush_def,
        "magicDefence": magic_def,
        "rangedDefence": ranged_def,
        "meleeStrength": melee_str,
        "rangedStrength": ranged_str,
        "magicDamagePercent": magic_dmg,
        "prayerBonus": prayer,
        "dragonbane": dragonbane,
        "demonbane": demonbane,
        "ratbane": ratbane,
        "undeadBonus": undead_bonus,
        "slayerTaskBonus": slayer_bonus,
        "dragonfireProtection": dragonfire_prot,
        "venomProtection": venom_prot,
        "wildernessBonus": wildy_bonus,
        "spear": spear,
        "mirrorShield": mirror_shield,
        "leafBladed": leaf_bladed,
        "bootsOfStone": boots_of_stone,
        "voidKnight": void_knight,
        "kalphitebane": kalphitebane,
        "ammoType": ammo_type,
        "hasSpecialAttack": has_spec,
        "specEnergyCost": spec_energy,
        "specEffect": spec_effect,
        "specialEffect": special_effect,
        "source": source,
        "verifiedDate": verified_date
    }
    items.append(item)

# ==============================================================================
# 1. ONE-HANDED MELEE WEAPONS
# ==============================================================================
add_item(1333, "Rune scimitar", "WEAPON", "MELEE", attack_type="SLASH", weapon_category="Scimitar", speed=4, price=15000, tier=1,
         req_skills={"ATTACK": 40}, stab_atk=7, slash_atk=45, crush_atk=-2, melee_str=44, special_effect="Budget 4-tick slash weapon for level 40 Attack")
add_item(4587, "Dragon scimitar", "WEAPON", "MELEE", attack_type="SLASH", weapon_category="Scimitar", speed=4, price=60000, tier=3,
         req_skills={"ATTACK": 60}, quest_req="Monkey Madness I", stab_atk=8, slash_atk=67, crush_atk=-2, melee_str=66,
         has_spec=True, spec_energy=55, spec_effect="Prevents opponent from using protection prayers for 5 seconds",
         special_effect="Premier 4-tick slash weapon; Monkey Madness I unlock")
add_item(21009, "Dragon sword", "WEAPON", "MELEE", attack_type="STAB", weapon_category="Sword", speed=4, price=40000, tier=3,
         req_skills={"ATTACK": 60}, stab_atk=65, slash_atk=55, crush_atk=-2, melee_str=63,
         has_spec=True, spec_energy=40, spec_effect="25% increased accuracy and maximum hit; penetrates protection prayers",
         special_effect="Reliable 4-tick one-handed stab weapon for dragons and metal monsters")
add_item(1434, "Dragon mace", "WEAPON", "MELEE", attack_type="CRUSH", weapon_category="Mace", speed=4, price=35000, tier=3,
         req_skills={"ATTACK": 60}, quest_req="Heroes' Quest", stab_atk=40, slash_atk=-4, crush_atk=60, melee_str=66, prayer=5,
         has_spec=True, spec_energy=25, spec_effect="25% increased accuracy and 50% increased damage",
         special_effect="High-value 4-tick crush weapon with +5 Prayer bonus (Calvar'ion & Sarachnis staple)")
add_item(4158, "Leaf-bladed sword", "WEAPON", "MELEE", attack_type="STAB", weapon_category="Sword", speed=4, price=40000, tier=2,
         req_skills={"ATTACK": 50, "SLAYER": 55}, stab_atk=67, melee_str=50, leaf_bladed=True,
         special_effect="Fast 4-tick stab weapon; damages Kurasks and Turoths")
add_item(20727, "Leaf-bladed battleaxe", "WEAPON", "MELEE", attack_type="CRUSH", weapon_category="Axe", speed=5, price=85000, tier=4,
         req_skills={"ATTACK": 65, "SLAYER": 55}, slash_atk=72, crush_atk=92, melee_str=92, leaf_bladed=True,
         special_effect="Heavy crush/slash axe with massive +92 Strength; excels on Kurasks & Turoths")
add_item(23509, "Sarachnis cudgel", "WEAPON", "MELEE", attack_type="CRUSH", weapon_category="Mace", speed=4, price=400000, tier=4,
         req_skills={"ATTACK": 65}, crush_atk=70, melee_str=70,
         special_effect="Top-tier fast 4-tick crush weapon; excels against crush-weak foes (Sarachnis, Spindel)")
add_item(28678, "Zombie axe", "WEAPON", "MELEE", attack_type="SLASH", weapon_category="Axe", speed=5, price=1200000, tier=5,
         req_skills={"ATTACK": 65}, slash_atk=105, crush_atk=90, melee_str=85,
         special_effect="High-damage 5-tick weapon with exceptional +105 Slash and +90 Crush accuracy")
add_item(4151, "Abyssal whip", "WEAPON", "MELEE", attack_type="SLASH", weapon_category="Whip", speed=4, price=1600000, tier=5,
         req_skills={"ATTACK": 70}, slash_atk=82, melee_str=82,
         has_spec=True, spec_energy=50, spec_effect="Drains target run energy by 10% and restores player run energy by 10%",
         special_effect="Standard 4-tick general slash weapon for mid-to-high PvM")
add_item(12006, "Abyssal tentacle", "WEAPON", "MELEE", attack_type="SLASH", weapon_category="Whip", speed=4, price=2500000, tier=6,
         req_skills={"ATTACK": 75}, slash_atk=90, melee_str=86,
         has_spec=True, spec_energy=50, spec_effect="Binds target for 5 seconds and inflicts poison",
         special_effect="High DPS upgrade to Abyssal whip; inflicts poison (Vardorvis & ToB staple)")
add_item(22324, "Ghrazi rapier", "WEAPON", "MELEE", attack_type="STAB", weapon_category="Rapier", speed=4, price=52000000, tier=8,
         req_skills={"ATTACK": 80}, stab_atk=94, slash_atk=55, melee_str=89,
         special_effect="Endgame 4-tick stab weapon from Theatre of Blood with massive +89 Strength")
add_item(23995, "Blade of saeldor", "WEAPON", "MELEE", attack_type="SLASH", weapon_category="Sword", speed=4, price=55000000, tier=8,
         req_skills={"ATTACK": 80}, slash_atk=94, stab_atk=55, melee_str=89,
         special_effect="Endgame 4-tick crystal slash weapon with +94 Slash accuracy and +89 Strength")
add_item(24417, "Inquisitor's mace", "WEAPON", "MELEE", attack_type="CRUSH", weapon_category="Mace", speed=4, price=140000000, tier=8,
         req_skills={"ATTACK": 80}, crush_atk=95, stab_atk=55, melee_str=89, prayer=2,
         special_effect="Best-in-slot 4-tick crush weapon; synergy with Inquisitor armour (+7.5% damage/accuracy)")
add_item(26219, "Osmumten's fang", "WEAPON", "MELEE", attack_type="STAB", weapon_category="Sword", speed=5, price=24000000, tier=8,
         req_skills={"ATTACK": 82}, stab_atk=105, slash_atk=75, melee_str=103,
         has_spec=True, spec_energy=25, spec_effect="Guaranteed accuracy; penetrates defence",
         special_effect="Rerolls accuracy twice on every attack; exceptional against high-defence bosses (ToA, Nex)")
add_item(22978, "Dragon hunter lance", "WEAPON", "MELEE", attack_type="STAB", weapon_category="Lance", speed=4, price=75000000, tier=8,
         req_skills={"ATTACK": 70}, stab_atk=85, slash_atk=65, crush_atk=65, melee_str=75, dragonbane=True,
         special_effect="Dragonbane weapon: +20% accuracy and +20% damage against all dragons (Vorkath, Olm, KBD)")
add_item(6746, "Darklight", "WEAPON", "MELEE", attack_type="SLASH", weapon_category="Sword", speed=4, price=1000, tier=2, untradeable=True,
         req_skills={"ATTACK": 30}, quest_req="Shadow of the Storm", stab_atk=10, slash_atk=18, melee_str=16, demonbane=True,
         special_effect="Demonbane sword: +60% accuracy and damage against lesser demons")
add_item(19675, "Arclight", "WEAPON", "MELEE", attack_type="SLASH", weapon_category="Sword", speed=4, price=50000, tier=7, untradeable=True,
         req_skills={"ATTACK": 75}, slash_atk=38, stab_atk=10, melee_str=8, demonbane=True,
         has_spec=True, spec_energy=50, spec_effect="Drains opponent's Attack, Strength, and Defence by 5% (10% on demons)",
         special_effect="Demonbane weapon: +70% accuracy and +30% damage against all demons (Cerberus, Kril, Sire)")
add_item(28682, "Emberlight", "WEAPON", "MELEE", attack_type="SLASH", weapon_category="Sword", speed=4, price=45000000, tier=9, untradeable=True,
         req_skills={"ATTACK": 80}, quest_req="While Guthix Sleeps", slash_atk=85, stab_atk=45, melee_str=80, demonbane=True,
         has_spec=True, spec_energy=50, spec_effect="Drains demon stats and inflicts massive slash damage",
         special_effect="Ultimate untradeable demonbane weapon: +70% accuracy and +36% damage against demons")
add_item(10581, "Keris", "WEAPON", "MELEE", attack_type="STAB", weapon_category="Dagger", speed=4, price=5000, tier=2, untradeable=True,
         req_skills={"ATTACK": 50}, quest_req="Contact!", stab_atk=40, melee_str=35, kalphitebane=True,
         special_effect="Kalphitebane dagger: +33% damage against Kalphites and Scarabs; 1/51 chance of 3x damage")
add_item(25985, "Keris partisan", "WEAPON", "MELEE", attack_type="STAB", weapon_category="Spear", speed=4, price=20000, tier=4, untradeable=True,
         req_skills={"ATTACK": 65}, quest_req="Beneath Cursed Sands", stab_atk=58, crush_atk=33, melee_str=45, prayer=3, kalphitebane=True,
         special_effect="Premier Kalphitebane spear: +33% damage against Kalphites and Scarabs; 1/51 chance of 3x damage")
add_item(27287, "Keris partisan of breaching", "WEAPON", "MELEE", attack_type="STAB", weapon_category="Spear", speed=4, price=100000, tier=7, untradeable=True,
         req_skills={"ATTACK": 70}, quest_req="Beneath Cursed Sands", stab_atk=58, crush_atk=33, melee_str=45, prayer=3, kalphitebane=True,
         special_effect="Jewelled Keris: +33% accuracy AND +33% damage against Tombs of Amascut scarabs/Kalphites")
add_item(27291, "Keris partisan of the sun", "WEAPON", "MELEE", attack_type="STAB", weapon_category="Spear", speed=4, price=100000, tier=8, untradeable=True,
         req_skills={"ATTACK": 70}, quest_req="Beneath Cursed Sands", stab_atk=58, crush_atk=33, melee_str=45, prayer=3, kalphitebane=True,
         special_effect="Sun gem Keris: 25% damage boost against targets under 25% HP; emergency heal in ToA")
add_item(28796, "Bone mace", "WEAPON", "MELEE", attack_type="CRUSH", weapon_category="Mace", speed=4, price=15000, tier=2, untradeable=True,
         req_skills={"ATTACK": 50}, crush_atk=40, melee_str=38, prayer=2, ratbane=True,
         special_effect="Ratbane mace: +10 flat maximum hit against Scurrius and giant rats")
add_item(22545, "Viggora's chainmace", "WEAPON", "MELEE", attack_type="CRUSH", weapon_category="Mace", speed=4, price=8000000, tier=6,
         req_skills={"ATTACK": 60}, crush_atk=67, melee_str=66, wildy_bonus=True,
         special_effect="Wilderness crush mace: +50% accuracy and +50% damage in Wilderness when charged with revenant ether")
add_item(27657, "Ursine chainmace", "WEAPON", "MELEE", attack_type="CRUSH", weapon_category="Mace", speed=4, price=18000000, tier=7,
         req_skills={"ATTACK": 70}, crush_atk=75, melee_str=73, wildy_bonus=True,
         has_spec=True, spec_energy=50, spec_effect="Bear hug: 100% accurate crush attack, binds target for 3 seconds, inflicts bleed",
         special_effect="Upgraded wilderness crush mace: +50% accuracy and +50% damage; powerful bleed special attack")
add_item(11889, "Zamorakian hasta", "WEAPON", "MELEE", attack_type="STAB", weapon_category="Hasta", speed=4, price=11000000, tier=6,
         req_skills={"ATTACK": 70}, stab_atk=75, slash_atk=65, crush_atk=65, melee_str=75, prayer=2,
         stab_def=13, slash_def=13, crush_def=13, magic_def=2, ranged_def=13,
         special_effect="One-handed 4-tick versatile stab/crush weapon with solid defensive stats (ToA budget staple)")
add_item(28307, "Dual macuahuitl", "WEAPON", "MELEE", attack_type="CRUSH", weapon_category="Mace", speed=4, price=3200000, tier=6, two_handed=True,
         req_skills={"ATTACK": 75}, crush_atk=85, melee_str=78,
         special_effect="Strikes twice per attack (first roll determines second hit); Blood moon armour synergy")

# ==============================================================================
# 2. TWO-HANDED MELEE WEAPONS & SPECIAL ATTACK WEAPONS
# ==============================================================================
add_item(13263, "Abyssal bludgeon", "WEAPON", "MELEE", attack_type="CRUSH", weapon_category="Bludgeon", speed=4, two_handed=True, price=12000000, tier=7,
         req_skills={"ATTACK": 70, "STRENGTH": 70}, crush_atk=102, melee_str=85, prayer=1,
         special_effect="Two-handed 4-tick crush weapon with +102 Crush accuracy (Spindel, Gargoyles, Nightmare)")
add_item(11802, "Armadyl godsword", "WEAPON", "MELEE", attack_type="SLASH", weapon_category="Godsword", speed=6, two_handed=True, price=11000000, tier=7,
         req_skills={"ATTACK": 75}, slash_atk=132, crush_atk=80, melee_str=132, prayer=8,
         has_spec=True, spec_energy=50, spec_effect="The Judgement: Doubles accuracy and increases maximum hit by 37.5%",
         special_effect="High-accuracy burst special attack weapon")
add_item(11804, "Bandos godsword", "WEAPON", "MELEE", attack_type="SLASH", weapon_category="Godsword", speed=6, two_handed=True, price=17000000, tier=8,
         req_skills={"ATTACK": 75}, slash_atk=132, crush_atk=80, melee_str=132, prayer=8,
         has_spec=True, spec_energy=50, spec_effect="Warstrike: Doubles accuracy, increases damage by 21%, drains enemy Defence by damage dealt",
         special_effect="Premier boss defense-reduction weapon; essential for high-level PvM")
add_item(11806, "Saradomin godsword", "WEAPON", "MELEE", attack_type="SLASH", weapon_category="Godsword", speed=6, two_handed=True, price=14000000, tier=7,
         req_skills={"ATTACK": 75}, slash_atk=132, crush_atk=80, melee_str=132, prayer=8,
         has_spec=True, spec_energy=50, spec_effect="Healing Blade: Doubles accuracy, heals HP by 50% of damage and Prayer by 25%",
         special_effect="Sustains trips at Slayer, GWD, and endurance bossing")
add_item(11808, "Zamorak godsword", "WEAPON", "MELEE", attack_type="SLASH", weapon_category="Godsword", speed=6, two_handed=True, price=6500000, tier=6,
         req_skills={"ATTACK": 75}, slash_atk=132, crush_atk=80, melee_str=132, prayer=8,
         has_spec=True, spec_energy=50, spec_effect="Ice Cleave: Doubles accuracy, freezes target in place for 20 seconds",
         special_effect="Freezes bosses in place; great utility at Mutadiles and K'ril")
add_item(26233, "Ancient godsword", "WEAPON", "MELEE", attack_type="SLASH", weapon_category="Godsword", speed=6, two_handed=True, price=22000000, tier=8,
         req_skills={"ATTACK": 75}, slash_atk=132, crush_atk=80, melee_str=132, prayer=8,
         has_spec=True, spec_energy=50, spec_effect="Blood Sacrifice: Marks target for 25 damage after 5 ticks and heals player for 25 HP",
         special_effect="Reliable heal + burst damage special attack for Colosseum and Nex")
add_item(13576, "Dragon warhammer", "WEAPON", "MELEE", attack_type="CRUSH", weapon_category="Warhammer", speed=6, price=22000000, tier=8,
         req_skills={"ATTACK": 60, "STRENGTH": 60}, crush_atk=95, melee_str=85,
         has_spec=True, spec_energy=50, spec_effect="Smash: Increases damage by 50% and reduces target Defence level by 30% on hit",
         special_effect="Premier flat 30% defense reduction hammer for raids (CoX, ToB) and high-defence bosses")
add_item(13652, "Dragon claws", "WEAPON", "MELEE", attack_type="SLASH", weapon_category="Claws", speed=4, two_handed=True, price=90000000, tier=9,
         req_skills={"ATTACK": 60}, slash_atk=57, stab_atk=41, melee_str=56,
         has_spec=True, spec_energy=50, spec_effect="Slice and Dice: 4 rapid hits with very high accuracy and immense burst damage",
         special_effect="Best-in-slot melee burst special attack for Raids, Vorkath, and Zulrah")
add_item(27690, "Voidwaker", "WEAPON", "MELEE", attack_type="SLASH", weapon_category="Sword", speed=4, price=95000000, tier=9,
         req_skills={"ATTACK": 75}, slash_atk=80, stab_atk=70, melee_str=80,
         has_spec=True, spec_energy=50, spec_effect="Disrupt: 100% GUARANTEED magic damage hit between 50% and 150% of maximum melee hit",
         special_effect="Guaranteed magic damage hit that NEVER misses; excels against high-defence bosses (Nex, Vardorvis, ToA)")
add_item(5698, "Dragon dagger (p++)", "WEAPON", "MELEE", attack_type="STAB", weapon_category="Dagger", speed=4, price=25000, tier=3,
         req_skills={"ATTACK": 60}, quest_req="Lost City", stab_atk=40, slash_atk=25, melee_str=40,
         has_spec=True, spec_energy=25, spec_effect="Puncture: Two rapid strikes with 15% increased accuracy and maximum hit; inflicts poison",
         special_effect="Best budget special attack weapon in OSRS; 4 specs per bar")
add_item(11824, "Zamorakian spear", "WEAPON", "MELEE", attack_type="STAB", weapon_category="Spear", speed=4, two_handed=True, price=10000000, tier=6, spear=True,
         req_skills={"ATTACK": 70}, stab_atk=85, slash_atk=65, crush_atk=65, melee_str=75, prayer=2,
         has_spec=True, spec_energy=25, spec_effect="Shove: Pushes target back 1 tile and stuns for 3 seconds",
         special_effect="Corporeal Beast mandatory weapon: Deals full 100% damage (other weapons deal 50%)")
add_item(21003, "Elder maul", "WEAPON", "MELEE", attack_type="CRUSH", weapon_category="Warhammer", speed=6, two_handed=True, price=145000000, tier=9,
         req_skills={"ATTACK": 75, "STRENGTH": 75}, crush_atk=135, melee_str=147,
         has_spec=True, spec_energy=50, spec_effect="Pulverize: 35% Defence reduction on hit with massive accuracy multiplier",
         special_effect="Mega-rare crush weapon from CoX; premier 35% Defence reduction special attack")
add_item(4718, "Dharok's greataxe", "WEAPON", "MELEE", attack_type="SLASH", weapon_category="Axe", speed=7, two_handed=True, price=1800000, tier=5,
         req_skills={"ATTACK": 70, "STRENGTH": 70}, slash_atk=103, crush_atk=95, melee_str=105,
         special_effect="Dharok the Wretched's set weapon; deals up to 100% bonus damage as HP drops")
add_item(4726, "Guthan's warspear", "WEAPON", "MELEE", attack_type="STAB", weapon_category="Spear", speed=5, two_handed=True, price=1200000, tier=5, spear=True,
         req_skills={"ATTACK": 70}, stab_atk=75, slash_atk=75, crush_atk=75, melee_str=75,
         special_effect="Guthan the Infested's set weapon; 25% chance to heal HP equal to damage dealt")
add_item(4734, "Karil's crossbow", "WEAPON", "RANGED", attack_type="RANGED", weapon_category="Crossbow", speed=3, two_handed=True, price=150000, tier=5,
         req_skills={"RANGED": 70}, ranged_atk=84,
         special_effect="Fast 3-tick crossbow firing bolt racks; Karil's set synergy")
add_item(4755, "Verac's flail", "WEAPON", "MELEE", attack_type="CRUSH", weapon_category="Flail", speed=5, two_handed=True, price=400000, tier=5,
         req_skills={"ATTACK": 70}, crush_atk=82, melee_str=72, prayer=6,
         special_effect="Verac the Defiled's set weapon; 25% chance to ignore opponent prayer and defence (Callisto/Venenatis staple)")
add_item(28338, "Soulreaper axe", "WEAPON", "MELEE", attack_type="SLASH", weapon_category="Axe", speed=5, two_handed=True, price=320000000, tier=10,
         req_skills={"ATTACK": 80, "STRENGTH": 80}, slash_atk=121, melee_str=135,
         has_spec=True, spec_energy=25, spec_effect="Consumes soul stacks to heal 8 HP per stack and strike with increased accuracy",
         special_effect="Endgame mega-axe: Builds up to 5 soul stacks granting +6% Strength per stack (+30% total)")

# ==============================================================================
# 3. RANGED WEAPONS
# ==============================================================================
add_item(861, "Magic shortbow (i)", "WEAPON", "RANGED", attack_type="RANGED", weapon_category="Bow", speed=3, two_handed=True, price=120000, tier=3, untradeable=True,
         req_skills={"RANGED": 50}, ranged_atk=75,
         has_spec=True, spec_energy=50, spec_effect="Fires two arrows in rapid succession",
         special_effect="Premier 3-tick budget bow on Rapid; fires rune and amethyst arrows")
add_item(9185, "Rune crossbow", "WEAPON", "RANGED", attack_type="RANGED", weapon_category="Crossbow", speed=4, price=10000, tier=3,
         req_skills={"RANGED": 61}, ranged_atk=90,
         special_effect="Reliable budget crossbow capable of firing enchanted broad/diamond/ruby bolts")
add_item(21902, "Dragon crossbow", "WEAPON", "RANGED", attack_type="RANGED", weapon_category="Crossbow", speed=4, price=1100000, tier=5,
         req_skills={"RANGED": 64}, ranged_atk=94,
         special_effect="Mid-tier crossbow firing dragon bolts (dragon ruby/diamond bolts)")
add_item(11785, "Armadyl crossbow", "WEAPON", "RANGED", attack_type="RANGED", weapon_category="Crossbow", speed=4, price=28000000, tier=7,
         req_skills={"RANGED": 70}, ranged_atk=100, prayer=1,
         has_spec=True, spec_energy=40, spec_effect="Doubles accuracy for the shot",
         special_effect="High accuracy crossbow with +1 Prayer and extended attack range")
add_item(21012, "Dragon hunter crossbow", "WEAPON", "RANGED", attack_type="RANGED", weapon_category="Crossbow", speed=4, price=65000000, tier=8, dragonbane=True,
         req_skills={"RANGED": 70}, ranged_atk=95,
         special_effect="Dragonbane crossbow: +30% accuracy and +30% damage against dragons (Vorkath & Olm head BiS)")
add_item(26374, "Zaryte crossbow", "WEAPON", "RANGED", attack_type="RANGED", weapon_category="Crossbow", speed=4, price=320000000, tier=10,
         req_skills={"RANGED": 80}, ranged_atk=110, prayer=1,
         has_spec=True, spec_energy=75, spec_effect="Guarantees enchanted bolt activation (Ruby bolt deals 110 guaranteed damage on boss)",
         special_effect="Best-in-slot crossbow: 10% boosted enchanted bolt effects and guaranteed spec proc (Nex & high-inv ToA staple)")
add_item(12926, "Toxic blowpipe", "WEAPON", "RANGED", attack_type="RANGED", weapon_category="Blowpipe", speed=2, two_handed=True, price=3400000, tier=6,
         req_skills={"RANGED": 75}, ranged_atk=30, ranged_str=20,
         has_spec=True, spec_energy=50, spec_effect="Heals for 50% of damage dealt and increases accuracy and damage",
         special_effect="Blistering 2-tick attack speed on Rapid; high DPS against low-defence targets and heals")
add_item(25865, "Bow of faerdhinen", "WEAPON", "RANGED", attack_type="RANGED", weapon_category="Bow", speed=4, two_handed=True, price=130000000, tier=9, untradeable=True,
         req_skills={"RANGED": 75, "AGILITY": 70}, ranged_atk=128, ranged_str=106,
         special_effect="Endgame crystal bow: Synergizes with Crystal armour for +30% accuracy and +15% damage")
add_item(20997, "Twisted bow", "WEAPON", "RANGED", attack_type="RANGED", weapon_category="Bow", speed=5, two_handed=True, price=1700000000, tier=10,
         req_skills={"RANGED": 85}, ranged_atk=70, ranged_str=20,
         special_effect="Ultimate ranged weapon: Scales accuracy and damage based on the opponent's Magic level (up to +250% damage)")
add_item(27610, "Venator bow", "WEAPON", "RANGED", attack_type="RANGED", weapon_category="Bow", speed=4, two_handed=True, price=65000000, tier=8,
         req_skills={"RANGED": 75}, ranged_atk=75, ranged_str=20,
         special_effect="Bounces to up to 2 adjacent targets for 66% damage (Slayer, Grotesque Guardians, Colosseum)")
add_item(28688, "Scorching bow", "WEAPON", "RANGED", attack_type="RANGED", weapon_category="Bow", speed=4, two_handed=True, price=75000000, tier=9, untradeable=True, demonbane=True,
         req_skills={"RANGED": 80}, ranged_atk=95, ranged_str=75,
         has_spec=True, spec_energy=50, spec_effect="Binds demon and inflicts continuous burning damage",
         special_effect="Premier demonbane bow: +30% accuracy and +30% damage against demons (Tormented Demons & Kril)")
add_item(22550, "Craw's bow", "WEAPON", "RANGED", attack_type="RANGED", weapon_category="Bow", speed=3, two_handed=True, price=12000000, tier=6, wildy_bonus=True,
         req_skills={"RANGED": 60}, ranged_atk=75, ranged_str=60,
         special_effect="Wilderness shortbow: +50% accuracy and +50% damage in Wilderness when charged with ether")
add_item(27655, "Webweaver bow", "WEAPON", "RANGED", attack_type="RANGED", weapon_category="Bow", speed=3, two_handed=True, price=30000000, tier=8, wildy_bonus=True,
         req_skills={"RANGED": 70}, ranged_atk=85, ranged_str=70,
         has_spec=True, spec_energy=50, spec_effect="Fires 4 rapid venomous shots in succession",
         special_effect="Upgraded wilderness bow: +50% accuracy and +50% damage; lethal 4-shot venom special attack")
add_item(28794, "Bone bow", "WEAPON", "RANGED", attack_type="RANGED", weapon_category="Bow", speed=3, two_handed=True, price=15000, tier=2, untradeable=True, ratbane=True,
         req_skills={"RANGED": 50}, ranged_atk=50, ranged_str=35,
         special_effect="Ratbane bow: +10 flat maximum hit against Scurrius and giant rats")
add_item(28310, "Eclipse atlatl", "WEAPON", "RANGED", attack_type="RANGED", weapon_category="Thrown", speed=3, two_handed=True, price=1800000, tier=6,
         req_skills={"RANGED": 75}, ranged_atk=65, ranged_str=45,
         special_effect="Thrown weapon whose max hit scales off the player's MELEE STRENGTH; Eclipse moon synergy")

# ==============================================================================
# 4. MAGIC WEAPONS (POWERED & TRADITIONAL)
# ==============================================================================
add_item(11907, "Trident of the seas", "WEAPON", "MAGIC", attack_type="MAGIC", weapon_category="Powered Staff", speed=4, price=50000, tier=4, untradeable=True,
         req_skills={"MAGIC": 75}, magic_atk=15, magic_def=2,
         special_effect="Powered staff with built-in spell attacking at 4-tick speed; max hit scales with Magic level")
add_item(12899, "Trident of the swamp", "WEAPON", "MAGIC", attack_type="MAGIC", weapon_category="Powered Staff", speed=4, price=2800000, tier=6, untradeable=True,
         req_skills={"MAGIC": 78}, magic_atk=25, magic_def=2, venom_prot=False,
         special_effect="Upgraded toxic trident; +3 higher max hit than seas and inflicts venom")
add_item(22323, "Sanguinesti staff", "WEAPON", "MAGIC", attack_type="MAGIC", weapon_category="Powered Staff", speed=4, price=65000000, tier=8, untradeable=True,
         req_skills={"MAGIC": 82}, magic_atk=25,
         special_effect="Theatre of Blood powered staff; +1 max hit over swamp and 1/6 chance to heal for 50% of damage")
add_item(27275, "Tumeken's shadow", "WEAPON", "MAGIC", attack_type="MAGIC", weapon_category="Powered Staff", speed=5, two_handed=True, price=1350000000, tier=10,
         req_skills={"MAGIC": 85}, magic_atk=35,
         special_effect="Ultimate powered staff: TRIPLES magic accuracy and magic damage bonuses outside raids (4x inside ToA)")
add_item(28676, "Warped sceptre", "WEAPON", "MAGIC", attack_type="MAGIC", weapon_category="Powered Staff", speed=4, price=250000, tier=3, untradeable=True,
         req_skills={"MAGIC": 62, "SLAYER": 56}, magic_atk=12,
         special_effect="Budget powered staff accessible at 62 Magic; 4-tick speed")
add_item(1409, "Iban's staff (u)", "WEAPON", "MAGIC", attack_type="MAGIC", weapon_category="Staff", speed=5, price=200000, tier=2, untradeable=True,
         req_skills={"MAGIC": 50, "ATTACK": 50}, quest_req="Underground Pass", magic_atk=10, magic_def=2,
         special_effect="Casts Iban Blast with fixed base damage of 25 (premier early-game bossing staff for Barrows/Zulrah)")
add_item(21006, "Kodai wand", "WEAPON", "MAGIC", attack_type="MAGIC", weapon_category="Wand", speed=4, price=110000000, tier=9,
         req_skills={"MAGIC": 75}, magic_atk=28, magic_dmg=15,
         special_effect="Unlimited water runes, 15% chance to save runes when casting, and +15% magic damage (Barrage BiS)")
add_item(11791, "Staff of the dead", "WEAPON", "MAGIC", attack_type="MAGIC", weapon_category="Staff", speed=4, price=5500000, tier=6,
         req_skills={"MAGIC": 75, "ATTACK": 75}, magic_atk=17, melee_str=72, magic_dmg=15,
         has_spec=True, spec_energy=100, spec_effect="Halves all incoming melee damage for 1 minute",
         special_effect="+15% magic damage bonus; strong defensive special attack")
add_item(24423, "Harmonised nightmare staff", "WEAPON", "MAGIC", attack_type="MAGIC", weapon_category="Staff", speed=4, price=160000000, tier=9,
         req_skills={"MAGIC": 75}, magic_atk=20, magic_dmg=24,
         special_effect="Casts standard spellbook spells at rapid 4-tick speed; incredible synergy with elemental weaknesses")
add_item(27659, "Accursed sceptre", "WEAPON", "MAGIC", attack_type="MAGIC", weapon_category="Powered Staff", speed=4, price=15000000, tier=7, wildy_bonus=True,
         req_skills={"MAGIC": 70}, magic_atk=25, magic_dmg=10,
         has_spec=True, spec_energy=50, spec_effect="Condemn: Deals up to 50% extra damage and reduces opponent Defence and Magic by 15%",
         special_effect="Wilderness powered staff: +50% accuracy and +50% damage; powerful defence/magic debuff special attack")
add_item(28685, "Purging staff", "WEAPON", "MAGIC", attack_type="MAGIC", weapon_category="Staff", speed=5, price=30000000, tier=8, untradeable=True, demonbane=True,
         req_skills={"MAGIC": 80}, magic_atk=25, magic_dmg=10,
         special_effect="Demonbane staff: Casts Demonbane spells (Dark Demonbane) with amplified accuracy and damage")
add_item(28792, "Bone staff", "WEAPON", "MAGIC", attack_type="MAGIC", weapon_category="Staff", speed=4, price=15000, tier=2, untradeable=True, ratbane=True,
         req_skills={"MAGIC": 50}, magic_atk=15,
         special_effect="Ratbane staff: Built-in 4-tick spell with +10 flat maximum hit against Scurrius")

# ==============================================================================
# 5. SHIELDS AND OFF-HANDS
# ==============================================================================
add_item(12954, "Dragon defender", "SHIELD", "MELEE", attack_type="NONE", price=50000, tier=5, untradeable=True,
         req_skills={"ATTACK": 60, "DEFENCE": 60}, stab_atk=25, slash_atk=24, crush_atk=23, melee_str=6,
         stab_def=25, slash_def=24, crush_def=23, magic_def=-3, ranged_def=-2,
         special_effect="Best offensive melee off-hand obtainable without raids; essential general PvM off-hand")
add_item(22322, "Avernic defender", "SHIELD", "MELEE", attack_type="NONE", price=85000000, tier=9, untradeable=True,
         req_skills={"ATTACK": 70, "DEFENCE": 70}, stab_atk=30, slash_atk=29, crush_atk=28, melee_str=8,
         stab_def=30, slash_def=29, crush_def=28, magic_def=-5, ranged_def=-4,
         special_effect="Best-in-slot melee offensive offhand; upgraded Avernic hilt from Theatre of Blood")
add_item(1540, "Anti-dragon shield", "SHIELD", "MELEE", price=500, tier=1, dragonfire_prot=True,
         req_skills={}, stab_def=7, slash_def=9, crush_def=8, magic_def=2, ranged_def=8,
         special_effect="Fundamental dragonfire protection shield; mandatory for green/blue/red/black dragons and KBD")
add_item(11283, "Dragonfire shield", "SHIELD", "MELEE", price=4500000, tier=6, dragonfire_prot=True,
         req_skills={"DEFENCE": 75}, melee_str=7, stab_atk=-10, slash_atk=-10, crush_atk=-10, magic_atk=-10, ranged_atk=-5,
         stab_def=70, slash_def=75, crush_def=72, magic_def=10, ranged_def=72,
         special_effect="Fully charged DFS grants +7 Melee Strength, massive defences, and complete dragonfire protection")
add_item(22002, "Dragonfire ward", "SHIELD", "RANGED", price=7500000, tier=7, dragonfire_prot=True,
         req_skills={"DEFENCE": 75, "RANGED": 75}, ranged_atk=15,
         stab_def=58, slash_def=54, crush_def=56, magic_def=18, ranged_def=52,
         special_effect="Best-in-slot dragonfire shield for Ranged with +15 Ranged accuracy (Vorkath BiS)")
add_item(21633, "Ancient wyvern shield", "SHIELD", "MAGIC", price=8500000, tier=7, dragonfire_prot=True,
         req_skills={"DEFENCE": 75, "MAGIC": 75}, magic_atk=15, magic_dmg=2,
         stab_def=60, slash_def=58, crush_def=62, magic_def=20, ranged_def=58,
         special_effect="Provides complete protection against Wyvern icy breath and dragonfire; +2% Magic damage")
add_item(12817, "Elysian spirit shield", "SHIELD", "MELEE", price=620000000, tier=10,
         req_skills={"DEFENCE": 75, "PRAYER": 75}, prayer=3,
         stab_def=63, slash_def=65, crush_def=75, magic_def=2, ranged_def=57,
         special_effect="70% chance to reduce incoming damage by 25%; ultimate defensive shield in OSRS")
add_item(12825, "Arcane spirit shield", "SHIELD", "MAGIC", price=120000000, tier=8,
         req_skills={"DEFENCE": 75, "PRAYER": 75}, magic_atk=20, prayer=3,
         stab_def=53, slash_def=55, crush_def=73, magic_def=2, ranged_def=52,
         special_effect="Premier magic offhand with +20 Magic attack and high defensive stats")
add_item(27277, "Elidinis' ward (f)", "SHIELD", "MAGIC", price=110000000, tier=9, untradeable=True,
         req_skills={"DEFENCE": 80, "MAGIC": 80, "PRAYER": 80}, magic_atk=25, magic_dmg=5, prayer=4,
         stab_def=53, slash_def=55, crush_def=73, magic_def=2, ranged_def=52,
         special_effect="Fortified Elidinis' ward from ToA: +5% Magic damage and +25 Magic attack (BiS mage offhand)")
add_item(20714, "Tome of fire", "SHIELD", "MAGIC", price=1200000, tier=5,
         req_skills={"MAGIC": 50}, magic_atk=8,
         special_effect="Provides unlimited fire runes and boosts Fire spell damage by 10% (amplifies fire elemental weaknesses)")
add_item(24444, "Tome of water", "SHIELD", "MAGIC", price=1800000, tier=5,
         req_skills={"MAGIC": 50}, magic_atk=8, magic_def=8,
         special_effect="Provides unlimited water runes; boosts Water spell accuracy and damage by 20%")
add_item(28690, "Tome of earth", "SHIELD", "MAGIC", price=2500000, tier=5,
         req_skills={"MAGIC": 50}, magic_atk=8, magic_def=8,
         special_effect="Provides unlimited earth runes; boosts Earth spell accuracy and damage by 10%")
add_item(12610, "Book of law", "SHIELD", "RANGED", price=150000, tier=3,
         req_skills={}, ranged_atk=10, prayer=5,
         special_effect="God book of Armadyl: +10 Ranged attack and +5 Prayer bonus for budget ranged setups")
add_item(12612, "Book of darkness", "SHIELD", "MAGIC", price=120000, tier=3,
         req_skills={}, magic_atk=10, prayer=5,
         special_effect="God book of Zaros: +10 Magic attack and +5 Prayer bonus for budget magic setups")
add_item(3842, "Unholy book", "SHIELD", "MELEE", price=100000, tier=2,
         req_skills={}, stab_atk=8, slash_atk=8, crush_atk=8, magic_atk=8, ranged_atk=8, prayer=5,
         special_effect="Zamorak God book: +8 offensive bonus across all attack styles and +5 Prayer")

# ==============================================================================
# 6. HELMETS
# ==============================================================================
add_item(10828, "Helm of neitiznot", "HEAD", "MELEE", price=50000, tier=4,
         req_skills={"DEFENCE": 55}, quest_req="The Fremennik Isles", prayer=3, melee_str=3,
         stab_def=31, slash_def=29, crush_def=34, magic_def=3, ranged_def=30,
         special_effect="Excellent mid-game melee helm with +3 Strength and +3 Prayer bonus; no negative attack bonuses")
add_item(24271, "Neitiznot faceguard", "HEAD", "MELEE", price=22000000, tier=7,
         req_skills={"DEFENCE": 70}, quest_req="The Fremennik Exiles", prayer=3, melee_str=6,
         stab_def=36, slash_def=34, crush_def=38, magic_def=3, ranged_def=34,
         special_effect="Premier general melee helm with +6 Melee Strength and +3 Prayer; no negative attack penalties")
add_item(26382, "Torva full helm", "HEAD", "MELEE", price=360000000, tier=10,
         req_skills={"DEFENCE": 80}, melee_str=8,
         stab_def=68, slash_def=65, crush_def=63, magic_def=-5, ranged_def=65,
         special_effect="Best-in-slot melee helmet: +8 Melee Strength and immense defensive stats")
add_item(11864, "Slayer helmet (i)", "HEAD", "MELEE", price=1500000, tier=6, untradeable=True, slayer_bonus=True,
         req_skills={"DEFENCE": 10},
         stab_def=30, slash_def=32, crush_def=27, magic_def=-1, ranged_def=30,
         special_effect="Imbued Slayer helm: +16.67% melee accuracy & damage, +15% ranged & magic accuracy & damage on Slayer task")
add_item(11865, "Black mask (i)", "HEAD", "MELEE", price=850000, tier=4, untradeable=True, slayer_bonus=True,
         req_skills={"DEFENCE": 10},
         special_effect="Imbued Black mask: +16.67% melee boost, +15% ranged and magic boost on Slayer task")
add_item(11665, "Void melee helm", "HEAD", "MELEE", price=50000, tier=4, untradeable=True, void_knight=True,
         req_skills={"DEFENCE": 42},
         special_effect="Void Knight set helm: Grants +10% Melee accuracy and +10% Melee damage with full Void set")
add_item(11664, "Void ranger helm", "HEAD", "RANGED", price=50000, tier=4, untradeable=True, void_knight=True,
         req_skills={"DEFENCE": 42},
         special_effect="Void Knight set helm: Grants +10% Ranged accuracy and +10% (+12.5% Elite) damage with full Void set")
add_item(11663, "Void mage helm", "HEAD", "MAGIC", price=50000, tier=4, untradeable=True, void_knight=True,
         req_skills={"DEFENCE": 42},
         special_effect="Void Knight set helm: Grants +45% Magic accuracy with full Void set")
add_item(26235, "Masori mask (f)", "HEAD", "RANGED", price=95000000, tier=10,
         req_skills={"DEFENCE": 80, "RANGED": 80}, ranged_atk=12, ranged_str=4,
         stab_def=20, slash_def=18, crush_def=22, magic_def=6, ranged_def=16,
         special_effect="Best-in-slot fortified ranged helmet from ToA: +12 Ranged accuracy and +4 Ranged strength")
add_item(21018, "Ancestral hat", "HEAD", "MAGIC", price=85000000, tier=9,
         req_skills={"DEFENCE": 65, "MAGIC": 75}, magic_atk=8, magic_dmg=2,
         stab_def=12, slash_def=10, crush_def=14, magic_def=15,
         special_effect="Best-in-slot magic hat from Chambers of Xeric: +8 Magic attack and +2% Magic damage")
add_item(28254, "Virtus mask", "HEAD", "MAGIC", price=45000000, tier=8,
         req_skills={"DEFENCE": 75, "MAGIC": 75}, magic_atk=6, magic_dmg=1, prayer=1,
         stab_def=14, slash_def=12, crush_def=16, magic_def=18,
         special_effect="Ancient Magicks helmet: +4% Ancient Magicks spell damage and +1% general magic damage")
add_item(23971, "Crystal helm", "HEAD", "RANGED", price=12000000, tier=7, untradeable=True,
         req_skills={"DEFENCE": 70}, ranged_atk=9, prayer=1,
         stab_def=16, slash_def=14, crush_def=18, magic_def=4, ranged_def=14,
         special_effect="Crystal armour helm: Boosts Bowfa accuracy by +5% and damage by +2.5%")
add_item(12931, "Serpentine helm", "HEAD", "MELEE", price=4200000, tier=6, venom_prot=True,
         req_skills={"DEFENCE": 75}, melee_str=5, stab_atk=-5, slash_atk=-5, crush_atk=-5, magic_atk=-5, ranged_atk=-5,
         stab_def=52, slash_def=55, crush_def=54, magic_def=0, ranged_def=50,
         special_effect="Complete immunity to poison and venom; 1/6 chance to inflict venom on hit (Zulrah & Vorkath staple)")

# ==============================================================================
# 7. BODY ARMOUR
# ==============================================================================
add_item(10551, "Fighter torso", "BODY", "MELEE", price=200000, tier=4, untradeable=True,
         req_skills={"DEFENCE": 40}, melee_str=4,
         stab_def=85, slash_def=85, crush_def=85, magic_def=0, ranged_def=80,
         special_effect="Barbarian Assault reward: Grants +4 Melee Strength equal to Bandos chestplate at 40 Defence")
add_item(11832, "Bandos chestplate", "BODY", "MELEE", price=28000000, tier=7,
         req_skills={"DEFENCE": 65}, melee_str=4, prayer=1,
         stab_def=98, slash_def=93, crush_def=105, magic_def=-6, ranged_def=133,
         special_effect="Premier mid-to-high tier melee body with +4 Melee Strength, high defence, and +1 Prayer")
add_item(26384, "Torva platebody", "BODY", "MELEE", price=520000000, tier=10,
         req_skills={"DEFENCE": 80}, melee_str=6,
         stab_def=135, slash_def=128, crush_def=125, magic_def=-10, ranged_def=145,
         special_effect="Best-in-slot melee chestplate: +6 Melee Strength and exceptional defensive stats")
add_item(9672, "Proselyte hauberk", "BODY", "MELEE", price=15000, tier=2,
         req_skills={"DEFENCE": 30}, quest_req="The Slug Menace", prayer=8,
         stab_def=44, slash_def=37, crush_def=26, magic_def=-11, ranged_def=40,
         special_effect="Highest prayer-bonus-per-slot body armour (+8 Prayer); essential for AFK and Slayer prayer builds")
add_item(2503, "Black d'hide body", "BODY", "RANGED", price=8000, tier=3,
         req_skills={"RANGED": 70, "DEFENCE": 40}, ranged_atk=15,
         stab_def=28, slash_def=15, crush_def=35, magic_def=45, ranged_def=50,
         special_effect="Exceptional budget ranged body with immense +45 Magic defence")
add_item(11838, "Saradomin d'hide body", "BODY", "RANGED", price=900000, tier=5,
         req_skills={"RANGED": 70, "DEFENCE": 40}, ranged_atk=15, prayer=1,
         stab_def=28, slash_def=15, crush_def=35, magic_def=45, ranged_def=50,
         special_effect="Blessed dragonhide body: +15 Ranged attack and +1 Prayer bonus with zero negative melee attack stats")
add_item(23975, "Crystal body", "BODY", "RANGED", price=35000000, tier=8, untradeable=True,
         req_skills={"DEFENCE": 70}, ranged_atk=25, prayer=3,
         stab_def=48, slash_def=42, crush_def=52, magic_def=35, ranged_def=48,
         special_effect="Crystal body: Boosts Bow of faerdhinen accuracy by +15% and damage by +7.5%")
add_item(26237, "Masori body (f)", "BODY", "RANGED", price=180000000, tier=10,
         req_skills={"DEFENCE": 80, "RANGED": 80}, ranged_atk=30, ranged_str=4,
         stab_def=65, slash_def=58, crush_def=68, magic_def=45, ranged_def=55,
         special_effect="Best-in-slot fortified ranged chestplate: +30 Ranged accuracy and +4 Ranged strength")
add_item(4091, "Mystic robe top", "BODY", "MAGIC", price=120000, tier=3,
         req_skills={"MAGIC": 40, "DEFENCE": 20}, magic_atk=20,
         stab_def=0, slash_def=0, crush_def=0, magic_def=20, ranged_def=0,
         special_effect="Standard budget magic robe top for burst/barrage tasks and early bossing")
add_item(4712, "Ahrim's robetop", "BODY", "MAGIC", price=4200000, tier=6,
         req_skills={"MAGIC": 70, "DEFENCE": 70}, magic_atk=30, magic_dmg=1,
         stab_def=42, slash_def=32, crush_def=52, magic_def=30, ranged_def=0,
         special_effect="Barrows magic robetop: High magic attack and +1% magic damage")
add_item(21021, "Ancestral robe top", "BODY", "MAGIC", price=175000000, tier=10,
         req_skills={"DEFENCE": 65, "MAGIC": 75}, magic_atk=35, magic_dmg=2,
         stab_def=35, slash_def=28, crush_def=42, magic_def=42,
         special_effect="Best-in-slot magic chest piece from Chambers of Xeric: +35 Magic attack and +2% Magic damage")
add_item(28256, "Virtus robe top", "BODY", "MAGIC", price=95000000, tier=8,
         req_skills={"DEFENCE": 75, "MAGIC": 75}, magic_atk=31, magic_dmg=1, prayer=1,
         stab_def=40, slash_def=32, crush_def=48, magic_def=50,
         special_effect="Virtus body: Boosts Ancient Magicks damage by +4% and general magic damage by +1%")
add_item(8839, "Void knight top", "BODY", "MELEE", price=100000, tier=4, untradeable=True, void_knight=True,
         req_skills={"DEFENCE": 42},
         stab_def=45, slash_def=45, crush_def=45, magic_def=45, ranged_def=45,
         special_effect="Void Knight top: Required for Void set bonus")
add_item(13072, "Elite void top", "BODY", "MELEE", price=250000, tier=6, untradeable=True, void_knight=True,
         req_skills={"DEFENCE": 42}, prayer=3,
         stab_def=45, slash_def=45, crush_def=45, magic_def=45, ranged_def=45,
         special_effect="Elite void top: Upgraded Void with +3 Prayer; increases Ranged set damage to 12.5% and Magic damage to 2.5%")

# ==============================================================================
# 8. LEG ARMOUR
# ==============================================================================
add_item(11834, "Bandos tassets", "LEGS", "MELEE", price=22000000, tier=7,
         req_skills={"DEFENCE": 65}, melee_str=2, prayer=1,
         stab_def=71, slash_def=70, crush_def=66, magic_def=-21, ranged_def=63,
         special_effect="Premier melee tassets with +2 Melee Strength and +1 Prayer bonus")
add_item(26386, "Torva platelegs", "LEGS", "MELEE", price=460000000, tier=10,
         req_skills={"DEFENCE": 80}, melee_str=4,
         stab_def=112, slash_def=105, crush_def=100, magic_def=-10, ranged_def=120,
         special_effect="Best-in-slot melee platelegs: +4 Melee Strength and extreme defensive bonuses")
add_item(9674, "Proselyte cuisse", "LEGS", "MELEE", price=12000, tier=2,
         req_skills={"DEFENCE": 30}, quest_req="The Slug Menace", prayer=6,
         stab_def=33, slash_def=28, crush_def=20, magic_def=-8, ranged_def=30,
         special_effect="+6 Prayer bonus; premier prayer legs for Slayer and endurance PvM")
add_item(2501, "Black d'hide chaps", "LEGS", "RANGED", price=4000, tier=3,
         req_skills={"RANGED": 70}, ranged_atk=8,
         stab_def=17, slash_def=11, crush_def=22, magic_def=28, ranged_def=31,
         special_effect="Classic budget ranged legs with great magic defence")
add_item(11840, "Saradomin d'hide chaps", "LEGS", "RANGED", price=750000, tier=5,
         req_skills={"RANGED": 70, "DEFENCE": 1}, ranged_atk=8, prayer=1,
         stab_def=17, slash_def=11, crush_def=22, magic_def=28, ranged_def=31,
         special_effect="Blessed dragonhide chaps: +8 Ranged attack and +1 Prayer with no defence requirement")
add_item(23979, "Crystal legs", "LEGS", "RANGED", price=25000000, tier=8, untradeable=True,
         req_skills={"DEFENCE": 70}, ranged_atk=17, prayer=2,
         stab_def=36, slash_def=32, crush_def=39, magic_def=26, ranged_def=36,
         special_effect="Crystal legs: Boosts Bow of faerdhinen accuracy by +10% and damage by +5%")
add_item(26239, "Masori chaps (f)", "LEGS", "RANGED", price=140000000, tier=10,
         req_skills={"DEFENCE": 80, "RANGED": 80}, ranged_atk=20, ranged_str=2,
         stab_def=48, slash_def=43, crush_def=50, magic_def=33, ranged_def=41,
         special_effect="Best-in-slot fortified ranged legs: +20 Ranged attack and +2 Ranged strength")
add_item(4093, "Mystic robe bottom", "LEGS", "MAGIC", price=80000, tier=3,
         req_skills={"MAGIC": 40, "DEFENCE": 20}, magic_atk=15, magic_def=15,
         special_effect="Standard budget magic robe bottom for burst tasks")
add_item(4714, "Ahrim's robeskirt", "LEGS", "MAGIC", price=3200000, tier=6,
         req_skills={"MAGIC": 70, "DEFENCE": 70}, magic_atk=22, magic_dmg=1,
         stab_def=30, slash_def=23, crush_def=37, magic_def=22,
         special_effect="Barrows magic skirt with +22 Magic attack and +1% magic damage")
add_item(21024, "Ancestral robe bottom", "LEGS", "MAGIC", price=145000000, tier=10,
         req_skills={"DEFENCE": 65, "MAGIC": 75}, magic_atk=26, magic_dmg=2,
         stab_def=26, slash_def=21, crush_def=31, magic_def=31,
         special_effect="Best-in-slot magic robe legs from CoX: +26 Magic attack and +2% Magic damage")
add_item(28258, "Virtus robe bottom", "LEGS", "MAGIC", price=75000000, tier=8,
         req_skills={"DEFENCE": 75, "MAGIC": 75}, magic_atk=23, magic_dmg=1, prayer=1,
         stab_def=30, slash_def=24, crush_def=36, magic_def=38,
         special_effect="Virtus legs: Boosts Ancient Magicks damage by +4% and general magic damage by +1%")
add_item(8840, "Void knight robe", "LEGS", "MELEE", price=100000, tier=4, untradeable=True, void_knight=True,
         req_skills={"DEFENCE": 42},
         stab_def=30, slash_def=30, crush_def=30, magic_def=30, ranged_def=30,
         special_effect="Void Knight robe: Required for Void set bonus")
add_item(13073, "Elite void robe", "LEGS", "MELEE", price=250000, tier=6, untradeable=True, void_knight=True,
         req_skills={"DEFENCE": 42}, prayer=3,
         stab_def=30, slash_def=30, crush_def=30, magic_def=30, ranged_def=30,
         special_effect="Elite void robe: Upgraded Void with +3 Prayer bonus")

# ==============================================================================
# 9. BOOTS
# ==============================================================================
add_item(3105, "Climbing boots", "BOOTS", "MELEE", price=1000, tier=1,
         req_skills={"AGILITY": 1}, melee_str=2,
         special_effect="Premier early-game melee boots with +2 Strength")
add_item(11840, "Dragon boots", "BOOTS", "MELEE", price=150000, tier=5,
         req_skills={"DEFENCE": 60}, melee_str=4,
         stab_def=16, slash_def=17, crush_def=18, ranged_def=16,
         special_effect="Standard mid-level melee boots with +4 Strength and solid defence")
add_item(13239, "Primordial boots", "BOOTS", "MELEE", price=32000000, tier=8,
         req_skills={"DEFENCE": 75, "STRENGTH": 75}, melee_str=5, stab_atk=2, slash_atk=2, crush_atk=2,
         stab_def=22, slash_def=23, crush_def=24, ranged_def=22,
         special_effect="Best-in-slot melee offensive boots with +5 Strength and +2 melee accuracy")
add_item(19937, "Blessed d'hide boots", "BOOTS", "RANGED", price=850000, tier=5,
         req_skills={"RANGED": 70, "DEFENCE": 40}, ranged_atk=7, prayer=1,
         stab_def=5, slash_def=6, crush_def=7, magic_def=5, ranged_def=5,
         special_effect="Top-value ranged boots: +7 Ranged attack and +1 Prayer bonus")
add_item(13237, "Pegasian boots", "BOOTS", "RANGED", price=36000000, tier=8,
         req_skills={"DEFENCE": 75, "RANGED": 75}, ranged_atk=12,
         stab_def=12, slash_def=13, crush_def=14, magic_def=12, ranged_def=12,
         special_effect="Best-in-slot ranged accuracy boots with +12 Ranged attack")
add_item(13235, "Eternal boots", "BOOTS", "MAGIC", price=8500000, tier=7,
         req_skills={"DEFENCE": 75, "MAGIC": 75}, magic_atk=8, magic_dmg=1,
         stab_def=5, slash_def=5, crush_def=5, magic_def=8, ranged_def=5,
         special_effect="Best-in-slot magic boots with +8 Magic attack and +1% Magic damage")
add_item(22954, "Devout boots", "BOOTS", "HYBRID", price=1800000, tier=6,
         req_skills={"PRAYER": 60}, prayer=5,
         stab_def=5, slash_def=5, crush_def=5, magic_def=5, ranged_def=5,
         special_effect="Highest prayer bonus boots in OSRS (+5 Prayer); optimal for Inferno and Slayer prayer setups")
add_item(23037, "Boots of brimstone", "BOOTS", "MELEE", price=250000, tier=5, boots_of_stone=True,
         req_skills={"DEFENCE": 44, "MAGIC": 44, "RANGED": 44}, stab_atk=3, slash_atk=3, crush_atk=3, magic_atk=3, ranged_atk=3,
         melee_str=3, stab_def=11, slash_def=12, crush_def=13, magic_def=5, ranged_def=11,
         special_effect="Hybrid boots with protection against Mount Karuulm heat floor (Hydra, Drake, Wyrm requirement)")

# ==============================================================================
# 10. GLOVES
# ==============================================================================
add_item(7462, "Barrows gloves", "GLOVES", "MELEE", price=100000, tier=6, untradeable=True,
         req_skills={"DEFENCE": 40}, quest_req="Recipe for Disaster",
         stab_atk=12, slash_atk=12, crush_atk=12, magic_atk=6, ranged_atk=12, melee_str=12,
         stab_def=12, slash_def=12, crush_def=12, magic_def=6, ranged_def=12,
         special_effect="Best versatile mid-to-endgame hybrid gloves in OSRS; +12 Strength and +12 all styles")
add_item(22981, "Ferocious gloves", "GLOVES", "MELEE", price=7500000, tier=8,
         req_skills={"ATTACK": 80, "DEFENCE": 80}, quest_req="Dragon Slayer II",
         stab_atk=16, slash_atk=16, crush_atk=16, melee_str=14, magic_atk=-16, ranged_atk=-16,
         special_effect="Best-in-slot melee offensive gloves with +14 Melee Strength and +16 Melee accuracy")
add_item(26229, "Zaryte vambraces", "GLOVES", "RANGED", price=210000000, tier=10,
         req_skills={"DEFENCE": 80, "RANGED": 80}, ranged_atk=18, ranged_str=2, prayer=2,
         special_effect="Best-in-slot ranged gloves with +18 Ranged accuracy, +2 Ranged strength, and +2 Prayer")
add_item(19544, "Tormented bracelet", "GLOVES", "MAGIC", price=14000000, tier=8,
         req_skills={"HITPOINTS": 75}, magic_atk=10, magic_dmg=5,
         special_effect="Best-in-slot magic bracelet with +10 Magic attack and massive +5% Magic damage")
add_item(777, "Chaos gauntlets", "GLOVES", "MAGIC", price=25000, tier=2, untradeable=True,
         req_skills={}, quest_req="Family Crest",
         special_effect="Family Crest reward: Increases Bolt spell damage by +3 (empowers early-game Magic training & Zulrah)")

# ==============================================================================
# 11. CAPES
# ==============================================================================
add_item(6570, "Fire cape", "CAPE", "MELEE", price=100000, tier=6, untradeable=True,
         req_skills={}, melee_str=4, prayer=2, stab_atk=1, slash_atk=1, crush_atk=1, magic_atk=1, ranged_atk=1,
         stab_def=11, slash_def=11, crush_def=11, magic_def=11, ranged_def=11,
         special_effect="Iconic TzHaar Fight Caves reward: +4 Melee Strength and +2 Prayer; standard PvM cape")
add_item(21295, "Infernal cape", "CAPE", "MELEE", price=5000000, tier=10, untradeable=True,
         req_skills={}, melee_str=8, prayer=2, stab_atk=4, slash_atk=4, crush_atk=4, magic_atk=1, ranged_atk=1,
         stab_def=12, slash_def=12, crush_def=12, magic_def=12, ranged_def=12,
         special_effect="Ultimate melee cape in OSRS: +8 Melee Strength, +4 accuracy, and +2 Prayer")
add_item(22109, "Ava's assembler", "CAPE", "RANGED", price=100000, tier=8, untradeable=True,
         req_skills={"RANGED": 70}, quest_req="Dragon Slayer II", ranged_atk=8, ranged_str=2,
         stab_def=1, slash_def=1, crush_def=1, magic_def=8, ranged_def=2,
         special_effect="Best-in-slot ranged cape: 80% ammo retrieval, +8 Ranged attack, and +2 Ranged strength")
add_item(10499, "Ava's accumulator", "CAPE", "RANGED", price=10000, tier=3, untradeable=True,
         req_skills={"RANGED": 50}, quest_req="Animal Magnetism", ranged_atk=4,
         special_effect="72% ammo retrieval and +4 Ranged attack; essential budget ranged device")
add_item(21791, "Imbued saradomin cape", "CAPE", "MAGIC", price=100000, tier=7, untradeable=True,
         req_skills={"MAGIC": 75}, magic_atk=15, magic_dmg=2, prayer=2,
         special_effect="Mage Arena II cape: +15 Magic attack and +2% Magic damage; Best-in-slot magic cape")
add_item(22114, "Mythical cape", "CAPE", "MELEE", price=10000, tier=4, untradeable=True,
         req_skills={"ATTACK": 40}, quest_req="Dragon Slayer II", crush_atk=6, melee_str=1, prayer=1,
         special_effect="Crush-focused cape with +6 Crush attack; BiS budget crush cape for Calvar'ion/Sarachnis")

# ==============================================================================
# 12. AMULETS & NECKLACES
# ==============================================================================
add_item(1712, "Amulet of glory", "AMULET", "MELEE", price=12000, tier=2,
         req_skills={}, stab_atk=10, slash_atk=10, crush_atk=10, magic_atk=10, ranged_atk=10, melee_str=6, prayer=3,
         stab_def=3, slash_def=3, crush_def=3, magic_def=3, ranged_def=3,
         special_effect="Classic early-game amulet; +10 offensive bonus in all combat styles and +6 Strength")
add_item(6585, "Amulet of fury", "AMULET", "MELEE", price=2800000, tier=5,
         req_skills={}, stab_atk=10, slash_atk=10, crush_atk=10, magic_atk=10, ranged_atk=10, melee_str=8, prayer=5,
         stab_def=15, slash_def=15, crush_def=15, magic_def=15, ranged_def=15,
         special_effect="High defense and versatile hybrid amulet: +8 Strength, +15 all defence, and +5 Prayer")
add_item(19553, "Amulet of torture", "AMULET", "MELEE", price=13000000, tier=8,
         req_skills={"HITPOINTS": 75}, stab_atk=15, slash_atk=15, crush_atk=15, melee_str=10, prayer=2,
         special_effect="Best-in-slot melee offensive amulet: +10 Melee Strength and +15 Melee accuracy")
add_item(19547, "Necklace of anguish", "AMULET", "RANGED", price=13500000, tier=8,
         req_skills={"HITPOINTS": 75}, ranged_atk=15, ranged_str=5, prayer=2,
         special_effect="Best-in-slot ranged amulet: +15 Ranged accuracy and +5 Ranged strength")
add_item(12002, "Occult necklace", "AMULET", "MAGIC", price=900000, tier=6,
         req_skills={"MAGIC": 70}, magic_atk=12, magic_dmg=4,
         special_effect="Premier magic necklace with +12 Magic attack and +4% Magic damage bonus")
add_item(12018, "Salve amulet (ei)", "AMULET", "MELEE", price=100000, tier=8, untradeable=True, undead_bonus=True,
         req_skills={}, quest_req="Haunted Mine", prayer=3,
         special_effect="Imbued enchanted Salve: +20% accuracy AND +20% damage for ALL styles against undead (Vorkath & Calvar'ion BiS)")
add_item(24780, "Amulet of blood fury", "AMULET", "MELEE", price=18000000, tier=8,
         req_skills={}, stab_atk=10, slash_atk=10, crush_atk=10, magic_atk=10, ranged_atk=10, melee_str=8, prayer=5,
         special_effect="Fury with blood shard: 20% chance on melee hit to heal 30% of damage dealt (endurance bossing BiS)")

# ==============================================================================
# 13. RINGS
# ==============================================================================
add_item(11773, "Berserker ring (i)", "RING", "MELEE", price=4500000, tier=6, untradeable=True,
         req_skills={}, melee_str=8,
         stab_def=4, slash_def=4, crush_def=4, magic_def=4, ranged_def=4,
         special_effect="Imbued Berserker ring: +8 Melee Strength bonus; premier mid-to-high melee DPS ring")
add_item(28301, "Ultor ring", "RING", "MELEE", price=155000000, tier=10,
         req_skills={}, melee_str=12,
         special_effect="Best-in-slot melee strength ring from Desert Treasure II: +12 Melee Strength")
add_item(11771, "Archers ring (i)", "RING", "RANGED", price=4800000, tier=6, untradeable=True,
         req_skills={}, ranged_atk=8,
         special_effect="Imbued Archers ring: +8 Ranged attack bonus")
add_item(28304, "Venator ring", "RING", "RANGED", price=62000000, tier=9,
         req_skills={}, ranged_atk=10, ranged_str=2,
         special_effect="Best-in-slot ranged ring from DT2: +10 Ranged attack and +2 Ranged strength")
add_item(11770, "Seers ring (i)", "RING", "MAGIC", price=1200000, tier=5, untradeable=True,
         req_skills={}, magic_atk=12,
         special_effect="Imbued Seers ring: +12 Magic attack bonus")
add_item(28307, "Magus ring", "RING", "MAGIC", price=78000000, tier=9,
         req_skills={}, magic_atk=15, magic_dmg=2,
         special_effect="Best-in-slot magic ring from DT2: +15 Magic attack and +2% Magic damage")
add_item(28310, "Bellator ring", "RING", "MELEE", price=72000000, tier=9,
         req_skills={}, slash_atk=20, melee_str=6,
         special_effect="Slash accuracy specialist ring from DT2: +20 Slash accuracy and +6 Melee Strength (Vardorvis BiS)")
add_item(25975, "Lightbearer", "RING", "MELEE", price=2800000, tier=7,
         req_skills={},
         special_effect="Tombs of Amascut ring: Doubles special attack regeneration rate (recovers 10% spec energy every 15 seconds)")
add_item(19550, "Ring of suffering (i)", "RING", "MELEE", price=14000000, tier=7, untradeable=True, prayer=4,
         req_skills={},
         stab_def=20, slash_def=20, crush_def=20, magic_def=20, ranged_def=20,
         special_effect="Imbued Ring of suffering: +20 all defences, +4 Prayer, stores ring of recoil charges for automated recoil damage (Zulrah BiS)")

# ==============================================================================
# 14. AMMUNITION & BLESSINGS
# ==============================================================================
add_item(9242, "Ruby bolts (e)", "AMMO", "RANGED", ammo_type="BOLT", price=800, tier=5,
         req_skills={"RANGED": 61}, ranged_str=103,
         special_effect="Blood Sacrifice: 6% chance (6.6% with Kandarin hard) to deal 20% of target current HP (up to 100 damage)")
add_item(9244, "Dragon ruby bolts (e)", "AMMO", "RANGED", ammo_type="BOLT", price=1600, tier=7,
         req_skills={"RANGED": 64}, ranged_str=122,
         special_effect="Premier dragon ruby bolts: Deals 20% of target current HP (up to 100/110 with ZCB) with +122 Ranged strength")
add_item(9243, "Diamond bolts (e)", "AMMO", "RANGED", ammo_type="BOLT", price=450, tier=5,
         req_skills={"RANGED": 61}, ranged_str=105,
         special_effect="Armour Piercing: 10% chance to ignore 100% of target defence and increase max hit by 15%")
add_item(9245, "Dragon diamond bolts (e)", "AMMO", "RANGED", ammo_type="BOLT", price=1400, tier=7,
         req_skills={"RANGED": 64}, ranged_str=122,
         special_effect="Premier dragon diamond bolts: Ignores 100% of target defence with +122 Ranged strength")
add_item(21326, "Amethyst arrows", "AMMO", "RANGED", ammo_type="ARROW", price=180, tier=6,
         req_skills={"RANGED": 50}, ranged_str=55,
         special_effect="High-grade arrows with +55 Ranged strength for Bows (MSB(i), Venator, Dark bow)")
add_item(11212, "Dragon arrows", "AMMO", "RANGED", ammo_type="ARROW", price=1400, tier=8,
         req_skills={"RANGED": 60}, ranged_str=60,
         special_effect="Best-in-slot arrows with +60 Ranged strength; mandatory ammunition for the Twisted bow")
add_item(25867, "Dragon dart", "AMMO", "RANGED", ammo_type="DART", price=1800, tier=8,
         req_skills={"RANGED": 60}, ranged_str=35,
         special_effect="Best-in-slot darts for the Toxic blowpipe with +35 Ranged strength")
add_item(25869, "Amethyst dart", "AMMO", "RANGED", ammo_type="DART", price=250, tier=6,
         req_skills={"RANGED": 50}, ranged_str=28,
         special_effect="Top value darts for the Toxic blowpipe with +28 Ranged strength")
add_item(20220, "Peace blessing", "AMMO", "MELEE", ammo_type="BLESSING", price=15000, tier=2, prayer=1,
         req_skills={},
         special_effect="God blessing: +1 Prayer bonus in ammunition slot when not using ammo")
add_item(22947, "Rada's blessing 4", "AMMO", "MELEE", ammo_type="BLESSING", price=100000, tier=6, untradeable=True, prayer=2,
         req_skills={}, quest_req="Kourend & Kebos Elite Diary",
         special_effect="Elite Kourend Diary reward: +2 Prayer bonus in ammunition slot and teleports to Mount Karuulm")


# ==========================================
# EXPANSION ITEMS FOR 75-MONSTER WIKI STRATEGIES
# ==========================================

# 1. Mandatory Protection Shields
add_item(4156, "Mirror shield", "SHIELD", "MELEE", price=5000, tier=2,
         req_skills={"DEFENCE": 20, "SLAYER": 25}, stab_def=10, slash_def=12, crush_def=10, ranged_def=10, magic_def=-2,
         mirror_shield=True, special_effect="Mandatory protection against cockatrices and basilisks gaze")

add_item(24266, "V's shield", "SHIELD", "MELEE", price=150000, tier=5,
         req_skills={"DEFENCE": 60}, quest_req="The Fremennik Exiles",
         stab_def=48, slash_def=46, crush_def=44, ranged_def=48, magic_def=-4, melee_str=0, prayer=1,
         mirror_shield=True, special_effect="Superior mirror shield for Basilisk Knights with enhanced defences")

add_item(12821, "Spectral spirit shield", "SHIELD", "MAGIC", price=45000000, tier=8,
         req_skills={"DEFENCE": 75, "PRAYER": 70, "MAGIC": 65},
         stab_def=53, slash_def=55, crush_def=73, magic_def=30, ranged_def=52, prayer=3,
         special_effect="Spectral Ward: Reduces prayer drain from supernatural attacks by 50% (Cerberus ghosts)")

# 2. Mandatory Footwear & Monster Tools
add_item(22951, "Boots of stone", "BOOTS", "MELEE", price=12000, tier=3,
         req_skills={"DEFENCE": 44, "SLAYER": 77}, stab_def=7, slash_def=7, crush_def=7, ranged_def=7, melee_str=1,
         boots_of_stone=True, special_effect="Protects against the scorching caustic floor of Mount Karuulm Slayer Dungeon")

add_item(7159, "Insulated boots", "BOOTS", "MELEE", price=1500, tier=2,
         req_skills={}, stab_def=2, slash_def=2, crush_def=2, ranged_def=2,
         special_effect="Insulated rubber soles protect against Rune Dragon lightning attacks and killerawatts")

add_item(4162, "Rock hammer", "WEAPON", "MELEE", attack_type="CRUSH", price=500, tier=1, speed=4,
         req_skills={"SLAYER": 75}, crush_atk=10, melee_str=8,
         special_effect="Mandatory tool to deliver the crushing death blow to Gargoyles and Grotesque Guardians")

add_item(21742, "Granite hammer", "WEAPON", "MELEE", attack_type="CRUSH", price=450000, tier=4, speed=4,
         req_skills={"ATTACK": 50, "STRENGTH": 50}, crush_atk=56, melee_str=68,
         has_spec=True, spec_energy=60, spec_effect="Overpower: +50% accuracy and +5 minimum hit; automatically auto-smashes gargoyles")

# 3. Special Weapons
add_item(22325, "Scythe of vitur", "WEAPON", "MELEE", attack_type="SLASH", price=450000000, tier=9, speed=5, two_handed=True,
         req_skills={"ATTACK": 80}, slash_atk=110, crush_atk=30, melee_str=75,
         special_effect="Sweeping Strike: Hits up to 3 separate times against large 2x2 and 3x3 monsters (100%/50%/25%)")

add_item(13092, "Crystal halberd", "WEAPON", "MELEE", attack_type="SLASH", price=400000, tier=5, speed=7, two_handed=True,
         req_skills={"ATTACK": 70, "AGILITY": 50}, quest_req="Roving Elves", slash_atk=85, stab_atk=85, melee_str=118, prayer=1,
         has_spec=True, spec_energy=30, spec_effect="Sweep: Strikes large monsters twice in one devastating swing (ToB/Corp staple)")

add_item(11959, "Black chinchompa", "WEAPON", "RANGED", attack_type="RANGED", ammo_type="CHINCHOMPA", price=3500, tier=7, speed=3, two_handed=False,
         req_skills={"RANGED": 65}, ranged_atk=80,
         special_effect="Explosive AoE: Explodes in a 3x3 area hitting up to 9 targets at once (Kree'arra minion method)")

add_item(1409, "Iban's staff", "WEAPON", "MAGIC", attack_type="MAGIC", price=200000, tier=4, speed=5,
         req_skills={"MAGIC": 50, "ATTACK": 50}, quest_req="Underground Pass", magic_atk=10, magic_def=10,
         special_effect="Iban Blast: Premier mid-game combat spell dealing up to 25 base magic damage (Barrows killer)")

add_item(27299, "Ancient sceptre", "WEAPON", "MAGIC", attack_type="MAGIC", price=1800000, tier=6, speed=4,
         req_skills={"MAGIC": 70, "ATTACK": 60}, quest_req="Desert Treasure I", magic_atk=20, melee_str=50, magic_dmg=5, prayer=2,
         special_effect="Ancient Enhancement: Increases the effectiveness of all Ancient Magicks combat spells by 10%")

add_item(1215, "Dragon dagger", "WEAPON", "MELEE", attack_type="SLASH", price=25000, tier=4, speed=4,
         req_skills={"ATTACK": 60}, quest_req="Lost City", stab_atk=40, slash_atk=25, melee_str=40,
         has_spec=True, spec_energy=25, spec_effect="Puncture: Double-hit special attack consuming only 25% energy")

# 4. Barrows Sets (Dharok, Karil, Verac)
add_item(4716, "Dharok's helm", "HEAD", "MELEE", price=850000, tier=5,
         req_skills={"DEFENCE": 70}, stab_def=45, slash_def=48, crush_def=44, ranged_def=51)

add_item(4720, "Dharok's platebody", "BODY", "MELEE", price=1200000, tier=5,
         req_skills={"DEFENCE": 70}, stab_def=122, slash_def=120, crush_def=105, ranged_def=132)

add_item(4722, "Dharok's platelegs", "LEGS", "MELEE", price=1100000, tier=5,
         req_skills={"DEFENCE": 70}, stab_def=85, slash_def=82, crush_def=70, ranged_def=92)

add_item(4718, "Dharok's greataxe", "WEAPON", "MELEE", attack_type="SLASH", price=1400000, tier=5, speed=7, two_handed=True,
         req_skills={"ATTACK": 70, "STRENGTH": 70}, slash_atk=103, crush_atk=95, melee_str=105,
         special_effect="Wretched Strength: Complete Dharok set deals up to +100% additional damage as player Hitpoints decrease")

add_item(4736, "Karil's leathertop", "BODY", "RANGED", price=2800000, tier=5,
         req_skills={"RANGED": 70, "DEFENCE": 70}, ranged_atk=30, magic_def=65, stab_def=47, slash_def=42, crush_def=60)

add_item(4738, "Karil's leatherskirt", "LEGS", "RANGED", price=1800000, tier=5,
         req_skills={"RANGED": 70, "DEFENCE": 70}, ranged_atk=17, magic_def=35, stab_def=26, slash_def=20, crush_def=34)

add_item(4734, "Karil's crossbow", "WEAPON", "RANGED", attack_type="RANGED", ammo_type="BOLT_RACK", price=400000, tier=5, speed=3, two_handed=True,
         req_skills={"RANGED": 70}, ranged_atk=84, ranged_str=0)

add_item(4753, "Verac's helm", "HEAD", "MELEE", price=650000, tier=5,
         req_skills={"DEFENCE": 70}, stab_def=55, slash_def=58, crush_def=54, ranged_def=56, prayer=3)

add_item(4757, "Verac's brassard", "BODY", "MELEE", price=900000, tier=5,
         req_skills={"DEFENCE": 70}, stab_def=82, slash_def=78, crush_def=70, ranged_def=80, prayer=2)

add_item(4759, "Verac's plateskirt", "LEGS", "MELEE", price=1200000, tier=5,
         req_skills={"DEFENCE": 70}, stab_def=51, slash_def=49, crush_def=47, ranged_def=51, prayer=4)

add_item(4755, "Verac's flail", "WEAPON", "MELEE", attack_type="CRUSH", price=350000, tier=5, speed=5, two_handed=True,
         req_skills={"ATTACK": 70}, crush_atk=82, melee_str=72, prayer=6,
         special_effect="Defiler: Complete Verac set grants a 25% chance to ignore 100% of target protection prayers and defence")

# 5. Moons of Peril Equipment Sets
add_item(28926, "Blood moon chestplate", "BODY", "MELEE", price=3800000, tier=6,
         req_skills={"DEFENCE": 75, "STRENGTH": 75}, melee_str=4, stab_def=75, slash_def=80, crush_def=65)

add_item(28929, "Blood moon tassets", "LEGS", "MELEE", price=3200000, tier=6,
         req_skills={"DEFENCE": 75, "STRENGTH": 75}, melee_str=3, stab_def=52, slash_def=58, crush_def=48)

add_item(28938, "Eclipse moon chestplate", "BODY", "RANGED", price=2900000, tier=6,
         req_skills={"RANGED": 75, "DEFENCE": 75}, ranged_str=2, ranged_atk=28, stab_def=50, slash_def=45, crush_def=55)

add_item(28941, "Eclipse moon tassets", "LEGS", "RANGED", price=2500000, tier=6,
         req_skills={"RANGED": 75, "DEFENCE": 75}, ranged_str=1, ranged_atk=16, stab_def=35, slash_def=30, crush_def=38)

add_item(28949, "Atlatl dart", "AMMO", "RANGED", ammo_type="ATLATL_DART", price=650, tier=6,
         ranged_str=20, special_effect="Ammunition for Eclipse atlatl; scales damage with Melee Strength")

add_item(28952, "Blue moon chestplate", "BODY", "MAGIC", price=2800000, tier=6,
         req_skills={"MAGIC": 75, "DEFENCE": 75}, magic_atk=26, magic_dmg=1, stab_def=40, slash_def=35, crush_def=45)

add_item(28955, "Blue moon tassets", "LEGS", "MAGIC", price=2400000, tier=6,
         req_skills={"MAGIC": 75, "DEFENCE": 75}, magic_atk=17, magic_dmg=1, stab_def=28, slash_def=24, crush_def=32)

add_item(28961, "Blue moon spear", "WEAPON", "MAGIC", attack_type="MAGIC", price=1900000, tier=6, speed=4, two_handed=True, spear=True,
         req_skills={"ATTACK": 75, "MAGIC": 75}, magic_atk=25, stab_atk=85, melee_str=78, magic_dmg=5)

# 6. Standard Mid-Tier & Dragonhide Armor
add_item(10386, "God d'hide body", "BODY", "RANGED", price=45000, tier=4,
         req_skills={"RANGED": 70, "DEFENCE": 40}, ranged_atk=30, magic_def=50, prayer=1, stab_def=40, slash_def=32, crush_def=45)

add_item(10378, "God d'hide chaps", "LEGS", "RANGED", price=35000, tier=4,
         req_skills={"RANGED": 70}, ranged_atk=17, magic_def=28, prayer=1, stab_def=22, slash_def=18, crush_def=25)

add_item(1127, "Rune platebody", "BODY", "MELEE", price=39000, tier=2,
         req_skills={"DEFENCE": 40}, quest_req="Dragon Slayer I", stab_def=82, slash_def=80, crush_def=72, ranged_def=80)

add_item(1079, "Rune platelegs", "LEGS", "MELEE", price=38000, tier=2,
         req_skills={"DEFENCE": 40}, stab_def=51, slash_def=49, crush_def=47, ranged_def=49)

add_item(1163, "Rune full helm", "HEAD", "MELEE", price=21000, tier=2,
         req_skills={"DEFENCE": 40}, stab_def=30, slash_def=32, crush_def=27, ranged_def=30)

add_item(1201, "Rune kiteshield", "SHIELD", "MELEE", price=32000, tier=2,
         req_skills={"DEFENCE": 40}, stab_def=44, slash_def=48, crush_def=46, ranged_def=46)

# 7. Salve Amulets
add_item(4081, "Salve amulet", "AMULET", "MELEE", price=5000, tier=3, untradeable=True,
         req_skills={}, quest_req="Haunted Mine", undead_bonus=True,
         special_effect="+15% Melee accuracy and Melee damage against undead monsters")

add_item(10551, "Salve amulet (e)", "AMULET", "MELEE", price=15000, tier=5, untradeable=True,
         req_skills={}, quest_req="Tarn's Lair", undead_bonus=True,
         special_effect="+20% Melee accuracy and Melee damage against undead monsters")

# 8. Bolt Aliases & Broad Ammunition
add_item(21944, "Ruby dragon bolts (e)", "AMMO", "RANGED", ammo_type="BOLT", price=1600, tier=7,
         req_skills={"RANGED": 64}, ranged_str=122,
         special_effect="Deals 20% of target current HP (up to 100/110 with ZCB) with +122 Ranged strength")

add_item(21946, "Diamond dragon bolts (e)", "AMMO", "RANGED", ammo_type="BOLT", price=1400, tier=7,
         req_skills={"RANGED": 64}, ranged_str=122,
         special_effect="Ignores 100% of target defence with +122 Ranged strength")

add_item(21938, "Sapphire dragon bolts (e)", "AMMO", "RANGED", ammo_type="BOLT", price=1200, tier=7,
         req_skills={"RANGED": 64}, ranged_str=122,
         special_effect="Clear Mind: Smites target prayer/shield for massive damage (Muspah shield phase)")

add_item(11875, "Broad bolts", "AMMO", "RANGED", ammo_type="BOLT", price=60, tier=4,
         req_skills={"RANGED": 55, "SLAYER": 55}, ranged_str=100, leaf_bladed=True,
         special_effect="Bane of Kurasks and Turoths: High-value Slayer ammunition with +100 Ranged strength")

add_item(4160, "Broad arrows", "AMMO", "RANGED", ammo_type="ARROW", price=60, tier=4,
         req_skills={"RANGED": 50, "SLAYER": 55}, ranged_str=28, leaf_bladed=True,
         special_effect="Arrows capable of harming Kurasks and Turoths")

# 9. Additional Wilderness, Prayer, and BiS Additions
add_item(542, "Monk's robe top", "BODY", "MELEE", price=1000, tier=1,
         req_skills={"PRAYER": 31}, prayer=6,
         special_effect="Lightweight prayer robe top (+6 Prayer); staple for Wilderness bosses")

add_item(544, "Monk's robe", "LEGS", "MELEE", price=1000, tier=1,
         req_skills={"PRAYER": 31}, prayer=5,
         special_effect="Lightweight prayer robe bottom (+5 Prayer); staple for Wilderness bosses")

add_item(11118, "Combat bracelet", "GLOVES", "MELEE", price=12000, tier=2,
         stab_atk=7, slash_atk=7, crush_atk=7, magic_atk=3, ranged_atk=7,
         stab_def=5, slash_def=5, crush_def=5, magic_def=3, ranged_def=5, melee_str=6,
         special_effect="Budget non-quest offensive gloves (+6 Melee strength, +7 offensive stats)")

add_item(20701, "Twisted buckler", "SHIELD", "RANGED", price=18000000, tier=8,
         req_skills={"RANGED": 75, "DEFENCE": 75}, ranged_atk=18, ranged_str=10,
         stab_def=18, slash_def=15, crush_def=12, magic_def=10, ranged_def=26,
         special_effect="BiS ranged shield from Chambers of Xeric (+18 Ranged attack, +10 Ranged strength)")

# Write out to data/equipment.json
target_dir = r"C:\Users\Gaige\.gemini\antigravity\scratch\bosswise\src\main\resources\data"
os.makedirs(target_dir, exist_ok=True)
target_file = os.path.join(target_dir, "equipment.json")

with open(target_file, "w", encoding="utf-8") as f:
    json.dump(items, f, indent=2)

print(f"Successfully generated {len(items)} structured OSRS equipment records in {target_file}")
