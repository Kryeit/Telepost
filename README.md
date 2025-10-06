<p align="center">
  <img width="200" src="https://github.com/Kryeit/Telepost/blob/1.21-neoforge/src/main/resources/assets/telepost/icon.png">
</p>

<h1 align="center">Telepost<br>
	<a href="https://www.curseforge.com/minecraft/mc-mods/telepost/files"><img src="https://cf.way2muchnoise.eu/versions/telepost.svg" alt="Supported Versions"></a>
	<a href="https://github.com/Kryeit/Telepost/LICENSE"><img src="https://img.shields.io/github/license/Creators-of-Create/Create?style=flat&color=900c3f" alt="License"></a>
	<a href="https://discord.gg/vq5vwuyDGU"><img src="https://img.shields.io/discord/929394649884405761?color=5865f2&label=Discord&style=flat" alt="Discord"></a>
	<a href="https://www.curseforge.com/minecraft/mc-mods/telepost"><img src="http://cf.way2muchnoise.eu/telepost.svg" alt="CF"></a>
    <a href="https://modrinth.com/mod/telepost"><img src="https://img.shields.io/modrinth/dt/telepost?logo=modrinth&label=&suffix=%20&style=flat&color=242629&labelColor=5ca424&logoColor=1c1c1c" alt="Modrinth"></a>
    <br><br>
</h1>

**Mod description**



<a href="https://github.com/Kryeit/Telepost/blob/old-1.20.4-fabric/README.md">
If you have Telepost with version lower to 4.0 (<4.0) go here

</a>

#

Telepost doesn't require any other mod to function. However, it has softdepend functions for:
- [Luckperms](https://www.spigotmc.org/resources/1-12-2-1-20-4-griefdefender-claim-plugin-grief-prevention-protection.68900/): For permission management. (max posts per player, leverage per player, command permissions)

<img src="https://cdn.modrinth.com/data/w8avchdW/images/392f24bba4c1cec4557709388a9691ea6637be9b.png" width="430" />

Nerfed teleportation! Easier than ever. Players can create posts if not near enough others. This creates a teleport hall for all players.
You may teleport there if you are standing inside another post.
You can also have your own post to call a /home.

<img src="https://cdn.modrinth.com/data/w8avchdW/images/1c2f0f10937a1d51eeee8614beba5aa0c46cd851.png" width="430" />

- `/post` or `/nearestpost` or `/closestpost`: shows you where the nearest post is.
- `/sethomepost` or `/sethome`: sets a home on the nearest post.
- `/homepost` or `/home` or `/h`: teleports you to your home.
- `/visit <Post>` or `/v`: teleports you to a Post.
- `/forcevisit <Player> <Post>` or `/fv`: teleports you to a Post without requiring to be standing on a post. OPs only.
- `/post list [page]`: shows a paginated GUI of the posts.
- `/post create <Post> <x> <z>`: creates a post with a given name.
- `/post rename <OldName> <NewName>`: renames a post.
- `/post transfer <PostName> <Player>`: transfers a post to another player. OPs only.
- `/post delete <PostName>`: deletes a post. OPs only.
- `/post ally <Player>`: Mark a player as your ally.
- `/post enemy <Player>`: Mark a player as your enemy.
- `/post forgive <Player>`: Forgive a player. (Delete ally/enemy status)

<img src="https://cdn.modrinth.com/data/w8avchdW/images/9b2886e9c145a733c26c5c6a3982683474d295d5.png" width="430" />

## Important permissions:
- `telepost.posts.NUMBER` - Maximum number of posts a player can own. Default is 1.
- `telepost.leverage.NUMBER` - Leverage distance to create posts. See the Leverage section for more information. Default is POST-GAP / 2.

## Command permissions:
* `telepost.command.post` - Use /post, /nearestpost, /closestpost
* `telepost.command.sethome` - Use /sethomepost, /sethome
* `telepost.command.home` - Use /homepost, /home, /h
* `telepost.command.visit` - Use /visit, /v
* `telepost.command.forcevisit` - Use /forcevisit (OPs only)
* `telepost.command.list` - Use /post list
* `telepost.command.create` - Use /post create
* `telepost.command.rename` - Use /post rename
* `telepost.command.transfer` - Use /post transfer (OPs only)
* `telepost.command.delete` - Use /post delete (OPs only)
* `telepost.command.ally` - Use /post ally
* `telepost.command.enemy` - Use /post enemy
* `telepost.command.forgive` - Use /post forgive

# Config
```json5
{
  "db-url": "jdbc:postgresql://kryeit.com:5432/servus",
  "db-user": "postgres",
  "db-password": "lel",
  "post-gap": 2000,
  "post-diameter": 31
}
```
- You need to input the credentials for a PostgreSQL database. **Required to have one in order to use the mod.**
- `post-gap`: The minimum distance in blocks that must be between two posts. Default is 2000 blocks.
- `post-diameter`: The diameter of the post area. Default is 31 blocks. This is also used to offset structures when placing them with `/post create`.

# Leverage

*Imagine `"post-gap": 1000,` inside the `config.json`.*

When creating a post, you must select a location. You can't create a post closer to 1000 blocks from another post.

If you are an ally of a player, their posts will have leverage for you. Once you have leverage, you can create posts closer to your allies. If you had 200 of leverage you could create a post 800 blocks away from your ally's post.
If you are an enemy of a player, their posts will have negative leverage for you. You must create posts further away from your enemies. If you had 200 of leverage you could only create a post 1200 blocks away from your enemy's post.

Our Discord: [https://discord.gg/vq5vwuyDGU](https://discord.gg/vq5vwuyDGU)