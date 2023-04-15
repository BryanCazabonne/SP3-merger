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
import java.util.Locale;

import org.hipparchus.exception.LocalizedCoreFormats;
import org.hipparchus.geometry.euclidean.threed.Vector3D;
import org.orekit.errors.OrekitException;
import org.orekit.frames.Frame;
import org.orekit.propagation.Propagator;
import org.orekit.propagation.SpacecraftState;
import org.orekit.propagation.sampling.OrekitFixedStepHandler;
import org.orekit.time.AbsoluteDate;
import org.orekit.time.DateTimeComponents;
import org.orekit.time.TimeScale;
import org.orekit.utils.TimeStampedPVCoordinates;

/**
 * A writer for SP3 files.
 *
 * <p> Each instance corresponds to a single SP3 file.
 *
 * <p> This class can be used as a step handler for a {@link Propagator}.
 *
 * @author Bryan Cazabonne
 */
public class StreamingSp3Writer {

    /** New line separator for output file. */
    private static final String NEW_LINE = "\n";

    /** String value. */
    private static final String S = "%s";

    /** Space. */
    private static final String SPACE = " ";

    /** Integer I2 Format. */
    private static final String I2 = "%2d";

    /** Integer I4 Format. */
    private static final String I4 = "%4d";

    /** Real 14.6 Format. */
    private static final String F14_6 = "%14.6f";

    /** Real 11.8 Format. */
    private static final String F11_8 = "%11.8f";

    /** Default locale. */
    private static final Locale STANDARDIZED_LOCALE = Locale.US;

    /** Output stream. */
    private final Appendable writer;

    /** Time scale for all dates. */
    private final TimeScale timeScale;

    /** Satellite SP3 identifier. */
    private final String satelliteSp3Identifier;

    /**
     * Create a SP3 writer than streams data to the given output stream.
     *
     * @param writer     the output stream for the CPF file.
     * @param timeScale  for all times in the CPF
     */
    public StreamingSp3Writer(final Appendable writer,
                              final TimeScale timeScale,
                              final String satelliteSp3Identifier) {
        this.writer                 = writer;
        this.timeScale              = timeScale;
        this.satelliteSp3Identifier = satelliteSp3Identifier;
    }


    /**
     * Create a writer for a new SP3 ephemeris segment.
     * <p>
     * The returned writer can only write a single ephemeris segment in a SP3.
     * </p>
     * @param frame the reference frame to use for the segment. If this value is
     *              {@code null} then {@link Segment#handleStep(SpacecraftState,
     *              boolean)} will throw a {@link NullPointerException}.
     * @return a new SP3 segment, ready for writing.
     */
    public Segment newSegment(final Frame frame) {
        return new Segment(frame);
    }

