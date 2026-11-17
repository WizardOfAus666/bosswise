#!/usr/bin/env python3
"""
BOSSWISE data sync.

Rewrites the combat numbers in src/main/resources/data/equipment.json and
monsters.json from the Old School RuneScape Wiki's own DPS-calculator dataset
(https://github.com/weirdgloop/osrs-dps-calc, cdn/json/*.json). That dataset is
generated from the wiki infoboxes, so it is the same numbers the wiki shows.

Everything the wiki does not own (prices, tiers, quest/skill requirements,
strategy text, flags like dragonbane, locations, variant names) is left exactly
as it is. Only stats are touched, plus npcId/itemId when the id in the file
points at a different item or NPC.

Usage:  python3 sync_wiki_data.py <bosswise-repo> <path-to-osrs-dps-calc-checkout>
"""
import json, os, re, sys

BAD_VER = ('awakened', 'hard mode', 'entry mode', 'challenge mode', 'nightmare zone',
           'uncharged', 'empty', 'partially', 'broken', 'locked', 'dead', 'degraded',
           'poison', 'emir', 'soul wars', 'trailblazer', 'used', 'recoil', 'enraged',
           'escape caves', 'practice', 'tutorial', 'banner')
GOOD_VER = ('', 'normal', 'undamaged', 'charged', 'active', 'full', 'standard')

# BOSSWISE name -> wiki name, for the handful the wiki lists under another name.
ITEM_NAME_FIX = {
    "dragon sword": "Dragon shortsword",
    "dragon ruby bolts (e)": "Ruby dragon bolts (e)",
    "dragon diamond bolts (e)": "Diamond dragon bolts (e)",
    "sapphire dragon bolts (e)": "Sapphire dragon bolts (e)",
    "amethyst arrows": "Amethyst arrow",
    "dragon arrows": "Dragon arrow",
    "dragon dart": "Dragon dart",
    "peace blessing": "Peaceful blessing",
    "salve amulet (ei)": "Salve amulet(ei)",
    "salve amulet (i)": "Salve amulet(i)",
    "saradomin d'hide chaps": "Saradomin chaps",
    "god d'hide chaps": "Saradomin chaps",
    "god d'hide body": "Saradomin d'hide body",
    "blessed d'hide boots": "Saradomin d'hide boots",
    "bone bow": "Bone shortbow",
    "dragon dagger (p++)": "Dragon dagger",
}
ITEM_VER_HINT = {"dragon dagger (p++)": "poison++"}

# (BOSSWISE monster, variant) -> (wiki name, version hint)
MON_FIX = {
    ("goblin", "lumbridge goblin"): ("Goblin", "level 5"),
    ("guard", "varrock guard"): ("Guard", "varrock 1"),
    ("al-kharid warrior", "palace guard"): ("Al Kharid warrior", ""),
    ("ogre", "training camp ogre"): ("Ogre", "level 53"),
    ("black demon", "taverley black demon"): ("Black demon", "level 172"),
    ("greater demon", "wilderness greater demon"): ("Greater demon", "wilderness slayer cave"),
    ("greater demon", "catacombs greater demon"): ("Greater demon", "level 100"),
    ("revenant dragon", "revenant caves dragon"): ("Revenant dragon", ""),
    ("sand crab", "hosidius shore"): ("Sand Crab", "active"),
    ("tekton", "chambers tekton"): ("Tekton", "normal"),
    ("great olm", "great olm (head)"): ("Great Olm", "head (normal)"),
    ("chambers of xeric (cox)", "great olm"): ("Great Olm", "head (normal)"),
    ("tombs of amascut (toa)", "wardens p3"): ("Elidinis' Warden", "damaged"),
    ("theatre of blood (tob)", "verzik vitur p3"): ("Verzik Vitur", "normal mode, phase 3"),
    ("grotesque guardians", "dusk"): ("Dusk", "second form"),
    ("grotesque guardians", "dawn"): ("Dawn", ""),
    ("spindel", "wilderness lair"): ("Spindel", ""),
    ("artio", "wilderness lair"): ("Artio", ""),
    ("callisto", "callisto"): ("Callisto", ""),
    ("venenatis", "venenatis"): ("Venenatis", ""),
}
ATTR_TO_CLASS = {"dragon": "DRAGON", "demon": "DEMON", "undead": "UNDEAD",
                 "kalphite": "KALPHITE", "rat": "RAT", "vampyre": "VAMPYRE"}


def clean(s):
    if s is None:
        return ""
    s = re.sub(r"<br\s*/?>", " / ", str(s))
    s = "".join(ch for ch in s if ch.isprintable())
    return re.sub(r"\s+", " ", s).strip()


