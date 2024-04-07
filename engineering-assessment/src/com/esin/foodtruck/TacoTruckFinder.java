package com.esin.foodtruck;

import com.esin.utils.CsvFileUtil;

import java.util.List;
import java.util.stream.Collectors;

/**
 * The CLI to get the applicant names of all the taco trucks in the city.
 *
 * @author Peilian Wang
 */
public class TacoTruckFinder {

    public static void main(String[] args) {

        List<List<String>> dataList = CsvFileUtil.read("C:\\WangPeiLian\\22_esin_github\\engineering-assessment\\csv_input\\Mobile_Food_Facility_Permit.csv", "UTF-8");

        final int applicantIndex = 1;
        final int facilityTypeIndex = 2;
        final String facilityTypeText = "Truck";
        final int foodItemsIndex = 11;
        final String foodItemsTarget = "taco";

        List<String> applicantList = dataList.stream().
                filter(rowList -> facilityTypeText.equalsIgnoreCase(rowList.get(facilityTypeIndex))
                        && rowList.get(foodItemsIndex).toLowerCase().contains(foodItemsTarget))
                .map(rowList -> rowList.get(applicantIndex))
                .distinct()
                .collect(Collectors.toList());

        System.out.println();
        System.out.println("===== print applicant list for Taco trucks: =====");
        applicantList.forEach(System.out::println);
        System.out.println();
    }
}
