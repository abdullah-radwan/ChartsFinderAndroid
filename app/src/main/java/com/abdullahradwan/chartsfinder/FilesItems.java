package com.abdullahradwan.chartsfinder;

import java.io.File;

class FilesItems {

    final String chartName;

    final String chartType;

    final File chartFile;

    FilesItems(String chartName, String chartType, File chartFile){

        this.chartName = chartName;

        this.chartType = chartType;

        this.chartFile = chartFile;

    }

}