def score(row, hint, want_level):
    """Lower is better. Picks the standard, usable version of an item/monster."""
    ver = (row.get("version") or "").lower()
    s = 0
    toks = [t for t in re.split(r"[^a-z0-9+]+", (hint or "").lower()) if len(t) > 2]
    if ver and any(t in ver for t in toks):
        s -= 100
    if ver in GOOD_VER:
        s -= 20
    for b in BAD_VER:
        if b in ver and not any(b in t or t in b for t in toks):
            s += 50
    # charge counts: "100", "(10)", "4" -> prefer the fullest
    m = re.fullmatch(r"\(?(\d+)\)?", ver)
    if m:
        s -= int(m.group(1)) * 0.1
    if want_level is not None:
        s += min(abs(row.get("level", 0) - want_level), 40) * 0.5
    return s


def pick(rows, hint, want_level, prefer_id=None):
    if not rows:
        return None
    return sorted(rows, key=lambda r: score(r, hint, want_level) - (5 if r["id"] == prefer_id else 0))[0]


def sync_items(items, ref, report):
    by_name = {}
    for r in ref:
        by_name.setdefault(r["name"].lower(), []).append(r)
    by_id = {}
    for r in ref:
        by_id.setdefault(r["id"], []).append(r)
    changed = fields = 0
    for it in items:
        key = it["name"].lower()
        wiki_name = ITEM_NAME_FIX.get(key, it["name"]).lower()
        hint = ITEM_VER_HINT.get(key, "")
        if not hint:
            m = re.search(r"\(([^)]+)\)$", it["name"])
            if m:
                hint = m.group(1).lower()
        rows = by_name.get(wiki_name) or []
        if not rows:
            rows = [r for r in by_id.get(it["itemId"], []) if r["slot"] == it["slot"].lower()]
        if not rows:
            report.append("ITEM UNVERIFIED (no wiki row): %s" % it["name"])
            continue
        r = pick(rows, hint, None, prefer_id=it["itemId"])
        new = {
            "stabAttack": r["offensive"]["stab"], "slashAttack": r["offensive"]["slash"],
            "crushAttack": r["offensive"]["crush"], "magicAttack": r["offensive"]["magic"],
            "rangedAttack": r["offensive"]["ranged"],
            "stabDefence": r["defensive"]["stab"], "slashDefence": r["defensive"]["slash"],
            "crushDefence": r["defensive"]["crush"], "magicDefence": r["defensive"]["magic"],
            "rangedDefence": r["defensive"]["ranged"],
            "meleeStrength": r["bonuses"]["str"], "rangedStrength": r["bonuses"]["ranged_str"],
            # the wiki dataset stores magic damage in tenths of a percent (Kodai wand
            # is 150 = +15%); BOSSWISE stores whole percent, so scale it down.
            "magicDamagePercent": int(round(r["bonuses"]["magic_str"] / 10.0)),
            "prayerBonus": r["bonuses"]["prayer"],
        }
        if it.get("slot") == "WEAPON":
            if r.get("speed"):
                new["attackSpeed"] = r["speed"]
            new["twoHanded"] = bool(r.get("isTwoHanded"))
        diff = {k: (it.get(k), v) for k, v in new.items() if it.get(k) != v}
        # Only correct an item id that points at a *different item*. Ids the wiki
        # dataset simply does not model (it splits items by charge state) are left
        # alone, so in-game detection keeps working.
        wrong_item = any(x["name"].lower() != r["name"].lower() for x in by_id.get(it["itemId"], []))
        if it["itemId"] != r["id"] and (wrong_item or it["itemId"] not in by_id):
            if wrong_item:
                diff["itemId"] = (it["itemId"], r["id"])
                it["legacyItemId"] = it["itemId"]
                it["itemId"] = r["id"]
            else:
                report.append("ITEM id kept (not in wiki dataset): %s = %s (wiki %s uses %s)"
                              % (it["name"], it["itemId"], r["name"], r["id"]))
        if diff:
            changed += 1
            fields += len(diff)
            report.append("ITEM %s -> %s%s %s" % (it["name"], r["name"],
                          (" [%s]" % r["version"]) if r.get("version") else "", diff))
        it.update(new)
        it["source"] = "OSRS Wiki (dps-calc dataset)"
        it["wikiName"] = r["name"] + ((" (%s)" % r["version"]) if r.get("version") else "")
    return changed, fields


