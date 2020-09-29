# SpleefTime

A simple Minecraft Paper plugin to run Spleef matches on your server.

### Permissions
- `spleeftime.play`: allows players to use the `/match`, `/start`, `/accept`, `/decline`, and `/cancel` commands 
(i.e., all the commands necessary to create and play matches.)
- `spleeftime.create`: allows players to use the `/arena` command to create, edit, and delete arenas.

### Creating An Arena
- Fly to the area where you want the center of the arena to be. The center will be your current location.
- Use the `/arena create` command, whose syntax looks like:

    `/arena create <name> <size> <materialString> <arenaTypeString>`
    - `name`: how players will refer to your arena when creating new matches there
    - `size`: the size in blocks for the arena. Currently, only circular arena types
    are supported, and for these, `size` represents the maximum radius of the arena
    (including the catch zone below.) 
    - `materialString`: the block that you'd like the arena to be made of.
    - `arenaTypeString`: the type of arena to create. Current options are:
        - `CIRCULAR_ARENA`: just a simple circle
        - `TWO_LEVEL_CIRCULAR_ARENA`: a two-level arena; players start on the top level (which is glass)
        and then are knocked down for round 2 on the second level (which is the material specified by `materialString`).
        
### Other Arena Configuration Options    
- `/arena watch <name>`: set where players end up when they are out of a match in `<name>` arena, but before the match
    is over (i.e., where they can go to spectate).
- `/arena end <name>`: set where players end up after a match in `<name>` arena is over.

The default value for both of these options is the center of the arena (**so you should change them**).

To delete an arena, use `/arena delete <name>`.

### Playing Matches

Before players can play, they must create a "Spleef chest" to store items during the match. 
They can use any double chest for this by placing a sign on the chest that says `[Spleef]` on the first line.

To start a match, use the `/match` command:

`/match <arenaName> [players...]` where `arenaName` is the name given in `/arena create` and `[players...]` 
is a list of players in the match. The player who run the `/match` command will automatically be included.

Once a player is invited, they can `/accept <hostname>` or `/decline <hostname>` where `<hostname>` is the name
of the playerr who created the match. Once all players either accept or decline, the host can `/start` the match.

The arena will be generated and players will:
- have their inventory moved to their Spleef chests
- have their health and levels cached
- be given full health and hunger
- be given a diamond shovel
- be moved to the arena.

At the end of a match, all players will be given back their items, health, and levels and be teleported to the end location.

### FAQ
- **Q**: What if I disconnect in the middle of a match?
  
  **A**: Don't worry. You'll automatically have everything restored the next time you reconnect.
  
- **Q**: why are chests necessary if players never access them? Can't you just store this info in the config?

    **A**: No more questions. 