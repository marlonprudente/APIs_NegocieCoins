<!DOCTYPE html>
<!--
API desenvolvida pela NegocieCoins para acesso as funcionalidades da exchange.
 @author Marlon Prudente < m.prudente at btc-banco.com > < marlonoliveira at alunos.utfpr.edu.br > 
-->
<html>
    <head>
        <meta charset="UTF-8">
        <title></title>
    </head>
    <body>
        <?php

        /**
         * 
         * @param type $id ID da API gerada pelo usuário na NegocieCoins.
         * @param type $key Chave da API gerada pelo usuário na NegocieCoins.
         * @param type $function Função desejada da API, compõe o final da URL da API.
         * @param type $method Método utilizado na conexão com a API, pode ser GET, POST ou DELETE.
         * @param type $body Corpo de envio da requisição, nos métodos GET e DELETE ele é nulo.
         * @return type AMXHeader Cabeçalho no formato desejado para se autenticar na API da NegocieCoins.
         */
        function amx_authorization_header($id, $key, $function, $method, $body) {
            $url1 = 'https://broker.negociecoins.com.br/tradeapi/v1/' . $function; //URL + Função Ex: user/balance
            date_default_timezone_set('America/Sao_Paulo'); //Setando Timezone para BR
            $url = strtolower(urlencode($url1)); //Colocando a URL em Minusculo e fazendo o encode        
            $content = empty($body) ? '' : base64_encode(md5($body, true)); //se body está vazio, deixa ''(nulo, senão usa o base64_encode()
            $time = time(); //Tempo  
            $nonce = uniqid(); //É gerado um número randonico aleatório, para compor o cabeçalho AMX.
            $data = implode('', [$id, strtoupper($method), $url, $time, $nonce, $content]); //String sem separação com as variáveis que estão sendo mostradas
            $secret = base64_decode($key); //decode no password vetor de byte
            $signature = base64_encode(hash_hmac('sha256', $data, $secret, true)); //Encode na signature utilizando hash_hmac

            return 'amx ' . implode(':', [$id, $signature, $nonce, $time]); //retorna a header
        }

        /**
         * 
         * @param type $id ID da API gerada pelo usuário na NegocieCoins.
         * @param type $key Chave da API gerada pelo usuário na NegocieCoins.
         * @param type $function Função desejada da API, compõe o final da URL da API.
         * @param type $body Corpo de envio da requisição, nos métodos GET e DELETE ele é nulo.
         * @return type balance Balanço do usuário de todas as moedas na NegocieCoins.
         */
        function userbalance($id, $key, $function, $body = null) {
            $method = 'GET';
            $result = amx_authorization_header($id, $key, $function, 'GET', $body = null);
            return request($result, $function, $method, $body = null);
        }

        /**
         * 
         * @param type $id ID da API gerada pelo usuário na NegocieCoins.
         * @param type $key Chave da API gerada pelo usuário na NegocieCoins.
         * @param type $function Função desejada da API, compõe o final da URL da API.
         * @param type $body Corpo de envio da requisição, nos métodos GET e DELETE ele é nulo.
         * @return type orders Ordens do usuário de todas as moedas na NegocieCoins.
         */
        function userorders($id, $key, $function, $body) {
            $method = 'POST';
            $result = amx_authorization_header($id, $key, $function, $method, $body);
            return request($result, $function, $method, $body);
        }

        /**
         * 
         * @param type $header Cabeçalho no formato AMX, utilizado na autenticação.
         * @param type $function Função desejada da API, compõe o final da URL da API.
         * @param type $method Método utilizado na conexão com a API, pode ser GET, POST ou DELETE.
         * @param type $data Corpo de envio da requisição, nos métodos GET e DELETE ele é nulo.
         * @return type APIResponse Resposta em json da API.
         */
        function request($header, $function, $method, $data) {

            $url = 'https://broker.negociecoins.com.br/tradeapi/v1/' . $function;

            if ($method == 'GET' || $method == 'DELETE') {
                print_r("Entrei no if\n");
                $context = stream_context_create(array(
                    'http' => array(
                        'header' => "Authorization: " . $header,
                )));
            } else {
                $context = stream_context_create(array(
                    'http' => array(
                        'method' => 'POST',
                        'header' => "Authorization: " . $header . "\r\n" . 'content-length: ' . strlen($data) . "\r\n" . "content-type: application/json; charset=UTF-8" . "\r\n",
                        'content' => $data,
                )));
            }
            $result = file_get_contents($url, false, $context);
            $res_json = json_decode($result, true);
            return $res_json;
        }
        
        ?>


        <h1>NEGOCIECOINS - API - PHP</h1>

        <div style="float:left; width:49%;">
            <fieldset>                
                App ID: <input type="text" ID="TextBoxAppId" runat="server" Width="500px"><br />
                App Key: <input type="text" ID="TextBoxAppKey" runat="server" Width="500px"><br />
            </fieldset>

            <fieldset>
                <h3>GET "user/balance"</h3>
                <input type="button" ID="ButtonSaldo" runat="server" Text="Balance" OnClick="" value="Balance" />
            </fieldset>
            <fieldset>
                <p>TESTE</p>
            </fieldset>
        </div>
    </body>
</html>
