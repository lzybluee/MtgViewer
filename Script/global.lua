function table_contains_case(t, element)
	for _, value in pairs(t) do
		if value == element then
			return true
		end
	end
	return false
end

function table_contains(t, element)
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
