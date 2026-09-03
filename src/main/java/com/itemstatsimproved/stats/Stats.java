/*
 * Copyright (c) 2016-2018, Adam <Adam@sigterm.info>
 * All rights reserved.
 *
 * Redistribution and use in source and binary forms, with or without
 * modification, are permitted provided that the following conditions are met:
 *
 * 1. Redistributions of source code must retain the above copyright notice, this
 *    list of conditions and the following disclaimer.
 * 2. Redistributions in binary form must reproduce the above copyright notice,
 *    this list of conditions and the following disclaimer in the documentation
 *    and/or other materials provided with the distribution.
 *
 * THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS "AS IS" AND
 * ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE IMPLIED
 * WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE ARE
 * DISCLAIMED. IN NO EVENT SHALL THE COPYRIGHT OWNER OR CONTRIBUTORS BE LIABLE FOR
 * ANY DIRECT, INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES
 * (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES;
 * LOSS OF USE, DATA, OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND
 * ON ANY THEORY OF LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY, OR TORT
 * (INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE OF THIS
 * SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
 */
package com.itemstatsimproved.stats;

import net.runelite.api.Skill;
import com.itemstatsimproved.stats.EnergyStat;
import com.itemstatsimproved.stats.SkillStat;
import com.itemstatsimproved.stats.Stat;

public class Stats
{
	public static final com.itemstatsimproved.stats.Stat ATTACK = new com.itemstatsimproved.stats.SkillStat(Skill.ATTACK);
	public static final com.itemstatsimproved.stats.Stat DEFENCE = new com.itemstatsimproved.stats.SkillStat(Skill.DEFENCE);
	public static final com.itemstatsimproved.stats.Stat STRENGTH = new com.itemstatsimproved.stats.SkillStat(Skill.STRENGTH);
	public static final com.itemstatsimproved.stats.Stat HITPOINTS = new com.itemstatsimproved.stats.SkillStat(Skill.HITPOINTS);
	public static final com.itemstatsimproved.stats.Stat RANGED = new com.itemstatsimproved.stats.SkillStat(Skill.RANGED);
	public static final com.itemstatsimproved.stats.Stat PRAYER = new com.itemstatsimproved.stats.SkillStat(Skill.PRAYER);
	public static final com.itemstatsimproved.stats.Stat MAGIC = new com.itemstatsimproved.stats.SkillStat(Skill.MAGIC);
	public static final com.itemstatsimproved.stats.Stat COOKING = new com.itemstatsimproved.stats.SkillStat(Skill.COOKING);
	public static final com.itemstatsimproved.stats.Stat WOODCUTTING = new com.itemstatsimproved.stats.SkillStat(Skill.WOODCUTTING);
	public static final com.itemstatsimproved.stats.Stat FLETCHING = new com.itemstatsimproved.stats.SkillStat(Skill.FLETCHING);
	public static final com.itemstatsimproved.stats.Stat FISHING = new com.itemstatsimproved.stats.SkillStat(Skill.FISHING);
	public static final com.itemstatsimproved.stats.Stat FIREMAKING = new com.itemstatsimproved.stats.SkillStat(Skill.FIREMAKING);
	public static final com.itemstatsimproved.stats.Stat CRAFTING = new com.itemstatsimproved.stats.SkillStat(Skill.CRAFTING);
	public static final com.itemstatsimproved.stats.Stat SMITHING = new com.itemstatsimproved.stats.SkillStat(Skill.SMITHING);
	public static final com.itemstatsimproved.stats.Stat MINING = new com.itemstatsimproved.stats.SkillStat(Skill.MINING);
	public static final com.itemstatsimproved.stats.Stat HERBLORE = new com.itemstatsimproved.stats.SkillStat(Skill.HERBLORE);
	public static final com.itemstatsimproved.stats.Stat AGILITY = new com.itemstatsimproved.stats.SkillStat(Skill.AGILITY);
	public static final com.itemstatsimproved.stats.Stat THIEVING = new com.itemstatsimproved.stats.SkillStat(Skill.THIEVING);
	public static final com.itemstatsimproved.stats.Stat SLAYER = new com.itemstatsimproved.stats.SkillStat(Skill.SLAYER);
	public static final com.itemstatsimproved.stats.Stat FARMING = new com.itemstatsimproved.stats.SkillStat(Skill.FARMING);
	public static final com.itemstatsimproved.stats.Stat RUNECRAFT = new com.itemstatsimproved.stats.SkillStat(Skill.RUNECRAFT);
	public static final com.itemstatsimproved.stats.Stat HUNTER = new com.itemstatsimproved.stats.SkillStat(Skill.HUNTER);
	public static final com.itemstatsimproved.stats.Stat CONSTRUCTION = new com.itemstatsimproved.stats.SkillStat(Skill.CONSTRUCTION);
	public static final com.itemstatsimproved.stats.Stat SAILING = new SkillStat(Skill.SAILING);
	public static final Stat RUN_ENERGY = new EnergyStat();
}
