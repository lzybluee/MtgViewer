white = string_contains(mana, 'W') or string_contains(colorIndicator, 'White')
blue = string_contains(mana, 'U') or string_contains(colorIndicator, 'Blue')
black = string_contains(mana, 'B') or string_contains(colorIndicator, 'Black')
red = string_contains(mana, 'R') or string_contains(colorIndicator, 'Red')
green = string_contains(mana, 'G') or string_contains(colorIndicator, 'Green')

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

function hastext(str)
	return string_contains(text, str)
end

function hasrule(str)
	return string_contains(rule, str)
end

function hastype(str)
	return table_contains(types, str) or table_contains(subTypes, str) or table_contains(superTypes, str)
end

function containstype(str)
	return table_contains_str(types, str) or table_contains_str(subTypes, str) or table_contains_str(superTypes, str)
end

modern = table_contains(legal, 'Modern')
legacy = table_contains(legal, 'Legacy')

if colors > 1 then
	multicolor = true
else
	multicolor = false
end

pn = tonumber(power)
tn = tonumber(toughness)
ln = tonumber(loyalty)

common = (rarity == "Common")
uncommon = (rarity == "Uncommon")
rare = (rarity == "Rare")
mythic = (rarity == "Mythic Rare")

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

cmc = converted

split = isSplit
double = isDoubleFaced
flip = isFlip
legend = isLegendary

reprint = reprintIndex
id = multiverseid
