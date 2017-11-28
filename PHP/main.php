<!DOCTYPE html>
<!--
To change this license header, choose License Headers in Project Properties.
To change this template file, choose Tools | Templates
and open the template in the editor.
-->
<html>
    <head>
        <meta charset="UTF-8">
        <title></title>
    </head>
    <body>
        <?php

        // put your code here
        function amx_authorization_header($id, $key, $function, $method, $body) {
            $url1 = 'https://broker.negociecoins.com.br/tradeapi/v1/' . $function; //URL + Função Ex: user/balance
            date_default_timezone_set('America/Sao_Paulo'); //Setando Timezone para BR
            $url = strtolower(urlencode($url1)); //Colocando a URL em Minusculo e fazendo o encode
            //echo $url;
            $method = strtoupper($method); //Colocando o metodo em Maiusculo EX: get to GET
            $content = empty($body) ? '' : base64_encode(md5($body, true)); //se body está vazio, deixa ''(nulo, senão usa o base64_encode()
            $time = time(); //Tempo
            //echo $time;
            $nonce = uniqid(); //RandomID
            $data = implode('', [$id, $method, $url, $time, $nonce, $content]); //String sem separação com as variáveis que estão sendo mostradas
            $secret = base64_decode($key); //decode no password vetor de byte
            //echo $secret;
            $signature = base64_encode(hash_hmac('sha256', $data, $secret, true)); //Encode na signature utilizando hash_hmac

            return 'Authorization: amx ' . implode(':', [$id, $signature, $nonce, $time]); //retorna a header
        }

        function userbalance($id, $key, $function, $body = null) {
            $method = 'GET';
            $result = amx_authorization_header($id, $key, $function, 'GET', $body = null);
            return request($result, $function, $method);
        }

        function userorders($id, $key, $function, $body) {
            $method = 'POST';

            $result = amx_authorization_header($id, $key, $function, $method, $body);
            return request($result, $function, $method, $array);
        }

        function request($header, $function, $method, $data = Array()) {
            $postdata = http_build_query($data, '', '&', PHP_QUERY_RFC3986);

            $url = 'https://broker.negociecoins.com.br/tradeapi/v1/' . $function;

            $post_data = json_encode($data);

            if ($method == 'GET') {
                $context = stream_context_create(array(
                    'http' => array(
                        'header' => $header,
                )));
            } else {

                $context = stream_context_create(array(
                    'http' => array(
                        'method' => 'POST',
                        'header' => "Content-Type: application/json; charset=UTF-8" . "\r\n" .
                        'Content-Length: ' . strlen($postdata) . "\r\n" .
                        $header . "\r\n",
                        'content' => $postdata,
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
