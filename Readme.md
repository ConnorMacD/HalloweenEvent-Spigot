# HalloweenEvent-Spigot

HalloweenEvent-Spigot is a one-time item giving Spigot plugin. It was made quickly for a Minecraft server's halloween event, which was a scavenger hunt for items. When opening a HalloweenEvent chest, the player is given an item with their name in the Lore tags of that item. 

## Commands

- /halloweenchest
    - set <id>
    - delete

## How To Use
### Creating a Halloween chest
1. Have OP rights, or the permission 'halloweenevent.all'.
2. Place down a chest, or a trapped chest.
3. Use the command '/halloweenchest set <id>[:dv]' where id is the ID of the item, and dv is the optional data value.
4. Punch the placed chest. Whenever someone opens it, they'll be given an item with their name in the Lore tags.
5. After claiming your item, you are not allowed to open it again while it's been set as a Halloween chest.

### Deleting a Halloween chest

1. Have OP rights, or the permission 'halloweenevent.all'.
2. Use the '/halloweenchest delete' command.
3. Punch the chest to remove the chest's functionality while keeping the chest intact.

## Dependencies

HalloweenEvent-Spigot was built with SpigotAPI 1.8.8 R0.1. Newer versions of the SpigotAPI could possibly cause compilation to fail.

To get the SpigotAPI, you have to use Spigot's BuildTools, which can be found [here] [SpigBT]

## Building

* Fetch the GitHub repo and open in your preferred IDE.
* Using BuildTools, download the SpigotAPI.
* Add SpigotAPI as an external JAR.
* Build all files to a JAR file.
* Put the JAR file in a Spigot server's 'plugins' folder
* Start/Restart Spigot or use a plugin to manually load the plugin.


[//]: # (These are reference links used in the body of this note and get stripped out when the markdown processor does its job. There is no need to format nicely because it shouldn't be seen. Thanks SO - http://stackoverflow.com/questions/4823468/store-comments-in-markdown-syntax)


   [SpigBT]: <https://www.spigotmc.org/wiki/buildtools/>

