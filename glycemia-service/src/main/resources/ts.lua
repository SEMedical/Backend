-- Redis TimeSeries
local user_key=KEYS[1]
local timestamp=ARGV[1]
local glycemia_value=ARGV[2]
local existed=redis.call('EXISTS',user_key)
if existed==0 then
    redis.call('TS.CREATE',user_key,'RETENTION',31536000000)
end
local existing=redis.call('TS.RANGE', user_key, timestamp,timestamp)
if existing==nil or #existing==0 then
    redis.call('TS.ADD',user_key,timestamp,glycemia_value)
end

