function table_contains_case(t, element)
	if(not t) then
		return false
	end
	for _, value in pairs(t) do
		if value == element then
			return true
		end
	end
	return false
end

function table_contains(t, element)
	if(not t) then
		return false
	end
	for _, value in pairs(t) do
		if string.lower(value) == string.lower(element) then
			return true
		end
	end
	return false
end

function string_contains_case(s, str)
	if(s and string.find(s, str)) then
		return true
	else
		return false
	end
end

function string_contains(s, str)
	if(s and string.find(string.lower(s), string.lower(str))) then
		return true
	else
		return false
	end
end

function table_contains_str_case(t, str)
	if(not t) then
		return false
	end
	for _, value in pairs(t) do
		if string.find(value, str) then
			return true
		end
	end
	return false
end

function table_contains_str(t, str)
	if(not t) then
		return false
	end
	for _, value in pairs(t) do
		if string.find(string.lower(value), string.lower(str)) then
			return true
		end
	end
	return false
end

function clean_str(str)
	if(str) then
		return string.gsub(str, ' ?%(.-%)', '')
	end
end

function count_str(s, str)
	n = 0
	if(not s) then
		return 0
	end
	for _, _ in string.gmatch(s, str) do
		n = n + 1
	end
	return n
end

function str_to_number(str)
	if(not str) then
		return 0
	end
	n = tonumber(str)
	if(not n) then
		if(str == '*' or string.find(str, '%^')) then
			return 0
		elseif(string.find(str, '%*') == 1) then
			s = string.gsub(str, '%*', '')
			return tonumber(s)
		else
			s = string.gsub(str, '[-+*]', '')
			return tonumber(s)
		end
	end
	return n
end
