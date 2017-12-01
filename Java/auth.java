
/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package API.Trade;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.net.URL;
import java.net.URLEncoder;
import java.security.MessageDigest;
import java.sql.Timestamp;
import java.util.Base64;
import javax.crypto.Mac;
import javax.crypto.SecretKey;
import javax.crypto.spec.SecretKeySpec;
import javax.net.ssl.HttpsURLConnection;
import static org.apache.commons.codec.binary.Base64.*;

/**
 *
 * @author Marlon Prudente <m.prudente at btc-banco.com>
 * <marlonoliveira at alunos.utfpr.edu.br>
 */
public class Authentication {

    private static String computeSignature(String data, String secret) throws Exception {

        SecretKey secretKey = null;
        byte[] keyBytes = decodeBase64(secret);
        secretKey = new SecretKeySpec(keyBytes, "HmacSHA256");
        Mac mac = Mac.getInstance("HmacSHA256");

        mac.init(secretKey);

        byte[] text = data.getBytes();
        return new String(encodeBase64(mac.doFinal(text))).trim();
    }

    public String amx_authorization_header(String APIurl, String APIid, String APIkey, String APIfuncao, String APImetodo, String APIbody) throws Exception {

        String url = URLEncoder.encode(APIurl, "UTF-8").toLowerCase();
        String metodo = APImetodo.toUpperCase();
        String requestContentBase64String = "";
        
        /**Calcular contet se o mesmo não for nulo (para requisições tipo POST*/
        if (APIbody != null && !"".equals(APIbody)) {
            byte[] post = APIbody.getBytes();
            MessageDigest md5 = MessageDigest.getInstance("MD5");
            byte[] requestContentHash = md5.digest(post);
            requestContentBase64String = Base64.getEncoder().encodeToString(requestContentHash);            
        }

        /**Calculando UNIX Time: Gerando um requestTimeStamp*/
        Timestamp epochStart = new Timestamp(70, 00, 00, 21, 00, 00, 00);  /**Parametros para iniciar 01/01/1970 00:00:00 UTC*/
        Timestamp timestamp = new Timestamp(System.currentTimeMillis());       
        String time = "" + ((timestamp.getTime() - epochStart.getTime()) / 1000); /**Diferença entre datas, é dividido por 1000 para ficar no tamanho correto*/
        
        /**Gerando NONCE, variável aleatória para autenticação*/
        String nonce = java.util.UUID.randomUUID().toString().replace("-", "");
        
        /**Criando assinatura*/
        String signatureRawData = String.format("%s%s%s%s%s%s", APIid, metodo, url, time, nonce, requestContentBase64String);
        System.out.println("signatureRawData: " + signatureRawData);
        //Criando assinatura criptografada em hmacSha256
        String requestSignatureBase64String = computeSignature(signatureRawData, APIkey);  
        
        //Criando Header AMX, a partir dos parâmetros gerados  
        return String.format("amx %s:%s:%s:%s", APIid, requestSignatureBase64String, nonce, time); //Formato Header 
    }

    public String request(String APIid, String APIkey, String function, String method, String data) throws Exception {
       
        //Criando parâmetros para conexão com a NegocieCoins
        URL url = new URL("https://broker.negociecoins.com.br/tradeapi/v1/" + function);
        HttpsURLConnection urlConnection = (HttpsURLConnection) url.openConnection();   
        urlConnection.addRequestProperty("User-Agent", "Mozilla/5.0 (Windows NT 6.1; WOW64; rv:25.0) Gecko/20100101 Firefox/25.0");
        urlConnection.setReadTimeout(15000);
        urlConnection.setConnectTimeout(15000);
        urlConnection.setRequestMethod(method);
        urlConnection.setDoOutput(true);
        urlConnection.setRequestProperty("Content-Type", "application/json; charset=UTF-8"); 
        
        //Setando condições e atribuições para cada tipo de requisição
        if ("POST".equals(method.toUpperCase())) {
            urlConnection.setRequestProperty("Content-Length", Integer.toString(data.length()));
            //urlConnection.setRequestProperty("Content", data);
            String header = amx_authorization_header(url.toString(),APIid, APIkey, function, method, data);
            urlConnection.setRequestProperty("Authorization", header);
            //Tentar passar Conteudo via POST
            try (OutputStream os = urlConnection.getOutputStream()) {
                os.write(data.getBytes("UTF-8"));
            }            
            
        } else {
            //GET e DELETE não precisa de Content-Length
            String header = amx_authorization_header(url.toString(),APIid, APIkey, function, method, data);
            urlConnection.setRequestProperty("Authorization", header);
        }
        
        BufferedReader reader = null;
        try {  
            //Receber resposta da requisição
            reader = new BufferedReader(new InputStreamReader(urlConnection.getInputStream()));
            StringBuilder buffer = new StringBuilder();
            int read;
            char[] chars = new char[1024];
            while ((read = reader.read(chars)) != -1) {
                buffer.append(chars, 0, read);
            }
            return buffer.toString();

        } finally {
            if (reader != null) {
                reader.close();
            }
        }

    }

}
