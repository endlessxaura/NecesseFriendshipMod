# Introduction
This mod enables relationships between players and NPCs within a settlement. 
These relationships impact the happiness, interactions, and prices of the settlers.

## Features

### Friendship
The core of this mod is a friendship score for all combinations of settlers and players within a settlement.
As settlers have positive interactions, their friendship score increases.
As they have negative interactions, their friendship score decreases.

For players, there are a few new interactions with settlers. You can
have a conversation, where they will give a text response based on recent
events. This can give you insight into their personality and awards you
friendship points every day you do it.

You can also give settlers gifts! What they like depends on their personality,
so you'll need to get to know them. The more they like the gift, the more friendship
points they give you.

Finally, settlers that like you will give you gifts, too! As your friendship score
increases, they'll give you rare(ish) items based on their personality. Of course,
you might not like all of these, either. :P

### Personalities
Every settler has a unique personality. They have a set of gifts they like
and don't like, furniture preferences, and desired tasks. Playing to these
preferences will give the settler more happiness. Get to know your neighbors!

### Rooming with Friends
A settler won't take a barracks penalty for rooming with a friend. Conversely,
villagers that don't like each other take a penalty for rooming with each other.

### Fighting
Settlers that _really_ don't like each other will occasionally fight. You can
interact with them to break it up. If you're not careful, they might knock each
other out. Sometimes, someone just doesn't fit in the settlement and needs
to be banished for everyone else. Happier settlers quarrel with each other less,
so make sure everyone has their needs met.

This applies to you, too! So don't piss 'em off >:)

## Implementation
All settlers have a dictionary of friendship scores with other settlers. A
friendship score goes from -100 to 100. All relationships start out at 0.

`HumanInteractWithSettlerAINode` is the interaction behavior between settlers.
If an interaction is positive, determined by `interactionPositive`, they will 
gain 1 friendship point. If it is negative, they will lose 1 friendship point.

At various thresholds, their relationship status changes.
- 100: Devoted. +50 happiness bonus for rooming together. Will give you rare gifts.
- 75: Best Friends. +30 happiness bonus for rooming together. Will give you gifts.
- 50: Friends. +20 happiness bonus for rooming together.
- 25: Friendly. +10 happiness bonus for rooming together.
- 0: Acquaintances. No benefits.
- -25: Unfriendly. -10 happiness penalty for rooming together.
- -50: Quarreling. -20 happiness penalty for rooming together.
- -75: Enemies. -30 happiness penalty for rooming together. Will fight on negative interaction.
- -100: Nemeses. -50 happiness penalty for rooming together. Will fight on sight.

This score is kept internally. It is recorded via packets on the server, which
get sent to the client on interaction.