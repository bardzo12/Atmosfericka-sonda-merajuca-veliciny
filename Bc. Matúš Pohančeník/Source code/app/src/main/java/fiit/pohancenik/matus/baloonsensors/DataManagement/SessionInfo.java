package fiit.pohancenik.matus.baloonsensors.DataManagement;


/**
 * Created by matus on 3. 4. 2016
 * This class is used to save info about session into database.
 */
public class SessionInfo {

    private int ID;
    private String filePath;
    private String date;
    private String name;

    public SessionInfo(){
    }

    public SessionInfo(String filePath, String name, String date){
        this.filePath = filePath;
        this.name = name;
        this.date = date;
    }

    public void setID(int ID){
        this.ID = ID;
    }

    public void setFilePath(String fileName){
        this.filePath = fileName;
    }

    public void setDate(String date){
        this.date = date;
    }

    public void setName(String name){
        this.name = name;
    }

    public int getID(){
        return this.ID;
    }

    public String getFilePath(){
        return this.filePath;
    }

    public String getDate(){
        return this.date;
    }

    public String getName(){
        return this.name;
    }


}