    /**
     * Writes the SP3 header for the file.
     * @throws IOException if the stream cannot write to stream
     */
    public void writeHeader() throws IOException {
        // First line
        HeaderLineWriter.FIRST_LINE.write(writer, timeScale, satelliteSp3Identifier);
        writer.append(NEW_LINE);

        // Line Two
        HeaderLineWriter.LINE_TWO.write(writer, timeScale, satelliteSp3Identifier);
        writer.append(NEW_LINE);

        // Line Three
        HeaderLineWriter.LINE_THREE.write(writer, timeScale, satelliteSp3Identifier);
        writer.append(NEW_LINE);
        // Line Four
        HeaderLineWriter.LINE_EMPTY_SAT.write(writer, timeScale, satelliteSp3Identifier);
        writer.append(NEW_LINE);
        // Line Five
        HeaderLineWriter.LINE_EMPTY_SAT.write(writer, timeScale, satelliteSp3Identifier);
        writer.append(NEW_LINE);
        // Line Six
        HeaderLineWriter.LINE_EMPTY_SAT.write(writer, timeScale, satelliteSp3Identifier);
        writer.append(NEW_LINE);
        // Line Seven
        HeaderLineWriter.LINE_EMPTY_SAT.write(writer, timeScale, satelliteSp3Identifier);
        writer.append(NEW_LINE);

        // Line Eight
        HeaderLineWriter.LINE_ACCURACY_BLANC.write(writer, timeScale, satelliteSp3Identifier);
        writer.append(NEW_LINE);
        // Line Nine
        HeaderLineWriter.LINE_ACCURACY_BLANC.write(writer, timeScale, satelliteSp3Identifier);
        writer.append(NEW_LINE);

        // Line Ten
        HeaderLineWriter.LINE_ACCURACY_ZERO.write(writer, timeScale, satelliteSp3Identifier);
        writer.append(NEW_LINE);
        // Line Eleven
        HeaderLineWriter.LINE_ACCURACY_ZERO.write(writer, timeScale, satelliteSp3Identifier);
        writer.append(NEW_LINE);
        // Line Twelve
        HeaderLineWriter.LINE_ACCURACY_ZERO.write(writer, timeScale, satelliteSp3Identifier);
        writer.append(NEW_LINE);

        // Line Thirteen
        HeaderLineWriter.LINE_ACCURACY_THIRTEEN.write(writer, timeScale, satelliteSp3Identifier);
        writer.append(NEW_LINE);

        // Line Fourteen
        HeaderLineWriter.LINE_ACCURACY_FOURTEEN.write(writer, timeScale, satelliteSp3Identifier);
        writer.append(NEW_LINE);

        // Line Fifteen
        HeaderLineWriter.LINE_ACCURACY_FIFTEEN.write(writer, timeScale, satelliteSp3Identifier);
        writer.append(NEW_LINE);

        // Line Sixteen
        HeaderLineWriter.LINE_ACCURACY_SIXTEEN.write(writer, timeScale, satelliteSp3Identifier);
        writer.append(NEW_LINE);

        // Line Seventeen
        HeaderLineWriter.LINE_ZERO.write(writer, timeScale, satelliteSp3Identifier);
        writer.append(NEW_LINE);
        // Line Eighteen
        HeaderLineWriter.LINE_ZERO.write(writer, timeScale, satelliteSp3Identifier);
        writer.append(NEW_LINE);

        // Lines Nineteen to Twenty two
        HeaderLineWriter.LINE_C.write(writer, timeScale, satelliteSp3Identifier);
        writer.append(NEW_LINE);
        HeaderLineWriter.LINE_C.write(writer, timeScale, satelliteSp3Identifier);
        writer.append(NEW_LINE);
        HeaderLineWriter.LINE_C.write(writer, timeScale, satelliteSp3Identifier);
        writer.append(NEW_LINE);
        HeaderLineWriter.LINE_C.write(writer, timeScale, satelliteSp3Identifier);
        writer.append(NEW_LINE);
    }

    /**
     * Write end of file.
     * @throws IOException if the stream cannot write to stream
     */
    public void writeEndOfFile() throws IOException {
        writer.append("EOF");
    }

    /** A writer for a segment of a SP3. */
    public class Segment implements OrekitFixedStepHandler {

        /** Reference frame of the output states. */
        private final Frame frame;

        /**
         * Create a new segment writer.
         *
         * @param frame    for the output states. Used by {@link #handleStep(SpacecraftState,
         *                 boolean)}.
         */
        private Segment(final Frame frame) {
            this.frame = frame;
        }

        /** {@inheritDoc}. */
        @Override
        public void handleStep(final SpacecraftState currentState) {
            try {

                // Write ephemeris line
                writeEphemerisLine(currentState.getPVCoordinates(frame));

            } catch (IOException e) {
                throw new OrekitException(e, LocalizedCoreFormats.SIMPLE_MESSAGE,
                                          e.getLocalizedMessage());
            }

        }

        /** {@inheritDoc}. */
        @Override
        public void finish(final SpacecraftState finalState) {
            try {
                // Write ephemeris line
                writeEphemerisLine(finalState.getPVCoordinates(frame));

                // Write end of file
                writeEndOfFile();

            } catch (IOException e) {
                throw new OrekitException(e, LocalizedCoreFormats.SIMPLE_MESSAGE,
                                          e.getLocalizedMessage());
            }
        }

