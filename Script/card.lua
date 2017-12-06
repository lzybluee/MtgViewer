--[[
name -> String
simpleName -> String
otherPart -> StringArray
partIndex -> Integer
isSplit -> Boolean
isDoubleFaced -> Boolean
isFlip -> Boolean
isMeld -> Boolean
isLegendary -> Boolean
isFun -> Boolean
isInCore -> Boolean
types -> StringArray
subTypes -> StringArray
superTypes -> StringArray
mana -> String
converted -> Integer
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
rarityChanged -> Boolean
reprintTimes -> Integer

multiverseid -> Integer
rating -> Float
votes -> Integer
set -> String
code -> String
folder -> String
altCode -> String
number -> String
flavor -> String
artist -> String
rarity -> String
watermark -> String
specialType -> String
picture -> String
sameIndex -> Integer
formatedNumber -> String
order -> Integer
reprintIndex -> Integer
latest -> Boolean

function table_contains_case(t, element)
function table_contains(t, element)
function string_contains_case(s, str)
function string_contains(s, str)
function table_contains_case(t, str)
function table_contains_str(t, str)
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
nocolor = string_contains(mana, 'C')

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

multicolor = (colors > 1)
mono = (colors == 1)
colorless = (colors == 0)
cl = colorless

pn = tonumber(power)
tn = tonumber(toughness)
ln = tonumber(loyalty)

common = (rarity == "Common")
uncommon = (rarity == "Uncommon")
rare = (rarity == "Rare")
mythic = (rarity == "Mythic Rare")
special = (rarity == "Special")

cm = common
uc = uncommon
rr = rare
my = mythic
sp = special

rm = rare or mythic or special

w = white
u = blue
b = black
r = red
g = green

tw = textwhite
tu = textblue
tb = textblack
tr = textred
tg = textgreen

cw = w or tw
cu = u or tu
cb = b or tb
cr = r or tr
cg = g or tg

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

nw = count_str(mana, 'W')
nu = count_str(mana, 'U')
nb = count_str(mana, 'B')
nr = count_str(mana, 'R')
ng = count_str(mana, 'G')
nc = count_str(mana, 'C')

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

permanent = a or c or e or l or p
pm = permanent

split = isSplit
double = isDoubleFaced
flip = isFlip
legend = isLegendary

reprint = reprintIndex
id = multiverseid

nm = hasname
tx = hastext
tp = containstype

vanilla = not text
v = vanilla
