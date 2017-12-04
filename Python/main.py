import base64, hashlib, hmac, random, time, urllib, json, urllib2, requests

'''               
@param id ID da API gerada pelo usuário na NegocieCoins.
@param key Chave da API gerada pelo usuário na NegocieCoins.
@param url URL da API da NegocieCoins.
@param method Método utilizado na conexão com a API, pode ser GET, POST ou DELETE.
@param body Corpo de envio da requisição, nos métodos GET e DELETE ele é nulo.
@return AMXHeader Cabeçalho no formato desejado para se autenticar na API da NegocieCoins.         
'''
def amx_authorization_header(id, key, url, method, body):
   encoded_url = urllib.quote_plus(url).lower()
   method = method.upper()
   ##==================Tratando o content=====================================
   m = hashlib.md5()
   m.update(str(body))
   content = '' if body == None else base64.b64encode(m.digest())
   ##=========================================================================
   timestamp = str(int(time.time()))
   nonce = str(random.randint(0, 100000000))
   data = ''.join([id, method, encoded_url, timestamp, nonce, content]).encode()  
   secret = base64.b64decode(key)
   signature = str(base64.b64encode(hmac.new(secret, msg=data, digestmod=hashlib.sha256).digest()))
   ##===================Cabeçalho no formato AMX==============================
   header = 'amx %s' % ':'.join([id, signature, nonce, timestamp])
 
   return header      
       
'''
@param url URL da API da NegocieCoins.
@param header Cabeçalho no formato desejado para se autenticar na API da NegocieCoins.
@param jsondata Corpo de envio da requisição, nos métodos GET e DELETE ele é nulo.
@return  APIResponse Resposta em json da API.
'''
def exchange(url, header, metodo, jsondata):    
    if(metodo=='GET'):
       webpage = requests.get(url, headers={'Authorization': header, 'User-Agent': 'Mozilla/5.0 (Windows NT 6.1; WOW64; rv:25.0) Gecko/20100101 Firefox/25.0'})       
    elif(metodo=='POST'):
       webpage = requests.post(url, data=jsondata, headers={'Authorization': header, 'User-Agent': 'Mozilla/5.0 (Windows NT 6.1; WOW64; rv:25.0) Gecko/20100101 Firefox/25.0', 'Content-Type': 'application/json'})
    return webpage.json()

id1 = 'youID'
key = 'youKEY'
url1 = 'https://broker.negociecoins.com.br/tradeapi/v1/'
method = 'POST'
function = 'user/orders'
urlFinal = ''.join([url1, function])
jsonstr = '''{"pair" : "BRLBTC","type":"sell","status":"filled","startId": 1, "endId": 9999999,"startDate":"2017-01-01","endDate":"2017-12-31"}'''
result2 = amx_authorization_header(id1, key, urlFinal, method, jsonstr)

print(result2)

print(exchange(str(urlFinal), result2, method, jsonstr))


