package au.ivj.sandbox.garden.commands;

import java.util.List;

/**
 * Base type for commands.
 */
public interface Command
{
    void execute(List<String> payload);
}
