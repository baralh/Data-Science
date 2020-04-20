//Heman Baral
//V00622181
//CMSC 435
//Assignment 2



/*

To Do:

1. Mean Imputation
2. Conditional Mean Imputation
3. Hot Deck Imputation
4. Conditional Hot Deck Imputation
5. Calculation of the Mean Absolute Error

*/

import java.util.Scanner;
import java.util.ArrayList;
import java.util.HashMap;
import java.io.*;

public class a2 {

    public static class DataSet {


        public int columns;

        public int row;


        public ArrayList<String[]> data = new ArrayList<String[]>();

        public ArrayList<Double[]> new_data = new ArrayList<Double[]>();

        public ArrayList<HashMap<String, Object>> missVal;

        public DataSet(ArrayList<String[]> data) {

            this.data = data;
        }

        public DataSet(String filePath) throws IOException {

            this.data = readCsv(filePath);

            this.missVal = new ArrayList<>();

            this.new_data = new ArrayList<>();

            this.row = data.size();

            this.columns = data.get(1).length;

            Missing_Values();

            to_double();
        }



        public ArrayList<Double> column(int column){

            ArrayList<Double> a = new ArrayList<Double>();

            for (Double[] r : this.new_data){

                a.add(r[ column]);
            }

            return a;
        }




        public ArrayList<Double> columnCond(int column, String cond){

            ArrayList<Double> a = new ArrayList<Double>();

            for (int i=0; i<new_data.size();i++){

                if(data.get(i)[8].equals(cond)) {

                    a.add(new_data.get(i)[column]);
                }
            }

            return a;
        }



         public void imputation(){

             //HashMap<String, Object> missHash = new HashMap<String, Object>();

            for(HashMap<String,Object> missHash : missVal){
                
                //Mean Imputation 
                missHash.put("mean", Calc_mean(column((int)missHash.get("column"))));

                //Conditional mean imputation 
                missHash.put("mean_conditional",

                    Calc_mean(

                        columnCond( (int)missHash.get("column"), data.get((int)missHash.get("row"))[8])));

                //Hot Deck Imputation 
                missHash.put("hd",

                    Calc_hotDeck( (int) missHash.get("row"),(int) missHash.get("column")));
                //Hot Deck Conditional Imputation

                missHash.put("hd_conditional",

                    Calc_hotDeckCondition(

                        (int) missHash.get("row"),  (int) missHash.get("column"), data.get((int)missHash.get("row"))[8]));

            }

        }

        public void Missing_Values(){
            for (int i =0 ; i<this.row; i++ ){
                for(int j = 0; j<this.columns; j++){

                    if (this.data.get(i)[j].equals("?")){

                        HashMap <String, Object> missHash = new HashMap<>();
                        missHash.put("row",  i);
                        missHash.put("column",  j);

                        missVal.add(missHash);
                    }
                }
            }
        }

        

        public void to_double(){

            for (int i = 0 ; i<this.row; i++ ) {

                Double[] row = new Double[this.columns];

                for (int j = 0; j < this.columns-1; j++) {

                    row[j] = (data.get(i)[j].equals("?")?null:Double.parseDouble(data.get(i)[j])) ;
                }
                this.new_data.add(row);
            }
        }

        //data_typerithm for calculating Mean
        public double Calc_mean(ArrayList<Double> arr){

            double total=0;

            int j = 0;

            for (int i = 0; i < arr.size(); i++) {

                if (arr.get(i)!=null){

                    total+= arr.get(i);

                    j++;
                }

            }
            return total/j;
        }

        //data_typerithm for calculating Hot Deck

        public double Calc_hotDeck(int row, int column){

            double min_Distance = Double.POSITIVE_INFINITY;

            int index = -1;

            double distance= 0;

            Double curr[] = new_data.get(row);

            for (int i = 0; i < new_data.size(); i++) {

                if(i==row)

                    continue;

                distance = euclidean_distance(curr,new_data.get(i));

                if(distance<min_Distance) {

                    if (new_data.get(i)[column] != null) {

                        min_Distance = distance;

                        index = i;
                    }
                }
            }

            return  new_data.get(index)[column];

        }

        //data_typerithm for calculating Conditional Hot Deck 

