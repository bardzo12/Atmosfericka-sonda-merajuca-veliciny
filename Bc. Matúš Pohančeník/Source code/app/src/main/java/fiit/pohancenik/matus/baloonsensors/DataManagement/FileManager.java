package fiit.pohancenik.matus.baloonsensors.DataManagement;


import android.content.Context;


import android.os.Environment;
import android.util.Log;

import com.opencsv.CSVReader;
import com.opencsv.CSVWriter;

import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

/**
 * Created by matus on 19. 2. 2016.
 * This class is used to interact with files.
 */
public class FileManager {

    private Context context;
    private CSVWriter csvWriter;


    public FileManager(Context context){
        this.context = context;

    }



    public boolean deleteFile(String filePath){// when user decides delete session data, this function is called
        File file = new File(filePath);
        boolean deleted = file.delete();

        return deleted;
    }



    public File getPackageFolder() { // In the beginning of data saving process, this function is called
                                    // and package folder is returned.
                                    // If sd card is mounted, function returns sc card storage, if it isn't emulated external
                                    // storage is returned


            File[] files = context.getExternalFilesDirs(null);

            if(files[1] != null ){
                Log.i("File Manager","Returning sd card storage");
                return files[1];
            }else{
                Log.i("File Manager","Returning emulated external storage");
                return files[0];
            }


    }


    public File getFile(String filePath,String fileName) {// function needs as parameters absolute path to folder and filename with
                                                          // appropriate extension

        String state = Environment.getExternalStorageState();

        if (Environment.MEDIA_MOUNTED.equals(state)) { // if external storage is present
                                                        // doesn't matter if it is sd card or emulated external storage
            File exportDir = new File(filePath);
            if (!exportDir.exists()) {
                if (!exportDir.mkdirs()) {
                    Log.e("File Manager", "Directory not created");

                }
            }


            File file ;


            try {
                file = new File(exportDir, fileName);
                file.createNewFile();
                return file;

            } catch (IOException e) {
                e.printStackTrace();
                Log.e("File Manager","File not created");
            }


        }
        return null;

}




    public CSVWriter openStreamForFileCSV(String filePath) throws IOException { // function opens CSVWriter
        csvWriter = new CSVWriter(new FileWriter(filePath),';', CSVWriter.NO_QUOTE_CHARACTER);

        return csvWriter;
    }




    public void closeStreamForFileCSV() throws IOException { // close CSVWriter
        csvWriter.close();

    }

    public void writeToCSVFile(CSVWriter csvWriter, String[] data) { // write dat into the file


        csvWriter.writeNext(data);

    }

    public List readCSVFile(String filePath) throws IOException { // read CSV file and return data in list
        List<String[]> dataList = new ArrayList<String[]>();
        CSVReader reader = new CSVReader(new FileReader(filePath),';');
        String [] nextLine;
        while ((nextLine = reader.readNext()) != null) {
            // nextLine[] is an array of values from the line
            dataList.add(new String[]{nextLine[0],nextLine[1]});
        }


        return dataList;
    }




}