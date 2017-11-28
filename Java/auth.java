
/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package API.Trade;

import java.io.BufferedReader;
import java.io.DataOutputStream;
import java.io.InputStreamReader;
import java.net.URL;
import java.net.URLEncoder;
import java.security.MessageDigest;
import java.sql.Timestamp;
import java.text.SimpleDateFormat;
import java.util.Base64;
import java.util.TimeZone;
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

    public String amx_authorization_header(String APIid, String APIkey, String APIfuncao, String APImetodo, String APIbody) throws Exception {
        String url1 = "https://broker.negociecoins.com.br/tradeapi/v1/" + APIfuncao;
        String url = URLEncoder.encode(url1, "UTF-8").toLowerCase();
        String metodo = APImetodo.toUpperCase();
        String content = "";        
        
        //APIbody = "{\"page\":\"1\",\"pageSize\":\"2\",\"pair\":\"BRLBTC\",\"type\":\"buy\",\"status\":\"pending\"}";
        System.out.println("APIbody==>" + APIbody);
        
        if(APIbody != ""){
            byte[] post =  APIbody.getBytes("UTF-8"); 
            MessageDigest md5 = MessageDigest.getInstance("MD5");            
            byte[] requestContentHash = md5.digest(post);
            content = Base64.getUrlEncoder().encodeToString(requestContentHash); 
            //content = "IpapEctISNd9w+TqkCQUKg==";
            System.out.println("Content: " + content);
        }        
        //Calculando UNIX Time: Gerando um requestTimeStamp
        Timestamp epochStart = new Timestamp(70, 00, 00, 21, 00, 00, 00);
        SimpleDateFormat data2 = new SimpleDateFormat("dd/MM/yyyy HH:mm:ss");
        data2.setTimeZone(TimeZone.getTimeZone("UTC"));
        String date3 = data2.format(epochStart.getTime());
        System.out.println("Data: " + date3);
        Timestamp timestamp = new Timestamp(System.currentTimeMillis());
        SimpleDateFormat data1 = new SimpleDateFormat("dd/MM/yyyy HH:mm:ss");
        data1.setTimeZone(TimeZone.getTimeZone("UTC"));
        String date = data1.format(timestamp.getTime());
        System.out.println("Data atual: " + date);
        String time = "" + (( timestamp.getTime()- epochStart.getTime())/1000);
        System.out.println("TIME: " + time);          
        
        String nonce = java.util.UUID.randomUUID().toString().replace("-", "");
        String dados = String.format("%s%s%s%s%s%s", APIid, metodo, url, time, nonce, content);
        System.out.println("Signature: " + dados);
        String header = String.format("amx %s:%s:%s:%s", APIid, computeSignature(dados,APIkey), nonce, time); //Formato Header
        return header;
    }

    public String request(String header, String function, String method, String data) throws Exception{
        
        URL url = new URL("https://broker.negociecoins.com.br/tradeapi/v1/" + function);
        HttpsURLConnection urlConnection = (HttpsURLConnection) url.openConnection();
        //======================================================================       
        urlConnection.addRequestProperty("User-Agent", "Mozilla/5.0 (Windows NT 6.1; WOW64; rv:25.0) Gecko/20100101 Firefox/25.0");
        urlConnection.setReadTimeout(15000);
        urlConnection.setConnectTimeout(15000);
        urlConnection.setRequestMethod(method); 
        urlConnection.setDoInput(true);
        urlConnection.setDoOutput(true);
        urlConnection.setRequestProperty("Content-Type", "application/json; charset=UTF-8");
        urlConnection.setRequestProperty("Content-Length", Integer.toString(data.getBytes().length));
        //urlConnection.setRequestProperty("Content", data);
        urlConnection.setRequestProperty("Authorization", header);
        //urlConnection.connect();
        //======================================================================
        BufferedReader reader = null;
        try{
            //===========SEND REQUEST
            if("POST".equals(method.toUpperCase())){
            DataOutputStream wr = new DataOutputStream(urlConnection.getOutputStream());
            System.out.println("URLEncoder to POST: " + URLEncoder.encode(data,"UTF-8"));
            wr.writeBytes(URLEncoder.encode(data,"UTF-8"));
            wr.flush();
            wr.close();
            }
            //===========GET RESPONSE
            reader = new BufferedReader(new InputStreamReader(urlConnection.getInputStream()));
            StringBuffer buffer = new StringBuffer();
            int read;
            char[] chars = new char[1024];
            while ((read = reader.read(chars)) != -1) {
                buffer.append(chars, 0, read);
            }
            return buffer.toString();
            //===========
            
        } finally {
            if (reader != null) {
                reader.close();
            }
        }       
        //======================================================================
        
    }

}