        public double Calc_hotDeckCondition(int row, int column, String cond){

            double min_Distance = Double.POSITIVE_INFINITY;

            int index = -1;

            double distance= 0;

            Double curr[] = new_data.get(row);

            for (int i = 0; i < new_data.size(); i++) {

                if(i==row || !data.get(i)[8].equals(cond))

                    continue;

                distance = euclidean_distance(curr,new_data.get(i));

                if(distance<min_Distance) {

                    if (new_data.get(i)[column] != null) {

                        min_Distance = distance;

                        index = i;
                    }
                }
            }

            return  new_data.get(index)[column];

        }

        public double euclidean_distance(Double[] a, Double[] b){

            double distance= 0;

            for (int i = 0; i < a.length; i++) {

                distance+= (a[i] != null && b[i] != null)? Math.pow(a[i]-b[i],2) : 1;

            }

            return Math.sqrt(distance);
        }




        public double Mean_Absolute_Error(DataSet complete, String data_type){

            double abs_error = 0 ;

            int n = 0 ;

            for (HashMap<String ,Object> missHash: missVal  ){

                double x = (double) missHash.get(data_type);

                double t = complete.new_data.get((int) missHash.get("row"))[(int) missHash.get("column")];

                abs_error += Math.abs(x-t);

                n++;

            }
            return abs_error/n;

        };

        public void output(ArrayList<String[]> val, String fileName,String data_type) throws FileNotFoundException {

            PrintWriter out = new PrintWriter("V0622181_"+fileName+"_imputed_"+data_type+".csv");

            for (HashMap<String ,Object> missHash: missVal  ){

                val.get((int)missHash.get("row"))[(int)missHash.get("column")]=String.format("%.5f",(double) missHash.get(data_type));
            }
            for (String[] row: val){

                out.print(append_string(row,",")+"\n");
            }
            out.close();
        }


        public void save_func(String fileName) throws FileNotFoundException {

            ArrayList<String[]> val = new ArrayList<String[]>(data);

            output(val, fileName,"mean");

            output(val, fileName,"mean_conditional");

            output(val, fileName,"hd");

            output(val, fileName,"hd_conditional");


        }
    }


    public  static  ArrayList<String[]> readCsv(String filePath) throws IOException {

        BufferedReader reader = new BufferedReader(new FileReader(filePath));

        // reading file
        ArrayList<String[]> data  = new ArrayList<String[]>();

        String line = null;

        //except the first line
        reader.readLine(); 

        while ((line = reader.readLine()) != null) {

            String[] array = line.split(",");

            data.add(array);
        }
        return data;
    }



       private static String append_string(String[] array, String commas) {

        StringBuilder string_builder = new StringBuilder();

        for (int i = 0; i < array.length; i++) {

            if (i > 0) {

                string_builder.append(commas);
            }

            String values = array[i];

            string_builder.append(values);

        }

        return string_builder.toString();
    }



    public static void main(String[] args) throws IOException {
        

        DataSet missing05 = new DataSet("dataset_missing05.csv");

        DataSet missing20 = new DataSet("dataset_missing20.csv");

        DataSet complete_data = new DataSet("dataset_complete.csv");
   
      

        missing05.imputation();

        System.out.printf("Mabs_error_05_mean = %.4f\n",missing05.Mean_Absolute_Error(complete_data,"mean"));

        System.out.printf("Mabs_error_05_mean_conditional = %.4f\n",missing05.Mean_Absolute_Error(complete_data,"mean_conditional"));

        System.out.printf("Mabs_error_05_hd = %.4f\n",missing05.Mean_Absolute_Error(complete_data,"hd"));

        System.out.printf("Mabs_error_05_hd_conditional = %.4f\n",missing05.Mean_Absolute_Error(complete_data,"hd_conditional"));

       
        System.out.print("\n");

        missing20.imputation();

        System.out.printf("Mabs_error_20_mean = %.4f\n",missing20.Mean_Absolute_Error(complete_data,"mean"));

        System.out.printf("Mabs_error_20_mean_conditionalal = %.4f\n",missing20.Mean_Absolute_Error(complete_data,"mean_conditional"));

        System.out.printf("Mabs_error_20_hd = %.4f\n",missing20.Mean_Absolute_Error(complete_data,"hd"));

        System.out.printf("Mabs_error_20_hd_conditional = %.4f\n",missing20.Mean_Absolute_Error(complete_data,"hd_conditional"));

        missing05.save_func("missing05");

        missing20.save_func("missing20");

        return;
    }

 
}