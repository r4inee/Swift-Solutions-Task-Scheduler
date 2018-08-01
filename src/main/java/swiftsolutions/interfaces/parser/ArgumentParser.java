package swiftsolutions.interfaces.parser;

import swiftsolutions.cli.options.CoresOption;
import swiftsolutions.cli.options.OutputOption;
import swiftsolutions.cli.options.VisualizeOption;
import swiftsolutions.exceptions.ArgumentFormatException;

public interface ArgumentParser  {

    public void parse(String[] args) throws ArgumentFormatException;

    public String getFile();

    public int getProcessors();

    public CoresOption getCoresOption();

    public VisualizeOption getVisualizeOption();

    public OutputOption getOutputOption();

}