def sync_monsters(monsters, ref, report):
    by_name = {}
    for r in ref:
        by_name.setdefault(r["name"].lower(), []).append(r)
    by_id = {}
    for r in ref:
        by_id.setdefault(r["id"], []).append(r)
    changed = fields = 0
    for m in monsters:
        attrs_seen = []
        for v in m["variants"]:
            mk = (m["name"].lower(), (v.get("variantName") or "").lower())
            hint = ""
            if mk in MON_FIX:
                wname, hint = MON_FIX[mk]
                rows = by_name.get(wname.lower(), [])
            else:
                rows = by_name.get(mk[1]) or by_name.get(m["name"].lower()) or by_id.get(v["npcId"], [])
                hint = mk[1]
            if not rows:
                report.append("MONSTER UNVERIFIED (no wiki row): %s / %s" % (m["name"], v.get("variantName")))
                continue
            r = pick(rows, hint, v.get("combatLevel"), prefer_id=v["npcId"])
            new = {
                "hitpoints": r["skills"]["hp"], "attackLevel": r["skills"]["atk"],
                "strengthLevel": r["skills"]["str"], "defenceLevel": r["skills"]["def"],
                "magicLevel": r["skills"]["magic"], "rangedLevel": r["skills"]["ranged"],
                "stabDefence": r["defensive"]["stab"], "slashDefence": r["defensive"]["slash"],
                "crushDefence": r["defensive"]["crush"], "magicDefence": r["defensive"]["magic"],
                "rangedDefence": r["defensive"]["standard"],
                "combatLevel": r["level"] or v.get("combatLevel", 0),
            }
            if r.get("speed"):
                new["attackSpeed"] = r["speed"]
            styles = [clean(s) for s in (r.get("style") or []) if clean(s) and clean(s).lower() != "none"]
            diff = {k: (v.get(k), x) for k, x in new.items() if v.get(k) != x}
            if v["npcId"] != r["id"]:
                diff["npcId"] = (v["npcId"], r["id"])
                v["legacyNpcId"] = v["npcId"]
                v["npcId"] = r["id"]
            if styles and styles != v.get("attackStyles"):
                diff["attackStyles"] = (v.get("attackStyles"), styles)
            if diff:
                changed += 1
                fields += len(diff)
                report.append("MONSTER %s / %s -> %s%s %s" % (
                    m["name"], v.get("variantName"), r["name"],
                    (" [%s]" % r["version"]) if r.get("version") else "", diff))
            v.update(new)
            if styles:
                v["attackStyles"] = styles
            v["maxHit"] = clean(r.get("max_hit")) or "Unknown"
            v["attributes"] = list(r.get("attributes") or [])
            v["wikiName"] = r["name"] + ((" (%s)" % r["version"]) if r.get("version") else "")
            w = r.get("weakness") or {}
            if w.get("element"):
                v["elementalWeakness"] = str(w["element"]).upper()
                v["elementalWeaknessPercent"] = w.get("severity", 0)
            attrs_seen += v["attributes"]
        for a in attrs_seen:
            if a in ATTR_TO_CLASS and m.get("classification") in (None, "", "NONE"):
                m["classification"] = ATTR_TO_CLASS[a]
        if attrs_seen:
            m["attributes"] = sorted(set(attrs_seen))
        hits = [v.get("maxHit") for v in m["variants"] if v.get("maxHit") and v["maxHit"] != "Unknown"]
        if hits:
            m["maxHitSummary"] = " | ".join(dict.fromkeys(hits))
        el = [v for v in m["variants"] if v.get("elementalWeakness")]
        if el and m.get("elementalWeakness") in (None, "", "NONE"):
            m["elementalWeakness"] = el[0]["elementalWeakness"]
            m["elementalWeaknessPercent"] = el[0].get("elementalWeaknessPercent", 0)
    return changed, fields


def main():
    src, ref = sys.argv[1], sys.argv[2]
    d = os.path.join(src, "src/main/resources/data")
    items = json.load(open(os.path.join(d, "equipment.json")))
    monsters = json.load(open(os.path.join(d, "monsters.json")))
    rE = json.load(open(os.path.join(ref, "cdn/json/equipment.json")))
    rM = json.load(open(os.path.join(ref, "cdn/json/monsters.json")))
    report = []
    ic, if_ = sync_items(items, rE, report)
    mc, mf = sync_monsters(monsters, rM, report)
    json.dump(items, open(os.path.join(d, "equipment.json"), "w"), indent=2)
    json.dump(monsters, open(os.path.join(d, "monsters.json"), "w"), indent=2)
    open(os.path.join(src, "DATA_SYNC_REPORT.txt"), "w").write(
        "BOSSWISE data sync against the OSRS Wiki dps-calc dataset\n"
        "items corrected: %d (%d fields)   monster variants corrected: %d (%d fields)\n\n%s\n"
        % (ic, if_, mc, mf, "\n".join(report)))
    print("items corrected %d (%d fields); variants corrected %d (%d fields)" % (ic, if_, mc, mf))


if __name__ == "__main__":
    main()
