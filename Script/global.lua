function table_contains(t, element)
	for _, value in pairs(t) do
		if value == element then
			return true
		end
	end
	return false
end

function string_contains(s, color)
	if(s and string.find(s, color)) then
		return true
	else
		return false
	end
end
