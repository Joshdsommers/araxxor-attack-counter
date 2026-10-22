package com.araxxorattackcounter;

import com.google.inject.Provides;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashSet;
import java.util.Set;
import java.util.WeakHashMap;
import javax.inject.Inject;
import lombok.Getter;
import net.runelite.api.Actor;
import net.runelite.api.Client;
import net.runelite.api.GameState;
import net.runelite.api.NPC;
import net.runelite.api.Projectile;
import net.runelite.api.coords.WorldPoint;
import net.runelite.api.events.AnimationChanged;
import net.runelite.api.events.GameStateChanged;
import net.runelite.api.events.GameTick;
import net.runelite.api.events.NpcDespawned;
import net.runelite.api.events.NpcSpawned;
import net.runelite.api.events.ProjectileMoved;
import net.runelite.api.gameval.AnimationID;
import net.runelite.api.gameval.NpcID;
import net.runelite.api.gameval.SpotanimID;
import net.runelite.client.config.ConfigManager;
import net.runelite.client.eventbus.Subscribe;
import net.runelite.client.plugins.Plugin;
import net.runelite.client.plugins.PluginDescriptor;
import net.runelite.client.ui.overlay.OverlayManager;

@PluginDescriptor(
	name = "Araxxor Attack Counter",
	description = "Warns which special attack is coming after the 6th basic attack and resets the counter after the special attack",
	tags = {"araxxor", "arraxor", "boss", "counter", "special", "pvm"}
)
public class AraxxorAttackCounterPlugin extends Plugin
{
	private static final Set<Integer> BASIC_ATTACKS = Set.of(
		AnimationID.NPC_ARAXXOR_01_ATTACK_MELEE_01,
		AnimationID.NPC_ARAXXOR_01_ATTACK_MAGIC_01,
		AnimationID.NPC_ARAXXOR_01_ATTACK_RANGED_01,
		AnimationID.NPC_ARAXXOR_01_ATTACK_SLOW_MELEE_01,
		AnimationID.NPC_ARAXXOR_01_ATTACK_SLOW_RANGED_01,
		AnimationID.NPC_ARAXXOR_01_ATTACK_MELEE_ENRAGED_01
	);

	private static final Set<Integer> BASIC_ATTACK_PROJECTILES = Set.of(
		SpotanimID.ARAXXOR_RANGED_PROJECTILE,
		SpotanimID.ARAXXOR_MAGIC_PROJECTILE
	);

	private static final Set<Integer> SPECIAL_ATTACK_PROJECTILES = Set.of(
		SpotanimID.ARAXXOR_VENOM_DRIP,
		SpotanimID.ARAXXOR_POOLS_PROJ
	);

	private static final int SAME_ATTACK_TICKS = 4;

	private static final int SAME_SPECIAL_TICKS = 6;

	private static final Set<Integer> ENRAGE_ANIMATIONS = Set.of(
		AnimationID.NPC_ARAXXOR_01_ENRAGE_TRANSITION_01,
		AnimationID.NPC_ARAXXOR_01_ENRAGE_TRANSITION_02,
		AnimationID.NPC_ARAXXOR_01_ATTACK_MELEE_ENRAGED_01
	);

	private static final int EGG_COUNT = 3;

	@Inject
	private Client client;

	@Inject
	private OverlayManager overlayManager;

	@Inject
	private AraxxorAttackCounterOverlay overlay;

	@Getter
	private NPC araxxor;

	@Getter
	private int attackCount;

	private int lastAttackTick = -1;

	private int lastSpecialTick = -1;

	@Getter
	private AraxxorSpecial special;

	private boolean specialLocked;

	@Getter
	private boolean enraged;

	private final Set<NPC> eggs = new HashSet<>();

	private final Set<Projectile> seenProjectiles = Collections.newSetFromMap(new WeakHashMap<>());

	@Provides
	AraxxorAttackCounterConfig provideConfig(ConfigManager configManager)
	{
		return configManager.getConfig(AraxxorAttackCounterConfig.class);
	}

	@Override
	protected void startUp()
	{
		overlayManager.add(overlay);
		reset();
		if (client.getGameState() == GameState.LOGGED_IN)
		{
			for (NPC npc : client.getTopLevelWorldView().npcs())
			{
				if (npc.getId() == NpcID.ARAXXOR)
				{
					araxxor = npc;
				}
				else if (AraxxorSpecial.fromEgg(npc.getId()) != null)
				{
					eggs.add(npc);
				}
			}
		}
	}

