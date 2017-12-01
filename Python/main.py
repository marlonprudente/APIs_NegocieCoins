import base64, hashlib, hmac, random, time, urllib, json, urllib2, requests


def amx_authorization_header(id, key, url, method, body):
   print('Metodo: ' + method)
   encoded_url = urllib.quote_plus(url).lower()
   method = method.upper()
   ##Trabalhando content///////////////////////////////////////////////////////
   m = hashlib.md5()
   m.update(str(body))
   content = '' if body == None else base64.b64encode(m.digest())
   print('content: ' + content)
   ##==================////////////////////////////////////////////////////////
   timestamp = str(int(time.time()))
   nonce = str(random.randint(0, 100000000))
   data = ''.join([id, method, encoded_url, timestamp, nonce, content]).encode()
   print('DATA: ' + data)
   secret = base64.b64decode(key)
   signature = str(base64.b64encode(hmac.new(secret, msg=data, digestmod=hashlib.sha256).digest()))
   header = 'amx %s' % ':'.join([id, signature, nonce, timestamp])
   print('HEADER: ' + header)
   return header             


def exchange(url, result, metodo, jsondata):
    
    if(metodo=='GET'):
       webpage = requests.get(url, headers={'Authorization': result, 'User-Agent': 'Mozilla/5.0 (Windows NT 6.1; WOW64; rv:25.0) Gecko/20100101 Firefox/25.0'})
       
    elif(metodo=='POST'):
       webpage = requests.post(url, data=jsondata, headers={'Authorization': result, 'User-Agent': 'Mozilla/5.0 (Windows NT 6.1; WOW64; rv:25.0) Gecko/20100101 Firefox/25.0', 'Content-Type': 'application/json'})

    return webpage.json()

id1 = 'youID'
key = 'youKey'
url1 = 'https://broker.negociecoins.com.br/tradeapi/v1/'
method = 'POST'
function = 'user/orders'
urlFinal = ''.join([url1, function])
jsonstr = '''{"pair" : "BRLBTC","type":"sell","status":"filled","startId": 1, "endId": 9999999,"startDate":"2017-01-01","endDate":"2017-12-31"}'''
##jsonstr = ''
result2 = amx_authorization_header(id1, key, urlFinal, method, jsonstr)

print(result2)

print(exchange(str(urlFinal), result2, method, jsonstr))


