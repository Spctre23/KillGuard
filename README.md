# KillGuard

## Description
Prevents entities of specified types or tags from mistakenly being mass cleared with /kill.
- Useful for preventing block entities used in builds like item frames or paintings from being mistakenly cleared in an entity wipe.
- Protected tags / entities are stored in a persistent config. They can be added / removed with subcommands.

## Subcommands (parent command is /killguard)
- addtag
- addtype
- removetag
- removetype
- list
- clear
- reload

To intentionally clear protected entities, use /forcekill (identical to /kill but without entity protection)
