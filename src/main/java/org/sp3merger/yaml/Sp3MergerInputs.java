/* Copyright 2023 Bryan Cazabonne

   Licensed under the Apache License, Version 2.0 (the "License");
   you may not use this file except in compliance with the License.
   You may obtain a copy of the License at

       http://www.apache.org/licenses/LICENSE-2.0

   Unless required by applicable law or agreed to in writing, software
   distributed under the License is distributed on an "AS IS" BASIS,
   WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
   See the License for the specific language governing permissions and
   limitations under the License.
 */
package org.sp3merger.yaml;

import java.util.List;

/**
 * Initial data to initialize the SP3 merger.
 * <p>
 * Data are read from a YAML file.
 * </p>
 * @author Bryan Cazabonne
 */
public class Sp3MergerInputs {

    /** List of measurement files. */
    private List<String> measurementFiles;
    
    /** Output file name. */
    private String outputFileName;

    /**
     * Get the measurement files.
     * @return the measurement files
     */
    public List<String> getMeasurementFiles() {
        return measurementFiles;
    }

    /**
     * Set the measurement files.
     * @param measurementFiles the measurement files to set
     */
    public void setMeasurementFiles(List<String> measurementFiles) {
        this.measurementFiles = measurementFiles;
    }

    /**
     * Get the output file name.
     * @return the output file name
     */
    public String getOutputFileName() {
        return outputFileName;
    }

    /**
     * Set the output file name.
     * @param outputFileName the output file name to set
     */
    public void setOutputFileName(String outputFileName) {
        this.outputFileName = outputFileName;
    }





}