        /**
         * Write a single ephemeris line This method does not
         * write the velocity terms.
         *
         * @param pv the time, position, and velocity to write.
         * @throws IOException if the output stream throws one while writing.
         */
        public void writeEphemerisLine(final TimeStampedPVCoordinates pv)
            throws IOException {

            // Epoch
            writeValue(writer, S, "*  ");
            final AbsoluteDate epoch = pv.getDate();
            final DateTimeComponents dtc = epoch.getComponents(timeScale);
            writeValue(writer, I4, dtc.getDate().getYear());
            writeValue(writer, I2, dtc.getDate().getMonth());
            writeValue(writer, I2, dtc.getDate().getDay());
            writeValue(writer, I2, dtc.getTime().getHour());
            writeValue(writer, I2, dtc.getTime().getMinute());
            writeValue(writer, F11_8, dtc.getTime().getSecond());

            // New line
            writer.append(NEW_LINE);

            // Position in km
            writeValue(writer, S, "PL");
            writeValue(writer, S, satelliteSp3Identifier);
            final Vector3D position = pv.getPosition();
            writeValue(writer, F14_6, position.getX() * 0.001);
            writeValue(writer, F14_6, position.getY() * 0.001);
            writeValue(writer, F14_6, position.getZ() * 0.001);

            // Clock
            writeValue(writer, S, SPACE);
            writeValue(writer, S, "999999.999999");

            // New line
            writer.append(NEW_LINE);

            // Velocity in dm/s
            writeValue(writer, S, "VL");
            writeValue(writer, S, satelliteSp3Identifier);
            final Vector3D velocity = pv.getVelocity();
            writeValue(writer, F14_6, velocity.getX() * 10.0);
            writeValue(writer, F14_6, velocity.getY() * 10.0);
            writeValue(writer, F14_6, velocity.getZ() * 10.0);

            // Clock
            writeValue(writer, S, SPACE);
            writeValue(writer, S, "999999.999999");

            // New line
            writer.append(NEW_LINE);

        }

    }

    /**
     * Write a String value in the file.
     * @param cpfWriter writer
     * @param format format
     * @param value value
     * @throws IOException if value cannot be written
     */
    private static void writeValue(final Appendable cpfWriter, final String format, final String value)
        throws IOException {
        cpfWriter.append(String.format(STANDARDIZED_LOCALE, format, value));
    }

    /**
     * Write a integer value in the file.
     * @param cpfWriter writer
     * @param format format
     * @param value value
     * @throws IOException if value cannot be written
     */
    private static void writeValue(final Appendable cpfWriter, final String format, final int value)
        throws IOException {
        cpfWriter.append(String.format(STANDARDIZED_LOCALE, format, value)).append(SPACE);
    }

    /**
     * Write a real value in the file.
     * @param cpfWriter writer
     * @param format format
     * @param value value
     * @throws IOException if value cannot be written
     */
    private static void writeValue(final Appendable cpfWriter, final String format, final double value)
        throws IOException {
        cpfWriter.append(String.format(STANDARDIZED_LOCALE, format, value));
    }

    /** Writer for specific header lines. */
    public enum HeaderLineWriter {

        /** Header first line. */
        FIRST_LINE() {

            /** {@inheritDoc} */
            @Override
            public void write(final Appendable sp3Writer, final TimeScale timescale, String satelliteIdentifier)
                throws IOException {
                writeValue(sp3Writer, S, "#cV2014  1  4 21 56  0.00000000   30413 ORBIT ITRF  FIT CNES");
            }

        },

        /** Header second line. */
        LINE_TWO() {

            /** {@inheritDoc} */
            @Override
            public void write(final Appendable sp3Writer, final TimeScale timescale, String satelliteIdentifier)
                throws IOException {
                writeValue(sp3Writer, S, "## 1773 597360.00000000    60.00000000 56661 0.9138888888889");
            }

        },

        /** Header third line. */
        LINE_THREE() {

            /** {@inheritDoc} */
            @Override
            public void write(final Appendable sp3Writer, final TimeScale timescale, String satelliteIdentifier)
                throws IOException {
                writeValue(sp3Writer, S, "+    1   L");
                writeValue(sp3Writer, S, satelliteIdentifier);
                writeValue(sp3Writer, S, "  0  0  0  0  0  0  0  0  0  0  0  0  0  0  0  0");
            }

        },

