--[[
name -> String
simpleName -> String
otherPart -> StringArray
partIndex -> Integer
isSplit -> Boolean
isDoubleFaced -> Boolean
isMDFC -> Boolean
isFlip -> Boolean
isAdventure -> Boolean
isLegendary -> Boolean
isFun -> Boolean
types -> StringArray
subTypes -> StringArray
superTypes -> StringArray
mana -> String
value -> Integer
colorIndicator -> String
power -> String
toughness -> String
loyalty -> String
text -> String
rules -> String
legal -> StringArray
restricted -> StringArray
banned -> StringArray
reserved -> Boolean
reprintTimes -> Integer

multiverseId -> Integer
set -> String
code -> String
folder -> String
number -> String
flavor -> String
artist -> String
rarity -> String
watermark -> String
picture -> String
sameIndex -> Integer
formattedNumber -> String
setOrder -> Integer
reprintIndex -> Integer

function string_contains(s, str)
function string_contains_case(s, str)
function table_contains_str(t, str)
function table_contains_str_case(t, str)
function table_contains(t, element)
function table_contains_case(t, element)
]]

function has(obj, str)
	if(type(obj) == "table") then
		return table_contains_str(obj, str)
	end
	if(type(obj) == "string") then
		return string_contains(obj, str)
	end
end

white = string_contains(mana, 'W') or string_contains(colorIndicator, 'White')
blue = string_contains(mana, 'U') or string_contains(colorIndicator, 'Blue')
black = string_contains(mana, 'B') or string_contains(colorIndicator, 'Black')
red = string_contains(mana, 'R') or string_contains(colorIndicator, 'Red')
green = string_contains(mana, 'G') or string_contains(colorIndicator, 'Green')

clean = clean_str(text)

textwhite = string_contains(clean, '{W') or string_contains(clean, 'W}')
textblue = string_contains(clean, '{U') or string_contains(clean, 'U}')
textblack = string_contains(clean, '{B') or string_contains(clean, 'B}')
textred = string_contains(clean, '{R') or string_contains(clean, 'R}')
textgreen = string_contains(clean, '{G') or string_contains(clean, 'G}')
textnocolor = string_contains(clean, '{C') or string_contains(clean, 'C}')

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

function hasname(str)
	return string_contains(name, str)
end

function hastext(str)
	return string_contains(clean, str)
end

function hastype(str)
	return table_contains(types, str) or table_contains(subTypes, str) or table_contains(superTypes, str)
end

function containstype(str)
	return table_contains_str(types, str) or table_contains_str(subTypes, str) or table_contains_str(superTypes, str)
end

pn = tonumber(power)
tn = tonumber(toughness)
ln = tonumber(loyalty)

common = (rarity == "Common")
uncommon = (rarity == "Uncommon")
rare = (rarity == "Rare")
mythic = (rarity == "Mythic Rare")
special = (rarity == "Special")

w = white
u = blue
b = black
r = red
g = green

cw = w or textwhite
cu = u or textblue
cb = b or textblack
cr = r or textred
cg = g or textgreen

ccolors = 0
if cw then
	ccolors = ccolors + 1
end
if cu then
	ccolors = ccolors + 1
end
if cb then
	ccolors = ccolors + 1
end
if cr then
	ccolors = ccolors + 1
end
if cg then
	ccolors = ccolors + 1
end

a = artifact
c = creature
e = enchantment
i = instant
l = land
p = planeswalker
s = sorcery
t = tribal

m = (colors > 1)
mv = value

permanent = a or c or e or l or p

legend = isLegendary
double = isDoubleFaced

rm = rare or mythic

part = partIndex
reprint = reprintIndex
id = multiverseId
