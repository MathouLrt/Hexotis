package truel.mathias.hexotis;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;

import truel.mathias.hexotis.midi.MidiFile;
import truel.mathias.hexotis.midi.MidiTrack;
import truel.mathias.hexotis.midi.event.NoteOff;
import truel.mathias.hexotis.midi.event.NoteOn;
import truel.mathias.hexotis.midi.event.meta.Tempo;
import truel.mathias.hexotis.midi.event.meta.TimeSignature;

/**
 * Que doit t'on faire avec le midi?
 *
 * Ecrire un fichier midi
 *
 * Enregistrer les notes
 *
 *
 *
 * Plusieurs modes sur l'arduino?
 *2 Threads
 * Jeu sans enregistrement
 *      //30sec buffer
 *
 * Jeu avec enregistrement
 *  Jusqu'à 5min
 *  Envoi de la chaine de charatere a la fin
 *      //Uniquement si connecté
 *      //Sinon l'écris dans l'EEPROM
 */

public class MidiService {
    public static void MidiFileCreation(int mesureFracUp,int mesureFracDown,float tempo,String midiNotesContainer,String fileName) {
//        for(int i = 0; i < args.length; i++) {
//            Log.d(TAG,args[i]);
//            System.out.println(args[i]);
//        }
        fileName = "exemple1.mid";
        // 1. Create some MidiTracks
        MidiTrack tempoTrack = new MidiTrack();
        MidiTrack noteTrack = new MidiTrack();

        // 2. Add events to the tracks
        // 2a. Track 0 is typically the tempo map
        TimeSignature ts = new TimeSignature();
        ts.setTimeSignature(mesureFracUp, mesureFracDown, TimeSignature.DEFAULT_METER, TimeSignature.DEFAULT_DIVISION);

        Tempo t = new Tempo();
        t.setBpm(tempo);

        tempoTrack.insertEvent(ts);
        tempoTrack.insertEvent(t);

        // 2b. Track 1 will have some notes in it
        for(int i = 0; i < midiNotesContainer.length() ; i++ )
        {
            char[] midiNotesArray = midiNotesContainer.toCharArray();
            if ((midiNotesArray[i] == '0') && (midiNotesArray[i+1] == '$') && (midiNotesArray[i+2] == 'h')){
                i+=3;
            }
            int channel = 0;

            int pitch = Character.getNumericValue(midiNotesArray[i]);
            int velocity = Character.getNumericValue(midiNotesArray[i+1]);
            int tEvent = Character.getNumericValue(midiNotesArray[i+2]);

            if (velocity == 0) {
                NoteOff off = new NoteOff(tEvent, channel, pitch, 0);
                noteTrack.insertEvent(off);
            } else {
                NoteOn on = new NoteOn(tEvent, channel, pitch, velocity);
                noteTrack.insertEvent(on);
            }

//            // There is also a utility function for notes that you should use
//            // instead of the above.
//            noteTrack.insertNote(channel, pitch + 2, velocity, i * 480, 120);
        }

        // It's best not to manually insert EndOfTrack events; MidiTrack will
        // call closeTrack() on itself before writing itself to a file

        // 3. Create a MidiFile with the tracks we created
        ArrayList<MidiTrack> tracks = new ArrayList<MidiTrack>();
        tracks.add(tempoTrack);
        tracks.add(noteTrack);

        MidiFile midi = new MidiFile(MidiFile.DEFAULT_RESOLUTION, tracks);

        // 4. Write the MIDI data to a file
        File output = new File(fileName);
        try
        {
            midi.writeToFile(output);
        }
        catch(IOException e)
        {
            System.err.println(e);
        }
    }
}

