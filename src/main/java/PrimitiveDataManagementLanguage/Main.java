package PrimitiveDataManagementLanguage;

import java.util.List;
import java.util.Map;

public class Main {

    public static void main(String[] args) {

        RequestHandler starter = new RequestHandler();

        try {
            List<Map<String, Object>> insert1 =
                    starter.execute("INSERT VALUES LASTNAME=�������, id=12342, age=40, ACTIVE=false, cost=2.23");


            List<Map<String, Object>> insert2 =
                    starter.execute("INSERT VALUES �lastName�=��������, �ID�=2, �age�=35, �active�=true");

            List<Map<String, Object>> insert3 =
                    starter.execute("INSERT values �lastName�=���������, �id�=3, �age�=30, �active�=false");

            List<Map<String, Object>> insert4 =
                    starter.execute("insert VALUES �lastName�=�������, �id�=4, �age�=35, �active�=true");

            List<Map<String, Object>> insert5 =
                    starter.execute("INSERT VALUES �LASTNAME�=��������, �id�=5, �age�=30, �active�=false");

            List<Map<String, Object>> insert6 =
                    starter.execute("INSERT VALUES �LASTNAME�=���������, �id�=6, �AGE�=25, �active�=true");

            List<Map<String, Object>> insert7 =
                    starter.execute("INSERT VALUES �lastName�=����������, �id�=7, �age�=20, �active�=false");

            List<Map<String, Object>> insert8 =
                    starter.execute("INSERT VALUES �lastName�=��������, �id�=8, �age�=40, �active�=true");

            starter.output(insert8);

            List<Map<String, Object>> update1 =
                    starter.execute("UPDATE VALUES �lastName�=��������͒, �cost�=12.3 where �active�=false and �age�>=30");

            System.out.println("���������:");
            starter.output(update1);

            List<Map<String, Object>> select1 =
                    starter.execute("SELECT WHERE �age�<25 and �lastName� ilike �%��%�");

            System.out.println("�������:");
            starter.output(select1);

            List<Map<String, Object>> delete1 =
                    starter.execute("DELETE WHERE �id�=2 or �lastName� like �%�%�");

            System.out.println("�������:");
            starter.output(delete1);

        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}