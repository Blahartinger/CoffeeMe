package vouchr.coffeemachine.app.coffeeme;

import com.google.api.client.extensions.android.http.AndroidHttp;
import com.google.api.client.googleapis.extensions.android.gms.auth.GoogleAccountCredential;
import com.google.api.client.http.HttpTransport;
import com.google.api.client.json.JsonFactory;
import com.google.api.client.json.jackson2.JacksonFactory;
import com.google.api.services.sheets.v4.model.AppendValuesResponse;
import com.google.api.services.sheets.v4.model.ValueRange;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import vouchr.coffee.models.CoffeePot;
import vouchr.coffee.models.CoffeePotBuilder;

/**
 * Created by Bryan on 2017-03-16.
 */

public class CoffeeSheetService {

    private static final String spreadsheetId = "1a5KdfYJdqvlYzv2BscGwxeZ2cf880HGAK_keWCbijOE";

    private com.google.api.services.sheets.v4.Sheets gSheetsService = null;
    private HttpTransport transport = null;
    private JsonFactory jsonFactory = null;

    public CoffeeSheetService(GoogleAccountCredential googleAccountCredential) {
        transport = AndroidHttp.newCompatibleTransport();
        jsonFactory = JacksonFactory.getDefaultInstance();
        gSheetsService = new com.google.api.services.sheets.v4.Sheets.Builder(transport, jsonFactory, googleAccountCredential).setApplicationName("CoffeeMe").build();
    }

    public List<CoffeePot> getCoffeePots() throws IOException {
        String range = "Coffee Scores!A2:O";
        ValueRange response = this.gSheetsService.spreadsheets().values().get(spreadsheetId, range).execute();
        List<List<Object>> values = response.getValues();
        List<CoffeePot> results = new ArrayList<>();
        if (values != null) {
            for (List row : values) {
                String dateString = (String) row.get(0);
                String barista = (String) row.get(1);
                String beans = (String) row.get(2);
                String roast = (String) row.get(3);
                Float tbsp = Float.parseFloat((String) row.get(4));
                Double avgRating = Double.parseDouble((String) row.get(5));
                CoffeePot pot = CoffeePotBuilder.init().setDateString(dateString)
                        .setBarista(barista)
                        .setBeanName(beans)
                        .setRoast(roast)
                        .setTbspCount(tbsp)
                        .setAvgRating(avgRating)
                        .createCoffeePot();

                results.add(pot);
            }
        }
        return results;
    }

    public Boolean addNewPot(CoffeePot pot) {
        String newPotRange = null;
        try {
            int rowIndex = getCoffeePots().size();
            newPotRange = "A" + rowIndex + ":O" + rowIndex;
            ValueRange newPotRow = new ValueRange();
            newPotRow.setValues(Collections.singletonList(Arrays.<Object>asList(pot.getDateString(), pot.getBarista(), pot.getBeanName(), pot.getRoast(), pot.getTbspCount(), pot.getAvgRating())));
            AppendValuesResponse response = this.gSheetsService.spreadsheets().values().append(spreadsheetId, newPotRange, newPotRow).setValueInputOption("RAW").execute();
            return response.getUpdates().getUpdatedRows() > 0;
        } catch (IOException e) {
            e.printStackTrace();
            return false;
        }
    }

    public List<String> getBeans() throws IOException {
        String range = "Beans!A2:A";
        List<String> results = new ArrayList<String>();
        ValueRange response = this.gSheetsService.spreadsheets().values().get(spreadsheetId, range).execute();
        List<List<Object>> values = response.getValues();
        if (values != null) {
            for (List row : values) {
                results.add((String) row.get(0));
            }
        }
        return results;
    }

    public List<String> getRoasts() throws IOException {
        String range = "Roasts!A:A";
        List<String> results = new ArrayList<String>();
        ValueRange response = this.gSheetsService.spreadsheets().values().get(spreadsheetId, range).execute();
        List<List<Object>> values = response.getValues();
        if (values != null) {
            for (List row : values) {
                results.add((String) row.get(0));
            }
        }
        return results;
    }

    public List<String> getBaristas() throws IOException {
        String range = "Baristas!A2:A";
        List<String> results = new ArrayList<String>();
        ValueRange response = this.gSheetsService.spreadsheets().values().get(spreadsheetId, range).execute();
        List<List<Object>> values = response.getValues();
        if (values != null) {
            for (List row : values) {
                results.add((String) row.get(0));
            }
        }
        return results;
    }
}