        /** Header empty satellite line. */
        LINE_EMPTY_SAT() {

            /** {@inheritDoc} */
            @Override
            public void write(final Appendable sp3Writer, final TimeScale timescale, String satelliteIdentifier)
                throws IOException {
                writeValue(sp3Writer, S, "+          0  0  0  0  0  0  0  0  0  0  0  0  0  0  0  0  0");
            }

        },

        /** Header line height and nine. */
        LINE_ACCURACY_BLANC() {

            /** {@inheritDoc} */
            @Override
            public void write(final Appendable sp3Writer, final TimeScale timescale, String satelliteIdentifier)
                throws IOException {
                writeValue(sp3Writer, S, "++");
            }

        },

        /** Header line ten to twelve. */
        LINE_ACCURACY_ZERO() {

            /** {@inheritDoc} */
            @Override
            public void write(final Appendable sp3Writer, final TimeScale timescale, String satelliteIdentifier)
                throws IOException {
                writeValue(sp3Writer, S, "++         0  0  0  0  0  0  0  0  0  0  0  0  0  0  0  0  0");
            }

        },

        /** Header line thirteen. */
        LINE_ACCURACY_THIRTEEN() {

            /** {@inheritDoc} */
            @Override
            public void write(final Appendable sp3Writer, final TimeScale timescale, String satelliteIdentifier)
                throws IOException {
                writeValue(sp3Writer, S, String.format("%c L  cc TAI ccc cccc cccc cccc cccc ccccc ccccc ccccc ccccc"));
            }

        },

        /** Header line fourteen. */
        LINE_ACCURACY_FOURTEEN() {

            /** {@inheritDoc} */
            @Override
            public void write(final Appendable sp3Writer, final TimeScale timescale, String satelliteIdentifier)
                throws IOException {
                writeValue(sp3Writer, S, "%c cc cc ccc ccc cccc cccc cccc cccc ccccc ccccc ccccc ccccc");
            }

        },

        /** Header line fifteen. */
        LINE_ACCURACY_FIFTEEN() {

            /** {@inheritDoc} */
            @Override
            public void write(final Appendable sp3Writer, final TimeScale timescale, String satelliteIdentifier)
                throws IOException {
                writeValue(sp3Writer, S, "%f  1.2500000  1.025000000  0.00000000000  0.000000000000000");
            }

        },

        /** Header line sixteen. */
        LINE_ACCURACY_SIXTEEN() {

            /** {@inheritDoc} */
            @Override
            public void write(final Appendable sp3Writer, final TimeScale timescale, String satelliteIdentifier)
                throws IOException {
                writeValue(sp3Writer, S, "%f  0.0000000  0.000000000  0.00000000000  0.000000000000000");
            }

        },

        /** Header line Seventeen and Eighteen. */
        LINE_ZERO() {

            /** {@inheritDoc} */
            @Override
            public void write(final Appendable sp3Writer, final TimeScale timescale, String satelliteIdentifier)
                throws IOException {
                writeValue(sp3Writer, S, "%i    0    0    0    0      0      0      0      0         0");
            }

        },

        /** Header Nineteen to Twenty two. */
        LINE_C() {

            /** {@inheritDoc} */
            @Override
            public void write(final Appendable sp3Writer, final TimeScale timescale, String satelliteIdentifier)
                throws IOException {
                writeValue(sp3Writer, S, "/* CCCCCCCCCCCCCCCCCCCCCCCCCCCCCCCCCCCCCCCCCCCCCCCCCCCCCCCCC");
            }

        };


        /** Write a line.
         * @param sp3Writer writer
         * @param timescale time scale for dates
         * @param satelliteIdentifier TODO
         * @throws IOException
         *             if any buffer writing operations fail or if the underlying
         *             format doesn't support a configuration in the file
         */
        public abstract void write(Appendable sp3Writer, TimeScale timescale, String satelliteIdentifier)  throws IOException;

    }

}
