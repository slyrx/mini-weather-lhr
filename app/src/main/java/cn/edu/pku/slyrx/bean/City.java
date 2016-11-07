package cn.edu.pku.slyrx.bean;

/**
 * Created by slyrx on 16/11/1.
 */
public class City {
    private String province;
    private String city;
    private String number;
    private String firstPY;
    private String allPY;
    private String allFristPY;

    public City(String province, String city, String number, String firstPY, String allPY, String allFristPY){
        this.province = province;
        this.city = city;
        this.number = number;
        this.allPY = allPY;
        this.allFristPY =allFristPY;
    }

    public void setProvince(String province){
        this.province = province;
    }

    public void setCity(String city){
        this.city = city;
    }

    public String getCity(){
        return city;
    }
}
