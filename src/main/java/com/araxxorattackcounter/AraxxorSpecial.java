package com.araxxorattackcounter;

import lombok.Getter;
import lombok.RequiredArgsConstructor;
import net.runelite.api.gameval.AnimationID;
import net.runelite.api.gameval.NpcID;

@Getter
@RequiredArgsConstructor
enum AraxxorSpecial
{
	ACID_DRIP("Acid Drip", NpcID.ARAXXOR_MINION_EGG_EXPLODE, AnimationID.NPC_ARAXXOR_01_ATTACK_ACID_LEAK_01),
	ACID_SPRAY("Acid Spray", NpcID.ARAXXOR_MINION_EGG_MIRRORBACK, AnimationID.NPC_ARAXXOR_01_ATTACK_ACID_SPRAY_01),
	ACID_BULLET("Acid Bullet", NpcID.ARAXXOR_MINION_EGG_VENOM, AnimationID.NPC_ARAXXOR_01_ACID_CANNON_01);

	private final String displayName;
	private final int eggId;
	private final int animationId;

	static AraxxorSpecial fromEgg(int npcId)
	{
		for (AraxxorSpecial special : values())
		{
			if (special.eggId == npcId)
			{
				return special;
			}
		}
		return null;
	}

	static AraxxorSpecial fromAnimation(int animationId)
	{
		for (AraxxorSpecial special : values())
		{
			if (special.animationId == animationId)
			{
				return special;
			}
		}
		return null;
	}
}
