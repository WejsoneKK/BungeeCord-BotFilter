package net.md_5.bungee.command;

import net.md_5.bungee.api.CommandSender;
import net.md_5.bungee.api.plugin.Command;

public class CommandBungee extends Command
{

    public CommandBungee()
    {
        super( "bungee" );
    }

    @Override
    public void execute(CommandSender sender, String[] args)
    {
        sender.sendMessage( "§aBungeeCord by md_5 §7» §bFreezeCord by WejsoneKK (v0.1.1-1.20.1-SNAPSHOT) " );
    }
}