	@Override
	protected void shutDown()
	{
		overlayManager.remove(overlay);
		reset();
		eggs.clear();
	}

	@Subscribe
	public void onNpcSpawned(NpcSpawned event)
	{
		NPC npc = event.getNpc();
		if (npc.getId() == NpcID.ARAXXOR)
		{
			araxxor = npc;
			resetCount();
			special = null;
			specialLocked = false;
			enraged = false;
		}
		else if (AraxxorSpecial.fromEgg(npc.getId()) != null)
		{
			eggs.add(npc);
		}
		else if (npc.getId() == NpcID.ARAXXOR_ACID_CANNON_PROJECTILE && araxxor != null)
		{
			recordSpecial(AraxxorSpecial.ACID_BULLET);
		}
	}

	@Subscribe
	public void onNpcDespawned(NpcDespawned event)
	{
		eggs.remove(event.getNpc());
		if (event.getNpc() == araxxor)
		{
			reset();
		}
	}

	@Subscribe
	public void onGameStateChanged(GameStateChanged event)
	{
		if (event.getGameState() == GameState.LOGIN_SCREEN || event.getGameState() == GameState.HOPPING)
		{
			reset();
			eggs.clear();
		}
	}

	@Subscribe
	public void onGameTick(GameTick event)
	{
		if (specialLocked || eggs.isEmpty())
		{
			return;
		}

		special = eggs.stream()
			.max(Comparator.comparingInt(this::southEastScore))
			.map(egg -> AraxxorSpecial.fromEgg(egg.getId()))
			.orElse(special);
		specialLocked = eggs.size() >= EGG_COUNT;
	}

	@Subscribe
	public void onAnimationChanged(AnimationChanged event)
	{
		Actor actor = event.getActor();
		if (!(actor instanceof NPC) || ((NPC) actor).getId() != NpcID.ARAXXOR)
		{
			return;
		}

		araxxor = (NPC) actor;
		int animation = actor.getAnimation();
		AraxxorSpecial used = AraxxorSpecial.fromAnimation(animation);
		enraged |= ENRAGE_ANIMATIONS.contains(animation);
		if (BASIC_ATTACKS.contains(animation))
		{
			recordBasicAttack();
		}
		else if (used != null)
		{
			recordSpecial(used);
		}
	}

	@Subscribe
	public void onProjectileMoved(ProjectileMoved event)
	{
		Projectile projectile = event.getProjectile();
		if (araxxor == null || projectile.getSourceActor() != araxxor || !seenProjectiles.add(projectile))
		{
			return;
		}

		if (BASIC_ATTACK_PROJECTILES.contains(projectile.getId()))
		{
			recordBasicAttack();
		}
		else if (SPECIAL_ATTACK_PROJECTILES.contains(projectile.getId()))
		{
			recordSpecial(null);
		}
	}

	private void recordBasicAttack()
	{
		int tick = client.getTickCount();
		if (lastAttackTick >= 0 && tick - lastAttackTick < SAME_ATTACK_TICKS)
		{
			return;
		}

		lastAttackTick = tick;
		attackCount++;
		specialLocked |= special != null;
	}

	private void recordSpecial(AraxxorSpecial used)
	{
		if (used != null)
		{
			special = used;
			specialLocked = true;
		}

		int tick = client.getTickCount();
		if (lastSpecialTick >= 0 && tick - lastSpecialTick < SAME_SPECIAL_TICKS)
		{
			return;
		}

		lastSpecialTick = tick;
		resetCount();
	}

	private int southEastScore(NPC npc)
	{
		WorldPoint point = WorldPoint.fromLocalInstance(client, npc.getLocalLocation());
		return point.getX() - point.getY();
	}

	private void resetCount()
	{
		attackCount = 0;
		lastAttackTick = client.getTickCount();
	}

	private void reset()
	{
		araxxor = null;
		attackCount = 0;
		lastAttackTick = -1;
		lastSpecialTick = -1;
		special = null;
		specialLocked = false;
		enraged = false;
		seenProjectiles.clear();
	}
}
