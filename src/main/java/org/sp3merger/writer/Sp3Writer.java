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
package org.sp3merger.writer;

import java.io.IOException;
import java.util.List;

import org.orekit.errors.OrekitIllegalArgumentException;
import org.orekit.errors.OrekitMessages;
import org.orekit.files.general.EphemerisFile;
import org.orekit.files.general.EphemerisFile.EphemerisSegment;
import org.orekit.files.general.EphemerisFile.SatelliteEphemeris;
import org.orekit.files.general.EphemerisFileWriter;
import org.orekit.frames.Frame;
import org.orekit.time.TimeScale;
import org.orekit.utils.TimeStampedPVCoordinates;
import org.sp3merger.writer.StreamingSp3Writer.Segment;

/**
 * A SP3 Writer class that take a general {@link SP3File} object
 * and export it as a valid SP3 file.
 *
 * @author Bryan Cazabonne
 *
 * @see <a href="https://files.igs.org/pub/data/format/sp3_docu.txt">SP3-a file format</a>
 * @see <a href="https://files.igs.org/pub/data/format/sp3c.txt">SP3-c file format</a>
 * @see <a href="https://files.igs.org/pub/data/format/sp3d.pdf">SP3-d file format</a>
 */
public class Sp3Writer implements EphemerisFileWriter {

    /** Time scale for dates. */
    private final TimeScale timescale;

    /** The reference frame for ephemeris data. */
    private final Frame referenceFrame;

    /** Satellite SP3 identifier. */
    private final String satelliteSp3Identifier;

    /**
     * Constructor.
     * @param timescale time scale for dates
     * @param referenceFrame reference frame of ephemeris data
     */
    public Sp3Writer(final TimeScale timescale,
    		         final Frame referenceFrame,
    		         final String satelliteSp3Identifier) {
        this.timescale              = timescale;
        this.referenceFrame         = referenceFrame;
        this.satelliteSp3Identifier = satelliteSp3Identifier;
    }

    /** {@inheritDoc} */
    @Override
    public <C extends TimeStampedPVCoordinates, S extends EphemerisSegment<C>>
        void write(final Appendable writer, final EphemerisFile<C, S> ephemerisFile)
        throws IOException {

        // Verify if writer is not a null object
        if (writer == null) {
            throw new OrekitIllegalArgumentException(OrekitMessages.NULL_ARGUMENT, "writer");
        }

        // Verify if the populated ephemeris file to serialize into the buffer is not null
        if (ephemerisFile == null) {
            return;
        }

        // Get satellite and ephemeris segments to output.
        final SatelliteEphemeris<C, S> satEphem = ephemerisFile.getSatellites().get(satelliteSp3Identifier);
        final List<S> segments = satEphem.getSegments();

        // Writer
        final StreamingSp3Writer sp3Writer =
                        new StreamingSp3Writer(writer, timescale, satelliteSp3Identifier);
        // Write header
        sp3Writer.writeHeader();

        // Loop on ephemeris segments
        for (final S segment : segments) {
            final Segment segmentWriter = sp3Writer.newSegment(referenceFrame);
            // Loop on coordinates
            for (final TimeStampedPVCoordinates coordinates : segment.getCoordinates()) {
                segmentWriter.writeEphemerisLine(coordinates);
            }
        }

        // Write end of file
        sp3Writer.writeEndOfFile();

    }

}
