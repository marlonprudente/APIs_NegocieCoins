import base64, hashlib, hmac, random, time, urllib, _md5
from urllib.request import urlopen, Request

def amx_authorization_header(id, key, url, method, body=None):
   encoded_url = urllib.parse.quote_plus(url).lower()
   method = method.upper()
   ##Trabalhando content///////////////////////////////////////////////////////
   content = '' if body == None else base64.b64encode(_md5.new(body).digest())
   ##==================////////////////////////////////////////////////////////
   timestamp = str(int(time.time()))
   nonce = str(random.randint(0, 100000000))
   data = ''.join([id, method, encoded_url, timestamp, nonce, content]).encode()
   secret = base64.b64decode(key)
   signature = str(base64.b64encode(hmac.new(secret, msg=data, digestmod=hashlib.sha256).digest()), 'utf-8')
   header = 'amx %s' % ':'.join([id, signature, nonce, timestamp])
   print('HEADER: ' + header)
   return header             
##base64.b64encode(hashlib.md5.new(body).digest())

def exchange(url, result):
    req = Request(url, headers={'Authorization': result, 'User-Agent': 'Mozilla/5.0 (Windows NT 6.1; WOW64; rv:25.0) Gecko/20100101 Firefox/25.0'})
    webpage = urllib.request.urlopen(req).read()
    return webpage

id1 = 'youID'
key = 'youkey'
url1 = 'https://broker.negociecoins.com.br/tradeapi/v1/'
method = 'GET'
function = 'user/balance'
urlFinal = ''.join([url1, function])
##body = "{'pair':'BRLBTC','type':'sell','status':'filled','startId':1,'endId':9999999,'startDate':'2017-01-01','endDate':'2017-12-31'}"
result2 = amx_authorization_header(id1, key, urlFinal, method, body=None)

print(result2)

print(exchange(str(urlFinal), result2))


