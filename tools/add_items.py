#!/usr/bin/env python3
"""Adds the three items BOSSWISE was missing, using wiki stats. Idempotent."""
import json, sys

TEMPLATE_FLAGS = dict(dragonbane=False, demonbane=False, ratbane=False, undeadBonus=False,
                      slayerTaskBonus=False, dragonfireProtection=False, venomProtection=False,
                      wildernessBonus=False, spear=False, mirrorShield=False, leafBladed=False,
                      bootsOfStone=False, voidKnight=False, kalphitebane=False)

NEW = [
    dict(itemId=30634, name="Twinflame staff", slot="WEAPON", primaryStyle="MAGIC", attackType="MAGIC",
         weaponCategory="Staff", attackSpeed=6, twoHanded=False, defaultPrice=36000000, tier=8,
         untradeable=False, reqSkills={"MAGIC": 75}, questReq="",
         specialEffect="Casting a standard combat spell fires a second, weaker copy at the target "
                       "(1 tick later). Best on single targets you stay on; no spec attack.",
         wiki="Twinflame staff"),
    dict(itemId=12851, name="Amulet of the damned", slot="AMULET", primaryStyle="HYBRID", attackType="NONE",
         weaponCategory="", attackSpeed=0, twoHanded=False, defaultPrice=0, tier=7,
         untradeable=True, reqSkills={}, questReq="",
         specialEffect="Barrows set effects: Ahrim's +30% chance to lower Strength and +25% spell damage "
                       "on a successful drain, Karil's drains 3 Agility for 30% of hit, Dharok's keeps hitting "
                       "below 1 HP, Guthan's heals 10% above max, Torag's slows, Verac's hits 50% harder on "
                       "its ignore-defence hit. Dungeoneering reward (untradeable).",
         wiki="Amulet of the Damned (Full)"),
    dict(itemId=21140, name="Efaritay's aid", slot="RING", primaryStyle="MELEE", attackType="NONE",
         weaponCategory="", attackSpeed=0, twoHanded=False, defaultPrice=0, tier=5,
         untradeable=True, reqSkills={}, questReq="",
         specialEffect="Lets silver weapons fully damage tier 2 vampyres (Vyrewatch, Vyrewatch Sentinels) "
                       "and doubles silver weapon damage against tier 1. Needed with blisterwood/silver for "
                       "Vyrewatch Sentinel tasks.",
         wiki="Efaritay's aid"),
]

STATS = {
    30634: dict(stabAttack=7, slashAttack=-1, crushAttack=28, magicAttack=12, rangedAttack=0,
                stabDefence=2, slashDefence=3, crushDefence=1, magicDefence=12, rangedDefence=0,
                meleeStrength=22, rangedStrength=0, magicDamagePercent=0, prayerBonus=0),
    12851: dict(stabAttack=10, slashAttack=10, crushAttack=10, magicAttack=10, rangedAttack=10,
                stabDefence=3, slashDefence=3, crushDefence=3, magicDefence=3, rangedDefence=3,
                meleeStrength=6, rangedStrength=0, magicDamagePercent=0, prayerBonus=3),
    21140: dict(stabAttack=0, slashAttack=0, crushAttack=0, magicAttack=0, rangedAttack=0,
                stabDefence=0, slashDefence=0, crushDefence=0, magicDefence=0, rangedDefence=0,
                meleeStrength=0, rangedStrength=0, magicDamagePercent=0, prayerBonus=0),
}


def main():
    path = sys.argv[1] + "/src/main/resources/data/equipment.json"
    items = json.load(open(path))
    have = {i["itemId"] for i in items}
    added = []
    for spec in NEW:
        if spec["itemId"] in have:
            continue
        entry = dict(spec)
        wiki = entry.pop("wiki")
        entry.update(STATS[spec["itemId"]])
        entry.update(TEMPLATE_FLAGS)
        entry.update(ammoType="NONE", hasSpecialAttack=False, specEnergyCost=0, specEffect="",
                     source="OSRS Wiki (dps-calc dataset)", wikiName=wiki, verifiedDate="2026-11-17")
        items.append(entry)
        added.append(entry["name"])
    json.dump(items, open(path, "w"), indent=2)
    print("added:", added if added else "nothing (already present)")


if __name__ == "__main__":
    main()
