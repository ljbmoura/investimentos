package br.com.ljbm.servico;

import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;
import java.io.IOException;

public class AnbimaAPIClient {

//    private static final String API_URL = "https://api-sandbox.anbima.com.br/feed/precos-indices/v1/titulos-publicos/mercado-secundario-TPF?data=2024-08-30";
//	private static final String ACCESS_TOKEN = "XFhCgM4gk8TT";

//	private static final String API_URL = "https://developers.b3.com.br:8065/homebroker/api/bonds/v1/164";
	private static final String API_URL = "https://www.tesourodireto.com.br/json/br/com/b3/tesourodireto/service/api/treasurybondsinfo.json";
	private static final String ACCESS_TOKEN = "SAX0tO5b9AxqSX64kSQOzN7HJOhd4PFPTlcjRooVmyoQV71osaAqC8";
    
//    curl -X GET "https://api.anbima.com.br/feed/fundos/v1/fundos?page=0&size=1000" -H "accept: application/json" -H "client_id: l9fpgpHQsJgu" -H "access_token: 6QD9w3SoSZoy"
    
    // aqFOmn9oeJ2T / l9fpgpHQsJgu
//    jVKod9hZD4Ec
    
//    curl -X GET "https://api-sandbox.anbima.com.br/feed/precos-indices/v1/titulos-publicos/mercado-secundario-TPF?data=2024-08-30" 
//    	-H "accept: application/json" 
//    	-H "client_id: aqFOmn9oeJ2T" 
//    	-H "access_token: jVKod9hZD4Ec"
    
    public static String makeApiCall() throws IOException {
        OkHttpClient client = new OkHttpClient();

        Request request = new Request.Builder()
        		.url(API_URL)
//                .addHeader("Authorization", "Bearer: " + ACCESS_TOKEN)
                .addHeader("accept", "application/json")

//                .addHeader("access_token", ACCESS_TOKEN)
//                .addHeader("client_id", "aqFOmn9oeJ2T")
//                .addHeader("accept", "application/json")
            .build();

        try (Response response = client.newCall(request).execute()) {
            if (!response.isSuccessful()) {
            	System.out.println(response.toString());
            	System.out.println(response.networkResponse());
                throw new IOException("Unexpected code " + response);
            }
            return response.body().string();
        }
    }    

    public static void main(String[] args) {
        try {
            String response = makeApiCall();
            System.out.println(response);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }


}
