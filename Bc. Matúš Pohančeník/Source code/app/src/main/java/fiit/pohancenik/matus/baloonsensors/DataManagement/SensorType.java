package fiit.pohancenik.matus.baloonsensors.DataManagement;

/**
 * Created by matus on 11. 4. 2016.
 * Enum used to identify data type read from session file
 */
public enum SensorType {

    TEMPERATURE("Temperature"),
    HUMIDITY("Humidity"),
    PRESSURE("Barometric pressure");



    private String stringValue;

    public static SensorType fromString(String text) {
        if (text != null) {
            for (SensorType b : SensorType.values()) {
                if (text.equalsIgnoreCase(b.stringValue)) {
                    return b;
                }
            }
        }
        return null;
    }
    SensorType(String toString){
        stringValue = toString;

    }

    @Override
    public String toString(){
        return stringValue;
    }


}
