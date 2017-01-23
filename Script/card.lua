white = string_contains(mana, 'W')
blue = string_contains(mana, 'U')
black = string_contains(mana, 'B')
red = string_contains(mana, 'R')
green = string_contains(mana, 'G')

artifact = table_contains(types, 'Artifact')
creature = table_contains(types, 'Creature')
enchantment = table_contains(types, 'Enchantment')
instant = table_contains(types, 'Instant')
land = table_contains(types, 'Land')
planeswalker = table_contains(types, 'Planeswalker')
sorcery = table_contains(types, 'Sorcery')
tribal = table_contains(types, 'Tribal')

colors = 0
if white then
	colors = colors + 1
end
if blue then
	colors = colors + 1
end
if black then
	colors = colors + 1
end
if red then
	colors = colors + 1
end
if green then
	colors = colors + 1
end

if colors > 1 then
	multicolor = true
else
	multicolor = false
end

w = white
u = blue
b = black
r = red
g = green

a = artifact
c = creature
e = enchantment
i = instant
l = land
p = planeswalker
s = sorcery
t = tribal

m = multicolor