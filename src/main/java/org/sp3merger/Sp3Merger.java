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
package org.sp3merger;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.net.URISyntaxException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.hipparchus.geometry.euclidean.threed.Vector3D;
import org.orekit.attitudes.Attitude;
import org.orekit.data.DataContext;
import org.orekit.data.DataFilter;
import org.orekit.data.DataProvidersManager;
import org.orekit.data.DataSource;
import org.orekit.data.DirectoryCrawler;
import org.orekit.data.GzipFilter;
import org.orekit.data.UnixCompressFilter;
import org.orekit.files.general.OrekitEphemerisFile;
import org.orekit.files.sp3.SP3;
import org.orekit.files.sp3.SP3.SP3Coordinate;
import org.orekit.files.sp3.SP3.SP3Ephemeris;
import org.orekit.files.sp3.SP3Parser;
import org.orekit.gnss.HatanakaCompressFilter;
import org.orekit.propagation.SpacecraftState;
import org.orekit.time.AbsoluteDate;
import org.orekit.time.ChronologicalComparator;
import org.orekit.utils.AbsolutePVCoordinates;
import org.orekit.utils.PVCoordinates;
import org.orekit.utils.TimeStampedAngularCoordinates;
import org.sp3merger.writer.Sp3Writer;
import org.sp3merger.yaml.Sp3MergerInputs;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.dataformat.yaml.YAMLFactory;

/**
 * Main class of the SP3 merger.
 *
 * @author Bryan Cazabonne
 *
 */
public class Sp3Merger {

    /**
     * Private constructor for utility class.
     */
    public Sp3Merger() {
       // empty constructor
    }

    /** Program entry point.
     * @param args program arguments (unused here)
     */
    public static void main(final String[] args) throws URISyntaxException, IOException {

        // configure Orekit
        final File home       = new File(System.getProperty("user.home"));
        final File orekitData = new File(home, "orekit-data");
        final DataProvidersManager manager = DataContext.getDefault().getDataProvidersManager();
        manager.addProvider(new DirectoryCrawler(orekitData));

        // read input parameters
        final ObjectMapper mapper = new ObjectMapper(new YAMLFactory());
        mapper.findAndRegisterModules();
        final Sp3MergerInputs inputData = mapper.readValue(args[0], Sp3MergerInputs.class);

        // SP3 files
        final List<SP3> sp3Files = new ArrayList<>();
        for (String fileName : inputData.getMeasurementFiles()) {
            sp3Files.add(readSp3(fileName, orekitData));
        }

        // Satellite SP3 identifier
        final String satelliteSp3Identifier = sp3Files.get(0).getSatellites().keySet().iterator().next();

        // Sorted set of data
        final List<SpacecraftState> states = fillStates(sp3Files, satelliteSp3Identifier);

        // Creates an ephemeris file
        final OrekitEphemerisFile ephemerisFile = new OrekitEphemerisFile();
        final OrekitEphemerisFile.OrekitSatelliteEphemeris satelliteEphemeris = ephemerisFile.addSatellite(satelliteSp3Identifier);
        satelliteEphemeris.addNewSegment(states);

        // Writer
        // Supposes that each SP3 files use the same frame and time scale
        final Sp3Writer writer = new Sp3Writer(sp3Files.get(0).getTimeSystem().getTimeScale(DataContext.getDefault().getTimeScales()),
                                               states.get(0).getFrame(), satelliteSp3Identifier);
        writer.write(inputData.getOutputFileName(), ephemerisFile);

    }

    /** Parse a SP3 file.
     * @param fileName name of the file in resources
     * @param orekitData Orekit data folder
     * @return the parsed SP3 file
     * @throws URISyntaxException if file's URL is not formatted strictly according to to RFC2396 and cannot be converted to a URI.
     * @throws IOException if filtered stream cannot be created
     */
    private static SP3 readSp3(final String fileName,
                               final File orekitData) throws URISyntaxException, IOException {

        // set up filtering for cpf file
        DataSource nd = new DataSource(fileName, () -> new FileInputStream(new File(orekitData.getAbsolutePath(), fileName)));
        for (final DataFilter filter : Arrays.asList(new GzipFilter(),
                                                     new UnixCompressFilter(),
                                                     new HatanakaCompressFilter())) {
            nd = filter.filter(nd);
        }

        // parse the file
        final SP3Parser parser = new SP3Parser();
        return parser.parse(nd);

    }

    /**
     * Create a list of spacecraft states based on data contained in SP3 files.
     * @param sp3Files input list of SP3 files
     * @param satelliteSp3Identifier satellite SP3 identifier
     * @return a list of spacecraft states
     */
    private static List<SpacecraftState> fillStates(final List<SP3> sp3Files, final String satelliteSp3Identifier) {

    	// Initialize list
    	final List<SpacecraftState> states = new ArrayList<>();

    	// Map of data
    	final Map<AbsoluteDate, SpacecraftState> stateMap = new HashMap<>();

    	// Loop on files
    	for (final SP3 sp3 : sp3Files) {

    		// Get ephemeris
    		final SP3Ephemeris ephemeris = sp3.getSatellites().get(satelliteSp3Identifier);

    		System.out.println("NEW EPHEMERIS");
    		System.out.println("=============");
    		System.out.println("Ref Frame: " + sp3.getCoordinateSystem());
    		System.out.println("Nb of epochs: " + sp3.getNumberOfEpochs());
    		System.out.println("Start epoch: " + ephemeris.getStart().toString());
    		System.out.println("End epoch: " + ephemeris.getStop().toString());
    		System.out.println("");

    		// Loop on coordinates
    		for (final SP3Coordinate coordinates : ephemeris.getCoordinates()) {

    			final Attitude arbitraryAttitude = new Attitude(ephemeris.getFrame(),
                        new TimeStampedAngularCoordinates(coordinates.getDate(),
                                                           new PVCoordinates(Vector3D.PLUS_I,
                                                                             Vector3D.PLUS_J),
                                                           new PVCoordinates(Vector3D.PLUS_I,
                                                                             Vector3D.PLUS_J)));

    			// Coordinates epoch
    			final AbsoluteDate coordinateEpoch = coordinates.getDate();

    			// Create the spacecraft state
    			final SpacecraftState state = new SpacecraftState(new AbsolutePVCoordinates(ephemeris.getFrame(),
    			                                                                            coordinateEpoch,
    					                                                                    coordinates.getPosition(),
    					                                                                    coordinates.getVelocity()),
    					                                          arbitraryAttitude);

    			
    			// Fill map
    			stateMap.put(coordinates.getDate(), state);

    		}
    		

    	}

    	// Convert map to list
    	for (final Map.Entry<AbsoluteDate, SpacecraftState> entry : stateMap.entrySet()) {
    		states.add(entry.getValue());
    	}

    	// Sort
    	states.sort(new ChronologicalComparator());

    	// Return
    	return states;

    }

}
