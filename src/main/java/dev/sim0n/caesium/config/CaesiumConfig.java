package dev.sim0n.caesium.config;

import java.util.List;

public class CaesiumConfig {

    private String input;
    private String output;
    private String dictionary = "NUMBERS";
    private List<String> libraries;
    private Mutators mutators = new Mutators();

    public String getInput()              { return input; }
    public void   setInput(String v)      { this.input = v; }
    public String getOutput()             { return output; }
    public void   setOutput(String v)     { this.output = v; }
    public String getDictionary()         { return dictionary; }
    public void   setDictionary(String v) { this.dictionary = v; }
    public List<String> getLibraries()    { return libraries; }
    public void   setLibraries(List<String> v) { this.libraries = v; }
    public Mutators getMutators()         { return mutators; }
    public void   setMutators(Mutators v) { this.mutators = v; }

    public static class Mutators {
        private MutatorEntry shuffle       = new MutatorEntry();
        private MutatorEntry trim          = new MutatorEntry();
        private MutatorEntry lineNumber    = new LineNumberEntry();
        private MutatorEntry localVariable = new LocalVariableEntry();
        private MutatorEntry string        = new StringEntry();
        private MutatorEntry controlFlow   = new MutatorEntry();
        private MutatorEntry number        = new MutatorEntry();
        private MutatorEntry polymorph     = new MutatorEntry();
        private MutatorEntry reference     = new ReferenceEntry();
        private MutatorEntry classFolder   = new MutatorEntry();
        private MutatorEntry badAnnotation = new MutatorEntry();
        private MutatorEntry imageCrash    = new MutatorEntry();
        private MutatorEntry classRename   = new MutatorEntry();
        private MutatorEntry methodRename  = new MutatorEntry();
        private MutatorEntry fieldRename   = new MutatorEntry();

        public MutatorEntry getShuffle()       { return shuffle; }
        public void setShuffle(MutatorEntry v) { this.shuffle = v; }
        public MutatorEntry getTrim()          { return trim; }
        public void setTrim(MutatorEntry v)    { this.trim = v; }
        public MutatorEntry getLineNumber()    { return lineNumber; }
        public void setLineNumber(LineNumberEntry v) { this.lineNumber = v; }
        public MutatorEntry getLocalVariable() { return localVariable; }
        public void setLocalVariable(LocalVariableEntry v) { this.localVariable = v; }
        public MutatorEntry getString()        { return string; }
        public void setString(StringEntry v)   { this.string = v; }
        public MutatorEntry getControlFlow()   { return controlFlow; }
        public void setControlFlow(MutatorEntry v) { this.controlFlow = v; }
        public MutatorEntry getNumber()        { return number; }
        public void setNumber(MutatorEntry v)  { this.number = v; }
        public MutatorEntry getPolymorph()     { return polymorph; }
        public void setPolymorph(MutatorEntry v) { this.polymorph = v; }
        public MutatorEntry getReference()     { return reference; }
        public void setReference(ReferenceEntry v) { this.reference = v; }
        public MutatorEntry getClassFolder()   { return classFolder; }
        public void setClassFolder(MutatorEntry v) { this.classFolder = v; }
        public MutatorEntry getBadAnnotation() { return badAnnotation; }
        public void setBadAnnotation(MutatorEntry v) { this.badAnnotation = v; }
        public MutatorEntry getImageCrash()    { return imageCrash; }
        public void setImageCrash(MutatorEntry v) { this.imageCrash = v; }
        public MutatorEntry getClassRename()   { return classRename; }
        public void setClassRename(MutatorEntry v) { this.classRename = v; }
        public MutatorEntry getMethodRename()  { return methodRename; }
        public void setMethodRename(MutatorEntry v) { this.methodRename = v; }
        public MutatorEntry getFieldRename()   { return fieldRename; }
        public void setFieldRename(MutatorEntry v) { this.fieldRename = v; }
    }

    public static class MutatorEntry {
        private boolean enabled = false;
        public boolean isEnabled()          { return enabled; }
        public void setEnabled(boolean v)   { this.enabled = v; }
    }

    public static class LineNumberEntry extends MutatorEntry {
        private int type = 0;
        public int  getType()             { return type; }
        public void setType(int v)        { this.type = v; }
    }

    public static class LocalVariableEntry extends MutatorEntry {
        private int type = 0;
        public int  getType()             { return type; }
        public void setType(int v)        { this.type = v; }
    }

    public static class StringEntry extends MutatorEntry {
        private List<String> exclusions;
        public List<String> getExclusions()            { return exclusions; }
        public void         setExclusions(List<String> v) { this.exclusions = v; }
    }

    public static class ReferenceEntry extends MutatorEntry {
        private int type = 1;
        private List<String> exclusions;
        public int  getType()             { return type; }
        public void setType(int v)        { this.type = v; }
        public List<String> getExclusions()            { return exclusions; }
        public void         setExclusions(List<String> v) { this.exclusions = v; }
    }
}
