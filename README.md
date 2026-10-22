# Araxxor Attack Counter

RuneLite plugin that counts Araxxor's basic attacks (melee, magic, ranged) and draws the count above her.

- After the 6th basic attack she uses a special, so on the 6th attack the count is replaced by the incoming special in red (Acid Drip, Acid Spray or Acid Bullet).
- The special is read from the south-easternmost egg in the room (red = Acid Drip, white = Acid Spray, green = Acid Bullet) and corrected from her first special if it was wrong.
- The counter resets to 0 after each special attack (acid leak, acid spray, acid cannon).
- Once she enrages she stops using specials, so the counter is hidden for the rest of the fight.
- The warning attack number, colors, font size and height offset can be changed in the plugin config.
