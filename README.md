> # This repository has been archived.
> ### It has been replaced by [EntityLabelLib](https://github.com/ArcanePlugins/EntityLabelLib).
> This archive remains available so that the links to this from Spigot threads of mine don't die.

***

# EntityLabelSystem

This repository contains a test shell Bukkit plugin for the entity label (nametag) systems which
will be transferred to LevelledMobs 4. Writing and testing them in this shell will make it far
easier to isolate development, testing, and issues solely to the entity label system.

ELS will contain two packet-based label solutions:

1. A ProtocolLib-based solution.
2. A NMS-based solution utilizing reflection.

The ProtocolLib-based solution will be developed first, as it will have simpler code, allowing
fundamental ideas to be transferred into NMS form. In LevelledMobs 4, the ProtocolLib-based
solution will only act as a fallback label system in case there is a server version update in
the future which LevelledMobs has yet to support (but ProtocolLib does).

## Contributing

Contributions are warmly welcome! As with all of our other contributors, you will receive credit
for your kind work.

## Copyright Notice

        Copyright (C) 2022  lokka30 and contributors

        This program is free software: you can redistribute it and/or modify
        it under the terms of the GNU Affero General Public License as
        published by the Free Software Foundation, either version 3 of the
        License, or (at your option) any later version.

        This program is distributed in the hope that it will be useful,
        but WITHOUT ANY WARRANTY; without even the implied warranty of
        MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
        GNU Affero General Public License for more details.

        You should have received a copy of the GNU Affero General Public License
        along with this program.  If not, see <https://www.gnu.org/licenses/>.

