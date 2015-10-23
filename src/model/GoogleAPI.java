package model;

/**
 * Created by Martina on 21.10.2015.
 */
public class GoogleAPI extends TableItem {
    private String api_key;
    private String name;

    public GoogleAPI(String api_key, String name) {
        this.api_key = api_key;
        this.name = name;
    }

    public String getApi_key() {
        return api_key;
    }

    public void setApi_key(String api_key) {
        this.api_key = api_key;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }
}
